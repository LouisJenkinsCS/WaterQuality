/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import common.UserRole;
import database.DatabaseManager;
import static database.DatabaseManager.LogError;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import protocol.JSONProtocol;
import utilities.FileUtils;

/**
 *
 * @author Tyler Mutzek
 */
@WebServlet(name = "AdminServlet", urlPatterns = {"/AdminServlet"})
public class AdminServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);//Create a new session if one does not exists
        final Object lock = session.getId().intern();
        session.setAttribute("user", new common.User());
        common.User admin = (common.User) session.getAttribute("user");
        String action = request.getParameter("action");
        
        /*
            Admin is manually inputting data into the ManualDataValues table
        
            If data is parsed and the input succeeds or fails, inputstatus is set
            If the data fails to parse, input status will remain null so check
            if dateStatus, numberStatus, and etcStatus if they are not null and
            print whatever isn't null so the user can see what they did wrong.
        */
        if (action.trim().equalsIgnoreCase("InputData")) 
        {
            try
            { 
                boolean inputStatus = DatabaseManager.manualInput((String) request.getParameter("dataName"),
                    (String) request.getParameter("units"), LocalDateTime.parse((String) request.getParameter("time")),
                    Float.parseFloat((String) request.getParameter("value")), Integer.parseInt((String) request.getParameter("id")), 
                    admin);
                if (inputStatus) 
                {
                    request.setAttribute("inputStatus", "Data Input Successful");
                } 
                else 
                {
                    request.setAttribute("inputStatus", "Data Input Unsuccessful. Check your syntax");
                }
            }
            catch(DateTimeParseException e)
            {
                request.setAttribute("dateStatus","Invalid Format on Time");
            }
            catch(NumberFormatException e)
            {
                request.setAttribute("numberStatus","Value or ID is not a valid number");
            }
            catch(Exception e)
            {
                request.setAttribute("etcStatus","Extraneous Error: Are all the text fields not empty?");
            }
            
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
                request.setAttribute("etcStatus","Error: Did you not check any boxes for deletion?");
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
                    session.setAttribute("inputStatus", "New User Registration Successful");
                } 
                else 
                {
                    session.setAttribute("inputStatus", "New User Registration *Unsuccessful. Check your syntax");
                }
            }
            catch(Exception e)
            {
                request.setAttribute("etcStatus","Error registering user: " + e);
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
                    session.setAttribute("userDeletionStatus", "User Deletion Successful");
                } 
                else 
                {
                    session.setAttribute("userDeletionStatus", "User Deletion Unsuccessful");
                }
            }
            catch(Exception e)
            {
                request.setAttribute("etcStatus","Error: Did you not check any boxes for deletion?");
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
                request.setAttribute("etcStatus","Error: Did you not check any boxes for locking?");
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
                request.setAttribute("etcStatus","Error: Did you not check any boxes for unlocking?");
            }
        } 
        
        /*
            Admin is editing the description of a certain data value
            
            If editing the description succeeded or failed without error,
            editDescStatus is set. 
            If an error arises, etcStatus is set with the exception message as
            there are no obvious reasons for it to fail.
        */
        else if (action.trim().equalsIgnoreCase("EditDesc")) 
        {
            try
            {
                boolean editDescStatus = DatabaseManager.updateDescription((String) request.getParameter("description"),
                    (String) request.getParameter("dataName"));
                if (editDescStatus) 
                {
                    session.setAttribute("editDescStatus", "Description Update Successful");
                } 
                else 
                {
                    session.setAttribute("editDescStatus", "Description Update Unsuccessful");
                }
            }
            catch(Exception e)
            {
                request.setAttribute("etcStatus","Error editing description: " + e);
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
            JSONParser parser = new JSONParser();
            try
            {
                Object obj = parser.parse(FileUtils.readAll("resources/manual_entry_items.json"));
                JSONObject jObj = (JSONObject)obj;
                response.getWriter().append(jObj.toJSONString());
            }
            catch(Exception e)
            {
                DatabaseManager.LogError("Something went wrong..." + e.toString());
            }
            /*
            //We'll change to use this next group meeting
            session.setAttribute("manualItems", DatabaseManager.getManualDataNames());
            */
        }
        
        /*
            Gets a list of data from the ManualDataValues table within a time range
            
            If the list retrieval succeeded, filteredData will be set.
            
            If it failed due to invalid LocalDateTime format, dateStatus is
            set.
        */
        else if (action.trim().equalsIgnoreCase("getFilteredData")) 
        {
            JSONParser parser = new JSONParser();
            try
            {
                Object obj = parser.parse(FileUtils.readAll("resources/manual_entry_items.json"));
                
                JSONObject jObj = (JSONObject)obj;
                
                response.getWriter().append(jObj.toJSONString());
            }
            catch(Exception e)
            {
                DatabaseManager.LogError("Something went wrong..." + e.toString());
            }
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
        
        /*
            Deletes a number of data values from the ManualDataValues table
            with ids specified by an ArrayList of Integer IDs
        
            If it succeeds the data is deleted with no message displayed.
            If it fails, etcStatus is set with a possible cause.
        */
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
                request.setAttribute("etcStatus","Error: Did you not check any boxes for deletion?");
            }
        }
        
        /*
            Retrieves a list of all Errors
        
            If it succeeds, errorList is set with an ArrayList of ErrorMessages
            If it fails, etcStatus is set with the exception message as there 
            are no obvious reasons for failure.
        */
        else if (action.trim().equalsIgnoreCase("getAllErrors")) 
        {
            try
            {
                session.setAttribute("errorList", DatabaseManager.getErrors());
            }
            catch(Exception e)
            {
                request.setAttribute("etcStatus","Error getting error list: " + e);
            }
        }
        
        /*
            Retrieves a list of all Errors within a time range
        
            If it succeeds, errorList is set with an ArrayList of ErrorMessages
        
            If it fails and a DateTimeParseException is caught, dateStatus is
            set to inform the user that their datetime format is incorrect.
            
            If it fails with any other error, etcStatus is set with the exception 
            message as there are no other obvious reasons for failure.
        */
        else if (action.trim().equalsIgnoreCase("getFilteredErrors")) 
        {
            try
            {
                session.setAttribute("errorList", DatabaseManager.getErrorsInRange(
                    LocalDateTime.parse((String) request.getAttribute("filterLower")), 
                    LocalDateTime.parse((String) request.getAttribute("filterUpper"))
                    ));
            }
            catch(DateTimeParseException e)
            {
                request.setAttribute("dateStatus","Invalid Format on Time");
            }
            catch(Exception e)
            {
                request.setAttribute("etcStatus","Error getting error list: " + e);
            }
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
