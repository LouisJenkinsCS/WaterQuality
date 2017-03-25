/*
 * Includes various database managing 
 */
package database;

import async.DataParameter;
import async.DataReceiver;
import common.DataValue;
import common.ErrorMessage;
import common.ManualDataValue;
import common.User;
import common.UserRole;
import io.reactivex.schedulers.Schedulers;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import security.SecurityCode;

/**
 *
 * @author Tyler Mutzek
 */
public class DatabaseManager 
{
    static {
        createDataValueTable();
        createManualDataValueTable();
        DatabaseManager.createDataValueTable();
        DatabaseManager.createErrorLogsTable();
        DatabaseManager.createManualDataNamesTable();
        DatabaseManager.createUserTable();
        DataReceiver.getParameters().map(DataParameter::getName).subscribeOn(Schedulers.io()).blockingSubscribe(DatabaseManager::insertDataName);
    }
    /*
        Creates the data value table
        entryID is the unique id number of the data value
        dataName is the name of the data type (e.g. Temperature)
        units is the units associated with the data value
        sensor is the name of the sensor that recorded the data value
        timeRecorded is a the time the data was recorded
        dataValue is the value of the of data recorded
        delta is the difference between this data value and the last
    */
    public static void createDataValueTable()    
    {
        Statement createTable = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            createTable = conn.createStatement();
            String createSQL = "Create Table IF NOT EXISTS DataValues("
                    + "entryID INT primary key AUTO_INCREMENT,"
                    + "dataName varchar(40),"
                    + "units varchar(10),"
                    + "sensor varchar(20),"
                    + "timeRecorded varchar(25),"
                    + "dataValue FLOAT(3),"
                    + "delta FLOAT(8)"
                    + ");";
            createTable.execute(createSQL);
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error creating Data Value Table: " + ex);
        }
        finally
        {
            try
            {
                if(createTable != null)
                    createTable.close();
            }
            catch(SQLException e)
            {
                LogError("Error closing statement:" + e);
            }
        }
    }
    
    /*
        Creates the manual data value table
        entryID is the unique id number of the data value
        dataName is the name of the data type (e.g. Temperature)
        units is the units associated with the data value
        submittedBy is the name of the user that recorded the data value
        timeRecorded is a the time the data was recorded
        dataValue is the value of the of data recorded
    */
    public static void createManualDataValueTable()    
    {
        Statement createTable = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            createTable = conn.createStatement();
            String createSQL = "Create Table IF NOT EXISTS ManualDataValues("
                    + "entryID INT primary key AUTO_INCREMENT,"
                    + "dataName varchar(40),"
                    + "units varchar(10),"
                    + "submittedBy varchar(20),"
                    + "timeRecorded varchar(25),"
                    + "dataValue FLOAT(3)"
                    + ");";
            createTable.execute(createSQL);
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error creating Manual Data Value Table: " + ex);
        }
        finally
        {
            try
            {
                if(createTable != null)
                    createTable.close();
            }
            catch(SQLException e)
            {
                LogError("Error closing statement:" + e);
            }
        }
    }
    
    /*
        Creates the data description table
        dataName is the data type of the data value (e.g. Temperature)
        description is the description of this data type
    */
    public static void createDataDescriptionTable()    
    {
        Statement createTable = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            createTable = conn.createStatement();
            String createSQL = "Create Table IF NOT EXISTS DataDescriptions("
                    + "dataName varchar(40) primary key,"
                    + "description varchar(500)"
                    + ");";
            createTable.executeUpdate(createSQL);
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error creating Data Description Table: " + ex);
        }
        finally
        {
            try
            {
                if(createTable != null)
                    createTable.close();
            }
            catch(SQLException e)
            {
                LogError("Error closing statement: " + e);
            }
        }
    }
    
    /*
        Creates the user table
        userNumber is the unique id number for the user
        password is encrypted with SHA256
        locked is whether this user is locked or not
        AttemptedLoginCount is the number of recent failed logins
        The rest are self explanitory
    */
    public static void createUserTable()
    {
        Statement createTable = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            createTable = conn.createStatement();
            String createSQL = "Create Table IF NOT EXISTS users("
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
            createTable.executeUpdate(createSQL);
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error creating Users Table: " + ex);
        }
        finally
        {
            try
            {
                if(createTable != null)
                    createTable.close();
            }
            catch(SQLException e)
            {
                LogError("Error closing statement: " + e);
            }
        }
    }
    
    /*
        Creates a table to log the errors that may occur
        Time occured is the time the error happened
        Error is the error message
    */
    public static void createErrorLogsTable()    
    {
        Statement createTable = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            createTable = conn.createStatement();
            String createSQL = "Create Table IF NOT EXISTS ErrorLogs("
                    + "timeOccured varchar(25) primary key,"
                    + "error varchar(300)"
                    + ");";
            createTable.execute(createSQL);
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error creating Error Logs Table: " + ex);
        }
        finally
        {
            try
            {
                if(createTable != null)
                    createTable.close();
            }
            catch(SQLException e)
            {
                LogError("Error closing statement: " + e);
            }
        }
    }
    
    /*
        A table consisting only of the unique data names of all data
    
        This table is important for making the code modular as instead of hard
        coding a list of buttons for displaying a graph for each data type,
        a list of all data types can be obtained from this table and used to 
        produce said buttons, allowing for new data types pulled from netronix
        to automatically be accounted for everywhere it would be desirable to have
        access to them.
    */
    public static void createDataNamesTable()    
    {
        Statement createTable = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            createTable = conn.createStatement();
            String createSQL = "Create Table IF NOT EXISTS DataNames("
                    + "dataName varchar(40) primary key"
                    + ");";
            createTable.execute(createSQL);
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error creating DataNames Table: " + ex);
        }
        finally
        {
            try
            {
                if(createTable != null)
                    createTable.close();
            }
            catch(SQLException e)
            {
                LogError("Error closing statement: " + e);
            }
        }
    }
    
    /*
        Same purpose as DataNames table except for manual data
    */
    public static void createManualDataNamesTable()    
    {
        Statement createTable = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            createTable = conn.createStatement();
            String createSQL = "Create Table IF NOT EXISTS ManualDataNames("
                    + "dataName varchar(100) primary key"
                    + ");";
            createTable.execute(createSQL);
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error creating ManualDataNames Table: " + ex);
        }
        finally
        {
            try
            {
                if(createTable != null)
                    createTable.close();
            }
            catch(SQLException e)
            {
                LogError("Error closing statement: " + e);
            }
        }
    }
    
    /*
        Allows an admin to insert data into the data values table
        @param name the name of the data type
        @param units the units of this data type
        @param time the time this piece of data was retrieved
        @param value the value of this piece of data
        @param delta the difference between this data value and the last
        @param u the user who entered this data value
        @return whether this function was successful or not
    */
    //If this is static, admin_insertion.js can't use it...
    public static boolean manualInput(String name, String units, LocalDateTime time, float value, int id, User u)
    {
        boolean status;
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement insertData = null;
        try
        {
            conn.setAutoCommit(false);
            String insertSQL = "INSERT INTO ManualDataValues (dataName,units,submittedBy,timeRecorded,dataValue) "
                    + "values(?,?,?,?,?)";
            String submittedBy = u.getFirstName()+u.getLastName();
            if(submittedBy.length() > 20)
                submittedBy = submittedBy.substring(0, 20);
            
            insertData = conn.prepareStatement(insertSQL);
            insertData.setString(1, name);
            insertData.setString(2, units);
            insertData.setString(3, submittedBy);
            insertData.setString(4, time+"");
            insertData.setFloat(5, value);
            insertData.executeUpdate();
            conn.commit();
            status = true;
        }
        catch (Exception ex)//SQLException ex 
        {
            status = false;
            LogError("Error Manualing Inserting Data: " + ex);
            if(conn!=null)
            {
                try
                {
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    LogError("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(insertData != null)
                    insertData.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
        return status;
    }
    
    /*
        Allows an admin to delete data from the data values table
        @param entryID the id of the data to be deleted
        @param u the user doing the deletion
        @return whether this function was successful or not
    */
    public static boolean manualDeletion(int entryID, User u)
    {
        boolean status;
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement deleteData = null;
        try
        {
            //throws an error if a user without proper roles somehow invokes this function
            if(u.getUserRole() != common.UserRole.SystemAdmin)
                throw new Exception("Attempted Data Deletion by Non-Admin");
            conn.setAutoCommit(false);
            String deleteSQL = "Delete from DataValues where entryID = ?";
                
            deleteData = conn.prepareStatement(deleteSQL);
            deleteData.setInt(1, entryID);
            deleteData.executeUpdate();
            conn.commit();
            status = true;
        }
        catch (Exception ex)//SQLException ex 
        {
            status = false;
            LogError("Error Manualing Deleting Data: " + ex);
            if(conn!=null)
            {
                try
                {
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    LogError("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(deleteData != null)
                    deleteData.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
        return status;
    }
    
    /*
        Allows an admin to delete data from the manual data values table
        @param entryID the id of the data to be deleted
        @param u the user doing the deletion
        @return whether this function was successful or not
    */
    public static boolean manualDeletionM(int entryID, User u)
    {
        boolean status;
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement deleteData = null;
        try
        {
            //throws an error if a user without proper roles somehow invokes this function
            if(u.getUserRole() != common.UserRole.SystemAdmin)
                throw new Exception("Attempted Data Deletion by Non-Admin");
            conn.setAutoCommit(false);
            String deleteSQL = "Delete from ManualDataValues where entryID = ?";
                
            deleteData = conn.prepareStatement(deleteSQL);
            deleteData.setInt(1, entryID);
            deleteData.executeUpdate();
            conn.commit();
            status = true;
        }
        catch (Exception ex)//SQLException ex 
        {
            status = false;
            LogError("Error Manualing Deleting Data-M: " + ex);//-m disguishes this error for this function, not the datavalue function
            if(conn!=null)
            {
                try
                {
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    LogError("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(deleteData != null)
                    deleteData.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
        return status;
    }
    
    /*
        Deletes user with parameter user number
        @param userID the user number of the user being deleted
        @param u the user who is doing the deletion
        @return whether this function was successful or not
    */
    public static boolean deleteUser(int userID, User u)
    {
        boolean status;
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement deleteUser = null;
        try
        {
            //throws an error if a user without proper roles somehow invokes this function
            if(u.getUserRole() != common.UserRole.SystemAdmin)
                throw new Exception("Attempted User Deletion by Non-Admin");
            if(userID == u.getUserNumber())
                throw new Exception("User Attempting to delete self");
            conn.setAutoCommit(false);
            String deleteSQL = "Delete from users where userNumber = ?";
                
            deleteUser = conn.prepareStatement(deleteSQL);
            deleteUser.setInt(1, userID);
            deleteUser.executeUpdate();
            conn.commit();
            status = true;
        }
        catch (Exception ex)//SQLException ex 
        {
            status = false;
            LogError("Error Manualing Deleting User: " + ex);
            if(conn!=null)
            {
                try
                {
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    LogError("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(deleteUser != null)
                    deleteUser.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
        return status;
    }
    
    /*
        Returns a list of data within a certain time range
        @param name the name of the data type for which data is being requested
        @param lower the lower range of the time
        @param upper the upper range of the time
    */
    public static ArrayList<DataValue> getGraphData(String name, LocalDateTime lower, LocalDateTime upper)
    {
        ArrayList<DataValue> graphData = new ArrayList<>();
        PreparedStatement selectData = null;
        ResultSet dataRange = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String query = "Select * from DataValues Where dataName = ?"
                + " AND timeRecorded >= ? AND timeRecorded <= ?;";
            selectData = conn.prepareStatement(query);
            selectData.setString(1, name);
            selectData.setString(2, lower+"");
            selectData.setString(3, upper+"");
            dataRange = selectData.executeQuery();
            
            int entryID;
            String units;
            LocalDateTime time;
            float value;
            float delta;
            String sensor;
            while(dataRange.next())
            {
                entryID = dataRange.getInt(1);
                name = dataRange.getString(2);
                units = dataRange.getString(3);
                sensor = dataRange.getString(4);
                time = LocalDateTime.parse(dataRange.getString(5));
                value = dataRange.getFloat(6);
                delta = dataRange.getFloat(7);
                DataValue dV = new DataValue(entryID,name,units,sensor,time,value,delta);
                graphData.add(dV);
            }
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error Retrieving Graph Data: " + ex);
        }
        finally
        {
            try
            {
                if(selectData != null)
                    selectData.close();
                if(dataRange != null)
                    dataRange.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        return graphData;
    }
    
    /*
        Returns a list of all data
        @param name the name of the data type for which data is being requested
    */
    public static ArrayList<DataValue> getAllGraphData(String name)
    {
        ArrayList<DataValue> graphData = new ArrayList<>();
        PreparedStatement selectData = null;
        ResultSet dataRange = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String query = "Select * from DataValues Where dataName = ?;";
            selectData = conn.prepareStatement(query);
            selectData.setString(1, name);
            dataRange = selectData.executeQuery();
            
            int entryID;
            String units;
            LocalDateTime time;
            float value;
            float delta;
            String sensor;
            while(dataRange.next())
            {
                entryID = dataRange.getInt(1);
                name = dataRange.getString(2);
                units = dataRange.getString(3);
                sensor = dataRange.getString(4);
                time = LocalDateTime.parse(dataRange.getString(5));
                value = dataRange.getFloat(6);
                delta = dataRange.getFloat(7);
                DataValue dV = new DataValue(entryID,name,units,sensor,time,value,delta);
                graphData.add(dV);
            }
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error Retrieving Graph Data: " + ex);
        }
        finally
        {
            try
            {
                if(selectData != null)
                    selectData.close();
                if(dataRange != null)
                    dataRange.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        return graphData;
    }
    
    /*
        Returns a list of data within a certain time range
        @param name the name of the data type for which data is being requested
        @param lower the lower range of the time
        @param upper the upper range of the time
    */
    public static ArrayList<ManualDataValue> getManualData(String name, LocalDateTime lower, LocalDateTime upper)
    {
        ArrayList<ManualDataValue> graphData = new ArrayList<>();
        PreparedStatement selectData = null;
        ResultSet dataRange = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String query = "Select * from ManualDataValues Where dataName = ?"
                + " AND timeRecorded >= ? AND timeRecorded <= ?;";
            selectData = conn.prepareStatement(query);
            selectData.setString(1, name);
            selectData.setString(2, lower+"");
            selectData.setString(3, upper+"");
            dataRange = selectData.executeQuery();
            
            int entryID;
            String units;
            LocalDateTime time;
            float value;
            String submittedBy;
            while(dataRange.next())
            {
                entryID = dataRange.getInt(1);
                name = dataRange.getString(2);
                units = dataRange.getString(3);
                submittedBy = dataRange.getString(4);
                time = LocalDateTime.parse(dataRange.getString(5));
                value = dataRange.getFloat(6);
                ManualDataValue dV = new ManualDataValue(entryID,name,units,submittedBy,time,value);
                graphData.add(dV);
            }
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error Retrieving Graph Data: " + ex);
        }
        finally
        {
            try
            {
                if(selectData != null)
                    selectData.close();
                if(dataRange != null)
                    dataRange.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        return graphData;
    }
    
    /*
        Returns a list of all manual data
        @param name the name of the data type for which data is being requested
    */
    public static ArrayList<ManualDataValue> getAllManualData(String name)
    {
        ArrayList<ManualDataValue> graphData = new ArrayList<>();
        PreparedStatement selectData = null;
        ResultSet dataRange = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String query = "Select * from ManualDataValues Where dataName = ?;";
            selectData = conn.prepareStatement(query);
            selectData.setString(1, name);
            dataRange = selectData.executeQuery();
            
            int entryID;
            String units;
            LocalDateTime time;
            float value;
            String submittedBy;
            while(dataRange.next())
            {
                entryID = dataRange.getInt(1);
                name = dataRange.getString(2);
                units = dataRange.getString(3);
                submittedBy = dataRange.getString(4);
                time = LocalDateTime.parse(dataRange.getString(5));
                value = dataRange.getFloat(6);
                ManualDataValue dV = new ManualDataValue(entryID,name,units,submittedBy,time,value);
                graphData.add(dV);
            }
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error Retrieving Graph Data: " + ex);
        }
        finally
        {
            try
            {
                if(selectData != null)
                    selectData.close();
                if(dataRange != null)
                    dataRange.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        return graphData;
    }
    
    /*
        Adds a new user to the user table
        Encrypts the password via SHA256 encryption with salt before storing
        Last login and attempted login are initiallized to now
        @return whether this function was successful or not
    */
    public static boolean addNewUser(String username, String password, String firstName,
            String lastName, String email, UserRole userRole, User u)
    {
        boolean status;
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement insertUser = null;
        try
        {
            //throws an error if a user without proper roles somehow invokes this function
            if(u.getUserRole() != common.UserRole.SystemAdmin)
                throw new Exception("Attempted Data Deletion by Non-Admin");
            
            conn.setAutoCommit(false);
            String insertSQL = "INSERT INTO users (loginName,password,firstName,lastName,"
                    + "emailAddress,userRole,lastLoginTime,loginCount,salt,"
                    + "LastAttemptedLoginTime,locked,AttemptedLoginCount)"
                    + " values(?,?,?,?,?,?,?,?,?,?,?,?)";
            String salt = "Brandon";
            password = SecurityCode.encryptSHA256(password + salt);
            
            insertUser = conn.prepareStatement(insertSQL);
            insertUser.setString(1, username);
            insertUser.setString(2, password);
            insertUser.setString(3, firstName);
            insertUser.setString(4, lastName);
            insertUser.setString(5, email);
            insertUser.setString(6, userRole.getRoleName());
            insertUser.setString(7, LocalDateTime.now() + "");//last login time
            insertUser.setInt(8, 0);//login count
            insertUser.setString(9, salt);
            insertUser.setString(10, LocalDateTime.now() + "");//last attempted login time
            insertUser.setBoolean(11, false);
            insertUser.setInt(12, 0);//login attempted count
            insertUser.executeUpdate();
            conn.commit();
            status = true;
        }
        catch (Exception ex)//SQLException ex 
        {
            status = false;
            LogError("Error Adding New User: " + ex);
            if(conn!=null)
            {
                try
                {
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    LogError("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(insertUser != null)
                    insertUser.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
        return status;
    }
    
    /*
        locks the user with the parameter userID
        @param userID the ID of the user being locked
        @param u the user doing the locking
        @return whether this function was successful or not
    */
    public static boolean lockUser(int userID, User u)
    {
        boolean status;
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement lockUser = null;
        try
        {
            //throws an error if a user without proper roles somehow invokes this function
            if(u.getUserRole() != common.UserRole.SystemAdmin)
                throw new Exception("Attempted User Lock by Non-Admin");
            
            conn.setAutoCommit(false);
            String modifySQL = "UPDATE users "
                    + "SET locked = 1 "
                    + "WHERE userNumber = ?;";
          
            
            lockUser = conn.prepareStatement(modifySQL);
            lockUser.setInt(1, userID);
            lockUser.executeUpdate();
            conn.commit();
            status = true;
        }
        catch (Exception ex)//SQLException ex 
        {
            status = false;
            LogError("Error Locking User #" + userID + ": " + ex);
            if(conn!=null)
            {
                try
                {
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    LogError("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(lockUser != null)
                    lockUser.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or connection:" + excep);
            }
        }
        return status;
    }
    
    /*
        Unlocks the user with the parameter userID
        @param userID the ID of the user being unlocked
        @param u the user doing the unlocking
        @return whether this function was successful or not
    */
    public static boolean unlockUser(int userID, User u)
    {
        boolean status;
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement unlockUser = null;
        try
        {
            //throws an error if a user without proper roles somehow invokes this function
            if(u.getUserRole() != common.UserRole.SystemAdmin)
                throw new Exception("Attempted User Unlock by Non-Admin");
            
            conn.setAutoCommit(false);
            String modifySQL = "UPDATE users "
                    + "SET locked = 0 "
                    + "WHERE userNumber = ?;";
          
            
            unlockUser = conn.prepareStatement(modifySQL);
            unlockUser.setInt(1, userID);
            unlockUser.executeUpdate();
            conn.commit();
            status = true;
        }
        catch (Exception ex)//SQLException ex 
        {
            status = false;
            LogError("Error Unlocking User #" + userID + ": " + ex);
            if(conn!=null)
            {
                try
                {
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    LogError("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(unlockUser != null)
                    unlockUser.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
        return status;
    }
    
    /*
        Inserts a JSONObject into the data table
        Made primarily for pulling data from netronix and inputing it into our
        own tables
    
        @param j a json object containing data for the data value table
    */
    public static void insertJSON(JSONObject j)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement insertData = null;
        try
        {
            String name = (String)j.get("name");
            if(name.equals("Turbidity"))//turbidity values from the old sensor are not wanted
                return;
            
            conn.setAutoCommit(false);
            String insertSQL = "INSERT INTO DataValues (dataName,units,sensor,timeRecorded,dataValue,delta) "
                    + "values (?,?,?,?,?,?)";
            
            //removes special characters as prepared statement will replace them with ?
            String units = (String)j.get("unit");
            if(units.equals("℃"))
            {
                units = "C";
                //air temperature is stored as degrees C
                //This change allows for easy distinguishing among water and air temp
                if(name.equals("Temperature"))//dewpoint is also degrees C so this line is necessary here
                    name = "Air Temperature";
            }
            else if(units.equals("C") && name.equals("Temperature"))
            {
                name = "Water Temperature";//Water has units C and is only labeled temperature
            }
            //HDO refers to a method of checking dissolved oxygen
            //We only care that is it dissolved oxygen
            if(name.equals("HDO"))
                name = "DO";
            else if(name.equals("Turbidity Dig"))//dig only refers to digital, not something meaningful
                name = "Turbidity";
            units = units.replace("μ","u");//special chars not allowed
            
            String time = ((String)j.get("timestamp")).substring(0,19);
            if(dataExistsAtTime(name,time))
            {return;}//This tells us if the data is a duplicate. Don't insert it if so
            
            if(!dataNameExists(name))//updates the table of unique data names should any new ones appear
                insertDataName(name);
            
            insertData = conn.prepareStatement(insertSQL);
            insertData.setString(1, name);
            insertData.setString(2, units);
            insertData.setString(3, (String)j.get("sensor_name"));
            insertData.setString(4, time);
            insertData.setFloat(5, (float)(double)j.get("value"));
            insertData.setFloat(6, (float)(double)j.get("delta"));
            insertData.executeUpdate();
            conn.commit();
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error with JSON Insertion: " + ex);
            if(conn!=null)
            {
                try
                {
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    LogError("Rollback unsuccessful: " + excep);
                }
            }
        }
        finally
        {
            try
            {
                if(insertData != null)
                    insertData.close();
                if(conn != null)
                    conn.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
    }
    
    /*
        Gets the user with the parameter login name
        @return the user with this login name (null if none was found)
    */
    public static User getUserByLoginName(String username) 
    {
        User u = null;
        
        PreparedStatement getUserByLogin = null;
        ResultSet selectedUser = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String getSQL = "SELECT * FROM users WHERE loginName = ?;";
            getUserByLogin = conn.prepareStatement(getSQL);
            getUserByLogin.setString(1, username);
            selectedUser = getUserByLogin.executeQuery();
            selectedUser.next();
            u = new User(
                selectedUser.getInt("userNumber"),
                selectedUser.getString("loginName"),
                selectedUser.getString("password"),
                selectedUser.getString("salt"),
                selectedUser.getString("lastName"),
                selectedUser.getString("firstName"),
                selectedUser.getString("emailAddress"),
                UserRole.getUserRole(selectedUser.getString("userRole")),
                LocalDateTime.parse(selectedUser.getString("lastLoginTime")),
                LocalDateTime.parse(selectedUser.getString("lastAttemptedLoginTime")),
                selectedUser.getInt("loginCount"),
                selectedUser.getInt("attemptedLoginCount"),
                selectedUser.getBoolean("locked")
                );
        }
        catch(Exception e)
        {
            LogError("Error retrieving user with login name \"" + username + "\": "+ e);
        }
        finally
        {
            try
            {
                if(getUserByLogin != null)
                    getUserByLogin.close(); 
                if(selectedUser != null)
                    selectedUser.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        
        return u;
    }

    /*
        Returns the user info if the username and password are correct
        @return a user with these specs, or null if either are wrong
    */
    public static User validateUser(String username, String password) 
    {
        User u = null;
        
        PreparedStatement selectUser = null;
        ResultSet validatee = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            password = security.SecurityCode.encryptSHA256(password+getSaltByLoginName(username));
            String getSQL = "SELECT * FROM users WHERE loginName = ? and password = ?;";
            selectUser = conn.prepareStatement(getSQL);
            selectUser.setString(1, username);
            selectUser.setString(2, password);
            validatee = selectUser.executeQuery();
            validatee.next();
            u = new User(
                validatee.getInt("userNumber"),
                validatee.getString("loginName"),
                validatee.getString("password"),
                validatee.getString("salt"),
                validatee.getString("lastName"),
                validatee.getString("firstName"),
                validatee.getString("emailAddress"),
                UserRole.getUserRole(validatee.getString("userRole")),
                LocalDateTime.parse(validatee.getString("lastLoginTime")),
                LocalDateTime.parse(validatee.getString("lastAttemptedLoginTime")),
                validatee.getInt("loginCount"),
                validatee.getInt("attemptedLoginCount"),
                validatee.getBoolean("locked")
                );
        }
        catch(Exception e)
        {
            LogError("Error validating user \"" + username + "\": " + e);
        }
        finally
        {
            try
            {
                if(selectUser != null)
                    selectUser.close();
                if(validatee != null)
                    validatee.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        
        return u;
    }

    /*
        Updates the user's loginCount, attemptedLoginCount, lastLoginTime
        and lastAttemptedLoginTime
    */
    public static void updateUserLogin(User potentialUser) 
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement updateUser = null;
        try
        {
            conn.setAutoCommit(false);
            String updateSQL = "UPDATE users "
                    + "SET lastLoginTime = ?,"
                    + "lastAttemptedLoginTime = ?,"
                    + "loginCount = ?,"
                    + "attemptedLoginCount = ? "
                    + "WHERE userNumber = ?;";
            updateUser = conn.prepareStatement(updateSQL);
            updateUser.setString(1, potentialUser.getLastLoginTime().toString());
            updateUser.setString(2, potentialUser.getLastAttemptedLoginTime().toString());
            updateUser.setInt(3, potentialUser.getLoginCount());
            updateUser.setInt(4, potentialUser.getAttemptedLoginCount());
            updateUser.setInt(5, potentialUser.getUserNumber());
            updateUser.executeUpdate();
            conn.commit();
        }
        catch(Exception e)
        {
            LogError("Error updating user login: " + e);
            if(conn != null)
                try
                {
                    conn.rollback();
                }
            catch(SQLException excep)
            {
                LogError("Rollback unsuccessful: " + excep);
            }
        }
        finally
        {
            try
            {
                if(updateUser != null)
                    updateUser.close();
                if(conn != null)
                    conn.close();
            }
            catch(Exception excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
    }
    
    /*
        Updates the description with dataName 'name' using the description 'desc'
        @return whether this operation was sucessful or not
    */
    public static boolean updateDescription(String desc, String name)
    {
        boolean status;
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement updateDesc = null;
        try
        {
            conn.setAutoCommit(false);
            String updateSQL = "UPDATE DataDescriptions "
                    + "SET description = ? "
                    + "WHERE dataName = ?;";
            updateDesc = conn.prepareStatement(updateSQL);
            updateDesc.setString(1, desc);
            updateDesc.setString(2, name);
            updateDesc.executeUpdate();
            conn.commit();
            status = true;
        }
        catch(Exception e)
        {
            status = false;
            LogError("Error updating description for \"" + name + "\": " + e);
            if(conn != null)
                try
                {
                    conn.rollback();
                }
                catch(SQLException excep)
                {
                    LogError("Rollback unsuccessful: " + excep);
                }
        }
        finally
        {
            try
            {
                if(updateDesc != null)
                    updateDesc.close();
                if(conn != null)
                    conn.close();
            }
            catch(Exception excep)
            {
                LogError("Error closing statement or connection:" + excep);
            }
        }
        return status;
    }
    
    /*
        Retrieves the description for the parameter data name
        @param name the name of the data type being requested
    */
    public static String getDescription(String name)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement getDesc = null;
        ResultSet selectedDesc = null;
        String desc = null;
        try
        {
            String getSQL = "SELECT * FROM DataDescriptions WHERE dataName = ?";
            getDesc = conn.prepareStatement(getSQL);
            getDesc.setString(1, name);
            selectedDesc = getDesc.executeQuery();
            selectedDesc.next();
            desc = selectedDesc.getString("description");
        }
        catch(Exception e)
        {
            LogError("Error retrieving description for \"" + name + "\": " + e);
        }
        finally
        {
            try
            {
                if(getDesc != null)
                    getDesc.close();
                if(selectedDesc != null)
                    selectedDesc.close();
                if(conn != null)
                    conn.close();
            }
            catch(Exception excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
        return desc;
    }
    
    /*
        Inserts a description into the DataDescriptions table
        @param name the name of the data type that is getting a description
        @param the description (limit 500 characters)
    */
    public static void insertDescription(String name, String desc)
    {
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement insertDesc = null;
        try
        {
            String insertSQL = "INSERT INTO DataDescriptions values(?, ?)";
            insertDesc = conn.prepareStatement(insertSQL);
            insertDesc.setString(1, name);
            insertDesc.setString(2, desc);
            insertDesc.executeUpdate();
        }
        catch(Exception e)
        {
            LogError("Error inserting description for \"" + name + "\": " + e);
        }
        finally
        {
            try
            {
                if(insertDesc != null)
                    insertDesc.close();
                if(conn != null)
                    conn.close();
            }
            catch(Exception excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
    }

    /*
        Retrieves the salt of the user with the parameter login name
    */
    public static String getSaltByLoginName(String loginName) 
    {
        PreparedStatement getUserByLogin = null;
        ResultSet selectedUser = null;
        String salt = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String getSQL = "SELECT * FROM users WHERE loginName = ?;";
            getUserByLogin = conn.prepareStatement(getSQL);
            getUserByLogin.setString(1, loginName);
            selectedUser = getUserByLogin.executeQuery();
            selectedUser.next();
            salt = selectedUser.getString("salt");
        }
        catch(SQLException e)
        {
            LogError("Error retrieving salt by login name for \"" + loginName + "\": " + e);
        }
        finally
        {
            try
            {
                if(getUserByLogin != null)
                    getUserByLogin.close();
                if(selectedUser != null)
                    selectedUser.close();
            }
            catch(Exception excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        
        return salt;
    }
    
    /*
        Inserts the error message into a database table with now as the associated
        time for when the error occured
    */
    public static void LogError(String errorMessage)
    {
        PreparedStatement logError = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String insertSQL = "INSERT INTO ErrorLogs values (?,?)";
            logError = conn.prepareStatement(insertSQL);
            logError.setString(1, LocalDateTime.now().toString());
            logError.setString(2, errorMessage);
            logError.executeUpdate();
        }
        catch(SQLException e)
        {
            //System.out.println("Error inserting error:" + e);
        }
        finally
        {
            try
            {
                if(logError != null)
                    logError.close();
            }
            catch(Exception excep)
            {
                //LogError("Error closing statement or result set:" + excep);
            }
        }
    }
    
    /*
        Returns whether the parameter data name is already in the list of all
        unique data names or not
    */
    public static boolean dataNameExists(String name)
    {
        Statement selectDataNames = null;
        ResultSet dataNames = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String query = "Select * from DataNames";
            selectDataNames = conn.createStatement();
            dataNames = selectDataNames.executeQuery(query);
            
            while(dataNames.next())
                if(dataNames.getString(1).equals(name))
                    return true;
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error checking data names: " + ex);
        }
        finally
        {
            try
            {
                if(selectDataNames != null)
                    selectDataNames.close();
                if(dataNames != null)
                    dataNames.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        return false;
    }

    /*
        Inserts the data name into the table of unique data names
        This should only be called if it is known in advance that the 
        parameter name is not already in the table (ex: through the dataNameExists
        function)
    */
    public static void insertDataName(String name)
    {
        PreparedStatement insertDataName = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String insertSQL = "INSERT INTO DataNames values (?)";
            insertDataName = conn.prepareStatement(insertSQL);
            insertDataName.setString(1, name);
            insertDataName.executeUpdate();
        }
        catch(SQLException e)
        {
            LogError("Error inserting Data Name:" + e);
        }
        finally
        {
            try
            {
                if(insertDataName != null)
                    insertDataName.close();
            }
            catch(Exception excep)
            {
                LogError("Error closing statement or result set:" + excep);
            }
        }
    }
    
    /*
        Returns an arraylist of all unique data names
    */
    public static ArrayList<String> getDataNames()
    {
        ArrayList<String> dataNameList= new ArrayList<>();
        Statement selectDataNames = null;
        ResultSet dataNames = null;
        System.out.println("Getting Data Names...");
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            System.out.println("Made connection...");
            String query = "Select * from DataNames";
            selectDataNames = conn.createStatement();
            dataNames = selectDataNames.executeQuery(query);
            
            while(dataNames.next())
                    dataNameList.add(dataNames.getString(1));
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error retrieving data names: " + ex);
        }
        finally
        {
            try
            {
                if(selectDataNames != null)
                    selectDataNames.close();
                if(dataNames != null)
                    dataNames.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        
        System.out.println("Returning...");
        return dataNameList;
    }
    
    /*
        Returns whether the parameter data name is already in the list of all
        unique manual data names or not
    */
    public static boolean manualDataNameExists(String name)
    {
        Statement selectManualDataNames = null;
        ResultSet dataNames = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String query = "Select * from DataNames";
            selectManualDataNames = conn.createStatement();
            dataNames = selectManualDataNames.executeQuery(query);
            
            while(dataNames.next())
                if(dataNames.getString(1).equals(name))
                    return true;
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error checking data names: " + ex);
        }
        finally
        {
            try
            {
                if(selectManualDataNames != null)
                    selectManualDataNames.close();
                if(dataNames != null)
                    dataNames.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        return false;
    }

    /*
        Inserts the data name into the table of manual data names
        This should only be called if it is known in advance that the 
        parameter name is not already in the table (ex: through the manualDataNameExists
        function)
    */
    public static void insertManualDataName(String name)
    {
        PreparedStatement insertDataName = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String insertSQL = "INSERT INTO ManualDataNames values (?)";
            insertDataName = conn.prepareStatement(insertSQL);
            insertDataName.setString(1, name);
            insertDataName.executeUpdate();
        }
        catch(SQLException e)
        {
            LogError("Error inserting Data Name:" + e);
        }
        finally
        {
            try
            {
                if(insertDataName != null)
                    insertDataName.close();
            }
            catch(Exception excep)
            {
                LogError("Error closing statement or result set:" + excep);
            }
        }
    }
    
    
    
    /*
        Returns an arraylist of all manual data names
    */
    public static ArrayList<String> getManualDataNames()
    {
        ArrayList<String> dataNameList= new ArrayList<>();
        Statement selectDataNames = null;
        ResultSet dataNames = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String query = "Select * from ManualDataNames";
            selectDataNames = conn.createStatement();
            dataNames = selectDataNames.executeQuery(query);
            
            while(dataNames.next())
                    dataNameList.add(dataNames.getString(1));
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error retrieving data names: " + ex);
        }
        finally
        {
            try
            {
                if(selectDataNames != null)
                    selectDataNames.close();
                if(dataNames != null)
                    dataNames.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        return dataNameList;
    }
    
    /*
        Checks if data with the parameter dataName exists at the parameter time.
        Since it doesn't make sense for there to be 2 different values for a
        data type at the same time, this function is useful for preventing data
        duplicates from being pulled from netronix.
    
        time is in LocalDateTime.toString format (it's pointless to convert it
        to LocalDateTime just to pass it here, then convert it back as it is 
        pulled from the JSON as a string and it is needed as a string here).
    */
    public static boolean dataExistsAtTime(String dataName, String time)
    {
        PreparedStatement selectDataAtTime = null;
        ResultSet dataAtTime = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String query = "Select * from DataValues Where dataName = ? AND timeRecorded = ?";
            selectDataAtTime = conn.prepareStatement(query);
            selectDataAtTime.setString(1, dataName);
            selectDataAtTime.setString(2, time.toString());
            dataAtTime = selectDataAtTime.executeQuery();
            
            if(!dataAtTime.next())
                return false;//if there is no data, it doesn't exist at that time
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error checking if data exists at time: " + ex);
        }
        finally
        {
            try
            {
                if(selectDataAtTime != null)
                    selectDataAtTime.close();
                if(dataAtTime != null)
                    dataAtTime.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        return true;//else there is data at that time
    }
    
    /*
        Returns an arraylist of all errors
    */
    public static ArrayList<ErrorMessage> getErrors()
    {
        ArrayList<ErrorMessage> errorList= new ArrayList<>();
        Statement selectErrors = null;
        ResultSet selectedErrors = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String query = "Select * from ErrorLogs";
            selectErrors = conn.createStatement();
            selectedErrors = selectErrors.executeQuery(query);
            
            while(selectedErrors.next())
            {
                errorList.add(
                        new ErrorMessage(LocalDateTime.parse(selectedErrors.getString(1)), 
                        selectedErrors.getString(2))
                );
            }
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error retrieving data names: " + ex);
        }
        finally
        {
            try
            {
                if(selectErrors != null)
                    selectErrors.close();
                if(selectedErrors != null)
                    selectedErrors.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        return errorList;
    }
    
    /*
        Returns an arraylist of all errors within the parameter time range
    */
    public static ArrayList<ErrorMessage> getErrorsInRange(LocalDateTime lower, LocalDateTime upper)
    {
        ArrayList<ErrorMessage> errorList= new ArrayList<>();
        PreparedStatement selectErrors = null;
        ResultSet selectedErrors = null;
        try(Connection conn = Web_MYSQL_Helper.getConnection();)
        {
            String query = "Select * from ErrorLogs where timeOccured >= ? AND timeOccured <= ?";
            selectErrors = conn.prepareStatement(query);
            selectErrors.setString(1, lower.toString());
            selectErrors.setString(2, upper.toString());
            selectedErrors = selectErrors.executeQuery();
            
            while(selectedErrors.next())
            {
                errorList.add(
                        new ErrorMessage(LocalDateTime.parse(selectedErrors.getString(1)), 
                        selectedErrors.getString(2))
                );
            }
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error retrieving data names: " + ex);
        }
        finally
        {
            try
            {
                if(selectErrors != null)
                    selectErrors.close();
                if(selectedErrors != null)
                    selectedErrors.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        return errorList;
    }
    
}