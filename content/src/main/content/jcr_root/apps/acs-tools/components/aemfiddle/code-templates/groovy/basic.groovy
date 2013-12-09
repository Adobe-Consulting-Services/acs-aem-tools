response.setContentType("text/html");
response.setCharacterEncoding("UTF-8");

/*** Injected objects ***/
// sling (SlingScriptHelper)
// request (SlingHttpServletRequest)
// response (SlingHttpServletResponse)
// resource (Resource)

def resourceResolver = request.getResourceResolver();
def session = currentNode.getSession();
def pageManager = request.getResourceResolver().adaptTo(com.day.cq.wcm.api.PageManager);
def xss = sling.getService(com.adobe.granite.xss.XSSAPI);

// Code Here

println "Hello from ${resource.getPath()}!"
