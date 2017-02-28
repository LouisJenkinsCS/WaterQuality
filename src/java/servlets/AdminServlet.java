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
        String action = (String)request.getParameter("action");
        common.User admin = (common.User) session.getAttribute("user");
        
        if (action.trim().equalsIgnoreCase("InputData"))
        {
            boolean inputStatus = d.manualInput((String)session.getAttribute("dataName"),
                    (String)session.getAttribute("units"),(String)session.getAttribute("time"), 
                    (float)session.getAttribute("value"),(float)session.getAttribute("delta"),
                    (int)session.getAttribute("id"), admin);
            if(inputStatus)
                session.setAttribute("inputStatus", "Data Input Successful");
            else
                session.setAttribute("inputStatus", "Data Input Unsuccessful. Check your syntax");
        }
        
        else if(action.trim().equalsIgnoreCase("RemoveData"))
        {
            boolean dataRemovalStatus = d.manualDeletion((int)session.getAttribute("dataDeletionID"),
                    admin);
            if(dataRemovalStatus)
                session.setAttribute("dataDeletionStatus", "Data Deletion Successful");
            else
                session.setAttribute("dataDeletionStatus", "Data Deletion Unsuccessful");
            
        }
        
        else if(action.trim().equalsIgnoreCase("RegisterUser"))
        {
            boolean newUserStatus = d.addNewUser((String)session.getAttribute("username"), 
                    (String)session.getAttribute("password"), (String)session.getAttribute("firstName"),
                    (String)session.getAttribute("lastName"), (String)session.getAttribute("email"),
                    UserRole.getUserRole((String)session.getAttribute("userRole")),
                    admin);
            if(newUserStatus)
                session.setAttribute("Status", "New User Registration Successful");
            else
                session.setAttribute("inputStatus", "New User Registration *Unsuccessful. Check your syntax");
        }
        else if(action.trim().equalsIgnoreCase("RemoveUser"))
        {
            boolean userRemovalStatus = d.deleteUser((int)session.getAttribute("userDeletionID"),
                    admin);
            if(userRemovalStatus)
                session.setAttribute("userDeletionStatus", "User Deletion Successful");
            else
                session.setAttribute("userDeletionStatus", "User Deletion Unsuccessful");
        }
        
        else if(action.trim().equalsIgnoreCase("LockUser"))
        {
            boolean lockStatus = d.deleteUser((int)session.getAttribute("userLockID"),
                    admin);
            if(lockStatus)
                session.setAttribute("lockStatus", "User Deletion Successful");
            else
                session.setAttribute("lockStatus", "User Deletion Unsuccessful");
        }
        
        else if(action.trim().equalsIgnoreCase("UnlockUser"))
        {
            boolean unlockStatus = d.deleteUser((int)session.getAttribute("userUnlockID"),
                    admin);
            if(unlockStatus)
                session.setAttribute("unlockStatus", "User Unlock Successful");
            else
                session.setAttribute("unlockStatus", "User Unlock Unsuccessful");
        }
        
        else if(action.trim().equalsIgnoreCase("EditDesc"))
        {
            boolean editDescStatus = d.updateDescription((String)session.getAttribute("description"),
                    (String)session.getAttribute("dataName"));
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
