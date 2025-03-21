package rs.irm.utils;

import java.io.IOException;
import java.net.HttpURLConnection;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomFilter implements Filter {

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
		HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

		httpServletResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
		httpServletResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Cookie");
		httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
		httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST, DELETE");

		if (httpServletRequest.getMethod().equals("OPTIONS")) {
			httpServletResponse.setStatus(HttpURLConnection.HTTP_ACCEPTED);
			return;
		}

		filterChain.doFilter(servletRequest, servletResponse);
	}

}
