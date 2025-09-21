package rs.irm;

import java.util.ArrayList;
import java.util.HashSet;

import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.ApplicationPath;
import rs.irm.common.service.impl.AppInitServiceImpl;
import rs.irm.utils.CustomAbstractBinder;
import rs.irm.utils.CustomContainerRequestFilter;

@ApplicationPath("/api")
public class Irm extends ResourceConfig {

	public static String packagePath;

	public Irm() {

		packagePath = this.getClass().getPackage().getName();
		packages(packagePath);
		register(openApiResource());

		CustomAbstractBinder abstractBinder = new CustomAbstractBinder();
		register(abstractBinder);

		CustomContainerRequestFilter customContainerRequestFilter = new CustomContainerRequestFilter();
		register(customContainerRequestFilter);
	}

	private OpenApiResource openApiResource() {
		OpenApiResource openApiResource = new OpenApiResource();
		SwaggerConfiguration configuration = new SwaggerConfiguration();

		OpenAPI openAPI = new OpenAPI();
		Info info = new Info();
		info.setTitle("Interface reporting manager");
		info.setVersion("1.0");
		openAPI.setInfo(info);

		Server server = new Server();
		server.setUrl(AppInitServiceImpl.contextPath);
		openAPI.setServers(new ArrayList<Server>() {
			{
				add(server);
			}
		});

		configuration.setOpenAPI(openAPI);
		configuration.setResourcePackages(new HashSet<String>() {
			{
				add(packagePath);
			}
		});

		openApiResource.setOpenApiConfiguration(configuration);

		return openApiResource;
	}

}
