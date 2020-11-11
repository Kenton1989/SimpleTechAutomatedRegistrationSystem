package cx2002grp2.stars.data.database;

import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Logger;

import cx2002grp2.stars.Configs;
import cx2002grp2.stars.data.converter.Converter;
import cx2002grp2.stars.data.converter.ConverterFactory;
import cx2002grp2.stars.data.dataitem.Student;
import cx2002grp2.stars.data.dataitem.User;

public class StudentDB extends AbstractSingleKeyDatabase<String, Student> {

	private static Logger log = Configs.getLogger(StudentDB.class.getName());

	private static final String DB_FILE_PATH = "tables/student.csv";

	private static final Converter<Student> converter = ConverterFactory.studentConverter();

	private static final SimpleDatabaseLoader loader = SimpleDatabaseLoader.getLoader();

	private static StudentDB instance = new StudentDB();

	public static StudentDB getDB() {
		return instance;
	}

	protected StudentDB() {
		loadData();

		UserDB userDB = UserDB.getDB();
		// When a user is deleted, delete it from student database too, if it exists.
		userDB.addOnItemDeletedObserver(user -> delByKey(user.getUsername()));
		// When a key in user database changed, change the key for the same thing.
		userDB.addOnKeyChangedObserver((oldKey, newItem) -> changeKey(oldKey, newItem.getKey()));

	}

	/**
	 * 
	 * @param user
	 */
	public Student getFromUser(User user) {
		Objects.requireNonNull(user);

		return getByKey(user.getKey());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * When a student is added into the database, the student will be added into the
	 * user database too.
	 * 
	 * @param student the student to be inserted into database.
	 * @throws IllegalArgumentException if in the user database, there is a user
	 *                                  with the same user name as input student,
	 *                                  but the domain of that user is not
	 *                                  {@link User.Domain#STUDENT}, the incoming
	 *                                  student is considered as an illegal argumant
	 */
	@Override
	public Student addItem(Student student) {
		Objects.requireNonNull(student);
		Objects.requireNonNull(student.getKey());

		User user = UserDB.getDB().getByKey(student.getKey());

		if (user != null && user.getDomain() != student.getDomain()) {
			throw new IllegalArgumentException("Incapable Domain Replacement: Username: " + student.getUsername()
					+ ", original domain: " + user.getDomain() + ", new domain: " + student.getDomain());
		}

		UserDB.getDB().addItem(student);

		return super.addItem(student);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public boolean changeKey(String oldKey, String newKey) {
		Objects.requireNonNull(oldKey);
		Objects.requireNonNull(newKey);

		log.info("Try to change key from " + oldKey + " to " + newKey);
		boolean canChange = UserDB.getDB().changeKey(oldKey, newKey);

		// Fail to change key in the user database means the key in student database
		// cannot change, too.
		if (!canChange) {
			return false;
		}

		return super.changeKey(oldKey, newKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean changeKey(Student oldItem, String newKey) {
		return changeKey(oldItem.getKey(), newKey);
	}

	@Override
	protected void loadData() {
		loader.load(DB_FILE_PATH, this, converter);
	}

	@Override
	protected void saveData() {
		loader.save(this, DB_FILE_PATH, converter);
	}

}