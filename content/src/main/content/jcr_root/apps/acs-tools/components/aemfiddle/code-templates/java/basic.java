
package apps.acs_002dtools.components.aemfiddle.fiddle;

import com.day.cq.search.*;
import com.day.cq.wcm.api.*;
import com.day.cq.dam.api.*;
import org.apache.sling.api.*;
import org.apache.sling.api.resource.*;
import org.apache.sling.api.servlets.*;
import java.io.IOException;
import javax.jcr.*;
import java.util.*;

public class fiddle extends SlingAllMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        // Code here
        response.getWriter().println("Hello from " + request.getResource().getPath());
    }
}