package com.adobe.acs.tools.jstl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.SimpleTag;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.settings.SlingSettingsService;

/**
 * Caches body content for a specific TTL duration, capped at 10 minutes
 * If in author, this class does nothing but pass-through
 */
public class CacheTag implements SimpleTag {
    static final long MAX_TTL = 600000;
    static TimeLimitedMap<String,String> cache = new TimeLimitedMap<String, String>(TimeUnit.MILLISECONDS, MAX_TTL);
        
    @Override
    public void doTag() throws JspException, IOException {
        if (!isAuthor()) {        
            jspContext.getOut().flush();
            String cachedContent = cache.get(key);
            if (cachedContent == null || cache.getAge(key) >= ttl) {
                StringWriter writer = new StringWriter();
                body.invoke(writer);
                cachedContent = writer.getBuffer().toString();
                cache.put(key, cachedContent);
            }
            jspContext.getOut().write(cachedContent);
        } else {
            body.invoke(jspContext.getOut());
        }
    }

    JspTag parent;
    @Override
    public void setParent(JspTag jsptag) {
        parent = jsptag;
    }

    @Override
    public JspTag getParent() {
        return parent;
    }

    JspContext jspContext;
    @Override
    public void setJspContext(JspContext jc) {
        jspContext = jc;
    }

    JspFragment body;
    @Override
    public void setJspBody(JspFragment jf) {
        body = jf;
    }
    
    public long ttl = MAX_TTL;
    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
    
    public String key;
    public void setKey(String key) {
        this.key = key;
    }

    static Boolean authorMode = null;
    private boolean isAuthor() {
        if (authorMode != null) {
            return authorMode;
        }
        SlingScriptHelper sling = (SlingScriptHelper) jspContext.findAttribute("sling");
        SlingSettingsService settingsService = sling.getService(SlingSettingsService.class);
        authorMode = settingsService.getRunModes().contains("author");
        return authorMode;
    }
}
