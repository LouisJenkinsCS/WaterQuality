package mysql;


import common.User;
import database.Web_MYSQL_Helper;
import database.SQLUtility;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import utilities.WebErrorLogger;

/**
 * Represents a manager of the user's database, which implement
 * <code>UserManager</code> interface.This class can be used to modify and
 * extract the user's data which is stored in the database.
 *
 * @author cjones
 */
public class UserManager implements database.UserManager {

    /**
     * Returns this <code>User</code> object when the login name and password
     * are correct. At the same time the last login time and the number of
     * logins should be updated, if the login name and password are correct.
     *
     * @param loginName The login name of this user.
     * @param password The password of this user.
     * @return A <code>User</code> object contains all the information about
     * this user.
     */
    @Override
    public User validateUser(String loginName, String password) {
        String sql = "SELECT * FROM users WHERE loginName = ? AND userPassword = ? ; ";
        Connection conn = database.Web_MYSQL_Helper.getConnection();
        ResultSet rs = null;
        PreparedStatement stmt2 = null;
        User user = null;
        try {
            stmt2 = conn.prepareStatement(sql);
            stmt2.setString(1, loginName);
            stmt2.setString(2, password);
            rs = stmt2.executeQuery();
            if (rs == null || rs.next() == false) {
                Web_MYSQL_Helper.returnConnection(conn);
                return null;
            }
            user = SQLUtility.convertResultSetToUser(rs);
            
        } catch (SQLException ex) {
            WebErrorLogger.log(Level.SEVERE, "SQLException in validateUser(String loginName, String password) loginName=" + loginName + " password=" + password + " error: " + ex);
            return null;
        } finally {
            Web_MYSQL_Helper.closePreparedStatement(stmt2);
            Web_MYSQL_Helper.closeResultSet(rs);
            Web_MYSQL_Helper.returnConnection(conn);
        }

        updateUser(user);
        return user;
    }

    /**
     * Returns this <code>User</code> object which has been added to the user's
     * database successfully.
     *
     * @param user A <code>User</code> object which will be used to add to the
     * user's database.
     * @return A <code>User</code> object which has been added to the user's
     * database.
     */
    @Override
    public User addUser(User user) {
        String sql = "INSERT INTO users (userNumber, loginName, userPassword, salt, firstName, lastName, emailAddress,"
                + "userRole, lastLoginTime, loginCount) VALUES (DEFAULT,?,?,?,?,?,?,?,?,?,?); ";
        Connection conn = database.Web_MYSQL_Helper.getConnection();

        try {
            PreparedStatement stmt2 = conn.prepareStatement(sql);
            stmt2.setString(1, user.getLoginName().trim());
            stmt2.setString(2, user.getUserPassword().trim());
            stmt2.setString(3, user.getSalt().trim());
            stmt2.setString(4, user.getFirstName().trim());
            stmt2.setString(5, user.getLastName().trim());
            stmt2.setString(6, user.getEmailAddress().trim());
            stmt2.setString(7, user.getUserRole().getRoleName());
            stmt2.setString(8, user.getLastLoginTime().toString());
            stmt2.setInt(9, user.getLoginCount());

            stmt2.executeUpdate();

        } catch (SQLException ex) {
            WebErrorLogger.log(Level.SEVERE, "SQLException in addUser(User user) user="+user+" error: "+ex);
            Web_MYSQL_Helper.returnConnection(conn);
            return user;
        }

        return getUserByLoginName(user.getLoginName());//need to sue genterated keys
    }

    /**
     * Returns a <code>User</code> object according to the given user ID.
     *
     * @param userID The ID of a user that want to be exported.
     * @return A <code>User</code> object with this user's ID.
     */
    @Override
    public User getUserByID(int userID) {
        String sql = "SELECT * FROM users WHERE userNumber = ? ; ";
        Connection conn = database.Web_MYSQL_Helper.getConnection();
        ResultSet rs;
        User user = null;

        try {
            PreparedStatement stmt2 = conn.prepareStatement(sql);
            stmt2.setInt(1, userID);

            rs = stmt2.executeQuery();
            if (rs == null || rs.next() == false) {
                Web_MYSQL_Helper.closePreparedStatement(stmt2);
                Web_MYSQL_Helper.returnConnection(conn);
                return null;
            }
            user = SQLUtility.convertResultSetToUser(rs);
            
            rs.close();
            Web_MYSQL_Helper.closePreparedStatement(stmt2);
        } catch (SQLException ex) {
            WebErrorLogger.log(Level.SEVERE, "SQLException in getUserByID(int userID) userID="+userID + " error: " + ex);
            Web_MYSQL_Helper.returnConnection(conn);
            return null;
        }
        Web_MYSQL_Helper.returnConnection(conn);

        return user;
    }

    /**
     * Returns a <code>User</code> object according to the given login name.
     *
     * @param loginName The login name for a user that want to be exported.
     * @return A <code>User</code> object with this login name.
     */
    @Override
    public User getUserByLoginName(String loginName) {
        String sql = "SELECT * FROM users WHERE loginName = ? ; ";
        Connection conn = database.Web_MYSQL_Helper.getConnection();
        ResultSet rs;
        User user = null;

        try {
            PreparedStatement stmt2 = conn.prepareStatement(sql);
            stmt2.setString(1, loginName);

            rs = stmt2.executeQuery();
            if (rs == null || rs.next() == false) {
                Web_MYSQL_Helper.closePreparedStatement(stmt2);
                Web_MYSQL_Helper.returnConnection(conn);
                return null;
            }
             user = SQLUtility.convertResultSetToUser(rs);
            rs.close();
            Web_MYSQL_Helper.closePreparedStatement(stmt2);
        } catch (SQLException ex) {
            WebErrorLogger.log(Level.SEVERE, "SQLException in getUserByLoginName(String loginName) loginName=" 
                    + loginName + " error: " + ex);
            Web_MYSQL_Helper.returnConnection(conn);
            return null;
        }
        Web_MYSQL_Helper.returnConnection(conn);
        return user;
    }

    /**
     * Returns a collection of <code>User</code> object, which includes all the
     * users that use the given email address.
     *
     * @param emailAddress A email address.
     * @return A collection of <code>User</code> object according to the given
     * email address.
     */
    @Override
    public Collection<User> getAllUsersWithEmailAddress(String emailAddress) {
        Collection<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE emailAddress = ? ; ";
        Connection conn = database.Web_MYSQL_Helper.getConnection();
        ResultSet rs;
        User user = null;

        try {
            PreparedStatement stmt2 = conn.prepareStatement(sql);
            stmt2.setString(1, emailAddress);

            rs = stmt2.executeQuery();
            if (rs == null) {
                Web_MYSQL_Helper.closePreparedStatement(stmt2);
                Web_MYSQL_Helper.returnConnection(conn);
                return users;
            }
            while (rs.next()) {
                user = SQLUtility.convertResultSetToUser(rs);
                users.add(user);
            }

            Web_MYSQL_Helper.closePreparedStatement(stmt2);
        } catch (SQLException ex) {
            WebErrorLogger.log(Level.SEVERE, "SQLException in getAllUsersWithEmailAddress(String emailAddress) emailAddress="+emailAddress+" error: " + ex);
            Web_MYSQL_Helper.returnConnection(conn);
            return users;
        }

        Web_MYSQL_Helper.returnConnection(conn);

        return users;

    }

    /**
     * Returns a collection of <code>User</code> object, which includes all the
     * users in the database.
     *
     * @return A collection of <code>User</code> object, which includes all the
     * users in the database.
     */
    @Override
    public Collection<User> getAllUsers() {
        Collection<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ; ";
        Connection conn = database.Web_MYSQL_Helper.getConnection();
        ResultSet rs;
        User user = null;

        try {
            PreparedStatement stmt2 = conn.prepareStatement(sql);
            rs = stmt2.executeQuery();
            if (rs == null) {
                Web_MYSQL_Helper.closePreparedStatement(stmt2);
                Web_MYSQL_Helper.returnConnection(conn);
                return users;
            }
            while (rs.next()) {
                user = SQLUtility.convertResultSetToUser(rs);
                
                users.add(user);
            }
            Web_MYSQL_Helper.closePreparedStatement(stmt2);
        } catch (SQLException ex) {
            WebErrorLogger.log(Level.SEVERE, "SQLException in getAllUsers() error:" + ex);
            Web_MYSQL_Helper.returnConnection(conn);
            return users;
        }

        Web_MYSQL_Helper.returnConnection(conn);

        return users;
    }

    /**
     * Returns a <code>User</code> object with latest update, which is stored in
     * the database. This method also clears reset token if user is currently in
     * reset mode.
     *
     * @param user A <code>User</code> object which contains the latest
     * information.
     * @return A <code>User</code> object with latest update.
     */
    @Override
    public User updateUser(User user) {
        String sql = "UPDATE users SET loginName =?, userPassword=?, salt=?,"
                + "firstName = ?, lastName=?, emailAddress=?,"
                + "userRole=?, lastLoginTime=?, lastAttemptedLoginTime=?, loginCount = ?, attemptedLoginCount=?,"
                + " locked=? where userNumber = ? ; ";
        //User newUser = null;
        Connection conn = database.Web_MYSQL_Helper.getConnection();
        try {
            PreparedStatement stmt2 = conn.prepareStatement(sql);
            stmt2.setString(1, user.getLoginName().trim());
            stmt2.setString(2, user.getUserPassword().trim());
            stmt2.setString(3, user.getSalt().trim());
            stmt2.setString(4, user.getFirstName().trim());
            stmt2.setString(5, user.getLastName().trim());
            stmt2.setString(6, user.getEmailAddress().trim());
            stmt2.setString(7, user.getUserRole().getRoleName());
            stmt2.setString(8, user.getLastLoginTime().toString());
            stmt2.setString(9, user.getLastAttemptedLoginTime().toString());
            stmt2.setInt(10, user.getLoginCount());
            stmt2.setInt(11, user.getAttemptedLoginCount());
            stmt2.setBoolean(12, user.isLocked());
            stmt2.setInt(13, user.getUserNumber());
            

            stmt2.executeUpdate();

        } catch (SQLException ex) {
            WebErrorLogger.log(Level.SEVERE, "SQLException in updateUser(User user) " + user + " error: " + ex);
            Web_MYSQL_Helper.returnConnection(conn);
            return getUserByID(user.getUserNumber());
        }

        Web_MYSQL_Helper.returnConnection(conn);
        return getUserByID(user.getUserNumber());
    }

    /**
     * Return ture if the given user have been deleted from the database.
     * Otherwise, return false.
     *
     * @param user A <code>User</code> object which is expected to delete from
     * database
     * @return Ture if the given user have been deleted from the database
     * successfully.Otherwise, return false
     */
    @Override
    public boolean deleteUser(User user) {
        String sql = "DELETE FROM users WHERE userNumber =?; ";
        Connection conn = database.Web_MYSQL_Helper.getConnection();

        try {
            PreparedStatement stmt2 = conn.prepareStatement(sql);
            stmt2.setInt(1, user.getUserNumber());
            stmt2.executeUpdate();
            Web_MYSQL_Helper.returnConnection(conn);
            return !isUser(user);

        } catch (SQLException ex) {
            WebErrorLogger.log(Level.SEVERE, "SQLException in deleteUser(User user) " + user + " error: " + ex);
            Web_MYSQL_Helper.returnConnection(conn);
            return false;
        }

    }

    @Override
    public boolean deleteUserbyID(int userID) {
        User user = getUserByID(userID);
        if(user == null){ return false; }
        return deleteUser(user);
    }

    private boolean isUser(User user) {
        return getUserByLoginName(user.getLoginName()) != null;
    }

   @Override
    public Collection<User> getAllUsersWithoutSystemAdmins() {
        Collection<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE userRole != ?; ";
        Connection conn = database.Web_MYSQL_Helper.getConnection();
        ResultSet rs;
        User user = null;

        try {
            PreparedStatement stmt2 = conn.prepareStatement(sql);
            stmt2.setString(1, common.UserRole.SystemAdmin.getRoleName());
            rs = stmt2.executeQuery();
            if (rs == null) {
                Web_MYSQL_Helper.closePreparedStatement(stmt2);
                Web_MYSQL_Helper.returnConnection(conn);
                return users;
            }
            while (rs.next()) {
                user = SQLUtility.convertResultSetToUser(rs);
                users.add(user);
            }
            Web_MYSQL_Helper.closePreparedStatement(stmt2);
        } catch (SQLException ex) {
            WebErrorLogger.log(Level.SEVERE, "SQLException in getAllUsersWithoutSystemer() error: " + ex);
            Web_MYSQL_Helper.returnConnection(conn);
            return users;
        }

        Web_MYSQL_Helper.returnConnection(conn);

        return users;
    }

    
    @Override
    public String getSaltByLoginName(String loginName) {
        String result = null;
        String sql = "SELECT salt FROM users WHERE loginName = ? ; ";
        Connection conn = database.Web_MYSQL_Helper.getConnection();
        ResultSet rs;

        try {
            PreparedStatement stmt2 = conn.prepareStatement(sql);
            stmt2.setString(1, loginName.trim());

            rs = stmt2.executeQuery();

            if (rs.next()) {
                result = rs.getString("salt");
            }

            Web_MYSQL_Helper.closePreparedStatement(stmt2);
        } catch (SQLException ex) {
            WebErrorLogger.log(Level.SEVERE, "SQLException in getSaltByLoginName(String loginName) loginName="
                    +loginName+" error: " + ex);
            Web_MYSQL_Helper.returnConnection(conn);
            return null;
        }

        Web_MYSQL_Helper.returnConnection(conn);
        return result;
    }

    @Override
    public Collection<User> getSystemAdmins() {
        Collection<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE userRole = ?; ";
        Connection conn = database.Web_MYSQL_Helper.getConnection();
        ResultSet rs;
        User user = null;

        try {
            PreparedStatement stmt2 = conn.prepareStatement(sql);
            stmt2.setString(1, common.UserRole.SystemAdmin.getRoleName());
            rs = stmt2.executeQuery();
            if (rs == null) {
                Web_MYSQL_Helper.closePreparedStatement(stmt2);
                Web_MYSQL_Helper.returnConnection(conn);
                return users;
            }
            while (rs.next()) {
                user = SQLUtility.convertResultSetToUser(rs);
                users.add(user);
            }
            Web_MYSQL_Helper.closePreparedStatement(stmt2);
        } catch (SQLException ex) {
            WebErrorLogger.log(Level.SEVERE, "SQLException in getSystemAdmins() error: " + ex);
            Web_MYSQL_Helper.returnConnection(conn);
            return users;
        }

        Web_MYSQL_Helper.returnConnection(conn);

        return users;
    }

    public static void main(String[] args)
    {
        UserManager d = new UserManager();
        String name = "Turbidity";
        String desc = "A measure of how much sediment is suspended in the water.  Turbidity often increases after a rain events and is worsened when soil is allowed to erode into a waterway.";
        //d.addUser(name);
    }
}
    

    
    
