package servlets;

import async.Data;
import async.DataParameter;
import async.DataReceiver;
import async.DataValue;
import io.reactivex.Observable;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import org.javatuples.Pair;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.javatuples.Triplet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * <code>ControlServlet</code> is the main servlet that processes most
 * navigation requests. This servlet will redirect to other servlets depending
 * on the attributes passed and page directed from. UPDATE: LoginServlet now
 * handles all login processing. ControlServlet is now exclusively for
 * redirection.
 *
 * @author Joseph Picataggio
 */
@WebServlet(name = "ControlServlet", urlPatterns = {"/ControlServlet"})
public class ControlServlet extends HttpServlet {

    private void defaultHandler(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder data = new StringBuilder();
        
        long defaultId = DataReceiver.getParameters().blockingFirst().getId();
        Data source = DataReceiver.getData(Instant.now().minus(Period.ofWeeks(4)), Instant.now(), defaultId);
        DataReceiver
                .getParameters()
                // Display based on lexicographical ordering
                .sorted((DataParameter dp1, DataParameter dp2) -> dp1.getName().compareTo(dp2.getName()))
                // Generate a checkbox for each parameter.
                .map((DataParameter parameter) -> "<input type=\"checkbox\" name=\"" + parameter.getId() + "\" onclick=\"handleClick(this)\" class=\"data\" id=\"" + parameter.getId() + "\" value=\"data\">" + parameter.getName() + "<br>\n")
                .blockingSubscribe(data::append);

        StringBuilder categories = new StringBuilder("categories: [");
        source.getData()
                .map(DataValue::getTimestamp)
                .distinct()
                .sorted()
                .map((Instant ts) -> "\"" + ts.toString().replace("T", " ").replace("Z", "") + "\",")
                .blockingSubscribe(categories::append);
        categories.append("]");
        
        
        String defaultDescription = "<center><h1>None Selected</h1></center>";
        String defaultTable = "<table border='1'>\n"
                + "	<tr>\n"
                + "		<th>Timestamp</th>\n"
                + "               <th>(NULL)</th>\n"
                + "	</tr>\n"
                + "</table>";

        request.setAttribute("Parameters", data.toString());
        request.setAttribute("Descriptions", DataReceiver.generateDescriptions(source));
        request.setAttribute("HighChartJS_Categories", categories);
        request.setAttribute("HighChartJS_Series", DataReceiver.generateSeries(source));
        request.setAttribute("Table", DataReceiver.generateTable(source));
        request.getServletContext()
                .getRequestDispatcher("/dashboard.jsp")
                .forward(request, response);
    }

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

        // Nothing was selected, go back to dashboard.
        if (action == null) {
            defaultHandler(request, response);
        }

        log("Action is: " + action);

        if (action.trim().equalsIgnoreCase("getData")) {
            String start = request.getParameterValues("startdate")[0];
            String end = request.getParameterValues("enddate")[0];
            log("Start: " + start);
            log("End: " + start);
            
            if(!start.endsWith(":00")) {
                start += ":00";
            }
            start += "Z";
            
            if (!end.endsWith(":00")) {
                end += ":00";
            }
            end += "Z";
            
            Long[] selected = request
                    .getParameterMap()
                    .keySet()
                    .stream()
                    .filter(k -> !k.equals("startdate") && !k.equals("enddate") && !k.equals("Get Data") && !k.equals("control"))
                    .map(Long::parseLong)
                    .collect(Collectors.toList())
                    .toArray(new Long[0]);

            // Nothing selected...
            if (selected == null || selected.length == 0) {
                defaultHandler(request, response);
                return;
            }

            log("User Selected: " + Arrays.deepToString(selected));
            
            // Obtain the data for what is selected
            Data data = DataReceiver.getData(Instant.parse(start), Instant.parse(end), selected);
            String descriptions = DataReceiver.generateDescriptions(data);
            String chartjs = DataReceiver.generateChartJS(data);
            String table = DataReceiver.generateTable(data);
            StringBuilder categories = new StringBuilder("categories: [");
            data.getData()
                    .map(DataValue::getTimestamp)
                    .distinct()
                    .sorted()
                    .map((Instant ts) -> "\"" + ts.toString().replace("T", " ").replace("Z", "") + "\",")
                    .blockingSubscribe(categories::append);
            categories.append("]");
            
            StringBuilder paramData = new StringBuilder();
            DataReceiver
                 .getParameters()
                 // Display based on lexicographical ordering
                 .sorted((DataParameter dp1, DataParameter dp2) -> dp1.getName().compareTo(dp2.getName()))
                 // Generate a checkbox for each parameter.
                 .map((DataParameter parameter) -> "<input type=\"checkbox\" name=\"" + parameter.getId() + "\" onclick=\"handleClick(this)\" class=\"data\" id=\"" + parameter.getId() + "\" value=\"data\">" + parameter.getName() + "<br>\n")
                 .blockingSubscribe(paramData::append);

            request.setAttribute("Descriptions", DataReceiver.generateDescriptions(data));
            request.setAttribute("HighChartJS_Categories", categories);
            request.setAttribute("HighChartJS_Series", DataReceiver.generateSeries(data));
            request.setAttribute("Table", DataReceiver.generateTable(data));
            request.setAttribute("Parameters", paramData.toString());

            request.getServletContext()
                    .getRequestDispatcher("/dashboard.jsp")
                    .forward(request, response);
            log("Got Action: " + action);
            return;
        }

        //I modeled this after the above case ^^
        if (action.trim().equalsIgnoreCase("getDesc")) {
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
        if (action.trim().equalsIgnoreCase("login")) {
            //all this code should be in the login servlet

            boolean firstLogin = user.getLoginCount() == 0;
            user.setLoginCount(user.getLoginCount() + 1);
            LocalDateTime now = LocalDateTime.now();

//            user.setLastLoginTime(Timestamp.valueOf(LocalDateTime.now()));
//            user.setAttemptedLoginCount(0);
//            user.setLastAttemptedLoginTime(Timestamp.valueOf(LocalDateTime.now()));
//            um.updateUser(user);

            // Always lock a session variable to be thread safe.
            synchronized (lock) {
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
        if (action.trim().equalsIgnoreCase("resetpassword")) {
            user = um.getUserByID(Integer.parseInt(request.getParameter("UID")));
            synchronized (lock) {
                session.setAttribute("user", user);//update information in the session attribute
            }
            if (user.getUserPassword() != request.getParameter("token")) {
                //We have a problem, the url does not have the correct token, reject the attempt
                //The approve should contact an admin to state what happened
                log(user.getLoginName() + " tried to reset a password using the wrong token in the url");
                log("user id was " + request.getParameter("UID"));
                response.sendRedirect(request.getContextPath() + "/loginScreen.jsp");

            } else {
                response.sendRedirect(request.getContextPath() + "/html/ResetPassword.html");
            }
            return;    // return is needed
            //The difference between a redrect and a forward is important
            //Look at the URL in the browswer bar and notice a redirect changes it
        }
        if (action.trim().equalsIgnoreCase("add")) {
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
        defaultHandler(request, response);
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
