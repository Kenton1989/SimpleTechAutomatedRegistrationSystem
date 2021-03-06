package cx2002grp2.stars.dataconverter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import cx2002grp2.stars.database.CourseIndexDB;
import cx2002grp2.stars.database.StudentDB;
import cx2002grp2.stars.dataitem.CourseIndex;
import cx2002grp2.stars.dataitem.Registration;
import cx2002grp2.stars.dataitem.Student;
import cx2002grp2.stars.dataitem.Registration.Status;

/**
 * Concrete implementation for {@link Converter converter} of
 * {@link Registration Registration}
 */
public class RegistrationConverter implements Converter<Registration> {

    /**
     * Size of row of the table storing the registration.
     */
    private static final int ROW_SIZE = 4;
    /**
     * Position of field in the row of table.
     */
    private static final int STUD_POS = 0, INDEX_POS = 1, TIME_POS = 3, STAT_POS = 2;

    @Override
    public List<String> toStringList(Registration item) {
        try {
            if (item.isDropped()) {
                return null;
            }

            String[] row = new String[ROW_SIZE];

            row[STUD_POS] = item.getStudent().getUsername();
            row[INDEX_POS] = item.getCourseIndex().getIndexNo();
            row[TIME_POS] = item.getRegisterDateTime().toString();
            row[STAT_POS] = item.getStatus().name();

            return Arrays.asList(row);
        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Registration fromStringList(List<String> strings) {
        try {
            String username = strings.get(STUD_POS);
            Student student = StudentDB.getDB().getByKey(username);

            String index = strings.get(INDEX_POS);
            CourseIndex courseIndex = CourseIndexDB.getDB().getByKey(index);

            LocalDateTime registerDateTime = LocalDateTime.parse(strings.get(TIME_POS));

            Status status = Status.valueOf(strings.get(STAT_POS));

            return new Registration(student, courseIndex, registerDateTime, status);
        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

}
