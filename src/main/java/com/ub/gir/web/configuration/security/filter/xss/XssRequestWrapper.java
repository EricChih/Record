package com.ub.gir.web.configuration.security.filter.xss;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class XssRequestWrapper extends HttpServletRequestWrapper {
    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String parameter =  super.getParameter(name);

        if(StringUtils.isNotBlank(parameter)){
            parameter = HtmlUtils.htmlEscape(parameter);
        }
        return parameter;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] parameterValues =  super.getParameterValues(name);
        if (parameterValues == null) return null;

        for (int index = 0; index < parameterValues.length; index++) {
            parameterValues[index] = HtmlUtils.htmlEscape(parameterValues[index]);
        }
        return parameterValues;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        Enumeration<String> headers =  super.getHeaders(name);
        List<String> result = new ArrayList<>();
        while(headers.hasMoreElements()){
            String[] tokens = headers.nextElement().split(",");
            for(String token : tokens){
                result.add(HtmlUtils.htmlEscape(token));
            }
        }
        return Collections.enumeration(result);
    }
}
