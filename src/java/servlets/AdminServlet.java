/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import common.UserRole;
import database.DatabaseManager;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
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
        DatabaseManager d = new DatabaseManager();
        session.setAttribute("user", new common.User());
        common.User admin = (common.User) session.getAttribute("user");
        String action = request.getParameter("action");

        if (action.trim().equalsIgnoreCase("InputData")) {
            System.out.println(request.getParameter("dataName"));
            System.out.println(request.getParameter("units"));
            System.out.println(request.getParameter("value"));
            System.out.println(request.getParameter("id"));
            System.out.println(request.getParameter("time"));
            System.out.println(request.getParameter("delta"));
            System.out.println(admin.getFirstName());
            System.out.println(admin.getLastName());
            boolean inputStatus = d.manualInput((String) request.getParameter("dataName"),
                    (String) request.getParameter("units"), LocalDateTime.parse((String) request.getParameter("time")),
                    Float.parseFloat((String) request.getParameter("value")), Float.parseFloat((String) request.getParameter("delta")),
                    Integer.parseInt((String) request.getParameter("id")), admin);
            if (inputStatus) {
                request.setAttribute("inputStatus", "Data Input Successful");
            } else {
                request.setAttribute("inputStatus", "Data Input Unsuccessful. Check your syntax");
            }
        } else if (action.trim().equalsIgnoreCase("RemoveData")) {
            boolean dataRemovalStatus = d.manualDeletion((int) session.getAttribute("dataDeletionID"),
                    admin);
            if (dataRemovalStatus) {
                session.setAttribute("dataDeletionStatus", "Data Deletion Successful");
            } else {
                session.setAttribute("dataDeletionStatus", "Data Deletion Unsuccessful");
            }

        } else if (action.trim().equalsIgnoreCase("RegisterUser")) {
            boolean newUserStatus = d.addNewUser((String) session.getAttribute("username"),
                    (String) session.getAttribute("password"), (String) session.getAttribute("firstName"),
                    (String) session.getAttribute("lastName"), (String) session.getAttribute("email"),
                    UserRole.getUserRole((String) session.getAttribute("userRole")),
                    admin);
            if (newUserStatus) {
                session.setAttribute("Status", "New User Registration Successful");
            } else {
                session.setAttribute("inputStatus", "New User Registration *Unsuccessful. Check your syntax");
            }
        } else if (action.trim().equalsIgnoreCase("RemoveUser")) {
            boolean userRemovalStatus = d.deleteUser((int) session.getAttribute("userDeletionID"),
                    admin);
            if (userRemovalStatus) {
                session.setAttribute("userDeletionStatus", "User Deletion Successful");
            } else {
                session.setAttribute("userDeletionStatus", "User Deletion Unsuccessful");
            }
        } else if (action.trim().equalsIgnoreCase("LockUser")) {
            boolean lockStatus = d.deleteUser((int) session.getAttribute("userLockID"),
                    admin);
            if (lockStatus) {
                session.setAttribute("lockStatus", "User Deletion Successful");
            } else {
                session.setAttribute("lockStatus", "User Deletion Unsuccessful");
            }
        } else if (action.trim().equalsIgnoreCase("UnlockUser")) {
            boolean unlockStatus = d.deleteUser((int) session.getAttribute("userUnlockID"),
                    admin);
            if (unlockStatus) {
                session.setAttribute("unlockStatus", "User Unlock Successful");
            } else {
                session.setAttribute("unlockStatus", "User Unlock Unsuccessful");
            }
        } else if (action.trim().equalsIgnoreCase("EditDesc")) {
            boolean editDescStatus = d.updateDescription((String) session.getAttribute("description"),
                    (String) session.getAttribute("dataName"));
            if (editDescStatus) {
                session.setAttribute("editDescStatus", "Description Update Successful");
            } else {
                session.setAttribute("editDescStatus", "Description Update Unsuccessful");
            }
        }
        //This will be the servlet's case for getting the json?
        else if (action.trim().equalsIgnoreCase("getManualItems")) {
            System.out.println("Test got here");
            JSONParser parser = new JSONParser();
            try{
                Object obj = parser.parse(FileUtils.readAll("resources/manual_entry_items.json"));
                JSONObject jObj = (JSONObject)obj;
                response.getWriter().append(jObj.toJSONString());
            }
            catch(Exception e)
            {
                System.out.println("Something went wrong..." + e.toString());
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
