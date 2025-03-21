package rs.irm.common.enums;

import java.util.Locale;

public enum Language {

	srRS(Locale.forLanguageTag("sr-RS")),
	srLatnRS(Locale.forLanguageTag("sr-Latn-RS")),
	enUS(Locale.forLanguageTag("en-US"));
	
	public Locale locale;

	private Language(Locale locale) {
		this.locale = locale;
	}
	
	
}
