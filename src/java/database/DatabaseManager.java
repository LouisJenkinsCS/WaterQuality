/*
 * Includes various database managing 
 */
package database;

import common.DataValue;
import java.io.IOException;
import common.User;
import common.UserRole;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.json.simple.JSONObject;
import security.SecurityCode;

/**
 *
 * @author Tyler Mutzek
 */
public class DatabaseManager 
{
    
    /*
        Creates the data value table
        *Tested*
    */
    public void createDataValueTable()    
    {
        Statement s = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            s = conn.createStatement();
            String createTable = "Create Table IF NOT EXISTS DataValues("
                    + "entryID INT primary key AUTO_INCREMENT,"
                    + "dataName varchar(40),"
                    + "units varchar(10),"
                    + "sensor varchar(20),"
                    + "timeRecorded varchar(25),"
                    + "dataValue FLOAT(3)"
                    + ");";
            s.execute(createTable);
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Create Data Value Table");
        }
        finally
        {
            try
            {
                if(s != null)
                    s.close();
            }
            catch(SQLException e){System.out.println("Error closing statement");}
        }
    }
    
    /*
        Creates the data description table
        *Tested*
    */
    public void createDataDescriptionTable()    
    {
        Statement s = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            s = conn.createStatement();
            String createTable = "Create Table IF NOT EXISTS DataDescriptions("
                    + "dataName varchar(40) primary key,"
                    + "description varchar(500)"
                    + ");";
            s.executeUpdate(createTable);
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Create Data Description Table");
        }
        finally
        {
            try
            {
                if(s != null)
                    s.close();
            }
            catch(SQLException e){System.out.println("Error closing statement");}
        }
    }
    
    /*
        Creates the user table
        *Tested*
    */
    public void createUserTable()
    {
        Statement s = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            s = conn.createStatement();
            String createTable = "Create Table IF NOT EXISTS users("
                    + "userNumber INT primary key AUTO_INCREMENT,"
                    + "loginName varchar(15),"
                    + "password varchar(64),"
                    + "firstName varchar(15),"
                    + "lastName varchar(15),"
                    + "emailAddress varchar(50),"
                    + "userRole varchar(20),"
                    + "lastLoginTime varchar(25),"
                    + "loginCount INT,"
                    + "salt varchar(30),"
                    + "LastAttemptedLoginTime varchar(25),"
                    + "locked boolean,"
                    + "AttemptedLoginCount INT"
                    + ");";
            s.executeUpdate(createTable);
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Create Users Table");
        }
        finally
        {
            try
            {
                if(s != null)
                    s.close();
            }
            catch(SQLException e){System.out.println("Error closing statement");}
        }
    }
    
    /*
        Allows an admin to insert data into the data values table
        *Tested*
    */
    public void manualInput(String name, String units, LocalDateTime time, float value, User u)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String insertSQL = "INSERT INTO DataValues (dataName,units,sensor,timeRecorded,dataValue) "
                    + "values(?,?,?,?,?)";
            String sensor = u.getFirstName()+u.getLastName();
            if(sensor.length() > 20)
                sensor = sensor.substring(0, 20);
            p = conn.prepareStatement(insertSQL);
            p.setString(1, name);
            p.setString(2, units);
            p.setString(3, sensor);
            p.setString(4, time+"");
            p.setFloat(5, value);
            p.executeUpdate();
            conn.commit();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Manual Data Insertion\n" + ex);
            if(conn!=null)
            {
                try
                {
                    System.out.println("Transaction is being rolled back");
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    System.out.println("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                System.out.println("Error closing statement or connection");
            }
        }
    }
    
    /*
        Allows an admin to delete data from the data values table
        *Tested*
    */
    public void manualDeletion(int entryID, User u)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement p = null;
        try
        {
            if(u.getUserRole() != common.UserRole.SystemAdmin)
                throw new Exception("Attempted Data Deletion by Non-Admin");
            conn.setAutoCommit(false);
            String deleteSQL = "Delete from DataValues where entryID = ?";
                
            p = conn.prepareStatement(deleteSQL);
            p.setInt(1, entryID);
            p.executeUpdate();
            conn.commit();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Manual Data Deletion\n" + ex);
            if(conn!=null)
            {
                try
                {
                    System.out.println("Transaction is being rolled back");
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    System.out.println("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                System.out.println("Error closing statement or connection");
            }
        }
    }
    
    /*
        @param userID the user number of the user being deleted
        @param u the user who is doing the deletion
        Deletes user with parameter user number
        *Tested*
    */
    public void deleteUser(int userID, User u)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement p = null;
        try
        {
            if(u.getUserRole() != common.UserRole.SystemAdmin)
                throw new Exception("Attempted User Deletion by Non-Admin");
            if(userID == u.getUserNumber())
                throw new Exception("User Attempting to delete self");
            conn.setAutoCommit(false);
            String deleteSQL = "Delete from users where userNumber = ?";
                
            p = conn.prepareStatement(deleteSQL);
            p.setInt(1, userID);
            p.executeUpdate();
            conn.commit();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Manual User Deletion\n" + ex);
            if(conn!=null)
            {
                try
                {
                    System.out.println("Transaction is being rolled back");
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    System.out.println("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                System.out.println("Error closing statement or connection");
            }
        }
    }
    
    public ArrayList<DataValue> getGraphData(String name, LocalDateTime lower, LocalDateTime upper, String sensor)
    {
        ArrayList<DataValue> graphData = new ArrayList<>();
        PreparedStatement p = null;
        ResultSet rs = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String query = "Select * from DataValues Where dataName = ?"
                + " AND timeRecorded >= ? AND timeRecorded <= ? AND sensor = ?";
            p = conn.prepareStatement(query);
            p.setString(1, name);
            p.setString(2, lower+"");
            p.setString(3, upper+"");
            p.setString(4, sensor);
            rs = p.executeQuery();
                
            
            int entryID;
            String units;
            LocalDateTime time;
            float value;
            while(rs.next())
            {
                entryID = rs.getInt(1);
                name = rs.getString(2);
                units = rs.getString(3);
                sensor = rs.getString(4);
                time = LocalDateTime.parse(rs.getString(5));
                value = rs.getFloat(6);
                DataValue dV = new DataValue(entryID,name,units,sensor,time,value);
                graphData.add(dV);
                    
                rs.next();
            }
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Retrieve Graph Data");
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(rs != null)
                    rs.close();
            }
            catch(SQLException excep)
            {System.out.println("Error closing statement or result set");}
        }
        return graphData;
    }
    
    public void sensorDataInput(String name, String units, String sensor, LocalDateTime time, float value)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String insertSQL = "INSERT INTO DataValues values(?,?,?,?,?,?)";
                
            p = conn.prepareStatement(insertSQL);
            p.setString(1, name);
            p.setString(2, units);
            p.setString(3, sensor);
            p.setString(4, time+"");
            p.setString(5, value+"");
            p.executeUpdate();
            conn.commit();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Sensor Data Insertion");
            if(conn!=null)
            {
                try
                {
                    System.out.println("Transaction is being rolled back");
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    System.out.println("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                System.out.println("Error closing statement or connection");
            }
        }
    }
    
    /*
        Adds a new user to the user table
        *Tested*
    */
    public void addNewUser(String username, String password, String firstName,
            String lastName, String email, UserRole userRole)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String insertSQL = "INSERT INTO users (loginName,password,firstName,lastName,"
                    + "emailAddress,userRole,lastLoginTime,loginCount,salt,"
                    + "LastAttemptedLoginTime,locked,AttemptedLoginCount)"
                    + " values(?,?,?,?,?,?,?,?,?,?,?,?)";
            String salt = "Brandon";
            password = SecurityCode.encryptSHA256(password + salt);
            
            p = conn.prepareStatement(insertSQL);
            p.setString(1, username);
            p.setString(2, password);
            p.setString(3, firstName);
            p.setString(4, lastName);
            p.setString(5, email);
            p.setString(6, userRole.getRoleName());
            p.setString(7, LocalDateTime.now() + "");//last login time
            p.setInt(8, 0);//login count
            p.setString(9, salt);
            p.setString(10, LocalDateTime.now() + "");//last attempted login time
            p.setBoolean(11, false);
            p.setInt(12, 0);//login attempted count
            p.executeUpdate();
            conn.commit();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Add new user\n" + ex);
            if(conn!=null)
            {
                try
                {
                    System.out.println("Transaction is being rolled back");
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    System.out.println("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                System.out.println("Error closing statement or connection");
            }
        }
    }
    
    /*
        locks the user with the parameter userID
        *Tested*
    */
    public void lockUser(int userID)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String modifySQL = "UPDATE users "
                    + "SET locked = 1 "
                    + "WHERE userNumber = ?;";
          
            
            p = conn.prepareStatement(modifySQL);
            p.setInt(1, userID);
            p.executeUpdate();
            conn.commit();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Lock User #" + userID);
            if(conn!=null)
            {
                try
                {
                    System.out.println("Transaction is being rolled back");
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    System.out.println("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                System.out.println("Error closing statement or connection");
            }
        }
    }
    
    /*
        Unlocks the user with the parameter userID
    */
    public void unlockUser(int userID)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String modifySQL = "UPDATE users "
                    + "SET locked = 0 "
                    + "WHERE userNumber = ?;";
          
            
            p = conn.prepareStatement(modifySQL);
            p.setInt(1, userID);
            p.executeUpdate();
            conn.commit();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Lock User #" + userID);
            if(conn!=null)
            {
                try
                {
                    System.out.println("Transaction is being rolled back");
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    System.out.println("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                System.out.println("Error closing statement or connection");
            }
        }
    }
    
    public void insertJSON(JSONObject j)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String insertSQL = "INSERT INTO DataValues JSON ?";
            
            p = conn.prepareStatement(insertSQL);
            p.setString(1, j.toJSONString());
            p.executeUpdate();
            conn.commit();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: JSON Insertion");
            if(conn!=null)
            {
                try
                {
                    System.out.println("Transaction is being rolled back");
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    System.out.println("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                System.out.println("Error closing statement or connection");
            }
        }
    }
    
    /*
        Gets the user with the parameter login name
        *Tested*
    */
    public User getUserByLoginName(String username) 
    {
        User u = null;
        
        PreparedStatement p = null;
        ResultSet rs = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String getSQL = "SELECT * FROM users WHERE loginName = ?;";
            p = conn.prepareStatement(getSQL);
            p.setString(1, username);
            rs = p.executeQuery();
            rs.next();
            u = new User(
                rs.getInt("userNumber"),
                rs.getString("loginName"),
                rs.getString("password"),
                rs.getString("salt"),
                rs.getString("lastName"),
                rs.getString("firstName"),
                rs.getString("emailAddress"),
                UserRole.getUserRole(rs.getString("userRole")),
                LocalDateTime.parse(rs.getString("lastLoginTime")),
                LocalDateTime.parse(rs.getString("lastAttemptedLoginTime")),
                rs.getInt("loginCount"),
                rs.getInt("attemptedLoginCount"),
                rs.getBoolean("locked")
                );
        }
        catch(Exception e)
        {
            System.out.println("Error retrieving user by login name\n" + e);
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close(); 
                if(rs != null)
                    rs.close();
            }
            catch(SQLException excep)
            {System.out.println("Error closing statement or result set");}
        }
        
        return u;
    }

    /*
        Returns the user info if the username and password are correct
        *Tested*
    */
    public User validateUser(String username, String password) 
    {
        User u = null;
        
        PreparedStatement p = null;
        ResultSet rs = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            password = security.SecurityCode.encryptSHA256(password+getSaltByLoginName(username));
            String getSQL = "SELECT * FROM users WHERE loginName = ? and password = ?;";
            p = conn.prepareStatement(getSQL);
            p.setString(1, username);
            p.setString(2, password);
            rs = p.executeQuery();
            rs.next();
            u = new User(
                rs.getInt("userNumber"),
                rs.getString("loginName"),
                rs.getString("password"),
                rs.getString("salt"),
                rs.getString("lastName"),
                rs.getString("firstName"),
                rs.getString("emailAddress"),
                UserRole.getUserRole(rs.getString("userRole")),
                LocalDateTime.parse(rs.getString("lastLoginTime")),
                LocalDateTime.parse(rs.getString("lastAttemptedLoginTime")),
                rs.getInt("loginCount"),
                rs.getInt("attemptedLoginCount"),
                rs.getBoolean("locked")
                );
        }
        catch(Exception e)
        {
            System.out.println("Error validating user: " + username + "\n" + e);
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(rs != null)
                    rs.close();
            }
            catch(SQLException excep)
            {System.out.println("Error closing statement or result set");}
        }
        
        return u;
    }

    /*
        Updates the user's loginCount, attemptedLoginCount, lastLoginTime
        and lastAttemptedLoginTime
        *Tested*
    */
    public void updateUserLogin(User potentialUser) 
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String updateSQL = "UPDATE users "
                    + "SET lastLoginTime = ?,"
                    + "lastAttemptedLoginTime = ?,"
                    + "loginCount = ?,"
                    + "attemptedLoginCount = ? "
                    + "WHERE userNumber = ?;";
            p = conn.prepareStatement(updateSQL);
            p.setString(1, potentialUser.getLastLoginTime().toString());
            p.setString(2, potentialUser.getLastAttemptedLoginTime().toString());
            p.setInt(3, potentialUser.getLoginCount());
            p.setInt(4, potentialUser.getAttemptedLoginCount());
            p.setInt(5, potentialUser.getUserNumber());
            p.executeUpdate();
            conn.commit();
        }
        catch(Exception e)
        {
            System.out.println("Error updating user login\n" + e);
            if(conn != null)
                try
                {
                    System.out.println("Transaction is being rolled back");
                    conn.rollback();
                }
            catch(SQLException excep)
            {System.out.println("Rollback unsuccessful");}
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(conn != null)
                    conn.close();
            }
            catch(Exception excep)
            {
                System.out.println("Error closing statement or connection");
            }
        }
    }
    
    /*
        Updates the description with dataName 'name' using the description 'desc'
        *Tested*
    */
    public void updateDescription(String desc, String name)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String updateSQL = "UPDATE DataDescriptions "
                    + "SET description = ? "
                    + "WHERE dataName = ?;";
            p = conn.prepareStatement(updateSQL);
            p.setString(1, desc);
            p.setString(2, name);
            p.executeUpdate();
            conn.commit();
        }
        catch(Exception e)
        {
            System.out.println("Error updating description for " + name);
            if(conn != null)
                try
                {
                    System.out.println("Transaction is being rolled back");
                    conn.rollback();
                }
            catch(SQLException excep)
            {System.out.println("Rollback unsuccessful");}
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(conn != null)
                    conn.close();
            }
            catch(Exception excep)
            {
                System.out.println("Error closing statement or connection");
            }
        }
    }
    
    /*
        Retrieves the description for the parameter data name
        *Tested*
    */
    public String getDescription(String name)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement p = null;
        ResultSet rs = null;
        String desc = null;
        try
        {
            String getSQL = "SELECT * FROM DataDescriptions WHERE dataName = ?";
            p = conn.prepareStatement(getSQL);
            p.setString(1, name);
            rs = p.executeQuery();
            rs.next();
            desc = rs.getString("description");
        }
        catch(Exception e)
        {
            System.out.println("Error retrieving description for " + name);
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(rs != null)
                    rs.close();
                if(conn != null)
                    conn.close();
            }
            catch(Exception excep)
            {
                System.out.println("Error closing statement or connection");
            }
        }
        return desc;
    }
    
    /*
        Inserts a description into the DataDescriptions table
        *Tested*
    */
    public void insertDescription(String name, String desc)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement p = null;
        try
        {
            String insertSQL = "INSERT INTO DataDescriptions values(?, ?)";
            p = conn.prepareStatement(insertSQL);
            p.setString(1, name);
            p.setString(2, desc);
            p.executeUpdate();
        }
        catch(Exception e)
        {
            System.out.println("Error inserting description for " + name);
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(conn != null)
                    conn.close();
            }
            catch(Exception excep)
            {
                System.out.println("Error closing statement or connection");
            }
        }
    }

    /*
        Retrieves the salt of the user with the parameter login name
        *Tested*
    */
    public String getSaltByLoginName(String loginName) 
    {
        PreparedStatement p = null;
        ResultSet rs = null;
        String salt = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String getSQL = "SELECT * FROM users WHERE loginName = ?;";
            p = conn.prepareStatement(getSQL);
            p.setString(1, loginName);
            rs = p.executeQuery();
            rs.next();
            salt = rs.getString("salt");
        }
        catch(SQLException e)
        {
            System.out.println("Error retrieving salt by login name" + e);
        }
        finally
        {
            try
            {
                if(p != null)
                    p.close();
                if(rs != null)
                    rs.close();
            }
            catch(Exception excep)
            {System.out.println("Error closing statement or result set");}
        }
        
        return salt;
    }
    
    public static void main(String[] args)
    {
        DatabaseManager d = new DatabaseManager();
    }
}
