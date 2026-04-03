package org.jimisaac.task_manager_cloud_assignment.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
public class HttpLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HttpLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String contentType = httpRequest.getContentType();
        String servletPath = httpRequest.getServletPath();
        String pathInfo = httpRequest.getPathInfo();

        logger.info("Incoming Request - Method: {}, URI: {}, ServletPath: {}, PathInfo: {}, Query: {}, Content-Type: {}",
                method, uri, servletPath, pathInfo, queryString, contentType);

        // Log request headers
        Collections.list(httpRequest.getHeaderNames()).forEach(headerName ->
            logger.debug("Header: {} = {}", headerName, httpRequest.getHeader(headerName))
        );

        chain.doFilter(request, response);
    }
}

