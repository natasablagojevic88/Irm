package rs.irm.administration.service.impl;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.entity.ReportJob;
import rs.irm.administration.service.LoadReportJobService;
import rs.irm.administration.utils.ExecuteReportJob;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.service.impl.AppInitServiceImpl;

@Named
public class LoadReportJobServiceImpl implements LoadReportJobService {
	public static String jobGroup = "ReportJob";
	private String jobName;
	private String triggerName;

	@Override
	public void loadJob(ReportJob reportJob) {
		this.jobName = reportJob.getName();
		this.triggerName = reportJob.getId().toString();
		try {
			if (AppInitServiceImpl.scheduler.checkExists(new JobKey(jobName, jobGroup))) {
				if (reportJob.getActive()) {
					Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName, jobGroup)
							.forJob(new JobKey(jobName, jobGroup))
							.withSchedule(CronScheduleBuilder.cronSchedule(reportJob.getCron())).build();
					if (AppInitServiceImpl.scheduler.checkExists(new TriggerKey(triggerName, jobGroup))) {

						AppInitServiceImpl.scheduler.rescheduleJob(new TriggerKey(triggerName, jobGroup), trigger);
					} else {
						AppInitServiceImpl.scheduler.scheduleJob(trigger);
					}
				} else {
					if (AppInitServiceImpl.scheduler.checkExists(new TriggerKey(triggerName, jobGroup))) {

						AppInitServiceImpl.scheduler.unscheduleJob(new TriggerKey(triggerName, jobGroup));
						
						JobDetail job = JobBuilder.newJob(ExecuteReportJob.class).storeDurably()
								.withIdentity(jobName, jobGroup).build();
						AppInitServiceImpl.scheduler.addJob(job, true);
					}
				}

			} else {
				if (reportJob.getActive()) {
					JobDetail job = JobBuilder.newJob(ExecuteReportJob.class).withIdentity(jobName, jobGroup).build();
					Trigger trigger = TriggerBuilder.newTrigger().forJob(job).withIdentity(triggerName, jobGroup)
							.withSchedule(CronScheduleBuilder.cronSchedule(reportJob.getCron())).build();
					AppInitServiceImpl.scheduler.scheduleJob(job, trigger);
				} else {
					JobDetail job = JobBuilder.newJob(ExecuteReportJob.class).storeDurably()
							.withIdentity(jobName, jobGroup).build();
					AppInitServiceImpl.scheduler.addJob(job, true);
				}

			}
		} catch (SchedulerException e) {
			throw new WebApplicationException(e);
		}

	}

	@Override
	public void removeJob(ReportJob reportJob) {
		this.jobName = reportJob.getName();
		try {
			if (AppInitServiceImpl.scheduler.checkExists(new JobKey(jobName, jobGroup))) {
				AppInitServiceImpl.scheduler.deleteJob(new JobKey(jobName, jobGroup));
				
				if(ModelData.reportJobStates.containsKey(reportJob.getId())) {
					ModelData.reportJobStates.remove(reportJob.getId());
				}
			}
		} catch (SchedulerException e) {
			throw new WebApplicationException(e);
		}

	}

}
