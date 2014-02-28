package com.adobe.acs.tools.xss;

import java.text.Normalizer;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps HTTP request to prevent XSS in request parameters
 */
public class XSSProofRequest extends SlingHttpServletRequestWrapper {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public XSSProofRequest(SlingHttpServletRequest wrappedRequest) {
        super(wrappedRequest);
    }

    @Override
    public Map getParameterMap() {
        final Map map = super.getParameterMap();
        return new AbstractMap() {
            @Override
            public Set<Entry> entrySet() {
                Set<Entry> entries = map.entrySet();
                Set<Entry> newEntries = new HashSet<Entry>();
                for (final Entry e : entries) {
                    newEntries.add(new Entry() {
                        @Override
                        public Object getKey() {
                            return e.getKey();
                        }

                        @Override
                        public Object getValue() {
                            Object val = e.getValue();
                            if (val == null) return null;
                            if (val instanceof String) {
                                return filterXSSFromRequest((String) val);
                            } else if (val instanceof String[]) {
                                String[] src = (String[]) val;
                                String[] out = new String[src.length];
                                for (int i=0; i < src.length; i++) {
                                    out[i] = filterXSSFromRequest(src[i]);
                                }
                                return out;
                            }
                            return val;
                        }

                        @Override
                        public Object setValue(Object value) {
                            throw new UnsupportedOperationException("Request parameters may not be altered");
                        }
                    });
                }
                return newEntries;
            }
        };
    }
    
    @Override
    public String getParameter(String paramName) {
        return filterXSSFromRequest(super.getParameter(paramName));
    }

    private String filterXSSFromRequest(String value) {
        if (value == null) return null;
        String newValue = stripXSS(value);
        if (! value.equals(newValue)) {
            log.error("XSS attack vector detected >> "+value+" ; Rewritten as "+newValue);
        }
        return newValue;
    }

    static Pattern[] removePatterns = {
        // Avoid anything between script tags
        Pattern.compile("<\\s*script(.*)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid anything in a src='...' type of expression
        Pattern.compile("src[\r\n\\s]*=[\r\n\\s]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid anything in a src="..." type of expression
        Pattern.compile("src[\r\n\\s]*=[\r\n\\s]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Remove any lonesome </script> tag
        Pattern.compile("<\\s*/\\s*script\\s*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid eval(...) expressions
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid expression(...) expressions
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid javascript:... expressions
        Pattern.compile("j\\s*a\\s*v\\s*a\\s*s\\s*c\\s*r\\s*i\\s*p\\s*t\\s*:", Pattern.CASE_INSENSITIVE),
        // Avoid vbscript:... expressions
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        // Avoid url:... expressions
        Pattern.compile("url:", Pattern.CASE_INSENSITIVE),
        // Avoid onload= expressions
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Remove any lonesome </script> tag
        Pattern.compile("alert\\s*\\(", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Remove any &&'s
        Pattern.compile("&&", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Remove any window.location variation
        Pattern.compile("window\\s*\\.\\s*location", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Remove any window[location variation
        Pattern.compile("window\\s*\\[\\s*(')?location", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Remove any escaped hex characters
        Pattern.compile("\\\\x[A-Fa-f0-9]+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Remove any escaped octal characters
        Pattern.compile("\\\\0[0-9]+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Remove any escaped unicode characters
        Pattern.compile("\\\\u[0-9A-Fa-f]+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Remove any valueOf
        Pattern.compile("valueOf", Pattern.MULTILINE | Pattern.DOTALL),
        // Remove any expression
        Pattern.compile("expression\\s*\\(", Pattern.MULTILINE | Pattern.DOTALL),
        // Remove html escaped characters
        Pattern.compile("&#(x)?[0-9A-Fa-f]+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    static Pattern[] removeCodePatterns = {
        Pattern.compile("\\\\"),
        Pattern.compile("\\["),
        Pattern.compile("\\]"),
        Pattern.compile("\\{"),
        Pattern.compile("\\}"),
        Pattern.compile(";")
    };
    
    /**
     * Removes all the potentially malicious characters from a string
     *
     * @param value the raw string
     * @return the sanitized string
     */
    private String stripXSS(String value) {
        String cleanValue = null;
        if (value != null) {
            cleanValue = Normalizer.normalize(value, Normalizer.Form.NFD);

            // Avoid null characters
            cleanValue = cleanValue.replaceAll("\0", "");

            for (Pattern p : removePatterns) {
                cleanValue = p.matcher(cleanValue).replaceAll("");
            }

            if (!isValidJSON(cleanValue)) {
                for (Pattern p : removeCodePatterns) {
                    cleanValue = p.matcher(cleanValue).replaceAll("");
                }
                cleanValue = cleanValue.replaceAll("\"", "&quot;");
            }
            cleanValue = cleanValue.replaceAll("<", "&lt;");
//            if (!cleanValue.equals(value)) {
//                log.error("####Suspicious input changed; {} --> {}", value, cleanValue);
//            } else {
//                log.error("####INPUT OK; {} --> {}", value, cleanValue);                
//            }
        }
        return cleanValue;
    }

    private boolean isValidJSON(String value) {
        if (value == null || value.length() == 0) return false;
        char firstChar = value.charAt(0);
        if (firstChar == '[') {
            try {
                new JSONArray(value);
                return true;
            } catch (JSONException e) {
                return false;
            }
        } else if (firstChar == '{') {
            try {
                new JSONObject(value);
                return true;
            } catch (JSONException e) {
                return false;
            }            
        }
        return false;
    }

}
