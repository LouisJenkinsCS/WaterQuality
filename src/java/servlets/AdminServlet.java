/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import async.DataValue;
import common.UserRole;
import database.DatabaseManager;
import io.reactivex.Observable;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
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
        log("Action is: " + action);
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
        if (action.trim().equalsIgnoreCase("InputData")) 
        {
            String dataName = request.getParameter("dataName");
            
        } 
        
        /*
            Admin is deleting single pieces of data from the DataValues table
            
            If the deletion succeeds or fails without causing an error, 
            dataDeletionStatus is set.
            If an error arises, etcStatus is set with a suggested cause
        */
        else if (action.trim().equalsIgnoreCase("RemoveData")) 
        {
            try
            {
                boolean dataRemovalStatus = DatabaseManager.manualDeletion(Integer.parseInt((String) request.getParameter("dataDeletionID")),
                    admin);
                if (dataRemovalStatus) 
                {
                    session.setAttribute("dataDeletionStatus", "Data Deletion Successful");
                } 
                else 
                {
                    session.setAttribute("dataDeletionStatus", "Data Deletion Unsuccessful");
                }
            }
            catch(Exception e)
            {
                request.setAttribute("dataDeletionStatus","Error: Did you not check any boxes for deletion?");
            }
        } 
        
        /*
            Admin is registering a new user to the Users table
            
            If registering the user succeeds or fails without an error, 
            inputStatus is set.
            If an error arises, etcStatus is set with the exception message as
            there are no obvious reasons for it to fail.
        */
        else if (action.trim().equalsIgnoreCase("RegisterUser")) 
        {
            try
            {
                boolean newUserStatus = DatabaseManager.addNewUser((String) request.getParameter("username"),
                    (String) request.getParameter("password"), (String) request.getParameter("firstName"),
                    (String) request.getParameter("lastName"), (String) request.getParameter("email"),
                    UserRole.getUserRole((String) request.getParameter("userRole")),
                    admin);
                if (newUserStatus) 
                {
                    JSONObject obj = new JSONObject();
                    obj.put("status","Success");
                    response.getWriter().append(obj.toJSONString());
                } 
                else 
                {
                    JSONObject obj = new JSONObject();
                    obj.put("status","Failed");
                    response.getWriter().append(obj.toJSONString());
                }
            }
            catch(Exception e)
            {
                request.setAttribute("status","Error registering user: " + e);
            }
        } 
        
        /*
            Admin is deleting a user from the Users table
            
            If the deletion succeeds or fails with no error, userDeletionStatus
            is set.
            If an error arises, etcStatus is set with a suggested cause.
        */
        else if (action.trim().equalsIgnoreCase("RemoveUser")) 
        {
            try
            {
                boolean userRemovalStatus = DatabaseManager.deleteUser(Integer.parseInt((String) request.getParameter("userDeletionID")),
                    admin);
                if (userRemovalStatus) 
                {
                    JSONObject obj = new JSONObject();
                    obj.put("status","Success");
                    response.getWriter().append(obj.toJSONString());
                } 
                else 
                {
                    JSONObject obj = new JSONObject();
                    obj.put("status","Failed");
                    response.getWriter().append(obj.toJSONString());
                }
            }
            catch(Exception e)
            {
                request.setAttribute("userDeletionStatus","Error: Did you not check any boxes for deletion?");
            }
        } 
        
        /*
            Admin is setting the user's status to locked, preventing them from logging in
        
            If locking the user was successful or failed without an error,
            lockStatus is set.
            If an error arises, etcStatus is set with a suggested cause.
        */
        else if (action.trim().equalsIgnoreCase("LockUser")) 
        {
            try
            {
                boolean lockStatus = DatabaseManager.deleteUser(Integer.parseInt((String) request.getParameter("userLockID")),
                    admin);
                if (lockStatus) 
                {
                    session.setAttribute("lockStatus", "User Deletion Successful");
                } 
                else 
                {
                    session.setAttribute("lockStatus", "User Deletion Unsuccessful");
                }
            }
            catch(Exception e)
            {
                request.setAttribute("lockStatus","Error: Did you not check any boxes for locking?");
            }
        } 
        
        /*
            Admin is unlocking a user, allowing them to log in once again
        
            If unlocking the user was successful or failed without an error,
            lockStatus is set.
            If an error arises, etcStatus is set with a suggested cause.
        */
        else if (action.trim().equalsIgnoreCase("UnlockUser")) 
        {
            try
            {
                boolean unlockStatus = DatabaseManager.deleteUser(Integer.parseInt((String) request.getParameter("userUnlockID")),
                    admin);
                if (unlockStatus) 
                {
                    session.setAttribute("unlockStatus", "User Unlock Successful");
                } 
                else 
                {
                    session.setAttribute("unlockStatus", "User Unlock Unsuccessful");
                }
            }
            catch(Exception e)
            {
                request.setAttribute("unlockStatus","Error: Did you not check any boxes for unlocking?");
            }
        } 
        
        /*
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
        */
        
        /*
            Admin is editing the description of a certain data value
            
            If editing the description succeeded or failed without error,
            editDescStatus is set. 
            If an error arises, etcStatus is set with the exception message as
            there are no obvious reasons for it to fail.
        */
        else if (action.trim().equalsIgnoreCase("editParamDesc")) 
        {
            try
            {
                boolean editDescStatus = DatabaseManager.updateDescription((String) request.getParameter("desc"),
                    Long.parseLong(request.getParameter("desc_id")));
                if (editDescStatus) 
                {
                    JSONObject obj = new JSONObject();
                    obj.put("status","Success");
                    response.getWriter().append(obj.toJSONString());
                } 
                else 
                {
                    JSONObject obj = new JSONObject();
                    obj.put("status","Failed");
                    response.getWriter().append(obj.toJSONString());
                }
            }
            catch(Exception e)
            {
                request.setAttribute("editDescStatus","Error editing description: " + e);
            }
        }
        
        //This will be the servlet's case for getting the json?
        /*
            Autogenerated request upon loading the tab. Gives an arraylist of 
            the ManualDataNames to populate the dropdowns for selecting
            which manual data type to insert or view for deletion.
        */
        else if (action.trim().equalsIgnoreCase("getManualItems")) 
        {
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
                        System.out.println("Sent response...");
                    });
                                        
            /*
            //We'll change to use this next group meeting
            session.setAttribute("manualItems", DatabaseManager.getRemoteParameterNames());
            */
        }
        
        
        else if (action.trim().equalsIgnoreCase("getSensorItems")) 
        {
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
                        System.out.println("Sent response...");
                    });
                                        
            /*
            //We'll change to use this next group meeting
            session.setAttribute("manualItems", DatabaseManager.getRemoteParameterNames());
            */
        }
        
        /*
            Gets a list of data from the ManualDataValues table within a time range
            
            If the list retrieval succeeded, filteredData will be set.
            
            If it failed due to invalid LocalDateTime format, dateStatus is
            set.
        */
        else if (action.trim().equalsIgnoreCase("getData")) 
        {
            String parameter = request.getParameter("parameter");
            long start = Long.parseLong(request.getParameter("start"));
            long end = Long.parseLong(request.getParameter("end"));
            
            JSONObject empty = new JSONObject();
            empty.put("data", new JSONArray());
            
            Observable.just(parameter)
                    .flatMap(param -> DatabaseManager.getDataValues(Instant.ofEpochMilli(start), Instant.ofEpochMilli(end), param))
                    .observeOn(Schedulers.computation())
                    .groupBy(DataValue::getId)
                    .flatMap((GroupedObservable<Long, DataValue> gdv) -> 
                            gdv.map((DataValue dv) -> {
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
                            .flatMap((JSONArray arr) ->
                                
                                DatabaseManager.parameterIdToName(gdv.getKey())
                                        .doOnNext(System.out::println)
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
                        System.out.println("Sent response...");
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
        
        else if (action.trim().equalsIgnoreCase("getParameters")) {
            long type = Long.parseLong(request.getParameter("data"));
            
            Observable.just(type)
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
                    .buffer(Integer.MAX_VALUE)
                    .map(JSONUtils::toJSONArray)
                    .map(arr -> {
                        JSONObject obj = new JSONObject();
                        obj.put("data", arr);
                        return obj;
                    })
                    .defaultIfEmpty(EMPTY_RESULT)
                    .doOnNext(System.out::println)
                    .blockingSubscribe(resp -> {
                        response.getWriter().append(resp.toJSONString());
                        System.out.println("Sent response...");
                    });
                    
                    
                    
        }
        else if (action.trim().equalsIgnoreCase("insertData")) {
//            System.out.println("Test insertion got here");
//            request.getParameterMap().keySet().forEach(System.out::println);
            Observable.just(request.getParameter("data"))
                    .map(req -> (JSONArray) new JSONParser().parse(req))
                    .flatMap(JSONUtils::flattenJSONArray)
                    .doOnNext(System.out::println)
                    .flatMap(obj -> Observable.just(obj)
                            .map(o -> (JSONArray) o.get("values"))
                            .flatMap(JSONUtils::flattenJSONArray)
                            .filter(o -> o.get("timestamp") != null && o.get("value") != null)
                            .doOnNext(System.out::println)
                            .flatMap(o -> DatabaseManager
                                    .parameterNameToId((String) obj.get("name"))
                                    .map(id -> new DataValue(id, Instant.ofEpochMilli((long) o.get("timestamp")), o.get("value") != null ? Double.parseDouble(o.get("value").toString()) : Double.NaN))
                            )
                    )
                    .buffer(Integer.MAX_VALUE)
                    .flatMap(DatabaseManager::insertManualData)
                    .blockingSubscribe(count -> System.out.println("Inserted " + count + " fields..."));
                    
        }
        else if (action.trim().equalsIgnoreCase("deleteManualData")) 
        {
            try
            {
                ArrayList<Integer> deletionIDs = (ArrayList) session.getAttribute("deletionIDs");
                for(Integer i: deletionIDs)
                {
                    DatabaseManager.manualDeletionM(i.intValue(), admin);
                }
            }
            catch(Exception e)
            {
                request.setAttribute("manualDeleteStatus","Error: Did you not check any boxes for deletion?");
            }
        }
        
        /*
            Retrieves a list of all Users
        
            If it succeeds, errorList is set with an ArrayList of ErrorMessages
            If it fails, etcStatus is set with the exception message as there 
            are no obvious reasons for failure.
        */
        else if (action.trim().equalsIgnoreCase("getUserList")) 
        {
            try
            {
                response.getWriter()
                        .append(DatabaseManager
                        .getUsers()
                        .toJSONString());
            }
            catch(Exception e)
            {
                JSONObject obj = new JSONObject();
                obj.put("status","Error getting user list: " + e);
                response.getWriter().append(obj.toJSONString());
            }
        }
        
        /*
            Retrieves a list of all Errors
        
            If it succeeds, it appends the JSONString holding all the errors
            to the response's writer.
        */
        else if (action.trim().equalsIgnoreCase("getAllErrors")) 
        {
            try
            {
                response.getWriter()
                        .append(DatabaseManager
                        .getErrors()
                        .toJSONString());
            }
            catch(Exception e)
            {
                JSONObject obj = new JSONObject();
                obj.put("status","Error getting error list: " + e);
                response.getWriter().append(obj.toJSONString());
            }
        }
        
        /*
            Retrieves a list of all Errors within a time range
        
            If it succeeds, it appends the JSONString holding all the errors
            to the response's writer.
        
            If it fails and a DateTimeParseException is caught, dateStatus is
            set to inform the user that their datetime format is incorrect.
            
            If it fails with any other error, etcStatus is set with the exception 
            message as there are no other obvious reasons for failure.
        */
        else if (action.trim().equalsIgnoreCase("getFilteredErrors")) 
        {
            try
            {
                response.getWriter()
                        .append(DatabaseManager
                        .getErrorsInRange(Instant.ofEpochMilli(Long.parseLong(request.getParameter("start"))).toString(),
                                 Instant.ofEpochMilli(Long.parseLong(request.getParameter("end"))).toString())
                                .toJSONString());
            }
            catch(DateTimeParseException e)
            {
                JSONObject obj = new JSONObject();
                obj.put("status","Invalid Format on Time");
                response.getWriter().append(obj.toJSONString());
            }
            catch(Exception e)
            {
                JSONObject obj = new JSONObject();
                obj.put("status","Error getting error list: " + e);
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
        
        else if (action.trim().equalsIgnoreCase("getParameters"))
        {
            response.getWriter().append("Response");
        }
        
        //could also be done in LogoutServlet instead?
        else if (action.trim().equalsIgnoreCase("logout"))
        {
            session.removeAttribute("user");//logout on server
            session.invalidate();//clear session
            //write the response as JSON. assume success.
            JSONObject obj = new JSONObject();
            JSONObject jObjStatus = new JSONObject();
            jObjStatus.put("errorCode", "0");
            jObjStatus.put("errorMsg", "Logout successful.");
            obj.put("status",jObjStatus);
            response.getWriter().append(obj.toJSONString());
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
