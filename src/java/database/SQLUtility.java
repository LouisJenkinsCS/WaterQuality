package database;

import common.User;
import common.UserRole;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import utilities.WebErrorLogger;

/**
 *
 * @author cjones
 */
public class SQLUtility {

    public static User convertResultSetToUser(ResultSet rs) {
        User user = new User();
        try {
            user.setUserNumber(rs.getInt("userNumber"));
            user.setUserPassword(rs.getString("userPassword"));
            user.setLoginName(rs.getString("loginName"));
            user.setEmailAddress(rs.getString("emailAddress"));
            user.setFirstName(rs.getString("firstName"));
            user.setLastName(rs.getString("lastName"));
            user.setUserRole(UserRole.getUserRole(rs.getString("userRole")));
            user.setLastLoginTime(LocalDateTime.now());
            if (rs.getString("LastAttemptedLoginTime") == null) {
                user.setLastAttemptedLoginTime(LocalDateTime.now());
            } else {
                user.setLastAttemptedLoginTime(LocalDateTime.parse(rs.getString("LastAttemptedLoginTime")));
            }
            int loginCount = rs.getInt("loginCount");
            user.setLoginCount(loginCount);
            user.setAttemptedLoginCount(rs.getInt("attemptedLoginCount"));
            user.setLocked(rs.getBoolean("locked"));
            user.setSalt(rs.getString("salt"));

        } catch (SQLException ex) {
            WebErrorLogger.log(Level.SEVERE, "SQLException in convertResultSetToUser error " + ex, ex);
            return null;
        }
        return user;
    }
}
