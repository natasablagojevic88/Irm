package rs.irm.administration.service;

import rs.irm.administration.entity.ReportJob;

public interface LoadReportJobService {

	void loadJob(ReportJob reportJob);
	
	void removeJob(ReportJob reportJob);
}
