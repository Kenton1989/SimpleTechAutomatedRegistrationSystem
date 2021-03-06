package cx2002grp2.stars.database;

import cx2002grp2.stars.dataconverter.Converter;
import cx2002grp2.stars.dataconverter.ConverterFactory;
import cx2002grp2.stars.dataitem.Course;

/**
 * Database storing {@link Course}.
 * <p>
 * The database is implemented with Singleton pattern.
 */
public class CourseDB extends CaseInsensitiveStringKeyDB<Course> {

    /**
     * database file that course database is storing
     */
    private static final String DB_FILE_PATH = "tables/course.csv";

    /**
     * A unique instance of database, for Singleton pattern.
     */
    private static CourseDB instance = new CourseDB();
    
    /**
     * Get instance of database, for Singleton pattern.
     * @return the singleton course database.
     */
    public static CourseDB getDB() {
        return instance;
    }

    /**
     * Converter for converting course item from and into string list.
     */
    private Converter<Course> converter = ConverterFactory.courseConverter();

    /**
     * Loader used to load database from and into file.
     */
    private SimpleDatabaseLoader loader = SimpleDatabaseLoader.getLoader();

    /**
     * Construct a database with data loaded.
     */
    protected CourseDB() {
        loadData();
    }

    @Override
    public Course getByKey(String key) {
        return super.getByKey(key.toUpperCase());
    }

    @Override
    protected void loadData() {
        loader.load(DB_FILE_PATH, this, converter);
    }

    @Override
    public void saveData() {
        loader.save(this, DB_FILE_PATH, converter);
    }

}
