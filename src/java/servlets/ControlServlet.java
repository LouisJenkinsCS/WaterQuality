package servlets;




import async.DataReceiver;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <code>ControlServlet</code> is the main servlet that processes most 
 * navigation requests. This servlet will redirect to other servlets depending 
 * on the attributes passed and page directed from.
 * UPDATE: LoginServlet now handles all login processing. ControlServlet is
 * now exclusively for redirection.
 * @author Joseph Picataggio
 */
@WebServlet(name = "ControlServlet", urlPatterns = {"/ControlServlet"})
public class ControlServlet extends HttpServlet {
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        log("Printed");
        System.out.println("Date: " + DataReceiver.test(Instant.now().minus(Period.ofWeeks(1)), Instant.now()).blockingFirst());
        HttpSession session = request.getSession(true);//Create a new session if one does not exists
        final Object lock = session.getId().intern();//To synchronize the session variable
        database.UserManager um = database.Database.getDatabaseManagement().getUserManager();
        common.User user = (common.User) session.getAttribute("user");
        String action = request.getParameter("control");
        
        log("action is "+action) ;       
        // Fix the login data for the user
        if(action.trim().equalsIgnoreCase("login")){
            //all this code should be in the login servlet

            boolean firstLogin = user.getLoginCount() == 0;
            user.setLoginCount(user.getLoginCount()+1);
            LocalDateTime now = LocalDateTime.now();

            user.setLastLoginTime(now);
            user.setAttemptedLoginCount(0);
            user.setLastAttemptedLoginTime(now);
            um.updateUser(user);
        
            // Always lock a session variable to be thread safe.
            synchronized(lock){
                session.setAttribute("user", user);//update information in the session attribute
            }

            if (firstLogin) {//Force the user to reset the password
                response.sendRedirect(request.getContextPath() + "/html/ResetPassword.html");
                return; //return statement is needed
            } 

            request.getServletContext()
                .getRequestDispatcher("/index.html") //page we want after successful login. 
                .forward(request, response);
            //return; //should not be needed
            } //end of  code for login action
            // The next code we will write is for the resetpassword action
             if(action.trim().equalsIgnoreCase("resetpassword")){
                user =um.getUserByID(Integer.parseInt(request.getParameter("UID")));
                synchronized(lock){
                    session.setAttribute("user", user);//update information in the session attribute
                }
                if(user.getUserPassword() != request.getParameter("token")){
                    //We have a problem, the url does not have the correct token, reject the attempt
                     //The approve should contact an admin to state what happened
                   log(user.getLoginName() +" tried to reset a password using the wrong token in the url");
                   log("user id was "+ request.getParameter("UID") );
                   response.sendRedirect(request.getContextPath() + "/loginScreen.jsp");
            
                }
                else {
                    response.sendRedirect(request.getContextPath() + "/html/ResetPassword.html"); 
                }
                return;    // return is needed
                          //The difference between a redrect and a forward is important
                        //Look at the URL in the browswer bar and notice a redirect changes it
             }
             if(action.trim().equalsIgnoreCase("add")){
                //response.sendRedirect(request.getContextPath() + "/html/javascriptDisabled.html");
                request.getServletContext()
                .getRequestDispatcher("/html/javascriptDisabled.html") 
                .forward(request, response);
                return;    // return is needed

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
