package rs.irm.common.service.impl;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import rs.irm.common.enums.Language;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.utils.AppParameters;

@Named
public class ResourceBundleServiceImpl implements ResourceBundleService {

	@Context
	private HttpServletRequest request;

	public ResourceBundleServiceImpl() {
	}

	public ResourceBundleServiceImpl(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String getText(String key, Object[] params) {
		Language language=Language.valueOf(AppParameters.defaultlang);
		if(request!=null) {
			if(request.getAttribute("language")!=null) {
				language=Language.valueOf(request.getAttribute("language").toString());
			}
		}
		
		Locale locale=language.locale;
		
		ResourceBundle resourceBundle=ResourceBundle.getBundle("resource_bundle", locale);
		
		if(resourceBundle.containsKey(key)) {
			return MessageFormat.format(resourceBundle.getString(key), params);
		}else {
			return MessageFormat.format(key, params);
		}
	}

}
