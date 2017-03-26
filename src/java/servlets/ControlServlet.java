package servlets;

import async.Data;
import async.DataParameter;
import async.DataReceiver;
import async.DataValue;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import org.javatuples.Pair;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import protocol.JSONProtocol;
import utilities.TimeStampFormatter;

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
                .subscribeOn(Schedulers.computation())
                // Display based on lexicographical ordering
                .sorted((DataParameter dp1, DataParameter dp2) -> dp1.getName().compareTo(dp2.getName()))
                // Generate a checkbox for each parameter.
                .map((DataParameter parameter) -> "<input type=\"checkbox\" name=\"" + parameter.getId() + "\" onclick=\"handleClick(this); fetch();\" class=\"data\" id=\"" + parameter.getId() + "\" value=\"data\">" + parameter.getName() + "<br>\n")
                .blockingSubscribe(data::append);
        
        System.out.println("Thread: " + Thread.currentThread().getName());
        JSONProtocol proto = new JSONProtocol();
        proto
                .processUsing(source)
                .subscribeOn(Schedulers.computation())
                .blockingSubscribe((JSONObject resp) -> request.setAttribute("ChartData", resp));
        
        String defaultDescription = "<center><h1>None Selected</h1></center>";
        String defaultTable = "<table border='1'>\n"
                + "	<tr>\n"
                + "		<th>Timestamp</th>\n"
                + "               <th>(NULL)</th>\n"
                + "	</tr>\n"
                + "</table>";

        request.setAttribute("Parameters", data.toString());
        request.setAttribute("Descriptions", DataReceiver.generateDescriptions(source));
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
        String action = request.getParameter("action");

        // Nothing was selected, go back to dashboard.
        if (action == null) {
            defaultHandler(request, response);
            return;
        }

        log("Action is: " + action);
        
        if (action.trim().equalsIgnoreCase("fetchQuery")) {
            String data = request.getParameter("query");
            System.out.println("Data Received: " + data);
            JSONProtocol proto = new JSONProtocol();
            try {
                proto.process((JSONObject) new JSONParser().parse(data))
                        .subscribeOn(Schedulers.computation())
                        .blockingSubscribe(obj -> response.getWriter().append(obj.toJSONString()));
            } catch (ParseException ex) {
                Logger.getLogger(ControlServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
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
