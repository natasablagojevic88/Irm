package rs.irm.administration.enums;

public enum ReportJobFileType {
	
	PDF("pdf"),
	EXCEL("xlsx"),
	CSV("csv");
	
	public String extension;

	private ReportJobFileType(String extension) {
		this.extension = extension;
	}
	

}
