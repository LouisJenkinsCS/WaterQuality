package servlets;

import async.DataReceiver;
import bayesian.RunBayesianModel;
import com.github.davidmoten.rx.util.Pair;
import common.UserRole;
import database.DatabaseManager;
import io.reactivex.Observable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import utilities.JSONUtils;
import static utilities.TimestampUtils.toUTCInstant;

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
        common.User admin = (common.User) request.getSession(true).getAttribute("user");
        request.setAttribute("loggedIn", admin == null ? false : admin.getUserRole() == UserRole.SystemAdmin ? true : false);
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
        

        if (action.trim().equalsIgnoreCase("fetchQuery")) {
            String data = request.getParameter("query");
            JSONObject onEmpty = new JSONObject();
            onEmpty.put("data", new JSONArray());
            JSONProtocol proto = new JSONProtocol();
            try {
                proto.process((JSONObject) new JSONParser().parse(data))
                        .defaultIfEmpty(onEmpty)
                        .blockingSubscribe(obj -> response.getWriter().append(obj.toJSONString()));
            } catch (ParseException ex) {
                Logger.getLogger(ControlServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /*
         * getMostRecent
         * {data:[{
         *  id:
         *  time:
         *  value:
         *  }]
         *  }
         */
        else if (action.trim().equalsIgnoreCase("getMostRecent")) {
            DataReceiver.getData(DataReceiver.LATEST_DATE_URL)
                    .map((JSONObject obj) -> (JSONArray) obj.get("data"))
                    .flatMap(JSONUtils::flattenJSONArray)
                    .doOnNext(System.out::println)
                    .map((JSONObject obj) -> Triplet.with((Long) obj.get("id"), (Double) obj.get("value"), toUTCInstant((String) obj.get("timestamp")).toEpochMilli()))
                    .flatMap((Triplet<Long, Double, Long> triplet) -> Observable
                            .just(triplet.getValue0())
                            .map(DatabaseManager::remoteSourceToDatabaseId)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(triplet::setAt0)
                    )
                    .doOnNext(System.out::println)
                    .map((Triplet<Long, Double, Long> triplet) -> {
                        JSONObject obj = new JSONObject();
                        obj.put("id", triplet.getValue0());
                        obj.put("time", triplet.getValue2());
                        obj.put("value", triplet.getValue1());
                        return obj;
                    })
                    .doOnNext(System.out::println)
                    .buffer(Integer.MAX_VALUE)
                    .map(JSONUtils::toJSONArray)
                    .map((JSONArray arr) -> {
                        JSONObject resp = new JSONObject();
                        resp.put("data", arr);
                        return resp;
                    })
                    .doOnNext(System.out::println)
                    .blockingSubscribe(obj -> response.getWriter().append(obj.toJSONString()));
                    
                    
                    
                    
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
