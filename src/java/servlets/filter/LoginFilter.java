
package servlets.filter;

import common.User;
import common.UserRole;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import utilities.PropertyManager;

/**
 * Redirects a user to the login screen if they try to access the 
 * admin page without logging in or if their user role is not admin.
 * @author Tyler Mutzek
 */
public class LoginFilter implements Filter {
    private FilterConfig filterConfig = null;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;   
        HttpServletResponse res = (HttpServletResponse) response;
        String file = req.getServletPath();
        HttpSession session = req.getSession(false);
        
        
        if (file.equals("/admin.jsp") || file.equals("/AdminServlet"))
        {
            User user = (User)session.getAttribute("user");
            if (user == null || user.getUserRole() != UserRole.SystemAdmin) 
            {
                request.getRequestDispatcher("/LoginServlet").forward(request, response);
                return;
            }
            chain.doFilter(req, res);
        }
        else
            chain.doFilter(req, res);
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {        
        this.filterConfig = filterConfig;
        //log("LoginFilter: Initializing filter");
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }
    
    public void log(String msg) {
        filterConfig.getServletContext().log(msg);        
    }
}
