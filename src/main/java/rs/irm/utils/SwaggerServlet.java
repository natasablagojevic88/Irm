package rs.irm.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SwaggerServlet
 */
@WebServlet("/swagger-ui/*")
public class SwaggerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		InputStream inputStream=this.getClass().getClassLoader().getResourceAsStream("/META-INF/resources/webjars/swagger-ui/5.17.14"+request.getPathInfo());
		
		if(inputStream ==null) {
			response.getWriter().write("No content");
		}else {
			OutputStream outputStream = response.getOutputStream();
			byte[] buffer = new byte[8129];
			int len = inputStream.read(buffer);
			while (len != -1) {
				outputStream.write(buffer, 0, len);
			    len = inputStream.read(buffer);
			}
			inputStream.close();
			outputStream.close();
			
		}
	}

}
