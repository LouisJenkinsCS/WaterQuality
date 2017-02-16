package servlets;




import async.DataReceiver;
import io.reactivex.Observable;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import org.javatuples.Pair;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
        
        HttpSession session = request.getSession(true);//Create a new session if one does not exists
        final Object lock = session.getId().intern();//To synchronize the session variable
        database.UserManager um = database.Database.getDatabaseManagement().getUserManager();
        common.User user = (common.User) session.getAttribute("user");
        String action = request.getParameter("control");
        
        if (action == null) {
            StringBuilder data = new StringBuilder();
            
            DataReceiver
                    // Obtains the data pulled from server of last 24 hours as a JSONObject
                    .getData(DataReceiver.JSON_URL)
                    // The JSON sent contains the data inside of a JSONArray
                    .map((JSONObject obj) -> (JSONArray) obj.get("data"))
                    // Take each element from the JSONArray and emit as their own JSONObject
                    // I.E: Given an array = [1, 2, 3] which is a single item,
                    // it will emit the items 1, 2, and 3 individually. 
                    .flatMap(Observable::fromIterable)
                    // Obtain the name and unit of the parameter as a Pair<String, String>
                    .map(obj -> Pair.with((String) ((JSONObject) obj).get("name"), (String) ((JSONObject) obj).get("unit")))
                    // Format as a String
                    .map(p -> ((Pair) p).getValue0() + " (" + ((Pair) p).getValue1() + ")")
                    // Create the equivalent checkboxes.
                    .map(str -> "<input type=\"checkbox\" name=\"" + str + "\" onclick=\"handleClick(this)\" class=\"data\" id=\"" + str + "\" value=\"data\">" + str + "<br>\n")
                    .blockingSubscribe(data::append);
                   
            request.setAttribute("DummyData", data.toString());
            
            request.getServletContext()
                .getRequestDispatcher("/dashboard.jsp") //page we want after successful login. 
                .forward(request, response);
            return;
        }
        
        log("Action is: " + action);
        
        if (action.trim().equalsIgnoreCase("getData")) {
            request.getParameterMap().forEach((k, v) -> System.out.println("Key: " + k + ", Value: " + v));
            log(action);
            System.out.println("Got Action: " + action);
            return;
        }
        
        
        
        //I modeled this after the above case ^^
        if(action.trim().equalsIgnoreCase("getDesc"))
        {
            StringBuilder description = new StringBuilder();
            description.append("Test Dummy\n");
            request.setAttribute("datadesc", description.toString());
            
            //I don't understand this part, but I assume it's necessary?
            request.getServletContext()
                .getRequestDispatcher("/dashboard.jsp") 
                .forward(request, response);
            return;
        }
        
        // Fix the login data for the user
        if(action.trim().equalsIgnoreCase("login")){
            //all this code should be in the login servlet

            boolean firstLogin = user.getLoginCount() == 0;
            user.setLoginCount(user.getLoginCount()+1);
            LocalDateTime now = LocalDateTime.now();

            user.setLastLoginTime(Timestamp.valueOf(LocalDateTime.now()));
            user.setAttemptedLoginCount(0);
            user.setLastAttemptedLoginTime(Timestamp.valueOf(LocalDateTime.now()));
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
