package servlets;

import async.Data;
import async.DataParameter;
import async.DataReceiver;
import async.DataValue;
import bayesian.RunBayesianModel;
import database.DatabaseManager;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
        
        DatabaseManager.LogError("Action is: " + action);
        
        // Nothing was selected, go back to dashboard.
        if (action == null) {
            defaultHandler(request, response);
            return;
        }

        
        
        if (action.trim().equalsIgnoreCase("fetchQuery")) {
            String data = request.getParameter("query");
            System.out.println("Data Received: " + data);
            JSONProtocol proto = new JSONProtocol();
            try {
                proto.process((JSONObject) new JSONParser().parse(data))
                        .blockingSubscribe(obj -> response.getWriter().append(obj.toJSONString()));
            } catch (ParseException ex) {
                Logger.getLogger(ControlServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        } else if (action.trim().equalsIgnoreCase("getBayesian")) {
            Long date = Long.parseLong(request.getParameter("data"));
            DatabaseManager.LogError("Inside of 'getBayesian'");
            Instant day = Instant.ofEpochMilli(date).truncatedTo(ChronoUnit.DAYS);
            System.out.println("Day Selected: " + day);
//            try {
//                day = RunBayesianModel.getFullDayOfData().minus(Period.ofDays(1));
//            } catch (Exception ex) {
//                StringWriter errors = new StringWriter();
//                ex.printStackTrace(new PrintWriter(errors));
//                DatabaseManager.LogError("Exception: " + ex.getClass().getName() + "\nMessage: " + ex.getMessage() + "\nStack Trace: " + errors.toString());
//                return;
//            }
            DatabaseManager.LogError("Day Selected: " + day);
            try {
            RunBayesianModel.trialJAGS(day)
                    .map(obj -> {
                        obj.put("date", day.getEpochSecond() * 1000);
                        return obj;
                    })
                    .blockingSubscribe((JSONObject resp) -> { 
                        response.getWriter().append(resp.toJSONString());
                        System.out.println("Sent response...");
                    });
            } catch (Throwable t) {
                StringWriter errors = new StringWriter();
                t.printStackTrace(new PrintWriter(errors));
                DatabaseManager.LogError("Exception: " + t.getClass().getName() + "\nMessage: " + t.getMessage() + "\nStack Trace: " + errors.toString());
                throw t;
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
