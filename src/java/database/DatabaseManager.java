/*
 * Includes various database managing 
 */
package database;

import async.DataParameter;
import async.DataReceiver;
import async.DataValue;
import com.github.davidmoten.rx.Schedulers;
import common.User;
import common.UserRole;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import com.github.davidmoten.rx.jdbc.Database;
import com.github.davidmoten.rx.util.Pair;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import rx.Observable;
import security.SecurityCode;

/**
 * DatabaseManager provides a simple interface to the database. The database manager
 * will service all requests asynchronously through RxJava's Observable interface.
 * 
 * @author Tyler Mutzek & Louis Jenkins
 */
public class DatabaseManager {
    
    /*
        Below, we cache some commonly-used values in memory here, of which is used atomically. 
        We utilize a RCU (Read-Copy-Update) strategy, as it greatly improves read performance, 
        especially with writes being very seldom.
    */
    // (id <--> name) 
    public static volatile AtomicReference<ImmutableBiMap<Long, String>> atomicParameterMappings = new AtomicReference<>(ImmutableBiMap.of());
    
    static {
        updateParameterMappings();
        System.out.println("Updated Parameter Mappings!\n\n\n\n\n\n");
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
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
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
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
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
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
            createTable = conn.createStatement();
            String createSQL = "Create Table IF NOT EXISTS ErrorLogs("
                    + "timeOccured varchar(25) primary key,"
                    + "error varchar(300)"
                    + ");";
            createTable.execute(createSQL);
        }
        catch (Exception ex)//SQLException ex 
        {   
            // L.J: Changed as you can't use LogError if the log table isn't setup
            System.out.println("Error creating Error Logs Table: " + ex);
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                if(createTable != null)
                    createTable.close();
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
            }
            catch(SQLException e)
            {
                // L.J: Changed as you can't use LogError if the log table isn't setup
                LogError("Error closing statement: " + e);
            }
        }
    }
    
    // TODO: Convert!
    public static long insertData(List<DataValue> data) {
        Database db = Database.from(Web_MYSQL_Helper.getConnection());
        Map<Long, Long> sourceToDatabaseIdMap = new HashMap<>();
        Observable.from(data)
                .map(DataValue::getId)
                .distinct()
                .flatMap(id -> db.select("select parameter_id from remote_data_parameters where source = ?")
                        .parameter(id)
                        .getAs(Long.class)
                        .map(databaseId -> Pair.create(id, databaseId))
                        .defaultIfEmpty(Pair.create(id, id))
                )
                .toBlocking()
                .subscribe(pair -> sourceToDatabaseIdMap.put(pair.a(), pair.b()));
        Web_MYSQL_Helper.returnConnection(db.getConnectionProvider().get());
        
        
        long nBatch = 0;
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement insertData = null;
        try
        {
            conn.setAutoCommit(false);
            String insertSQL = "insert into data_values (time, value, parameter_id) values (?, ?, ?) on duplicate key update value=?";
            insertData = conn.prepareStatement(insertSQL);
            for (async.DataValue value : data) {
                insertData.setTimestamp(1, Timestamp.from(value.getTimestamp()));
                insertData.setDouble(2, value.getValue());
                insertData.setLong(3, sourceToDatabaseIdMap.get(value.getId()));
                insertData.setDouble(4, value.getValue());
                insertData.addBatch();
                
                System.out.println("Inserted: " + value);
                nBatch++;
                
                if (nBatch % 1000 == 0) {
                    insertData.executeBatch();
                    insertData.clearBatch();
                }
            }
            if (insertData != null) {
                insertData.executeBatch();
            }
            conn.commit();
        }
        catch (SQLException ex)
        {
            nBatch = 0;
            LogError("Error Inserting Batches of Data: " + ex);
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
                if(insertData != null) {
                    insertData.close();
                }
                if(conn != null) {
//                    conn.setAutoCommit(true);
                    Web_MYSQL_Helper.returnConnection(conn);
                }
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
        return nBatch;
    }
    
    public static io.reactivex.Observable<Long> parameterNameToId(String name) {
        ImmutableBiMap<String, Long> currentMapping = atomicParameterMappings.get().inverse();
        Long id = currentMapping.get(name);
        return id == null ? io.reactivex.Observable.empty() : io.reactivex.Observable.just(id);
    }
    
    public static io.reactivex.Observable<String> parameterIdToName(Long id) {
        ImmutableBiMap<Long, String> currentMapping = atomicParameterMappings.get();
        String name = currentMapping.get(id);
        return name == null ? io.reactivex.Observable.empty() : io.reactivex.Observable.just(name);
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
                    Web_MYSQL_Helper.returnConnection(conn);
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
    public static int manualDeletion(String [] dataDeletionTimes, int parameterID, User u)
    {
        int successfulDeletions = 0;
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement deleteData = null;
        try
        {
            //throws an error if a user without proper roles somehow invokes this function
            if(u.getUserRole() != common.UserRole.SystemAdmin)
                throw new Exception("Attempted Data Deletion by Non-Admin");
            conn.setAutoCommit(false);
            String deleteSQL = "Delete from data_values where time = ? and id = ?";
            deleteData = conn.prepareStatement(deleteSQL);
            deleteData.setInt(2, parameterID);
            
            for(String deletionTime : dataDeletionTimes)
            try
                {
                    deletionTime = deletionTime.substring(0,deletionTime.length()-1);
                    deletionTime = deletionTime.replace('T', ' ');
                    
                    deleteData.setString(1, deletionTime);
                    deleteData.executeUpdate();
                    conn.commit();
                    successfulDeletions++;
                }
                catch(Exception e)
                {
                    LogError("Error Deleting Data with ID:" + parameterID + "at time: " + deletionTime + ": " + e);
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
        catch (Exception ex)//SQLException ex 
        {
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
                    Web_MYSQL_Helper.returnConnection(conn);
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
        return successfulDeletions;
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
                    Web_MYSQL_Helper.returnConnection(conn);
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
    public static int deleteUsers(int [] userIDs, User u)
    {
        int successfulDeletions = 0;
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement deleteUser = null;
        try
        {
            //throws an error if a user without proper roles somehow invokes this function
            if(u.getUserRole() != common.UserRole.SystemAdmin)
                throw new Exception("Attempted User Deletion by Non-Admin");
            conn.setAutoCommit(false);
            String deleteSQL = "Delete from users where userNumber = ?";
            deleteUser = conn.prepareStatement(deleteSQL);
            for(int userID : userIDs)
            {
                if(userID == u.getUserNumber())
                    LogError("Error Deleting User: User Attempting to delete self");
                else
                {
                    deleteUser.setInt(1, userID);
                    deleteUser.executeUpdate();
                    conn.commit();
                    successfulDeletions++;
                }
            }
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error Deleting User: " + ex);
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
                    Web_MYSQL_Helper.returnConnection(conn);
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
        return successfulDeletions;
    }
    
    /**
     * Updates the current parameter mappings. This is a serialized procedure as the
     * interleaving of potentially two or more writers could cause the in-memory copy
     * to be out of date (I.E: Race Condition). Readers can still atomically obtain the
     * cached mappings atomically, and so there is no consequence.
     */
    public static synchronized void updateParameterMappings() {
        PreparedStatement selectMappings = null;
        ResultSet mappingResults = null;
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
            String query = "select id, name from data_parameters";
            selectMappings = conn.prepareStatement(query);
            mappingResults = selectMappings.executeQuery();
            
            BiMap<Long, String> mappings = HashBiMap.create();
            while(mappingResults.next()) {
                mappings.put(mappingResults.getLong(1), mappingResults.getString(2));
            }
            
           DatabaseManager.atomicParameterMappings.set(ImmutableBiMap.copyOf(mappings));
           System.out.println("Set: " + mappings);
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error Updating Parameter Mappings: " + ex);
        }
        finally
        {
            try
            {
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
                if(selectMappings != null)
                    selectMappings.close();
                if(mappingResults != null)
                    mappingResults.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
    }
    
    public static Optional<Long> remoteSourceToDatabaseId(Long id) {
        Database db = Database.from(Web_MYSQL_Helper.getConnection());
        return  db.select("select parameter_id from remote_data_parameters where source = ?")
                .parameter(id)
                .getAs(Long.class)
                .map(Optional::of)
                .toBlocking()
                .singleOrDefault(Optional.empty());
                
                
    }
    
    public static io.reactivex.Observable<async.DataValue> getDataValues(Instant start, Instant end, String name) {
        Database db = Database.from(Web_MYSQL_Helper.getConnection());
        PublishSubject<async.DataValue> results = PublishSubject.create();
        Subject<async.DataValue> serializedResults = results.toSerialized();
        long id = db.select("select id from data_parameters where name = ?")
                .parameter(name)
                .getAs(Long.class)
                .toBlocking()
                .single();
        
        db.select("select source from remote_data_parameters where parameter_id = ?")
                .parameter(id)
                .count()
                .subscribeOn(Schedulers.computation())
                .flatMap(cnt -> {
                    // Is it a remote data value?
                    if (cnt != 0) {
                        return db.select("select source from remote_data_parameters where parameter_id = ?")
                                .parameter(id)
                                .getAs(Long.class)
                                .flatMap(remoteKey -> Observable
                                        .from(DataReceiver
                                                .getRemoteData(start, end, remoteKey)
                                                .getRawData()
                                                .stream()
                                                .map(dv -> new async.DataValue(id, dv.getTimestamp(), dv.getValue()))
                                                .collect(Collectors.toList())
                                        )
                                ); 
                    } else {
                        return db.select("select time, value from data_values where parameter_id = ? and time < ? and time > ?")
                            .parameter(id)
                            .parameter(Timestamp.from(end))
                            .parameter(Timestamp.from(start))
                            .getAs(Long.class, Double.class)
                            .sorted((p1, p2) -> p1._1().compareTo(p2._1()))
                            .map(pair -> new async.DataValue(id, Instant.ofEpochMilli(pair._1()), pair._2()));
                    }
                })
                .subscribe(serializedResults::onNext, serializedResults::onError, () -> { serializedResults.onComplete(); Web_MYSQL_Helper.returnConnection(db.getConnectionProvider().get());});
        
        return results.compose(DataFilter.getFilter(id)::filter);
    }
    
    
    /*
        Adds a new user to the user table
        Encrypts the password via SHA256 encryption with salt before storing
        Last login and attempted login are initiallized to now
        @return whether this function was successful or not
    */
    public static int addNewUser(String username, String password, String firstName,
            String lastName, String email, UserRole userRole, User u)
    {
        int status;
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement insertUser = null;
        try
        {
            //throws an error if a user without proper roles somehow invokes this function
            if(u.getUserRole() != common.UserRole.SystemAdmin)
                throw new Exception("Attempted User Creation by Non-Admin");
            
            if(usernameExists(username))
                return  2;
            
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
            status = 1;
        }
        catch (Exception ex)//SQLException ex 
        {
            status = 0;
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
                    Web_MYSQL_Helper.returnConnection(conn);
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
    public static int lockUser(int [] userIDs, User u)
    {
        int successfulLocks = 0;
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
            
            for(int userID : userIDs)
            {
                if(userID == u.getUserNumber())
                    LogError("Error Locking User: User Attempting to Lock Self");
                else
                {
                    try
                    {
                        lockUser.setInt(1, userID);
                        lockUser.executeUpdate();
                        conn.commit();
                        successfulLocks++;
                    }
                    catch(Exception e)
                    {
                        LogError("Error Locking User with ID: " + userID + ": " + e);
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
            }
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error Locking Users: " + ex);
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
                    Web_MYSQL_Helper.returnConnection(conn);
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or connection:" + excep);
            }
        }
        return successfulLocks;
    }
    
    /*
        Unlocks the user with the parameter userID
        @param userID the ID of the user being unlocked
        @param u the user doing the unlocking
        @return whether this function was successful or not
    */
    public static int unlockUser(int [] userIDs, User u)
    {
        int successfulUnlocks = 0;
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
            for(int userID : userIDs)
            {
                try
                {
                    unlockUser.setInt(1, userID);
                    unlockUser.executeUpdate();
                    conn.commit();
                    successfulUnlocks++;
                }
                catch(Exception e)
                {
                    LogError("Error Unlocking User with ID: " + userID + ": " + e);
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
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error Unlocking Users: " + ex);
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
                    Web_MYSQL_Helper.returnConnection(conn);
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or connection: " + excep);
            }
        }
        return successfulUnlocks;
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
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
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
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
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
        Gets the user with the parameter login name
        @return a JSONObject holding all users
    */
    public static JSONObject getUsers() 
    {
        JSONObject userListFinal = new JSONObject();
        JSONArray userList = new JSONArray();
        
        Statement selectUsers = null;
        ResultSet selectedUsers = null;
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
            String query = "Select * from users";
            selectUsers = conn.createStatement();
            selectedUsers = selectUsers.executeQuery(query);
            
            JSONObject user;
            while(selectedUsers.next())
            {
                user = new JSONObject();
                user.put("userNumber",selectedUsers.getString("userNumber"));
                user.put("loginName",selectedUsers.getString("loginName"));
                user.put("lastName",selectedUsers.getString("lastName"));
                user.put("firstName",selectedUsers.getString("firstName"));
                user.put("locked",selectedUsers.getString("locked"));
                user.put("emailAddress",selectedUsers.getString("emailAddress"));
                user.put("userRole",selectedUsers.getString("userRole"));
                userList.add(user);
            }
            userListFinal.put("users", userList);
        }
        catch(Exception e)
        {
            LogError("Error retrieving users: "+ e);
        }
        finally
        {
            try
            {
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
                if(selectUsers != null)
                    selectUsers.close(); 
                if(selectedUsers != null)
                    selectedUsers.close();
            }
            catch(SQLException excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        
        return userListFinal;
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
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
            password = security.SecurityCode.encryptSHA256(password + getSaltByLoginName(username));
            String getSQL = "SELECT * FROM users WHERE loginName = ? and password = ?;";
            selectUser = conn.prepareStatement(getSQL);
            selectUser.setString(1, username);
            selectUser.setString(2, password);
            validatee = selectUser.executeQuery();
            validatee.next();
            System.out.println(Integer.parseInt(validatee.getString("userNumber")));
            u = new User(
                Integer.parseInt(validatee.getString("userNumber")),
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
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
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
                    Web_MYSQL_Helper.returnConnection(conn);
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
    public static boolean updateDescription(String desc, long id, String name)
    {
        boolean status;
        Connection conn = Web_MYSQL_Helper.getConnection();
        PreparedStatement updateDesc = null;
        PreparedStatement updateName = null;
        try
        {
            conn.setAutoCommit(false);
            String updateDescSQL = "UPDATE data_descriptions "
                    + "SET description = ? "
                    + "WHERE parameter_id = ?;";
                    
            String updateNameSQL = "UPDATE data_parameters "
                    + "SET name = ? "
                    + "WHERE id = ?;";
            
            updateDesc = conn.prepareStatement(updateDescSQL);
            updateDesc.setString(1, desc);
            updateDesc.setString(2, id + "");
            
            updateName = conn.prepareStatement(updateNameSQL);
            updateName.setString(1, name);
            updateName.setString(2, id + "");
            
            updateDesc.executeUpdate();
            updateName.executeUpdate();
            
            conn.commit();
            status = true;
        }
        catch(Exception e)
        {
            status = false;
            LogError("Error updating description for \"" + id + "\": " + e);
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
                    Web_MYSQL_Helper.returnConnection(conn);
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
    public static io.reactivex.Observable<String> getDescription(long id)
    {
        Database db = Database.from(Web_MYSQL_Helper.getConnection());
        PublishSubject<String> results = PublishSubject.create();
        Subject<String> serializedResults = results.toSerialized();
        
        db.select("select description from data_descriptions where parameter_id = ?")
                .parameter(id)
                .getAs(String.class)
                .subscribeOn(rx.schedulers.Schedulers.computation())
                .subscribe(serializedResults::onNext, serializedResults::onError, () -> { serializedResults.onComplete(); Web_MYSQL_Helper.returnConnection(db.getConnectionProvider().get());});
        
        return results;
    }
    

    /*
        Retrieves the salt of the user with the parameter login name
    */
    public static String getSaltByLoginName(String loginName) 
    {
        PreparedStatement getUserByLogin = null;
        ResultSet selectedUser = null;
        String salt = null;
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
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
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
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
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
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
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
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
        Returns an JSONObject of all errors
    */
    public static JSONObject getErrors()
    {
        JSONObject errorListFinal = new JSONObject();
        JSONArray errorList = new JSONArray();
        Statement selectErrors = null;
        ResultSet selectedErrors = null;
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
            String query = "Select * from ErrorLogs";
            selectErrors = conn.createStatement();
            selectedErrors = selectErrors.executeQuery(query);
            
            JSONObject error;
            while(selectedErrors.next())
            {
                error = new JSONObject();
                error.put("time", selectedErrors.getString(1));
                error.put("errorMessage", selectedErrors.getString(2));
                errorList.add(error);
            }
            errorListFinal.put("errors", errorList);
            
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error retrieving data names: " + ex);
        }
        finally
        {
            try
            {
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
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
        
        return errorListFinal;
    }
    
    /*
        Returns an JSONObject of all errors within a time range
    
        lower and upper are localdatetime format 
    */
    public static JSONObject getErrorsInRange(String lower, String upper)
    {
        JSONObject errorListFinal = new JSONObject();
        JSONArray errorList = new JSONArray();
        PreparedStatement selectErrors = null;
        ResultSet selectedErrors = null;
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
            String query = "Select * from ErrorLogs where timeOccured >= ? AND timeOccured <= ?";
            selectErrors = conn.prepareStatement(query);
            selectErrors.setString(1, lower);
            selectErrors.setString(2, upper);
            selectedErrors = selectErrors.executeQuery();
            
            JSONObject error;
            while(selectedErrors.next())
            {
                error = new JSONObject();
                error.put("time", selectedErrors.getString(1));
                error.put("errorMessage", selectedErrors.getString(2));
                errorList.add(error);
            }
            
            errorListFinal.put("errors", errorList);
        }
        catch (Exception ex)//SQLException ex 
        {
            LogError("Error retrieving data names: " + ex);
        }
        finally
        {
            try
            {
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
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
        return errorListFinal;
    }
    
    public static io.reactivex.Observable<String> getManualParameterNames()
    {
        Database db = Database.from(Web_MYSQL_Helper.getConnection());
        PublishSubject<String> results = PublishSubject.create();
        Subject<String> serializedResults = results.toSerialized();
        
        db.select("select parameter_id from manual_data_parameters")
                .getAs(Long.class)
                .subscribeOn(rx.schedulers.Schedulers.computation())
                .compose(db.select("select name from data_parameters where id = ?")
                        .parameterTransformer()
                        .getAs(String.class)
                )
                .subscribe(serializedResults::onNext, serializedResults::onError, () -> { serializedResults.onComplete(); Web_MYSQL_Helper.returnConnection(db.getConnectionProvider().get());});
        
        return results;
    }
    
    public static io.reactivex.Observable<String> getRemoteParameterNames()
    {
        Database db = Database.from(Web_MYSQL_Helper.getConnection());
        PublishSubject<String> results = PublishSubject.create();
        Subject<String> serializedResults = results.toSerialized();
        
        db.select("select parameter_id from remote_data_parameters")
                .getAs(Long.class)
                .subscribeOn(rx.schedulers.Schedulers.computation())
                .compose(db.select("select name from data_parameters where id = ?")
                        .parameterTransformer()
                        .getAs(String.class)
                )
                .subscribe(serializedResults::onNext, serializedResults::onError, () -> { serializedResults.onComplete(); Web_MYSQL_Helper.returnConnection(db.getConnectionProvider().get());});
        
        
        return results;
    }
    
    public static void insertManualParameter(DataParameter parameter) {
        Database db = Database.from(Web_MYSQL_Helper.getConnection());
        
        db.update("INSERT IGNORE INTO `WaterQuality`.`data_parameters` (`name`, `unit`) values (?, ?);")
                .dependsOn(db.beginTransaction())
                .parameter(parameter.getName())
                .parameter("".equals(parameter.getUnit()) ? null : parameter.getUnit())
                .returnGeneratedKeys()
                .getAs(Long.class)
                .flatMap(key -> Observable.just(key)
                                    .compose(db.update("INSERT IGNORE INTO `WaterQuality`.`data_descriptions` (`parameter_id`, `description`) values (?, ?);")
                                            .parameter(key)
                                            .parameter(parameter.getDescription())
                                            .dependsOnTransformer()
                                    )
                                    .compose(db.update("INSERT IGNORE INTO `WaterQuality`.`manual_data_parameters` (`parameter_id`, `source`) values (?, ?);")
                                            .parameter(key)
                                            .parameter(parameter.getId())
                                            .dependsOnTransformer()
                                    )
                )
                .compose(db.commitOnComplete_())
                .subscribe();
    }
    
    public static void insertRemoteParameter(DataParameter parameter) {
        Database db = Database.from(Web_MYSQL_Helper.getConnection());
                
        db.update("INSERT IGNORE INTO `WaterQuality`.`data_parameters` (`name`, `unit`) values (?, ?);")
                .dependsOn(db.beginTransaction())
                .parameter(parameter.getName())
                .parameter("".equals(parameter.getUnit()) ? null : parameter.getUnit())
                .returnGeneratedKeys()
                .getAs(Long.class)
                .flatMap(key -> Observable.just(key)
                                    .compose(db.update("INSERT IGNORE INTO `WaterQuality`.`data_descriptions` (`parameter_id`, `description`) values (?, ?);")
                                            .parameter(key)
                                            .parameter(parameter.getDescription())
                                            .dependsOnTransformer()
                                    )
                                    .compose(db.update("INSERT IGNORE INTO `WaterQuality`.`remote_data_parameters` (`parameter_id`, `source`, `remote_name`) values (?, ?, ?);")
                                            .parameter(key)
                                            .parameter(parameter.getId())
                                            .parameter(parameter.getSensor())
                                            .dependsOnTransformer()
                                    )
                )
                .compose(db.commitOnComplete_())
                .subscribe();
    }

    private static boolean usernameExists(String username) 
    {
        PreparedStatement getUserByLogin = null;
        ResultSet selectedUser = null;
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
            String getSQL = "SELECT * FROM users WHERE loginName = ?;";
            getUserByLogin = conn.prepareStatement(getSQL);
            getUserByLogin.setString(1, username);
            selectedUser = getUserByLogin.executeQuery();
            if(selectedUser.next())
                return true;
        }
        catch(SQLException e)
        {
            LogError("Error checking if login name exists for \"" + username + "\": " + e);
        }
        finally
        {
            try
            {
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
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
        return false;
    }
    
    public static String getDataParameter(long id) 
    {
        PreparedStatement getDataParameter = null;
        ResultSet selectedParameter = null;
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
            String getSQL = "SELECT * FROM data_parameters WHERE id = ?;";
            getDataParameter = conn.prepareStatement(getSQL);
            getDataParameter.setString(1, id + "");
            selectedParameter = getDataParameter.executeQuery();
            if(selectedParameter.next())
            {
                return selectedParameter.getString("unit");
            }
        }
        catch(SQLException e)
        {
            LogError("Error retrieving parameter with id " + id + ": " + e);
        }
        finally
        {
            try
            {
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
                if(getDataParameter != null)
                    getDataParameter.close();
                if(selectedParameter != null)
                    selectedParameter.close();
            }
            catch(Exception excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        return null;
    }
    
    public static void insertFilteredData(long paramId, Set<Long> times) {
        System.out.println("Inserting for " + paramId + " the values: " + times);
        PreparedStatement insertFilter = null;
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
            conn.setAutoCommit(false);
            String insertSQL = "replace into data_filter (time, parameter_id) values (?, ?)";
            insertFilter = conn.prepareStatement(insertSQL);
            for (long time : times) {
                insertFilter.setTimestamp(1, Timestamp.from(Instant.ofEpochMilli(time)));
                insertFilter.setLong(2, paramId);
                insertFilter.addBatch();
            }
            
            insertFilter.executeBatch();
            conn.commit();
        }
        catch(Exception e)
        {
            LogError("Error inserting filtered data with id " + paramId + ": " + e);
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
                if(conn != null) {
                    Web_MYSQL_Helper.returnConnection(conn);
                    conn.setAutoCommit(true);
                }
                if(insertFilter != null)
                    insertFilter.close();
            }
            catch(Exception excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
        return;
    }
    
    /*
    public static boolean undoDatabaseFunction()
    {
        Statement undo = null;
        Connection conn = null;
        String undoSQL = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
            undoSQL = UNDOLIST.pop();
            undo = conn.createStatement();
            undo.executeUpdate(undoSQL);
            return true;
        }
        catch(SQLException e)
        {
            LogError("Error undoing command: " + undoSQL);
            return false;
        }
        finally
        {
            try
            {
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
                if(undo != null)
                    undo.close();
            }
            catch(Exception excep)
            {
                LogError("Error closing statement or result set: " + excep);
            }
        }
    }
    */
    public static void main(String[] args) {
        User u = new User();
        u.setUserRole(UserRole.SystemAdmin);
        addNewUser("root", "root", "Louis", "Jenkins", "", UserRole.SystemAdmin, u);

    }

    public static boolean isUserLocked(User admin) 
    {
        if(admin == null)
            return false;
        PreparedStatement getUserByLogin = null;
        ResultSet selectedUser = null;
        Connection conn = null;
        try
        {
            conn = Web_MYSQL_Helper.getConnection();
            String getSQL = "SELECT * FROM users WHERE loginName = ?;";
            getUserByLogin = conn.prepareStatement(getSQL);
            getUserByLogin.setString(1, admin.getLoginName());
            selectedUser = getUserByLogin.executeQuery();
            selectedUser.next();
            return selectedUser.getString("locked").equals("1");
        }
        catch(SQLException e)
        {
            LogError("Error checking if user is locked: " + e);
        }
        finally
        {
            try
            {
                if(conn != null)
                    Web_MYSQL_Helper.returnConnection(conn);
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
        return false;
    }
}
