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
import java.sql.Timestamp;
import java.util.ArrayList;
import security.SecurityCode;

/**
 *
 * @author Tyler Mutzek
 */
public class DatabaseManager {

    public void createDataValueTable()    
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        try(Connection conn = sql.getConnection();)
        {
            Statement s = conn.createStatement();
            String createTable = "Create Table DataValues("
                    + "entryID number primary key AUTO_INCREMENT,"
                    + "name varchar,"
                    + "units varchar,"
                    + "sensor varchar"
                    + "time Timestamp"
                    + "value number"
                    + ");";
            s.executeQuery(createTable);
            s.close();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Create Data Value Table");
        }
        
    }
    
    public void createUserTable()
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        try(Connection conn = sql.getConnection();)
        {
            Statement s = conn.createStatement();
            String createTable = "Create Table Users("
                    + "userNumber number primary key AUTO_INCREMENT,"
                    + "loginName varchar,"
                    + "password varchar,"
                    + "salt varchar"
                    + "lastName varchar"
                    + "firstName varchar"
                    + "emailAddress varchar"
                    + "userRole varchar"
                    + "lastLogin Timestamp"
                    + "lastAttemptedLogin Timestamp"
                    + "loginCount number"
                    + "attemptedLoginCount number"
                    + "locked boolean"
                    + ");";
            s.executeQuery(createTable);
            s.close();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Create Data Value Table");
        }
    }
    
    public void manualInput(String name, String units, Timestamp time, float value, User u)
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        try(Connection conn = sql.getConnection();)
        {
            String insertSQL = "INSERT INTO WaterData values(?,?,?,?,?,?)";
            String sensor = u.getFirstName()+u.getLastName();
                
            PreparedStatement p = conn.prepareStatement(insertSQL);
            p.setString(1, name);
            p.setString(2, units);
            p.setString(3, sensor);
            p.setString(4, time+"");
            p.setString(5, value+"");
            p.executeUpdate();
            p.close();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Manual Data Insertion");
        }
    }
    
    public void manualDeletion(int entryID)
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        try(Connection conn = sql.getConnection();)
        {
            String deleteSQL = "Delete from WaterData where entryID = ?";
                
            PreparedStatement p = conn.prepareStatement(deleteSQL);
            p.setString(1, entryID+"");
            p.executeUpdate();
            p.close();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Manual Data Deletion");
        }
    }
    
    public void deleteUser(int userID)
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        try(Connection conn = sql.getConnection();)
        {
            String deleteSQL = "Delete from Users where entryID = ?";
                
            PreparedStatement p = conn.prepareStatement(deleteSQL);
            p.setString(1, userID+"");
            p.executeUpdate();
            p.close();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Manual Data Deletion");
        }
    }
    
    public ArrayList<DataValue> getGraphData(String name, Timestamp lower, Timestamp upper, String sensor)
    {
        ArrayList<DataValue> graphData = new ArrayList<>();
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        try(Connection conn = sql.getConnection();)
        {
            String query = "Select * from WaterData Where name = ?"
                + " AND time >= ? AND time <= ? AND sensor = ?";
            PreparedStatement p = conn.prepareStatement(query);
            p.setString(1, name);
            p.setString(2, lower+"");
            p.setString(3, upper+"");
            p.setString(4, sensor);
            ResultSet rs = p.executeQuery();
                
            
            int entryID;
            String units;
            Timestamp time;
            float value;
            while(!rs.isAfterLast())
            {
                entryID = rs.getInt(1);
                name = rs.getString(2);
                units = rs.getString(3);
                sensor = rs.getString(4);
                time = rs.getTimestamp(5);
                value = rs.getFloat(6);
                DataValue dV = new DataValue(entryID,name,units,sensor,time,value);
                graphData.add(dV);
                    
                rs.next();
            }
            rs.close();
            p.close();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Retrieve Graph Data");
        }
        return graphData;
    }
    
    public void sensorDataInput(String name, String units, String sensor, Timestamp time, float value)
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        try(Connection conn = sql.getConnection();)
        {
            String insertSQL = "INSERT INTO WaterData values(?,?,?,?,?,?)";
                
            PreparedStatement p = conn.prepareStatement(insertSQL);
            p.setString(1, name);
            p.setString(2, units);
            p.setString(3, sensor);
            p.setString(4, time+"");
            p.setString(5, value+"");
            p.executeUpdate();
            p.close();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Sensor Data Insertion");
        }
    }
    
    public void addNewUser(String username, String password, String firstName,
            String lastName, String email, UserRole userRole, boolean locked)
    {
        Web_MYSQL_Helper sql = new Web_MYSQL_Helper();
        try(Connection conn = sql.getConnection();)
        {
            String insertSQL = "INSERT INTO AdminList values(?,?,?,?,?,?,?,?,?,?,?,?)";
            String salt = "Brandon";
            password = SecurityCode.encryptSHA256(password + salt);
            
            PreparedStatement p = conn.prepareStatement(insertSQL);
            p.setString(1, username);
            p.setString(2, password);
            p.setString(3, salt);
            p.setString(4, firstName);
            p.setString(5, lastName);
            p.setString(6, email);
            p.setString(7, userRole.getRoleName());
            p.setString(8, new Timestamp(1483246800000L).toString());//last login time
            p.setString(9, new Timestamp(1483246800000L).toString());//last attempted login time
            p.setString(10, 0+"");//login count
            p.setString(11, 0+"");//login attempted count
            p.setString(12, locked+"");
            p.executeUpdate();
            p.close();
        }
        catch (Exception ex)//SQLException ex 
        {
            System.out.println("Error processing request: Add new user");
        }
    }
}
