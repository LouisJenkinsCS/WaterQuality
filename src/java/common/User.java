package common;

import java.io.Serializable;
import java.sql.Timestamp;
import utilities.Debug;

/**
 * Represents a generic user.
 *
 * @author cjones
 */
public class User implements Comparable<User>, Serializable {

    private int userNumber; //Primary key in our database --auto created
    private String loginName;
    private String userPassword;
    private String salt;
    private String lastName;
    private String firstName;
    private String emailAddress;
    private UserRole userRole;
    private Timestamp lastLoginTime;//time and date of last login
    private Timestamp lastAttemptedLoginTime;
    private int loginCount; // how many successful logins
    private int attemptedLoginCount; // How many consecutive failed login attempts 
    private boolean locked;//If the account is locked out

    /**
     * Constructs an empty user.
     */
    public User() {
    }

    /**
     * Constructs a new user with given properties such as a user number, login
     * name, password, last name, first name, email address, department, user
     * role, last login, and login count.
     *
     * @param userNumber A number that represents this user. This number is the
     * primary key in our database and is auto incremented.
     *
     * @param loginName The name that this user uses to login to the system.
     *
     * @param password The password that this user uses to login to the system.
     *
     * @param salt The salt used to hash the password for this user
     *
     * @param lastName The last name of this user.
     *
     * @param firstName The first name of this user.
     *
     * @param emailAddress The university email of this user.
     *
     * @param userRole The <code>UserRole</code> of this user. This sets which
     * privileges user has. Determines which screens and controls the user will
     * have access to.
     *
     * @param lastLogin The last time this user logged in.
     *
     * @param lastAttemptedLogin The first time the user attempts to log in in
     * fails within 15 minutes.
     *
     * @param attemptedLoginCount Number of failed logins before user gets
     * locked out.
     *
     * @param loginCount The number of attempted logins by this user. If the
     * user fails three times to login, the account will be locked until a chair
     * unlocks it.
     *
     * @param locked Boolean that represents whether a user is locked out or
     * not.
     */
    public User(int userNumber, String loginName, String password, String salt, String lastName, String firstName, String emailAddress, 
            UserRole userRole, Timestamp lastLogin, Timestamp lastAttemptedLogin, int loginCount, int attemptedLoginCount, boolean locked) {
        this.userNumber = userNumber;
        this.loginName = loginName;
        this.userPassword = password;
        this.lastName = lastName;
        this.firstName = firstName;
        this.emailAddress = emailAddress;
        this.userRole = userRole;
        this.lastLoginTime = lastLogin;
        this.lastAttemptedLoginTime = lastAttemptedLogin;
        this.loginCount = loginCount;
        this.attemptedLoginCount = attemptedLoginCount;
        this.salt = salt;
        this.locked = locked;
    }

    /**
     * Gets the number of times a user has attempted to log in a fail in a time
     * period specified in the property file.
     *
     * @return The number of login attempts.
     */
    public int getAttemptedLoginCount() {
        return attemptedLoginCount;
    }

    /**
     * Sets the number of times a user has attempted to log in and fail in a
     * time period specified in the project property file.
     *
     * @param attemptedLoginCount Sets the number of login attempts.
     */
    public void setAttemptedLoginCount(int attemptedLoginCount) {
        this.attemptedLoginCount = attemptedLoginCount;
    }

    /**
     * Gets the email address of this user.
     *
     * @return The email address of this user. If there is no email address null
     * is returned.
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the email address for this user. No error checking is performed.
     *
     * @param emailAddress The email address that is set for this user.
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Gets the user number of this user. This is the unique identifier for this
     * user.
     *
     * @return The user number of this user. If there is no user number null is
     * returned.
     */
    public int getUserNumber() {
        return userNumber;
    }

    /**
     * Sets the user number for this user. No error checking is performed.
     *
     * @param userNumber The user number that is set for this user.
     */
    public void setUserNumber(int userNumber) {
        this.userNumber = userNumber;
    }

    /**
     * Gets the last name of this user.
     *
     * @return The last name of the user. If no last name exists null is
     * returned.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of this user. No error checking is performed.
     *
     * @param lastName The last name that is set to this user.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the first name of this user.
     *
     * @return The first name of this user. If no first name exists null is
     * returned.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of this user. No error checking is performed.
     *
     * @param firstName The first name that is set to this user.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the <code>UserRole</code> for this user. This consists of an
     * enumeration of whether the student is a SystemAdmin, Administrator,
     * Chair, Advisor, Instructor, Staff, Student, or Guest.
     *
     * @return The <code>UserRole</code> for this user. If no
     * <code>UserRole</code> is set null is returned.
     */
    public UserRole getUserRole() {
        return userRole;
    }

    /**
     * Sets the <code> UserRole </code> for this user. No error checking is
     * performed.
     *
     * @param userRole The <code>UserRole</code> that is set to this user.
     */
    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    /**
     * Gets the login name of this user.
     *
     * @return The login name of this user. If there is no login name null is
     * returned.
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * Sets the login name of this user. No error checking is performed.
     *
     * @param loginName The login name that is set for this user.
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    /**
     * Gets the password for the login of this user.
     *
     * @return The password for this user. If no password exists null is
     * returned.
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * Sets the password for the login of this user. No error checking is
     * performed.
     *
     * @param userPassword The password that is set for this user.
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * Gets the salt for the login of this user.
     *
     * @return The salt for this user. If no password exists null is returned.
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Sets the salt for the login of this user. No error checking is performed.
     *
     * @param salt The salt that is set for this user.
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    /**
     * Gets the last time the user logged in.
     *
     * @return The last time this user logged in. If no last time logged in
     * exists null is returned.
     */
    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    /**
     * Sets the last time the user logged in. This will be set right when the
     * user logs into the system.
     *
     * @param lastLoginTime The last time this user logged in.
     */
    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    /**
     * Gets the time of the first time a user attempted to log in and failed.
     * This value will be set when a user fails to log in not within a 15 minute
     * time interval of the last time they fail to log in.
     *
     * @return The last time this user failed to log in not within a certain
     * time interval. If user has not failed to log in this value returns null.
     */
    public Timestamp getLastAttemptedLoginTime() {
        return lastAttemptedLoginTime;
    }

    /**
     * Sets the time of the first time a user attempted to log in and failed.
     * This value will be set when a user fails to log in not within a 15 minute
     * time frame of the last time they fail to log in.
     *
     * @param lastAttemptedLogin The time the user failed to log in not within a
     * certain time interval.
     */
    public void setLastAttemptedLoginTime(Timestamp lastAttemptedLogin) {
        this.lastAttemptedLoginTime = lastAttemptedLogin;
    }

    /**
     * Gets the number of log in attempts from this user. Only failed attempts
     * count and this gets reset when user logins successfully.
     *
     * @return The number of failed login attempts.
     */
    public int getLoginCount() {
        return loginCount;
    }

    /**
     * Sets the number of failed login attempts. No error checking is performed.
     *
     * @param loginCount The number of failed attempts for this user.
     */
    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    /**
     * Returns a boolean value that represents whether the <code>User</code> is
     * locked out of having administrator privileges. If a <code>User</code>
     * attempts and fails to log in 5 consecutive times. They become locked-out.
     *
     * @return True if the user is locked out of the web site, false otherwise.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Sets the user to be locked out of being able to log in.
     *
     * @param locked True if the <code>User</code> should become locked-out.
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Returns the hash value of this user number.
     *
     * @return The hash value of this user number.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.userNumber;
        return hash;
    }

    /**
     * Overrides the equals method to compare two <code> User </code>s. If the
     * object parameter is not a <code> User </code> or is null return false.
     * Otherwise, if the <code> User </code> is equal to this <code> User
     * </code>, return true.
     *
     * @param obj The <code> User </code> object to be compared to.
     * @return Whether or not the <code> User </code> given is equal to this <code> User
     * </code>.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final User other = (User) obj;
        return this.userNumber == other.userNumber;
    }

    /**
     * Returns the order of a <code> User </code> compared to this <code> User
     * </code>.
     *
     * @param o The <code> User </code> to compare to this <code> User </code>.
     * @return The order of the given <code> User </code> relative to this <code> User
     * </code>. Positive for before, zero for equal, and negative for after.
     */
    @Override
    public int compareTo(User o) {
        int order = lastName.compareToIgnoreCase(o.lastName);
        if (order != 0) {
            return order;
        }
        order = firstName.compareToIgnoreCase(o.firstName);
        return order;
    }

    @Override
    public String toString() {
        return "User{" + "userNumber=" + userNumber + ", loginName=" + loginName
                + ", userPassword=" + userPassword + ", salt=" + salt
                + ", lastName=" + lastName
                + ", firstName=" + firstName + ", emailAddress=" + emailAddress
                + ", userRole="  + userRole + ", lastLogin=" + lastLoginTime 
                + ", attemptedLogin=" + lastAttemptedLoginTime + ", loginCount=" 
                + loginCount + ", locked=" + locked + '}';
    }

    /**
     * Tests the <code>User</code> class. Reviews the setFirstName(),
     * setLastName(), setEmailAddress(), setAcademicOrganization(),
     * setLoginName(), setLastLogin(), setLoginCount(), setUserPassword(),
     * setUserRole(), toString(), compareTo(), equals(), hashCode(),
     * getFirstName(), getLastName(), getEmailAddress(),
     * getAcademicOrganization(), getLoginName(), getLastLogin(),
     * getLoginCount(), getUserPassword(), getUserRole(), and User().
     *
     * @param args Command line arguments.
     */
    public static void main(String args[]) {
        User u1 = new User();
        u1.setFirstName("Dan");
        u1.setLastName("James");
        u1.setEmailAddress("drj@drj.com");
        u1.setLoginName("drj");
        u1.setLastLoginTime(new Timestamp(900001));
        u1.setLoginCount(30);
        u1.setUserNumber(007);
        u1.setUserPassword("123abc");
        u1.setUserRole(UserRole.Student);

        User u2 = new User(8, "dtp", "mypasswordisbetter", "12asd21f8a", "Pany", "Dan", "dtp@dtp.com",
                UserRole.Student, new Timestamp(90000), null, 9001, 0, false);

        Debug.println("FNAME: " + u1.getFirstName());
        Debug.println("LNAME: " + u1.getLastName());
        Debug.println("EMAIL: " + u1.getEmailAddress());
        Debug.println("USERNUMBER: " + u1.getUserNumber());
        Debug.println("LOGINNAME: " + u1.getLoginName());
        Debug.println("PASSWORD: " + u1.getUserPassword());
        Debug.println("USERROLE: " + u1.getUserRole());
        Debug.println("LAST LOGIN: " + u1.getLastLoginTime());
        Debug.println("LOGIN COUNT: " + u1.getLoginCount());

        Debug.println();
        Debug.println("FNAME: " + u2.getFirstName());
        Debug.println("LNAME: " + u2.getLastName());
        Debug.println("EMAIL: " + u2.getEmailAddress());
        Debug.println("USERNUMBER: " + u2.getUserNumber());
        Debug.println("LOGINNAME: " + u2.getLoginName());
        Debug.println("PASSWORD: " + u2.getUserPassword());
        Debug.println("USERROLE: " + u2.getUserRole());
        Debug.println("LAST LOGIN: " + u2.getLastLoginTime());
        Debug.println("LOGIN COUNT: " + u2.getLoginCount());

        Debug.println();
        Debug.println(u1.equals(u1));
        Debug.println(u1.equals(u2));
        Debug.println(u1.compareTo(u1));
        Debug.println(u1.compareTo(u2));
        Debug.println(u2.compareTo(u1));

        Debug.println();
        Debug.println(u1.hashCode());
        Debug.println(u2.hashCode());
    }
}
