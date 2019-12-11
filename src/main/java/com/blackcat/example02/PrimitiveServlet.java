package com.blackcat.example02;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class PrimitiveServlet implements Servlet {

    public void init(ServletConfig servletConfig) throws ServletException {
       log.info("primitiveServlet init");
    }

    public ServletConfig getServletConfig() {
        return null;
    }

    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        log.info("primitiveServlet service");
        PrintWriter out = servletResponse.getWriter();
        out.println("Hello . Rose are red.");
        out.println("Violets are blue.");
    }

    public String getServletInfo() {
        return null;
    }

    public void destroy() {
        log.info("primitiveServlet destroy");
    }
}
