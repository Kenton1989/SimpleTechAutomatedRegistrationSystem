package cx2002grp2.stars.data.dataitem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import cx2002grp2.stars.data.Gender;

/**
 * Student
 */
public class Student extends User {
    
	private String matricNo;
	private Gender gender;
	private String fullName;
	private String nationality;
	private int yearOfStudy;
	private String programme;
	private Set<Registration> registrationList;

	public Student(User user, String matricNo, Gender gender, String fullName, String nationality, int yearOfStudy, String programme) {
		super(user);

		this.matricNo = matricNo;
		this.gender = gender;
		this.fullName = fullName;
		this.nationality = nationality;
		this.yearOfStudy = yearOfStudy;
		this.programme = programme;
		this.registrationList = new HashSet<>();
	}

	@Override
	public Domain getDomain() {
		return User.Domain.STUDENT;
	}

	@Override
	public void setDomain(Domain domain) {
		throw new UnsupportedOperationException("The domain of a student cannot change.");
	}

	/**
	 * 
	 * @return
	 */
	public String getMatricNo() {
		return this.matricNo;
	}

	/**
	 * 
	 * @param matricNo
	 */
	public void setMatricNo(String matricNo) {
		this.matricNo = matricNo;
	}

	/**
	 * 
	 * @return
	 */
	public Gender getGender() {
		return this.gender;
	}

	/**
	 * 
	 * @param gender
	 */
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	/**
	 * 
	 * @return
	 */
	public String getFullName() {
		return this.fullName;
	}

	/**
	 * 
	 * @param fullName
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**
	 * 
	 * @return
	 */
	public String getNationality() {
		return this.nationality;
	}

	/**
	 * 
	 * @param nationality
	 */
	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	/**
	 * 
	 * @return
	 */
	public int getYearOfStudy() {
		return yearOfStudy;
	}

	/**
	 * 
	 * @param yearOfStudy
	 */
	public void setYearOfStudy(int yearOfStudy) {
		this.yearOfStudy = yearOfStudy;
	}

	/**
	 * 
	 * @return
	 */
	public String getProgramme() {
		return programme;
	}

	/**
	 * 
	 * @param programme
	 */
	public void setProgramme(String programme) {
		this.programme = programme;
	}

    /**
     * 
     * @return
     */
	public Collection<Registration> getRegistrationList() {
        return Collections.unmodifiableSet(registrationList);
    }

	/**
	 * 
	 * @param regitration
	 */
	public boolean addRegistration(Registration regitration) {
        // TODO - implement Student.addRegistration
        return false;
	}

	/**
	 * 
	 * @param registration
	 */
	public boolean delRegistration(Registration registration) {
        // TODO - implement Student.delRegistration
        return false;
	}

}
