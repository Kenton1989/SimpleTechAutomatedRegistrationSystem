package cx2002grp2.stars;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cx2002grp2.stars.database.*;
import cx2002grp2.stars.dataitem.*;
import cx2002grp2.stars.dataitem.Registration.Status;

/**
 * Manager for all the events that may cause the registration information
 * changes.
 */
public class CourseAllocator {

	/**
	 * Default course allocator
	 */
	private static CourseAllocator defaultAllocator = new CourseAllocator();

	/**
	 * Get an instance of course allocator
	 * 
	 * @return an instance of course allocator
	 */
	public static CourseAllocator getInstance() {
		return defaultAllocator;
	}

	// Short name for databases
	private StudentDB studDB = StudentDB.getDB();
	private CourseIndexDB indexDB = CourseIndexDB.getDB();
	private RegistrationDB regDB = RegistrationDB.getDB();

	// Short name for notification sender
	private NotificationSender sender = Configs.getNotificationSender();

	/**
	 * Register a the given course index for the given student.
	 * <p>
	 * Possible reasons of failure:
	 * <ol>
	 * <li>The student or the course does not exist in the database.
	 * <li>The student reaches the limit of number of registration.
	 * <li>The student makes duplicated registration on the same course.
	 * <li>The new registration has time slot clashing with the existing courses.
	 * <li>The registered course index has no vacancy (the student will be put on
	 * the waitlist instead).
	 * </ol>
	 * 
	 * @param student  the student to register the course.
	 * @param newIndex the course index to be registered.
	 * @return the allocation result
	 */
	public CourseAllocator.Result registerCourse(Student student, CourseIndex newIndex) {
		// Validate inputs
		if (!studDB.hasItem(student)) {
			return invalidInputResult("Student: " + student);
		}
		if (!indexDB.hasItem(newIndex)) {
			return invalidInputResult("Index: " + newIndex);
		}

		// check registration number limit.
		int regCntLimit = Configs.getMaxRegistrationCount();
		if (student.getRegistrationList().size() >= regCntLimit) {
			return failure("Each student can only attempt to register for at most " + regCntLimit + " courses.");
		}

		// Check duplicated registration
		Course course = newIndex.getCourse();
		String courseCode = course.getCourseCode();
		Registration existingReg = regDB.getByCourseCode(courseCode, student.getUsername());
		if (existingReg != null) {
			CourseIndex existingIndex = existingReg.getCourseIndex();
			return failure("Duplicated registration: index " + existingIndex.getIndexNo() + " of course " + courseCode
					+ " already exists.");
		}

		// Check max AU limit
		double maxAu = Configs.getMaxAu(), totalAu = student.getRegisteredAU(), indexAu = newIndex.getCourse().getAu();
		if (maxAu < totalAu + indexAu) {
			return failure(String.format(
					"Max AU limit (%d AU) is exceeded. Current total AU is % and the AU of new course is %d.", maxAu,
					totalAu, indexAu));
		}

		// Check clashing
		Result clashingResult = checkTimeSlotClash(student.getRegistrationList(), newIndex, null);
		if (clashingResult != null) {
			return clashingResult;
		}

		Result result = null;
		Registration registration = null;

		// Check vacancy
		if (newIndex.getAvailableVacancy() <= 0) {
			result = failure("Index " + newIndex.getIndexNo() + " has no vacancy. Added to wait list instead.");
			registration = new Registration(student, newIndex, LocalDateTime.now(), Status.WAITLIST);
		}

		// If no failure result is produced
		if (result == null) {
			result = Result.SUCCESSFUL;
			registration = new Registration(student, newIndex, LocalDateTime.now(), Status.REGISTERED);
		}

		// Add new registration into registration database.
		regDB.addItem(registration);

		return result;
	}

	/**
	 * Drop a course for a student.
	 * <p>
	 * Possible reasons of failure:
	 * <ol>
	 * <li>The student or the course does not exist in the database.
	 * <li>The student didn't register for the given index.
	 * </ol>
	 * 
	 * @param student   the student to drop the course index
	 * @param dropIndex the course index to be dropped
	 * @return the result of dropping.
	 */
	public CourseAllocator.Result dropCourse(Student student, CourseIndex dropIndex) {
		// Validate inputs
		if (!studDB.hasItem(student)) {
			return invalidInputResult("Student: " + student);
		}
		if (!indexDB.hasItem(dropIndex)) {
			return invalidInputResult("Index: " + dropIndex);
		}

		// Check existence
		Registration existingReg = regDB.getByIndex(dropIndex.getIndexNo(), student.getUsername());

		if (existingReg == null) {
			return failure("The index doesn't exist in the student's registration list.");
		}

		// Drop the registration.
		regDB.delItem(existingReg);
		existingReg.drop();

		approveRegistration(dropIndex);

		return Result.SUCCESSFUL;
	}

	/**
	 * Change the maximum vacancy of the given course index.
	 * <p>
	 * Possible reasons of failure:
	 * <ol>
	 * <li>The course index does not exist in the database.
	 * <li>The new vacancy is not a positive number. (newMaxVcc smaller than 1)
	 * <li>The number of student that has already registered the course is more than
	 * the newMaxVcc.
	 * </ol>
	 * 
	 * @param courseIndex the course index which the max vacancy to be changed.
	 * @param newMaxVcc   the new max vacancy
	 * @return the result of changing
	 */
	public CourseAllocator.Result changeMaxVacancy(CourseIndex courseIndex, int newMaxVcc) {
		// Validate inputs
		if (!indexDB.hasItem(courseIndex)) {
			return invalidInputResult("Index: " + courseIndex);
		}
		if (newMaxVcc <= 0) {
			return failure("Max Vacancy must be greater than 0.");
		}

		if (courseIndex.getMaxVacancy() == newMaxVcc) {
			return Result.SUCCESSFUL;
		}

		int existingRegistered = courseIndex.getRegisteredList().size();
		if (newMaxVcc < existingRegistered) {
			return failure(existingRegistered
					+ " students has already registered the index, the new max vacancy cannot be smaller than that.");
		}

		courseIndex.setMaxVacancy(newMaxVcc);

		approveRegistration(courseIndex);

		return Result.SUCCESSFUL;
	}

	/**
	 * Change the index of an registration to another index.
	 * <ol>
	 * <li>The registration or the course index does not exist in the database.
	 * <li>The course code of the index does not match course code of the
	 * registration.
	 * <li>The status of registration is not {@link Registration.Status#REGISTERED}.
	 * <li>The new registration has no vacancy.
	 * <li>The new registration has time slot clashing with the existing courses.
	 * </ol>
	 * 
	 * @param currentReg the registration whose index to be changed.
	 * @param newIndex   the new index of the registration.
	 * @return the result of changing registration.
	 */
	public CourseAllocator.Result changeIndex(Registration currentReg, CourseIndex newIndex) {
		// Validate inputs
		if (!regDB.hasItem(currentReg)) {
			return invalidInputResult("Registration: " + currentReg);
		}
		if (!indexDB.hasItem(newIndex)) {
			return invalidInputResult("Index: " + newIndex);
		}

		// Check registration status
		if (currentReg.getStatus() != Status.REGISTERED) {
			return failure("The current course index haven't been successfully registered, the index cannot change.");
		}

		// Check consistent course code
		String oldCourseCode = currentReg.getCourse().getCourseCode();
		String newCourseCode = newIndex.getCourse().getCourseCode();
		if (!oldCourseCode.equals(newCourseCode)) {
			return failure("Course mismatch: the course of existing index: " + oldCourseCode
					+ "; The course of intended index: " + newCourseCode);
		}

		// Check vacancy
		if (newIndex.getAvailableVacancy() <= 0) {
			return failure("The index " + newIndex.getIndexNo() + " has no vacancy.");
		}

		// Check time slot clashing
		Result clashingResult = checkTimeSlotClash(currentReg.getStudent().getRegistrationList(), newIndex, currentReg);
		if (clashingResult != null) {
			return clashingResult;
		}

		CourseIndex oldIndex = currentReg.getCourseIndex();
		currentReg.setCourseIndex(newIndex);

		approveRegistration(oldIndex);

		return Result.SUCCESSFUL;
	}

	/**
	 * Swap the two registration of two student.
	 * <ol>
	 * <li>The registrations does not exist in the database.
	 * <li>The status of any one of two registrations is not
	 * {@link Registration.Status#REGISTERED}.
	 * <li>Two registrations share the same index.
	 * <li>The course codes of two registrations do not match.
	 * <li>The new registration has time slot clashing with the existing courses.
	 * </ol>
	 * 
	 * @param reg1 one registration
	 * @param reg2 the other registration.
	 * @return the result of swapping registration.
	 */
	public CourseAllocator.Result swapRegistration(Registration reg1, Registration reg2) {
		// Validate inputs
		if (!regDB.hasItem(reg1) || !indexDB.hasItem(reg1.getCourseIndex())) {
			return invalidInputResult("Registration: " + reg1);
		}
		if (!regDB.hasItem(reg2) || !indexDB.hasItem(reg2.getCourseIndex())) {
			return invalidInputResult("Registration: " + reg2);
		}

		// Check registration status
		if (reg1.getStatus() != Status.REGISTERED) {
			return failure("User " + reg1.getStudent().getUsername() + " haven't successfully registered index "
					+ reg1.getCourseIndex().getIndexNo() + ", the index cannot be swapped.");
		}
		if (reg2.getStatus() != Status.REGISTERED) {
			return failure("User " + reg2.getStudent().getUsername() + " haven't successfully registered index "
					+ reg2.getCourseIndex().getIndexNo() + ", the index cannot be swapped.");
		}

		// Check the duplication of indexes of registrations.
		if (reg1.getCourseIndex().getIndexNo().equals(reg2.getCourseIndex().getIndexNo())) {
			return failure("Two indexes of the registrations to be swapped are the same.");
		}

		// Check consistent course code
		String courseIndex1 = reg1.getCourseIndex().getIndexNo();
		String courseIndex2 = reg2.getCourseIndex().getIndexNo();
		String courseCode1 = reg1.getCourse().getCourseCode();
		String courseCode2 = reg2.getCourse().getCourseCode();
		if (!courseCode1.equals(courseCode2)) {
			return failure("Course mismatch: the course of index " + courseIndex1 + ": " + courseCode1
					+ "; The course of index " + courseIndex2 + ": " + courseCode2);
		}

		// Checking time slot clashing
		Result clashingResult;
		clashingResult = checkTimeSlotClash(reg1.getStudent().getRegistrationList(), reg2.getCourseIndex(), reg1);
		if (clashingResult != null) {
			return clashingResult;
		}
		clashingResult = checkTimeSlotClash(reg2.getStudent().getRegistrationList(), reg1.getCourseIndex(), reg2);
		if (clashingResult != null) {
			return clashingResult;
		}

		// Swapping index.
		CourseIndex temp = reg1.getCourseIndex();
		reg1.setCourseIndex(reg2.getCourseIndex());
		reg2.setCourseIndex(temp);

		return Result.SUCCESSFUL;
	}

	/**
	 * Checking whether two schedules clash
	 * 
	 * @param s1 one schedule
	 * @param s2 the other schedule
	 * @return true if they clash with each other
	 */
	private boolean isClashed(Schedule s1, Schedule s2) {
		if (s1.getDayOfWeek() != s2.getDayOfWeek()) {
			return false;
		}

		if (s1.getBeginTime().compareTo(s2.getEndTime()) >= 0 || s2.getBeginTime().compareTo(s1.getEndTime()) >= 0) {
			return false;
		}

		Set<Integer> weekIntersection = new HashSet<>(s1.teachingWeeks());
		weekIntersection.retainAll(s2.teachingWeeks());
		if (weekIntersection.isEmpty()) {
			return false;
		}

		return true;
	}

	/**
	 * Checking whether two indexes clash.
	 * <p>
	 * Two indexes clash if any two schedules of them clash.
	 * 
	 * @param index1 one index
	 * @param index2 the other index
	 * @return true if they clash with each other
	 */
	private boolean isClashed(CourseIndex index1, CourseIndex index2) {
		List<Schedule> scheduleList1 = index1.getScheduleList();
		List<Schedule> scheduleList2 = index2.getScheduleList();

		for (int i = 0; i < scheduleList1.size(); ++i) {
			for (int j = 0; j < scheduleList2.size(); ++j) {
				if (isClashed(scheduleList1.get(i), scheduleList2.get(j))) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Generate a failure result.
	 * 
	 * @param msg the message of result.
	 * @return a failed result with the given message.
	 */
	private Result failure(String msg) {
		return new Result(false, msg);
	}

	/**
	 * Generate result for invalid input
	 * 
	 * @param value the invalid value in string format.
	 * @return a result showing invalid input.
	 */
	private Result invalidInputResult(String value) {
		return failure("Invalid input: " + value);
	}

	/**
	 * Generate result for time slots clashing
	 * 
	 * @param idx1 one of the clashing index pairs
	 * @param idx2 the other on of the clashing index pairs
	 * @return a result showing time slot clashing.
	 */
	private Result timeSlotClash(CourseIndex idx1, CourseIndex idx2) {
		return failure("Time slots clashing: indexes " + idx1.getIndexNo() + " and " + idx2.getIndexNo() + " clash.");
	}

	/**
	 * Checking if an index clashes with any registration in the given registration
	 * set.
	 * <p>
	 * If so, produce a {@link Result} with
	 * {@link CourseAllocator#timeSlotClash(CourseIndex, CourseIndex)} and two
	 * clashing indexes. Otherwise, return null.
	 * <p>
	 * A registration can be specified to be the exception, which will not be
	 * checked for time slot clashing. The value of exception is typically null
	 * (which means not exception) or a value in the given registration set.
	 * 
	 * @param regList     the given registration list
	 * @param courseIndex the course index used to find potential clashing.
	 * @param except      the registration doesn't participate in the clashing
	 *                    checking.
	 * @return result of clashing checking. If not clashing happens, return null.
	 */
	private Result checkTimeSlotClash(Iterable<Registration> regList, CourseIndex courseIndex, Registration except) {
		for (Registration reg : regList) {
			if (reg == except) {
				continue;
			}
			if (isClashed(reg.getCourseIndex(), courseIndex)) {
				return timeSlotClash(reg.getCourseIndex(), courseIndex);
			}
		}
		return null;
	}

	/**
	 * Approve student's registration on wait list of the given course index.
	 * <p>
	 * Notification will be sent through {@link Configs#getNotificationSender()}
	 * 
	 * @param index the index whose wait list is checked.
	 */
	private void approveRegistration(CourseIndex index) {
		if (index.getRegisteredList().size() >= index.getMaxVacancy() || index.getWaitList().size() <= 0) {
			return;
		}

		int limit = Math.min(index.getWaitList().size(), index.getMaxVacancy() - index.getRegisteredList().size());

		// The registrations to be approved.
		List<Registration> approvals = new ArrayList<>(limit);

		// Searching from the earliest wait list to the latest wait list.
		for (Registration reg : index.getWaitList()) {
			// Check approval limit
			if (approvals.size() >= limit) {
				break;
			}
			// Check AU limit.
			if (Configs.getMaxAu() >= reg.getStudent().getRegisteredAU() + index.getCourse().getAu()) {
				approvals.add(reg);
			}
		}

		for (Registration reg : approvals) {
			// Notify course index so that it can maintain the wait list.
			index.changeRegistrationStatus(reg, Status.REGISTERED);
			// Notify the student for registration approval.
			sender.sendWaitlistNotification(reg);
		}
	}

	/**
	 * A class representing the result of a function of allocator.
	 * <p>
	 * The following information will be included:
	 * <ol>
	 * <li>Whether the attempt to execute the function is successful.
	 * <li>The message produced by the allocator. If an function failed to finish
	 * normally, the message will show the reason of failure.
	 * </ol>
	 */
	public static class Result {

		/**
		 * A result representing success.
		 */
		public static final Result SUCCESSFUL = new Result(true, "Successful");

		private boolean isSuccessful;
		private String message;

		/**
		 * Construct a result with the given success state and message.
		 * 
		 * @param isSuccessful the success state or result
		 * @param message      the message of result
		 */
		public Result(boolean isSuccessful, String message) {
			this.isSuccessful = isSuccessful;
			this.message = message;
		}

		/**
		 * Get whether the attempt of allocation is successful.
		 * 
		 * @return whether the attempt of allocation is successful.
		 */
		public boolean isSuccessful() {
			return this.isSuccessful;
		}

		/**
		 * Get the message produce by the allocator.
		 * <p>
		 * If the allocation fail (that is {@link Result#isSuccessful()} return false),
		 * the message is guaranteed to be a non-empty string.
		 * 
		 * @return the message produce by the allocator.
		 */
		public String message() {
			return this.message;
		}

	}

	public static void main(String[] args) {
		Configs.init();
		Student stud = StudentDB.getDB().getByKey("testuser0");
		CourseIndex index = CourseIndexDB.getDB().getByKey("10046");
		System.out.println(stud);
		System.out.println(index);
		TablePrinter.getPrinter().printIndexAndSchedule(index);
		Result result = CourseAllocator.getInstance().registerCourse(stud, index);
		System.out.println(result.message());
		result = CourseAllocator.getInstance().registerCourse(stud, index);
		System.out.println(result.message());
		RegistrationDB.getDB().saveData();
	}

}