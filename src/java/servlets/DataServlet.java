/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import async.DataReceiver;
import database.DatabaseManager;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
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
 *
 * @author lpj11535
 */
@WebServlet(name = "DataServlet", urlPatterns = {"/DataServlet"}, asyncSupported = true)
public class DataServlet extends HttpServlet {

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
        request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
        HttpSession session = request.getSession(true);//Create a new session if one does not exists
        final Object lock = session.getId().intern();//To synchronize the session variable
        database.UserManager um = database.Database.getDatabaseManagement().getUserManager();
        common.User user = (common.User) session.getAttribute("user");
        String action = request.getParameter("action");

        if (action == null) {
            return;
        }

        if (action.trim().equalsIgnoreCase("fetchQuery")) {
            String data = request.getParameter("query");
            JSONObject onEmpty = new JSONObject();
            onEmpty.put("data", new JSONArray());
            JSONProtocol proto = new JSONProtocol();
            try {
                AsyncContext async = request.startAsync();
                System.out.println("Async Available: " + async.getRequest().isAsyncSupported() + ", Async Started: " + async.getRequest().isAsyncStarted());
                proto.process((JSONObject) new JSONParser().parse(data))
                        .subscribeOn(Schedulers.computation())
                        .defaultIfEmpty(onEmpty)
                        .subscribe(obj -> {
                            async.getResponse().getWriter().append(obj.toJSONString());
                            async.complete();
                        });
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
         */ else if (action.trim().equalsIgnoreCase("getMostRecent")) {
            AsyncContext async = request.startAsync();
            
            // Get most recent readings from Netronix's 'LAST' readings
            DataReceiver.getData(DataReceiver.LATEST_DATE_URL)
                    // Everything is performed on another thread, freeing the Apache tomcat thread.
                    .subscribeOn(Schedulers.computation())
                    .map((JSONObject obj) -> (JSONArray) obj.get("data"))
                    .flatMap(JSONUtils::flattenJSONArray)
                    .map((JSONObject obj) -> Triplet.with((Long) obj.get("id"), (Double) obj.get("value"), toUTCInstant((String) obj.get("timestamp")).toEpochMilli()))
                    // Drop the source id in favor for the database id...
                    .flatMap((Triplet<Long, Double, Long> triplet) -> DatabaseManager.remoteSourceToDatabaseId(triplet.getValue0())
                            .map(triplet::setAt0)
                            .toObservable()
                    )
                    // (id, value, time)
                    .map((Triplet<Long, Double, Long> triplet) -> {
                        JSONObject obj = new JSONObject();
                        obj.put("id", triplet.getValue0());
                        obj.put("time", triplet.getValue2());
                        obj.put("value", triplet.getValue1());
                        return obj;
                    })
                    .buffer(Integer.MAX_VALUE)
                    .map(JSONUtils::toJSONArray)
                    .map((JSONArray arr) -> {
                        JSONObject resp = new JSONObject();
                        resp.put("data", arr);
                        return resp;
                    })
                    .subscribe(obj -> {
                        async.getResponse().getWriter().append(obj.toJSONString());
                        async.complete();
                    });

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
