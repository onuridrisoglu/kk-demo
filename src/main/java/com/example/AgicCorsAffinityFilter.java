package com.example;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Workaround for Azure Application Gateway generating two stickiness cookies:
 * the primary affinity cookie and a CORS variant. Kubernetes Kit only clears the
 * primary one, leaving the CORS cookie alive and routing the user back to the
 * same pod. This filter also expires the CORS cookie whenever the Kit expires
 * the primary one.
 */
@Component
public class AgicCorsAffinityFilter implements Filter {

    private static final String CORS_COOKIE_SUFFIX = "CORS";

    private final String primaryCookieName;

    public AgicCorsAffinityFilter(
            @Value("${VAADIN_KUBERNETES_STICKY_SESSION_COOKIE_NAME:ApplicationGatewayAffinity}") String primaryCookieName) {
        this.primaryCookieName = primaryCookieName;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(request, new CorsAffinityClearingWrapper((HttpServletResponse) response));
    }

    private class CorsAffinityClearingWrapper extends HttpServletResponseWrapper {

        CorsAffinityClearingWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void addCookie(Cookie cookie) {
            super.addCookie(cookie);
            if (primaryCookieName.equals(cookie.getName()) && cookie.getMaxAge() == 0) {
                Cookie corsCookie = new Cookie(primaryCookieName + CORS_COOKIE_SUFFIX, "");
                corsCookie.setMaxAge(0);
                corsCookie.setPath(cookie.getPath() != null ? cookie.getPath() : "/");
                if (cookie.getDomain() != null) {
                    corsCookie.setDomain(cookie.getDomain());
                }
                super.addCookie(corsCookie);
            }
        }
    }
}
