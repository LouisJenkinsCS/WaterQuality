/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import async.DataReceiver;
import async.DataValue;
import common.UserRole;
import database.DataFilter;
import database.DatabaseManager;
import io.reactivex.Observable;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.time.Instant;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.javatuples.Quartet;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static utilities.DataToCSV.dataToCSV;
import utilities.JSONUtils;

/**
 *
 * @author Tyler Mutzek
 */
@WebServlet(name = "AdminServlet", urlPatterns = {"/AdminServlet"})
public class AdminServlet extends HttpServlet {

    private static final JSONObject BAD_REQUEST = new JSONObject();
    private static final JSONObject EMPTY_RESULT = new JSONObject();

    static {
        EMPTY_RESULT.put("data", new JSONArray());
        BAD_REQUEST.put("status", "Generic Error...");
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);//Create a new session if one does not exists
        final Object lock = session.getId().intern();
        common.User admin = (common.User) session.getAttribute("user");
        String action = request.getParameter("action");
        
        if(DatabaseManager.isUserLocked(admin))
        {
            session.removeAttribute("user");//logout on server
            
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/WaterQuality/");
            return;
        }
        
        if (action == null) {
            return;
        }

        /*
            Admin is manually inputting data into the ManualDataValues table
        
            If data is parsed and the input succeeds or fails, inputstatus is set
            If the data fails to parse, input status will remain null so check
            if dateStatus, numberStatus, and etcStatus if they are not null and
            print whatever isn't null so the user can see what they did wrong.
         */
        if (action.trim().equalsIgnoreCase("InputData")) {
            String dataName = request.getParameter("dataName");

        } /*
            Admin is deleting single pieces of data from the DataValues table
            
            If the deletion succeeds or fails without causing an error, 
            dataDeletionStatus is set.
            If an error arises, etcStatus is set with a suggested cause
         */ else if (action.trim().equalsIgnoreCase("RemoveData")) {
            try {
//                response.getWriter().append("This is your response. Success.");
                JSONObject req = (JSONObject) new JSONParser().parse(request.getParameter("data"));
                Observable.just(req)
                        .map(o -> (JSONArray) o.get("time"))
                        .map(arr -> arr.stream().mapToLong(o -> (Long) o).boxed().collect(Collectors.toSet()))
                        .blockingSubscribe(allTimes -> DatabaseManager
                                .parameterNameToId((String) req.get("parameter"))
                                .subscribe(id -> DataFilter
                                        .getFilter(id)
                                        .add((Set<Long>) allTimes)
                                )
                        );
                /*
                String tempIDs = request.getParameter("deletionIDs");
                String [] dataDeletionTimes = tempIDs.split(",");
                
                int successfulDeletions = DatabaseManager.manualDeletion(dataDeletionTimes, Integer.parseInt((String)request.getParameter("parameter")),
                        admin);
                if (successfulDeletions == dataDeletionTimes.length) {
                    JSONObject obj = new JSONObject();
                    obj.put("status", "Data Deletion Successful");
                    response.getWriter().append(obj.toJSONString());
                } else {
                    JSONObject obj = new JSONObject();
                    obj.put("status", dataDeletionTimes.length - successfulDeletions + " deletions failed, check the error logs");
                    response.getWriter().append(obj.toJSONString());
                }
                */
            } catch (Exception e) {
                request.setAttribute("status", "Error: " + e);
            }
        } /*
            Admin is registering a new user to the Users table
            
            If registering the user succeeds or fails without an error, 
            inputStatus is set.
            If an error arises, etcStatus is set with the exception message as
            there are no obvious reasons for it to fail.
         */ else if (action.trim().equalsIgnoreCase("RegisterUser")) {
            try {
                int newUserStatus = DatabaseManager.addNewUser((String) request.getParameter("username"),
                        (String) request.getParameter("password"), (String) request.getParameter("firstName"),
                        (String) request.getParameter("lastName"), (String) request.getParameter("email"),
                        UserRole.getUserRole((String) request.getParameter("userRole")),
                        admin);
                switch (newUserStatus) {
                    case 0: {
                        JSONObject obj = new JSONObject();
                        obj.put("status", "Failed");
                        response.getWriter().append(obj.toJSONString());
                        break;
                    }
                    case 1: {
                        JSONObject obj = new JSONObject();
                        obj.put("status", "Success");
                        response.getWriter().append(obj.toJSONString());
                        break;
                    }
                    case 2: {
                        JSONObject obj = new JSONObject();
                        obj.put("status", "Error: Username Already Exists");
                        response.getWriter().append(obj.toJSONString());
                        break;
                    }
                    default:
                        break;
                }
            } catch (Exception e) {
                request.setAttribute("status", "Error registering user: " + e);
            }
        } /*
            Admin is deleting a user from the Users table
            
            If the deletion succeeds or fails with no error, userDeletionStatus
            is set.
            If an error arises, etcStatus is set with a suggested cause.
         */ else if (action.trim().equalsIgnoreCase("RemoveUser")) {
            try {
                String userIDsTemp = request.getParameter("userDeletionIDs");
                String [] userIDs = userIDsTemp.split(",");
                
                int [] userIntIDs = new int[userIDs.length];
                for(int i = 0; i < userIDs.length; i++)
                    userIntIDs[i] = Integer.parseInt(userIDs[i]);
                
                
                int userRemovalStatus = DatabaseManager.deleteUsers(userIntIDs,
                        admin);
                if (userRemovalStatus == userIntIDs.length) {
                    JSONObject obj = new JSONObject();
                    obj.put("status", "Successfully Deleted Users");
                    response.getWriter().append(obj.toJSONString());
                } else {
                    JSONObject obj = new JSONObject();
                    obj.put("status", userIntIDs.length - userRemovalStatus + " deletions failed, check the error logs");
                    response.getWriter().append(obj.toJSONString());
                }
            } catch (Exception e) {
                request.setAttribute("status", "Oops, an error occured. Check the error logs");
            }
        } /*
            Admin is setting the user's status to locked, preventing them from logging in
        
            If locking the user was successful or failed without an error,
            lockStatus is set.
            If an error arises, etcStatus is set with a suggested cause.
         */ else if (action.trim().equalsIgnoreCase("LockUser")) {
            try {
                String userIDsTemp = request.getParameter("userLockIDs");
                String [] userIDs = userIDsTemp.split(",");
                
                int [] userIntIDs = new int[userIDs.length];
                for(int i = 0; i < userIDs.length; i++)
                    userIntIDs[i] = Integer.parseInt(userIDs[i]);
                
                int successfulLocks = DatabaseManager.lockUser(userIntIDs,
                        admin);
                if (successfulLocks == userIntIDs.length) {
                    JSONObject obj = new JSONObject();
                    obj.put("status", "Locking Users Successful");
                    response.getWriter().append(obj.toJSONString());
                } else {
                    JSONObject obj = new JSONObject();
                    obj.put("status", userIntIDs.length - successfulLocks + " locks failed, check the error logs");
                    response.getWriter().append(obj.toJSONString());
                }
            } catch (Exception e) {
                request.setAttribute("status", "Error: " + e);
            }
        } /*
            Admin is unlocking a user, allowing them to log in once again
        
            If unlocking the user was successful or failed without an error,
            lockStatus is set.
            If an error arises, etcStatus is set with a suggested cause.
         */ else if (action.trim().equalsIgnoreCase("UnlockUser")) {
            try {
                String userIDsTemp = request.getParameter("userUnlockIDs");
                String [] userIDs = userIDsTemp.split(",");
                
                int [] userIntIDs = new int[userIDs.length];
                for(int i = 0; i < userIDs.length; i++)
                    userIntIDs[i] = Integer.parseInt(userIDs[i]);
                
                int successfulUnlocks = DatabaseManager.unlockUser(userIntIDs,
                        admin);
                if (successfulUnlocks == userIntIDs.length) {
                    JSONObject obj = new JSONObject();
                    obj.put("status", "Unlocking Users Successful");
                    response.getWriter().append(obj.toJSONString());
                } else {
                    JSONObject obj = new JSONObject();
                    obj.put("status", userIntIDs.length - successfulUnlocks + " unlocks failed, check the error logs");
                    response.getWriter().append(obj.toJSONString());
                }
            } catch (Exception e) {
                request.setAttribute("status", "Error: " + e);
            }
        } /*
            Gets the description of the parameter selected on the page
            and returns it
        
        else if (action.trim().equalsIgnoreCase("getParamDesc")) 
        {
            try
            {
                response.getWriter()
                        .append(DatabaseManager
                                .getDescription(request.getParameter("name"))
                                .toJSONString());
            }
            catch(Exception e)
            {
                JSONObject obj = new JSONObject();
                obj.put("status","Error editing description: " + e);
                response.getWriter().append(obj.toJSONString());
            }
        }
         */ /*
            Admin is editing the description of a certain data value
            
            If editing the description succeeded or failed without error,
            editDescStatus is set. 
            If an error arises, etcStatus is set with the exception message as
            there are no obvious reasons for it to fail.
         */ else if (action.trim().equalsIgnoreCase("editParamDesc")) {
            try {
                boolean editDescStatus = DatabaseManager.updateDescription((String) request.getParameter("desc"),
                        Long.parseLong(request.getParameter("desc_id")), (String) request.getParameter("name"));
                if (editDescStatus) {
                    JSONObject obj = new JSONObject();
                    obj.put("status", "Success");
                    response.getWriter().append(obj.toJSONString());
                } else {
                    JSONObject obj = new JSONObject();
                    obj.put("status", "Failed");
                    response.getWriter().append(obj.toJSONString());
                }
            } catch (Exception e) {
                request.setAttribute("editDescStatus", "Error editing description: " + e);
            }
        } //This will be the servlet's case for getting the json?
        /*
            Autogenerated request upon loading the tab. Gives an arraylist of 
            the ManualDataNames to populate the dropdowns for selecting
            which manual data type to insert or view for deletion.
         */ else if (action.trim().equalsIgnoreCase("getManualItems")) {
            Observable.just(0)
                    .flatMap(_ignored -> DatabaseManager.getManualParameterNames())
                    .observeOn(Schedulers.computation())
                    .map((String name) -> {
                        JSONObject wrappedName = new JSONObject();
                        wrappedName.put("name", name);
                        return wrappedName;
                    })
                    .buffer(Integer.MAX_VALUE)
                    .map((List<JSONObject> data) -> {
                        JSONArray wrappedData = new JSONArray();
                        wrappedData.addAll(data);
                        return wrappedData;
                    })
                    .map((JSONArray data) -> {
                        JSONObject root = new JSONObject();
                        root.put("data", data);
                        return root;
                    })
                    .defaultIfEmpty(EMPTY_RESULT)
                    .blockingSubscribe((JSONObject resp) -> {
                        response.getWriter().append(resp.toJSONString());
//                        System.out.println("Sent response...");
                    });

            /*
            //We'll change to use this next group meeting
            session.setAttribute("manualItems", DatabaseManager.getRemoteParameterNames());
             */
        } else if (action.trim().equalsIgnoreCase("getSensorItems")) {
            Observable.just(0)
                    .flatMap(_ignored -> DatabaseManager.getRemoteParameterNames())
                    .observeOn(Schedulers.computation())
                    .map((String name) -> {
                        JSONObject wrappedName = new JSONObject();
                        wrappedName.put("name", name);
                        return wrappedName;
                    })
                    .buffer(Integer.MAX_VALUE)
                    .map((List<JSONObject> data) -> {
                        JSONArray wrappedData = new JSONArray();
                        wrappedData.addAll(data);
                        return wrappedData;
                    })
                    .map((JSONArray data) -> {
                        JSONObject root = new JSONObject();
                        root.put("data", data);
                        return root;
                    })
                    .defaultIfEmpty(EMPTY_RESULT)
                    .blockingSubscribe((JSONObject resp) -> {
                        response.getWriter().append(resp.toJSONString());
//                        System.out.println("Sent response...");
                    });

            /*
            //We'll change to use this next group meeting
            session.setAttribute("manualItems", DatabaseManager.getRemoteParameterNames());
             */
        } /*
            Gets a list of data from the ManualDataValues table within a time range
            
            If the list retrieval succeeded, filteredData will be set.
            
            If it failed due to invalid LocalDateTime format, dateStatus is
            set.
         */ else if (action.trim().equalsIgnoreCase("getData")) {
            String parameter = request.getParameter("parameter");
            long start = Long.parseLong(request.getParameter("start"));
            long end = Long.parseLong(request.getParameter("end"));

            JSONObject empty = new JSONObject();
            empty.put("data", new JSONArray());

            Observable.just(parameter)
                    .flatMap(param -> DatabaseManager.getDataValues(Instant.ofEpochMilli(start), Instant.ofEpochMilli(end), param))
                    .groupBy(DataValue::getId)
                    .flatMap((GroupedObservable<Long, DataValue> gdv)
                            -> gdv.map((DataValue dv) -> {
                        JSONObject obj = new JSONObject();
                        obj.put("timestamp", dv.getTimestamp().getEpochSecond() * 1000);
                        obj.put("value", dv.getValue());
                        return obj;
                    })
                            .buffer(Integer.MAX_VALUE)
                            .map((List<JSONObject> data) -> {
                                JSONArray arr = new JSONArray();
                                arr.addAll(data);
                                return arr;
                            })
                            .flatMap((JSONArray arr)
                                    -> DatabaseManager.parameterIdToName(gdv.getKey())
//                                    .doOnNext(System.out::println)
                                    .map(name -> {
                                        JSONObject obj = new JSONObject();
                                        obj.put("dataValues", arr);
                                        obj.put("name", name);
                                        return obj;
                                    })
                            )
                    )
                    .buffer(Integer.MAX_VALUE)
                    .map(list -> {
                        JSONArray arr = new JSONArray();
                        arr.addAll(list);
                        return arr;
                    })
                    .map(arr -> {
                        JSONObject obj = new JSONObject();
                        obj.put("data", arr);
                        return obj;
                    })
                    .defaultIfEmpty(empty)
                    .blockingSubscribe(resp -> {
                        response.getWriter().append(resp.toJSONString());
//                        System.out.println("Sent response...");
                    });
            /*
            //We'll change to use this next group meeting
            //Gets a list of data values within a time range for display on a chart so the user can select which ones to delete
            String dataName = (String) request.getParameter("filterDataName"); //name of the data type to be filtered
            String lower = (String) request.getParameter("filterLower"); //lower time bound in LocalDateTime format of the data
            String upper = (String) request.getParameter("filterUpper"); //upper time bound in LocalDateTime format of the data
            try
            {
                session.setAttribute("filteredData", DatabaseManager.getManualData(dataName,LocalDateTime.parse(lower),LocalDateTime.parse(upper)));
            }
            catch(DateTimeParseException e)
            {
                session.setAttribute("dateStatus", "Invalid Format on Lower or Upper Time Bound.");
            }
             */
        }
         else if (action.trim().equalsIgnoreCase("getDataDeletion")) {
            String parameter = request.getParameter("parameter");
            long start = Long.parseLong(request.getParameter("start"));
            long end = Long.parseLong(request.getParameter("end"));

            JSONObject empty = new JSONObject();
            empty.put("data", new JSONArray());

            Observable.just(parameter)
                    .flatMap(param -> DatabaseManager.getDataValues(Instant.ofEpochMilli(start), Instant.ofEpochMilli(end), param))
                    .observeOn(Schedulers.computation())
                    .groupBy(DataValue::getId)
                    .flatMap((GroupedObservable<Long, DataValue> gdv)
                            -> gdv.map((DataValue dv) -> {
                        JSONObject obj = new JSONObject();
                        obj.put("timestamp", dv.getTimestamp().toEpochMilli());
                        obj.put("value", dv.getValue());
                        return obj;
                    })
                            .buffer(Integer.MAX_VALUE)
                            .map((List<JSONObject> data) -> {
                                JSONArray arr = new JSONArray();
                                arr.addAll(data);
                                return arr;
                            })
                            .flatMap((JSONArray arr)
                                    -> DatabaseManager.parameterIdToName(gdv.getKey())
//                                    .doOnNext(System.out::println)
                                    .map(name -> {
                                        JSONObject obj = new JSONObject();
                                        obj.put("dataValues", arr);
                                        obj.put("name", name);
                                        return obj;
                                    })
                            )
                    )
                    .buffer(Integer.MAX_VALUE)
                    .map(list -> {
                        JSONArray arr = new JSONArray();
                        arr.addAll(list);
                        return arr;
                    })
                    .map(arr -> {
                        JSONObject obj = new JSONObject();
                        obj.put("data", arr);
                        return obj;
                    })
                    .defaultIfEmpty(EMPTY_RESULT)
                    .blockingSubscribe(resp -> {
                        response.getWriter().append(resp.toJSONString());
//                        System.out.println("Sent response...");
                    });
            /*
            //We'll change to use this next group meeting
            //Gets a list of data values within a time range for display on a chart so the user can select which ones to delete
            String dataName = (String) request.getParameter("filterDataName"); //name of the data type to be filtered
            String lower = (String) request.getParameter("filterLower"); //lower time bound in LocalDateTime format of the data
            String upper = (String) request.getParameter("filterUpper"); //upper time bound in LocalDateTime format of the data
            try
            {
                session.setAttribute("filteredData", DatabaseManager.getManualData(dataName,LocalDateTime.parse(lower),LocalDateTime.parse(upper)));
            }
            catch(DateTimeParseException e)
            {
                session.setAttribute("dateStatus", "Invalid Format on Lower or Upper Time Bound.");
            }
             */
        }else if (action.trim().equalsIgnoreCase("getParameters")) {
            long type = Long.parseLong(request.getParameter("data"));

            Observable.just(type)
                    // Bit 1 is SENSOR, bit 2 is MANUAL; Client can construct a mask by OR'ing them together.
                    .flatMap(typ -> Observable.merge(
                    (typ & 0x1) != 0 ? DatabaseManager.getRemoteParameterNames()
                                    .flatMap(name -> DatabaseManager.parameterNameToId(name)
                                    .flatMap(id -> DatabaseManager.getDescription(id)
                                    .map(descr -> Quartet.with(1, id, name, descr))
                                    )
                                    ) : Observable.empty(),
                    (typ & 0x2) != 0 ? DatabaseManager.getManualParameterNames()
                                    .flatMap(name -> DatabaseManager.parameterNameToId(name)
                                    .flatMap(id -> DatabaseManager.getDescription(id)
                                    .map(descr -> Quartet.with(2, id, name, descr))
                                    )
                                    ) : Observable.empty()
            ))
                    .groupBy(Quartet::getValue0, Quartet::removeFrom0)
                    .flatMap(group -> group
                    .sorted((t1, t2) -> t1.getValue1().compareTo(t2.getValue1()))
                    .map(triplet -> {
                        JSONObject obj = new JSONObject();
                        obj.put("id", triplet.getValue0());
                        obj.put("name", triplet.getValue1());
                        obj.put("unit", DatabaseManager.getDataParameter(triplet.getValue0()));
                        obj.put("description", triplet.getValue2());
                        return obj;
                    })
                    .buffer(Integer.MAX_VALUE)
                    .map(JSONUtils::toJSONArray)
                    .map(arr -> {
                        JSONObject obj = new JSONObject();
                        obj.put("mask", group.getKey());
                        obj.put("descriptors", arr);
                        return obj;
                    })
                    )
                    // Collect both SENSOR and/or REMOTE data into a JSONArray
                    .buffer(Integer.MAX_VALUE)
                    .map(JSONUtils::toJSONArray)
                    // Add to the root JSONObject's 'data' field.
                    .map(arr -> {
                        JSONObject obj = new JSONObject();
                        obj.put("data", arr);
                        return obj;
                    })
                    // If NOTHING has been found (I.E: User gave bits that were not implemented), we return
                    // an empty JSONObject in a format similar enough to not cause a crash (if implemented correctly
                    // by the front-end).
                    .defaultIfEmpty(EMPTY_RESULT)
                    // Send response.
                    .blockingSubscribe(resp -> {
                        response.getWriter().append(resp.toJSONString());
//                        System.out.println("Sent response...");
                    });

        } else if (action.trim().equalsIgnoreCase("insertData")) {
            
            Observable.just(request.getParameter("data"))
                    .map(req -> (JSONArray) new JSONParser().parse(req))
                    .flatMap(JSONUtils::flattenJSONArray)
//                    .doOnNext(System.out::println)
                    .flatMap(obj -> Observable.just(obj)
                            .map(o -> (JSONArray) o.get("values"))
                            .flatMap(JSONUtils::flattenJSONArray)
                            .filter(o -> o.get("timestamp") != null && o.get("value") != null)
                            .flatMap(o -> DatabaseManager
                                    .parameterNameToId((String) obj.get("name"))
                                    .map(id -> new DataValue(id, Instant.ofEpochMilli((long) o.get("timestamp")), o.get("value") != null ? Double.parseDouble(o.get("value").toString()) : Double.NaN))
                            )
                    )
                    .buffer(Integer.MAX_VALUE)
                    .doOnNext(System.out::println)
                    .flatMap(DatabaseManager::insertManualData)
                    .blockingSubscribe(updated -> System.out.println("Updated # of Rows: " + updated));
                    
        }
        else if (action.trim().equalsIgnoreCase("deleteManualData")) 
        {
            try
            {
                ArrayList<Integer> deletionIDs = (ArrayList) session.getAttribute("deletionIDs");
                for (Integer i : deletionIDs) {
                    DatabaseManager.manualDeletionM(i.intValue(), admin);
                }
            } catch (Exception e) {
                request.setAttribute("status", "Error: Did you not select anything for deletion?");
            }
        } /*
            Retrieves a list of all Users
        
            If it succeeds, errorList is set with an ArrayList of ErrorMessages
            If it fails, etcStatus is set with the exception message as there 
            are no obvious reasons for failure.
         */ else if (action.trim().equalsIgnoreCase("getUserList")) {
            try {
                response.getWriter()
                        .append(DatabaseManager
                                .getUsers()
                                .toJSONString());
            } catch (Exception e) {
                JSONObject obj = new JSONObject();
                obj.put("status", "Error getting user list: " + e);
                response.getWriter().append(obj.toJSONString());
            }
        } /*
            Retrieves a list of all Errors
        
            If it succeeds, it appends the JSONString holding all the errors
            to the response's writer.
         */ else if (action.trim().equalsIgnoreCase("getAllErrors")) {
            try {
                response.getWriter()
                        .append(DatabaseManager
                                .getErrors()
                                .toJSONString());
            } catch (Exception e) {
                JSONObject obj = new JSONObject();
                obj.put("status", "Error getting error list: " + e);
                response.getWriter().append(obj.toJSONString());
            }
        } /*
            Retrieves a list of all Errors within a time range
        
            If it succeeds, it appends the JSONString holding all the errors
            to the response's writer.
        
            If it fails and a DateTimeParseException is caught, dateStatus is
            set to inform the user that their datetime format is incorrect.
            
            If it fails with any other error, etcStatus is set with the exception 
            message as there are no other obvious reasons for failure.
         */ else if (action.trim().equalsIgnoreCase("getFilteredErrors")) {
            try {
                response.getWriter()
                        .append(DatabaseManager
                                .getErrorsInRange(Instant.ofEpochMilli(Long.parseLong(request.getParameter("start"))).toString().substring(0,19),
                                        Instant.ofEpochMilli(Long.parseLong(request.getParameter("end"))).toString().substring(0,19))
                                .toJSONString());
            } catch (DateTimeParseException e) {
                JSONObject obj = new JSONObject();
                obj.put("status", "Invalid Format on Time");
                response.getWriter().append(obj.toJSONString());
            } catch (Exception e) {
                JSONObject obj = new JSONObject();
                obj.put("status", "Error getting error list: " + e);
                response.getWriter().append(obj.toJSONString());
            }
        }
        
        else if (action.trim().equalsIgnoreCase("insertCSV"))
        {
            int count = 0;
            System.out.println("Received - c: " + count++);
        }
        
        else if (action.trim().equalsIgnoreCase("getRoles"))
        {
            response.getWriter().append(UserRole.getUserRoles().toJSONString());
        }
        else if (action.trim().equalsIgnoreCase("logout")) {
            session.removeAttribute("user");//logout on server
            
            session.invalidate();//clear session
            //write the response as JSON. assume success.
            JSONObject obj = new JSONObject();
            JSONObject jObjStatus = new JSONObject();
            jObjStatus.put("errorCode", "0");
            jObjStatus.put("errorMsg", "Logout successful.");
            obj.put("status", jObjStatus);
            response.getWriter().append(obj.toJSONString());
        }
        else if(action.trim().equalsIgnoreCase("isUserLoggedIn"))
        {
            JSONObject obj = new JSONObject();
            if(admin == null)
            {
                obj.put("isLoggedIn", 0);
                obj.put("isAdmin", 0);
            }
            else
            {
                obj.put("isLoggedIn", 1);
                if(admin.getUserRole() == UserRole.SystemAdmin)
                {
                    obj.put("isAdmin", 1);
                }
                else
                    obj.put("isAdmin", 0);
            }
            response.getWriter().append(obj.toJSONString());
        } else if (action.trim().equalsIgnoreCase("getBayesianCSV")) {
            Long start = Long.parseLong(request.getParameter("startDate"));
            Long end = Long.parseLong(request.getParameter("endDate"));
            System.out.println("start: " + start + ", end: " + end);
            
            long PAR = 637957793;
            long HDO = 1050296639;
            long Temp = 1050296629;
            long Pressure = 639121405;
            long Depth = 1050296637;

            dataToCSV(DataReceiver
                            .getRemoteData(
                                    Instant
                                            .ofEpochMilli(start)
                                            .truncatedTo(ChronoUnit.DAYS), 
                                    Instant.ofEpochMilli(end)
                                            .truncatedTo(ChronoUnit.DAYS), 
                                    PAR, HDO, Temp, Pressure, Depth
                            ))
                    .subscribeOn(Schedulers.computation())
                    .blockingSubscribe(resp -> 
                            response
                            .getWriter()
                            .append(resp)
                    );
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
