package rs.irm;

import java.util.ArrayList;
import java.util.HashSet;

import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.ApplicationPath;
import rs.irm.utils.AppParameters;
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

	@SuppressWarnings("serial")
	private OpenApiResource openApiResource() {
		OpenApiResource openApiResource = new OpenApiResource();
		SwaggerConfiguration configuration = new SwaggerConfiguration();

		OpenAPI openAPI = new OpenAPI();
		Info info = new Info();
		info.setTitle("Interface reporting manager");
		info.setVersion("1.0");
		openAPI.setInfo(info);

		Server server = new Server();
		server.setUrl(AppParameters.baseurl);
		openAPI.setServers(new ArrayList<Server>() {
			{
				add(server);
			}
		});

		SecurityScheme securityScheme = new SecurityScheme();
		securityScheme.setName("JWT");
		securityScheme.setBearerFormat("JWT");
		securityScheme.setScheme("bearer");
		securityScheme.setType(Type.HTTP);

		Components components = new Components();
		components.addSecuritySchemes("JWT", securityScheme);
		openAPI.setComponents(components);

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
