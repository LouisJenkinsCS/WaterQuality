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
                    .defaultIfEmpty(empty)
                    .blockingSubscribe(resp -> {
                        response.getWriter().append(resp.toJSONString());
                        System.out.println("Sent response...");
                    });
        }
        
        else if (action.trim().equalsIgnoreCase("getParameters")) {
            // User sends the types of data parameters they want, SENSOR | MANUAL
            long type = Long.parseLong(request.getParameter("data"));
            
            Observable.just(type)
                    // Bit 1 is SENSOR, bit 2 is MANUAL; Client can construct a mask by OR'ing them together.
                    .flatMap(typ -> Observable.merge(
                            // SENSOR - Get all remote names
                            (typ & 0x1) != 0 ? DatabaseManager.getRemoteParameterNames()
                                    // From name, obtain their identifier
                                    .flatMap(name -> DatabaseManager.parameterNameToId(name)
                                            // From identifier, obtain their description
                                            .flatMap(id -> DatabaseManager.getDescription(id)
                                                    // With all of the above, create a tuple that contains them as well as the bit set.
                                                    // The bit is used to group everything during later computation
                                                    .map(descr -> Quartet.with(1, id, name, descr))
                                            )
                                            // When there is nothing, we return nothing.
                                    ) : Observable.empty(),
                            // MANUAL - Same as the above, except the bit set here in 2 instead of 1.
                            (typ & 0x2) != 0 ? DatabaseManager.getManualParameterNames()
                                    .flatMap(name -> DatabaseManager.parameterNameToId(name)
                                            .flatMap(id -> DatabaseManager.getDescription(id)
                                                    .map(descr -> Quartet.with(2, id, name, descr))
                                            )
                                    ) : Observable.empty()
                    ))
                    // Group each the data above by it's bit set; this is so that we can split them
                    // into two separate JSONObjects with their own 'mask' and 'descriptors'. We also drop
                    // the bit, as the bit can be obtained by using the 'key' of the group.
                    // Reminder: (bit, id, name, description) becomes (id, name, description)
                    .groupBy(Quartet::getValue0, Quartet::removeFrom0)
                    .flatMap(group -> group
                            // We sort for the front-end so it is easier to process
                            .sorted((t1, t2) -> t1.getValue1().compareTo(t2.getValue1()))
                            // Construct the JSONObject representation of the 3-tuple (id, name, description)
                            .map(triplet -> {
                                JSONObject obj = new JSONObject();
                                obj.put("id", triplet.getValue0());
                                obj.put("name", triplet.getValue1());
                                obj.put("description", triplet.getValue2());
                                return obj;  
                            })
                            // Collect all of them into a JSONArray
                            .buffer(Integer.MAX_VALUE)
                            .map(JSONUtils::toJSONArray)
                            // Now, we have a JSONArray of all the above in descriptors, as per protocol.
                            // The mask is the bit set so the front-end knows what set this belongs to.
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
                        System.out.println("Sent response...");
                    });     
        }
        else if (action.trim().equalsIgnoreCase("insertData")) {
            
            Observable.just(request.getParameter("data"))
                    .map(req -> (JSONObject) new JSONParser().parse(req))
                    .map(obj -> (JSONArray) obj.get("data"))
                    .flatMap(JSONUtils::flattenJSONArray)
                    .flatMap(obj -> Observable.just(obj)
                            .map(o -> (JSONArray) o.get("values"))
                            .flatMap(JSONUtils::flattenJSONArray)
                            .flatMap(o -> DatabaseManager
                                    .parameterNameToId((String) o.get("name"))
                                    .map(id -> new DataValue(id, Instant.ofEpochMilli((long) o.get("timestamp")), (double) o.get("value")))
                            )
                    )
                    // Not Implemented
                    .subscribe(dv -> System.out.println("Insert for: " + dv));
                    
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
