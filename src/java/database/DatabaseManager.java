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

    public void createDataValueTable()    
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Statement s = null;
        try(Connection conn = sql.getConnection();)
        {
            s = conn.createStatement();
            String createTable = "Create Table DataValues("
                    + "entryID number primary key AUTO_INCREMENT,"
                    + "name varchar,"
                    + "units varchar,"
                    + "sensor varchar"
                    + "time varchar"
                    + "value number"
                    + ");";
            s.executeQuery(createTable);
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
    
    public void createDataDescriptionTable()    
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Statement s = null;
        try(Connection conn = sql.getConnection();)
        {
            s = conn.createStatement();
            String createTable = "Create Table DataDescriptions("
                    + "name varchar primary key,"
                    + "description varchar"
                    + ");";
            s.executeQuery(createTable);
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
    
    public void createUserTable()
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Statement s = null;
        try(Connection conn = sql.getConnection();)
        {
            s = conn.createStatement();
            String createTable = "Create Table Users("
                    + "userNumber number primary key AUTO_INCREMENT,"
                    + "loginName varchar,"
                    + "password varchar,"
                    + "salt varchar"
                    + "lastName varchar"
                    + "firstName varchar"
                    + "emailAddress varchar"
                    + "userRole varchar"
                    + "lastLogin varchar"
                    + "lastAttemptedLogin varchar"
                    + "loginCount number"
                    + "attemptedLoginCount number"
                    + "locked boolean"
                    + ");";
            s.executeQuery(createTable);
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
    
    public void manualInput(String name, String units, LocalDateTime time, float value, User u)
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Connection conn = sql.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String insertSQL = "INSERT INTO WaterData values(?,?,?,?,?,?)";
            String sensor = u.getFirstName()+u.getLastName();
                
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
            System.out.println("Error processing request: Manual Data Insertion");
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
    
    public void manualDeletion(int entryID)
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Connection conn = sql.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String deleteSQL = "Delete from WaterData where entryID = ?";
                
            p = conn.prepareStatement(deleteSQL);
            p.setString(1, entryID+"");
            p.executeUpdate();
            conn.commit();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Manual Data Deletion");
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
    
    public void deleteUser(int userID)
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Connection conn = sql.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String deleteSQL = "Delete from Users where entryID = ?";
                
            p = conn.prepareStatement(deleteSQL);
            p.setString(1, userID+"");
            p.executeUpdate();
            conn.commit();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Manual User Deletion");
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
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        PreparedStatement p = null;
        ResultSet rs = null;
        try(Connection conn = sql.getConnection();)
        {
            String query = "Select * from WaterData Where name = ?"
                + " AND time >= ? AND time <= ? AND sensor = ?";
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
            while(!rs.isAfterLast())
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
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Connection conn = sql.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String insertSQL = "INSERT INTO WaterData values(?,?,?,?,?,?)";
                
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
    
    public void addNewUser(String username, String password, String firstName,
            String lastName, String email, UserRole userRole)
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Connection conn = sql.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String insertSQL = "INSERT INTO AdminList values(?,?,?,?,?,?,?,?,?,?,?,?)";
            String salt = "Brandon";
            password = SecurityCode.encryptSHA256(password + salt);
            
            p = conn.prepareStatement(insertSQL);
            p.setString(1, username);
            p.setString(2, password);
            p.setString(3, salt);
            p.setString(4, firstName);
            p.setString(5, lastName);
            p.setString(6, email);
            p.setString(7, userRole.getRoleName());
            p.setString(8, LocalDateTime.now() + "");//last login time
            p.setString(9, LocalDateTime.now() + "");//last attempted login time
            p.setString(10, 0+"");//login count
            p.setString(11, 0+"");//login attempted count
            p.setString(12, false+"");
            p.executeUpdate();
            conn.commit();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Add new user");
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
    
    public void lockUser(int userID)
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Connection conn = sql.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String modifySQL = "UPDATE Users"
                    + "SET locked = true"
                    + "WHERE userNumber = ?;";
          
            
            p = conn.prepareStatement(modifySQL);
            p.setString(1, userID+"");
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
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Connection conn = sql.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String insertSQL = "INSERT INTO WaterData JSON ?";
            
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
    
    public User getUserByLoginName(String username) 
    {
        User u = null;
        
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        PreparedStatement p = null;
        ResultSet rs = null;
        try(Connection conn = sql.getConnection();)
        {
            String getSQL = "SELECT * FROM Users WHERE loginName = ?;";
            p = conn.prepareStatement(getSQL);
            p.setString(1, username);
            rs = p.executeQuery();
            u = new User(
                rs.getInt("userNumber"),
                rs.getString("loginName"),
                rs.getString("userPassword"),
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
            System.out.println("Error retrieving user by login name");
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

    public User validateUser(String username, String password) 
    {
        User u = null;
        
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        PreparedStatement p = null;
        ResultSet rs = null;
        try(Connection conn = sql.getConnection();)
        {
            String getSQL = "SELECT * FROM Users WHERE loginName = ?, userPassword = ?;";
            p = conn.prepareStatement(getSQL);
            p.setString(1, username);
            p.setString(2, password);
            rs = p.executeQuery();
            u = new User(
                rs.getInt("userNumber"),
                rs.getString("loginName"),
                rs.getString("userPassword"),
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
            System.out.println("Error retrieving user by login name");
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

    public void updateUserLogin(User potentialUser) 
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Connection conn = sql.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String updateSQL = "UPDATE Users"
                    + "SET lastLoginTime = ?,"
                    + "lastAttemptedLoginTime = ?,"
                    + "loginCount = ?,"
                    + "attemptedLoginCount = ?"
                    + "WHERE loginName = ?;";
            p = conn.prepareStatement(updateSQL);
            p.setString(1, potentialUser.getLastLoginTime().toString());
            p.setString(2, potentialUser.getLastAttemptedLoginTime().toString());
            p.setString(3, potentialUser.getLoginCount()+"");
            p.setString(4, potentialUser.getAttemptedLoginCount()+"");
            p.setString(5, potentialUser.getLoginName());
            p.executeQuery();
            conn.commit();
        }
        catch(Exception e)
        {
            System.out.println("Error retrieving user by login name");
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
    
    public void updateDescription(String desc, String name)
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Connection conn = sql.getConnection();
        PreparedStatement p = null;
        try
        {
            conn.setAutoCommit(false);
            String updateSQL = "UPDATE DataDescriptions"
                    + "SET description = ?,"
                    + "WHERE name = ?;";
            p = conn.prepareStatement(updateSQL);
            p.setString(1, desc);
            p.setString(2, name);
            p.executeQuery();
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
    
    public String getDescription(String name)
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Connection conn = sql.getConnection();
        PreparedStatement p = null;
        ResultSet rs = null;
        String desc = null;
        try
        {
            String getSQL = "SELECT * FROM DataDescriptions WHERE name = ?";
            p = conn.prepareStatement(getSQL);
            p.setString(1, name);
            rs = p.executeQuery();
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
    
    public void insertDescription(String name, String desc)
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        Connection conn = sql.getConnection();
        PreparedStatement p = null;
        try
        {
            String insertSQL = "INSERT INTO DataDescriptions values(?, ?)";
            p = conn.prepareStatement(insertSQL);
            p.setString(1, name);
            p.setString(2, desc);
            p.executeQuery();
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

    public String getSaltByLoginName(String username) 
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        PreparedStatement p = null;
        ResultSet rs = null;
        String salt = null;
        try(Connection conn = sql.getConnection();)
        {
            String getSQL = "SELECT * FROM Users WHERE loginName = ?;";
            p = conn.prepareStatement(getSQL);
            p.setString(1, username);
            rs = p.executeQuery();
            salt = rs.getString("salt");
        }
        catch(SQLException e)
        {
            System.out.println("Error retrieving user by login name");
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
}
