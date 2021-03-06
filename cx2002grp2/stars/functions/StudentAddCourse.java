package cx2002grp2.stars.functions;

import cx2002grp2.stars.CourseAllocator.Result;
import cx2002grp2.stars.database.CourseIndexDB;
import cx2002grp2.stars.database.StudentDB;
import cx2002grp2.stars.dataitem.CourseIndex;
import cx2002grp2.stars.dataitem.Student;
import cx2002grp2.stars.dataitem.User;

/**
 * a function which is used to register a course.
 * <p>
 * using singleton pattern to make sure only one object is created.
 * <p>
 * this function can be accessed by student.
 */
public class StudentAddCourse extends AbstractFunction {

    /**
     * An instance of function, for Singleton pattern.
     */
    private static Function instance = new StudentAddCourse();

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
    private StudentAddCourse() {

    }

    @Override
    public boolean accessible(User user) {
        return normalStudentAccessible(user);
    }

    @Override
    public String name() {
        return "Add a Course";
    }

    @Override
    protected void implementation(User user) {
        Student student = StudentDB.getDB().getFromUser(user);
        if (student == null) {
            System.out.println("Student doesn't exist. Please try again.");
            return;
        }

        while (true) {
            System.out.print("Please enter course index: ");
            String rawIndex = this.sc().nextLine();

            CourseIndex courseIndex = CourseIndexDB.getDB().getByKey(rawIndex);
            if (courseIndex == null) {
                System.out.println("course Index doesn't exist.");
                if (!askYesNo("Try again?")) {
                    return;
                } else {
                    continue;
                }
            }

            this.tbPrinter().printIndexAndSchedule(courseIndex);

            if (this.askYesNo("Register for course index " + rawIndex + "?")) {
                Result result = this.allocator().registerCourse(student, courseIndex);
                System.out.println(result.message());
                if (!result.isSuccessful()) {
                    if (!askYesNo("Try again?")) {
                        return;
                    } else {
                        continue;
                    }
                }
            } else {
                System.out.println("Action cancelled.");
            }

            
            if (!askYesNo("Add another course?")) {
                return;
            }
        }

    }

}
