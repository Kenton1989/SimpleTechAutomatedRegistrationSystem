package cx2002grp2.stars.functions;

import java.util.List;

import cx2002grp2.stars.database.CourseDB;
import cx2002grp2.stars.database.RegistrationDB;
import cx2002grp2.stars.dataitem.Course;
import cx2002grp2.stars.dataitem.User;
import cx2002grp2.stars.dataitem.User.Domain;

/**
 * A class to manage the addition, modification and deletion of courses and its
 * related information.
 * <p>
 * when a course is deleted, all related course index, schedules and
 * registrations will be deleted.
 */
public class EditCourse extends AbstractFunction {

    /**
     * An instance of function, for Singleton pattern.
     */
    private static Function instance = new EditCourse();

    /**
     * An getter of function instance, for Singleton pattern.
     * 
     * @return an instance of function.
     */
    public static Function getInstance() {
        return instance;
    }

    /**
     * private constructor reserve for Singleton pattern
     */
    private EditCourse() {

    }

    @Override
    public boolean accessible(User user) {
        return user.getDomain() == Domain.STAFF;
    }

    @Override
    public String name() {
        return "Manage Courses";
    }

    @Override
    protected void implementation(User user) {
        while (true) {
            System.out.println("Please select the action: ");
            System.out.println("1: Add a Course");
            System.out.println("2: Edit a Course");
            System.out.println("3: Delete a course");
            System.out.println("4: Print all courses");
            System.out.println("5: Exit course manager");

            int selection = this.enterInt("", 1, 5);
            switch (selection) {
                case 1:
                    if (this.addCourse()) {
                        tbPrinter().printBreakLine();
                        System.out.println("List of all courses after addition:");
                        tbPrinter().printCourseList(CourseDB.getDB());
                        System.out.println("Successfully added.");
                    }
                    break;
                case 2:
                    if (this.editCourse()) {
                        System.out.println("Successfully edited.");
                    }
                    break;
                case 3:
                    if (this.deleteCourse()) {
                        System.out.println("Successfully deleted.");
                    }
                    break;
                case 4:
                    tbPrinter().printCourseList(CourseDB.getDB());
                    break;
                case 5:
                    return;
            }
        }
    }

    /**
     * add a new course into database.
     * <p>
     * This function will ask user to input basic information of a course, including
     * course code, course name, school and AU.
     * <p>
     * The course code cannot be the same as other course.
     * 
     * @return true if course is successfully added.
     */
    private boolean addCourse() {
        System.out.println("You selected: Add a Course");

        while (true) {
            System.out.println("Please input the Course Code:");
            String newCourseCode = this.sc().nextLine();

            if (CourseDB.getDB().hasKey(newCourseCode)) {
                System.out.println("The course code already exists in the database. Please try again.");
                if (askYesNo("Try again?"))
                    continue;
                else
                    return false;
            }

            System.out.println("Please input the Course Name:");
            String newCourseName = this.sc().nextLine();

            System.out.println("Please input the School the course belongs to :");
            String school = this.sc().nextLine();

            int AU = this.enterInt("Please input the AU of this course: ", 1, 6);

            Course newCourse;
            try {
                newCourse = new Course(newCourseCode, newCourseName, school, AU);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                System.out.println("Please try again.");
                if (askYesNo("Try again?"))
                    continue;
                else
                    return false;
            }

            System.out.println("The following course will be added:");
            tbPrinter().printCourseList(List.of(newCourse));

            if (!askYesNo("Confirm?")) {
                System.out.println("Course creation cancelled.");
                return false;
            }

            CourseDB.getDB().addItem(newCourse);
            return true;
        }

    }

    /**
     * Edit the course code of a course.
     * <p>
     * This function will ask user to input new course code, then ask whether to
     * make change.
     * 
     * @param course the course to be set a new course code.
     * @return true if course code is successfully edited.
     */
    private boolean editCourseCode(Course course) {
        this.tbPrinter().printBreakLine();
        System.out.println("You selected: course code");

        while (true) {
            System.out.println("Please input the Course Code:");
            String newCourseCode = this.sc().nextLine().trim().toUpperCase();

            if (course.getCourseCode().equals(newCourseCode)) {
                System.out.println("New course code is the same to the old one.");
                if (askYesNo("Try again?"))
                    continue;
                else
                    return false;
            }

            Course existingCourse = CourseDB.getDB().getByKey(newCourseCode);

            if (existingCourse != null) {
                System.out.println("The following course already exists.");
                tbPrinter().printCourseList(List.of(existingCourse));
                if (askYesNo("Try again?"))
                    continue;
                else
                    return false;
            }

            System.out.println("Old course code: " + course.getCourseCode());
            System.out.println("New course code: " + newCourseCode);
            if (this.askForApplyChange()) {
                CourseDB.getDB().changeKey(course, newCourseCode);
                break;
            } else {
                System.out.println("Action Cancelled.");
                if (askYesNo("Try again?"))
                    continue;
                else
                    return false;
            }
        }
        return true;
    }

    /**
     * Edit the course name of a course.
     * <p>
     * This function will ask user to input new course name, then ask whether to
     * make change.
     * 
     * @param course the course to be set a new course name.
     * @return true if course name is successfully edited.
     */
    private boolean editCourseName(Course course) {
        this.tbPrinter().printBreakLine();
        System.out.println("You selected: course name");

        while (true) {
            System.out.println("Please input the course name:");
            String newCourseName = this.sc().nextLine();

            System.out.println("Old course name: " + course.getCourseName());
            System.out.println("New course name: " + newCourseName);
            if (this.askForApplyChange()) {
                course.setCourseName(newCourseName);
                break;
            } else {
                System.out.println("Action Cancelled.");
                if (askYesNo("Try again?"))
                    continue;
                else
                    return false;
            }
        }

        return true;

    }

    /**
     * Edit the school of a course.
     * <p>
     * This function will ask user to input new school, then ask whether to make
     * change.
     * 
     * @param course the course to be set a new school.
     * @return true if school is successfully edited.
     */
    private boolean editSchool(Course course) {
        this.tbPrinter().printBreakLine();
        System.out.println("You selected: school");

        while (true) {
            System.out.println("Please input the school:");
            String newSchool = this.sc().nextLine();

            System.out.println("Old school: " + course.getSchool());
            System.out.println("New school: " + newSchool);
            if (this.askForApplyChange()) {
                course.setSchool(newSchool);
                break;
            } else {
                System.out.println("Action Cancelled.");
                if (askYesNo("Try again?"))
                    continue;
                else
                    return false;
            }
        }

        return true;

    }

    /**
     * Edit the AU of a course.
     * <p>
     * This function will ask user to input new AU, then ask whether to make change.
     * 
     * @param course the course to be set a new AU.
     * @return true if AU is successfully edited.
     * @deprecated AU of a course is supposed to be fixed. Changing AU of a course
     *             may results in the change of course allocation, since max AU
     *             limit is considered when allocating course. However, it is not
     *             supported by our allocation system.
     */
    @Deprecated
    private boolean editAU(Course course) {
        this.tbPrinter().printBreakLine();
        System.out.println("You selected: AU");
        while (true) {
            int AU = this.enterInt("Please input the AU of this course: ", 1, 6);

            System.out.println("Old AU: " + (int) course.getAu());
            System.out.println("New AU: " + AU);
            if (this.askForApplyChange()) {
                course.setAu(AU);
                break;
            } else {
                System.out.println("Action Cancelled.");
                if (askYesNo("Try again?"))
                    continue;
                else
                    return false;
            }
        }

        return true;
    }

    /**
     * Edit a course which already exists in the database.
     * <p>
     * This function will ask user to input course code, then ask what to edit about
     * the course.
     * <p>
     * User can edit course code, course name, school and AU.
     * <p>
     * When course code changes, all related schedules and registrations will also
     * change.
     * 
     * @return true if information is successfully edited.
     */
    private boolean editCourse() {
        System.out.println("You selected: Edit a Course");

        System.out.println("Please input the Course Code:");
        String courseCode = this.sc().nextLine();

        if (!CourseDB.getDB().hasKey(courseCode)) {
            System.out.println("The course code doesn't exist in the database. Please try again.");
            return false;
        }

        Course course = CourseDB.getDB().getByKey(courseCode);

        System.out.println("Current Course Info: ");
        tbPrinter().printCourseList(List.of(course));

        System.out.println("Please select the attribute to edit:");
        System.out.println("1: course code");
        System.out.println("2: course name");
        System.out.println("3: school");
        // System.out.println("4: AU");

        int selection = this.enterInt("", 1, 3);
        switch (selection) {
            case 1:
                return editCourseCode(course);
            case 2:
                return editCourseName(course);
            case 3:
                return editSchool(course);
            // case 4:
            // return editAU(course);
        }

        return false;
    }

    /**
     * Delete a course from database.
     * <p>
     * This function will ask user to input course code, then ask whether to delete
     * the course.
     * <p>
     * All related course index, schedules and registrations will be deleted.
     * 
     * @return true if selected course is successfully deleted.
     */
    private boolean deleteCourse() {
        System.out.println("You selected: Delete a Course");
        Course deleted = null;

        while (true) {
            System.out.println("Please input the Course Code:");
            String courseCode = this.sc().nextLine();
            deleted = CourseDB.getDB().getByKey(courseCode);

            if (deleted == null) {
                System.out.println("The course code doesn't exist in the database. Please try again.");
                if (askYesNo("Try again?"))
                    continue;
                else
                    return false;
            }

            System.out.println("Deleting Course");
            tbPrinter().printCourseList(List.of(deleted));

            System.out.println("WARNING: This action will delete ALL information related to\n"
                    + "this course, including ALL course indexes, schedules and registrations!!!");

            System.out.println("Number of indexes under this course: " + deleted.getIndexList().size());
            System.out.println("Number of registrations under this course: "
                    + RegistrationDB.getDB().getRegOfCourseCode(deleted.getCourseCode(), false).size());

            if (this.askYesNo("Confirm deletion?")) {
                CourseDB.getDB().delByKey(courseCode);
                break;
            } else {
                System.out.println("Action Cancelled.");
                if (askYesNo("Try again?"))
                    continue;
                else
                    return false;
            }
        }

        return true;
    }

}
