/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import common.UserRole;
import database.DatabaseManager;
import java.io.IOException;
import java.time.LocalDateTime;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Tyler Mutzek
 */
@WebServlet(name = "AdminServlet", urlPatterns = {"/AdminServlet"})
public class AdminServlet 
{
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(true);//Create a new session if one does not exists
        DatabaseManager d = new DatabaseManager();
        String action = request.getParameter("action");
        common.User admin = (common.User) session.getAttribute("user");
        
        if (action.trim().equalsIgnoreCase("InputData"))
        {
            boolean inputStatus = d.manualInput(session.getAttribute("dataName"),
                    session.getAttribute("units"),session.getAttribute("time"), 
                    session.getAttribute("value"),session.getAttribute("delta"),
                     admin);
            if(inputStatus)
                session.setAttribute("inputStatus", "Data Input Successful");
            else
                session.setAttribute("inputStatus", "Data Input Unsuccessful. Check your syntax");
        }
        
        else if(action.trim().equalsIgnoreCase("RemoveData"))
        {
            boolean dataRemovalStatus = d.manualDeletion(session.getAttribute("dataDeletionID"),
                    admin);
            if(dataRemovalStatus)
                session.setAttribute("dataDeletionStatus", "Data Deletion Successful");
            else
                session.setAttribute("dataDeletionStatus", "Data Deletion Unsuccessful");
            
        }
        
        else if(action.trim().equalsIgnoreCase("RegisterUser"))
        {
            boolean newUserStatus = d.addNewUser(session.getAttribute("username"), 
                    session.getAttribute("password"), session.getAttribute("firstName"),
                    session.getAttribute("lastName"), session.getAttribute("email"),
                    UserRole.getUserRole(session.getAttribute("userRole")),
                    admin);
            if(newUserStatus)
                session.setAttribute("Status", "New User Registration Successful");
            else
                session.setAttribute("inputStatus", "New User Registration *Unsuccessful. Check your syntax");
        }
        else if(action.trim().equalsIgnoreCase("RemoveUser"))
        {
            boolean userRemovalStatus = d.deleteUser(session.getAttribute("userDeletionID"),
                    admin);
            if(userRemovalStatus)
                session.setAttribute("userDeletionStatus", "User Deletion Successful");
            else
                session.setAttribute("userDeletionStatus", "User Deletion Unsuccessful");
        }
        
        else if(action.trim().equalsIgnoreCase("LockUser"))
        {
            boolean lockStatus = d.deleteUser(session.getAttribute("userLockID"),
                    admin);
            if(lockStatus)
                session.setAttribute("lockStatus", "User Deletion Successful");
            else
                session.setAttribute("lockStatus", "User Deletion Unsuccessful");
        }
        
        else if(action.trim().equalsIgnoreCase("UnlockUser"))
        {
            boolean unlockStatus = d.deleteUser(session.getAttribute("userUnlockID"),
                    admin);
            if(unlockStatus)
                session.setAttribute("unlockStatus", "User Unlock Successful");
            else
                session.setAttribute("unlockStatus", "User Unlock Unsuccessful");
        }
        
        else if(action.trim().equalsIgnoreCase("EditDesc"))
        {
            boolean editDescStatus = d.updateDescription(session.getAttribute("description"),
                    session.getAttribute("dataName"));
            if(editDescStatus)
                session.setAttribute("editDescStatus", "Description Update Successful");
            else
                session.setAttribute("editDescStatus", "Description Update Unsuccessful");
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
