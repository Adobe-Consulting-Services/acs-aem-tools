
import org.apache.sling.api.servlets.*;
import java.io.IOException;

public class MyServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {

    	// Code here
        response.getWriter().println("hello world");
        
	}
}