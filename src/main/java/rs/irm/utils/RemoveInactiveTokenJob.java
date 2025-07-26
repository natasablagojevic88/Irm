package rs.irm.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;

public class RemoveInactiveTokenJob implements Job {
	Logger logger = LogManager.getLogger(CheckConnectionJob.class);

	DatatableService datatableService = new DatatableServiceImpl();

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			RemoveInactiveToken removeInactiveToken = new RemoveInactiveToken();
			this.datatableService.executeMethod(removeInactiveToken);
		} catch (Exception e) {
			Exception lastException = e;
			while (lastException.getCause() != null) {
				lastException = (Exception) lastException.getCause();
			}
			logger.error(e.getMessage(), e);

		}

	}

}
