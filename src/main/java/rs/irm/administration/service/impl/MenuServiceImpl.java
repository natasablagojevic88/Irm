package rs.irm.administration.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import rs.irm.administration.dto.DashboardDTO;
import rs.irm.administration.dto.DashboardRoleInfoDTO;
import rs.irm.administration.dto.MenuDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.ReportGroupDTO;
import rs.irm.administration.entity.DefaultDashboard;
import rs.irm.administration.enums.ModelType;
import rs.irm.administration.service.MenuService;
import rs.irm.administration.utils.ModelData;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.ResourceBundleService;
import rs.irm.common.utils.CheckAdmin;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.utils.TableFilter;
import rs.irm.preview.dto.DashboardResultDTO;
import rs.irm.preview.service.PreviewDashboardService;

@Named
public class MenuServiceImpl implements MenuService {

	@Inject
	private CommonService commonService;

	@Inject
	private ResourceBundleService resourceBundleService;
	
	@Inject
	private DatatableService datatableService;
	
	@Inject
	private PreviewDashboardService previewDashboardService;

	@Override
	public List<MenuDTO> getMenu() {
		List<MenuDTO> list = new ArrayList<>();
		if (commonService.getRoles().contains(CheckAdmin.roleAdmin)) {
			MenuDTO administration = new MenuDTO();
			administration.setIcon("fa fa-user-circle-o");
			administration.setName(resourceBundleService.getText("administration", null));
			list.add(administration);

			MenuDTO users = new MenuDTO();
			users.setIcon("fa fa-user");
			users.setName(resourceBundleService.getText("users", null));
			users.setUrl("/administration/appusers");
			administration.getChildren().add(users);

			MenuDTO roles = new MenuDTO();
			roles.setIcon("fa fa-users");
			roles.setName(resourceBundleService.getText("roles", null));
			roles.setUrl("/administration/roles");
			administration.getChildren().add(roles);

			MenuDTO modeling = new MenuDTO();
			modeling.setIcon("fa fa-database");
			modeling.setName(resourceBundleService.getText("modeling", null));
			modeling.setUrl("/administration/modeling");
			administration.getChildren().add(modeling);

			MenuDTO sqlExecutor = new MenuDTO();
			sqlExecutor.setIcon("fa fa-terminal");
			sqlExecutor.setName(resourceBundleService.getText("sqlExecutor", null));
			sqlExecutor.setUrl("/administration/sqlexecutor");
			administration.getChildren().add(sqlExecutor);

			MenuDTO reports = new MenuDTO();
			reports.setIcon("fa fa-line-chart");
			reports.setName(resourceBundleService.getText("reports", null));
			reports.setUrl("/administration/reports");
			administration.getChildren().add(reports);

			MenuDTO dashoards = new MenuDTO();
			dashoards.setIcon("fa fa-tachometer");
			dashoards.setName(resourceBundleService.getText("dashoards", null));
			dashoards.setUrl("/administration/dashboards");
			administration.getChildren().add(dashoards);
			
			MenuDTO mailservers = new MenuDTO();
			mailservers.setIcon("fa fa-envelope");
			mailservers.setName(resourceBundleService.getText("SmtpServerDTO.title", null));
			mailservers.setUrl("/administration/mailservers");
			administration.getChildren().add(mailservers);
			
			MenuDTO jobs = new MenuDTO();
			jobs.setIcon("fa fa-clock-o");
			jobs.setName(resourceBundleService.getText("JobInfoDTO.title", null));
			jobs.setUrl("/administration/jobs");
			administration.getChildren().add(jobs);
		}

		addModel(list);

		MenuDTO reports = addReports();
		if (!reports.getChildren().isEmpty()) {
			list.add(reports);
		}

		MenuDTO dashboards = addDashboards();
		if (!dashboards.getChildren().isEmpty()) {
			list.add(dashboards);
		}

		return list;
	}

	void addModel(List<MenuDTO> list) {

		List<ModelDTO> withoutParent = ModelData.listModelDTOs.stream().filter(a -> a.getParentId() == null)
				.sorted(Comparator.comparing(ModelDTO::getName)).toList();

		boolean isAdmin = this.commonService.getRoles().contains(CheckAdmin.roleAdmin);
		Set<Long> emptyMenu = new HashSet<>();
		for (ModelDTO modelDTO : withoutParent) {
			if (isAdmin || this.commonService.getRoles().contains(modelDTO.getPreviewRoleCode())) {
				MenuDTO menuDTO = new MenuDTO();
				menuDTO.setIcon(modelDTO.getIcon());
				menuDTO.setName(resourceBundleService.getText(modelDTO.getName(), null));

				checkChildren(menuDTO, modelDTO, isAdmin, emptyMenu);

				if (checkAdd(modelDTO, emptyMenu)) {
					list.add(menuDTO);
				}

			}
		}
	}

	private List<ModelDTO> findChildrenModel(ModelDTO modelDTO) {
		return ModelData.listModelDTOs.stream().filter(a -> a.getParentId() != null)
				.filter(a -> a.getParentId() == modelDTO.getId()).toList();
	}

	private boolean checkAdd(ModelDTO modelDTO, Set<Long> emptyMenu) {
		return !ModelData.listModelDTOs.stream().filter(a -> a.getParentId() != null)
				.filter(a -> a.getParentId() == modelDTO.getId()).filter(a -> !emptyMenu.contains(a.getId())).toList()
				.isEmpty();
	}

	private void checkChildren(MenuDTO menuDTO, ModelDTO modelDTO, boolean isAdmin, Set<Long> emptyMenu) {
		List<ModelDTO> children = findChildrenModel(modelDTO);

		for (ModelDTO child : children) {
			if (isAdmin || commonService.getRoles().contains(child.getPreviewRoleCode())) {
				MenuDTO childMenu = new MenuDTO();
				childMenu.setIcon(child.getIcon());
				childMenu.setName(resourceBundleService.getText(child.getName(), null));
				childMenu.setUrl(child.getType().equals(ModelType.MENU.name()) ? null : "/preview/previewmodel");
				checkChildren(childMenu, child, isAdmin, emptyMenu);

				if (child.getType().equals(ModelType.TABLE.name())
						&& (!modelDTO.getType().equals(ModelType.TABLE.name()))) {
					childMenu.setId(child.getId());
					menuDTO.getChildren().add(childMenu);
				} else {
					if (checkAdd(child, emptyMenu)) {
						menuDTO.getChildren().add(childMenu);
					} else {
						emptyMenu.add(child.getId());
					}
				}

			} else {
				emptyMenu.add(child.getId());
			}

		}
	}

	private MenuDTO addReports() {
		MenuDTO reports = new MenuDTO();
		reports.setIcon("fa fa-area-chart");
		reports.setName(resourceBundleService.getText("reports", null));

		boolean isAdmin = this.commonService.getRoles().contains(CheckAdmin.roleAdmin);

		List<ReportGroupDTO> groups = new ArrayList<>(ModelData.listReportGroupDTOs);
		for (ReportGroupDTO group : groups) {
			group.setName(resourceBundleService.getText(group.getName(), null));
		}

		List<ReportDTO> reportDTOs = new ArrayList<>(ModelData.listReportDTOs);

		groups = groups.stream().sorted(Comparator.comparing(ReportGroupDTO::getName)).toList();

		for (ReportGroupDTO group : groups) {

			List<String> rolesList = ModelData.listReportGroupRolesDTOs.stream()
					.filter(a -> a.getReportGroupId().doubleValue() == group.getId().doubleValue())
					.map(a -> a.getRoleCode()).toList();

			boolean hasRole = false;

			for (String role : rolesList) {
				if (commonService.getRoles().contains(role)) {
					hasRole = true;
					break;
				}
			}

			if (!(isAdmin || hasRole)) {
				continue;
			}
			MenuDTO reportsMenu = new MenuDTO();
			reportsMenu.setIcon("fa fa-list");
			reportsMenu.setName(group.getName());

			List<ReportDTO> reportDTOsForGroup = reportDTOs.stream()
					.filter(a -> a.getReportGroupId().doubleValue() == group.getId().doubleValue())
					.sorted(Comparator.comparing(ReportDTO::getName)).toList();

			for (ReportDTO reportDTO : reportDTOsForGroup) {
				MenuDTO report = new MenuDTO();
				String reportType = reportDTO.getType();
				String icon = "";
				switch (reportType) {
				case "STANDARD": {
					icon = "fa fa-table";
					break;
				}
				case "GRAPH": {
					icon = "fa fa-bar-chart";
					break;
				}
				case "KPI": {
					icon = "fa fa-id-card-o";
					break;
				}
				case "JASPER": {
					icon = "fa fa-file-pdf-o";
					break;
				}
				case "EXECUTE": {
					icon = "fa fa-bolt";
					break;
				}
				default: {
					icon = "fa fa-table";
				}
				}
				report.setIcon(icon);
				report.setName(reportDTO.getName());
				report.setId(reportDTO.getId());
				report.setUrl("/preview/previewreport");
				reportsMenu.getChildren().add(report);
			}

			if (!reportsMenu.getChildren().isEmpty()) {
				reports.getChildren().add(reportsMenu);
			}

		}

		return reports;
	}

	private MenuDTO addDashboards() {
		MenuDTO dashboards = new MenuDTO();
		dashboards.setIcon("fa fa-tachometer");
		dashboards.setName(resourceBundleService.getText("dashoards", null));

		boolean isAdmin = this.commonService.getRoles().contains(CheckAdmin.roleAdmin);

		List<DashboardDTO> dashboardDTOs = new ArrayList<>(ModelData.listDashboardDTOs);
		for (DashboardDTO dashboardDTO : dashboardDTOs) {
			dashboardDTO.setName(this.resourceBundleService.getText(dashboardDTO.getName(), null));
		}
		dashboardDTOs = dashboardDTOs.stream().sorted(Comparator.comparing(DashboardDTO::getName)).toList();

		for (DashboardDTO dashboardDTO : dashboardDTOs) {
			MenuDTO dashboardMenuDTO = new MenuDTO();
			dashboardMenuDTO.setIcon("fa fa-tachometer");
			dashboardMenuDTO.setId(dashboardDTO.getId());
			dashboardMenuDTO.setName(dashboardDTO.getName());
			dashboardMenuDTO.setUrl("/preview/previewdashboard");

			if (isAdmin) {
				dashboards.getChildren().add(dashboardMenuDTO);
				continue;
			}

			List<DashboardRoleInfoDTO> roles = ModelData.dashboardRoleDtos.stream()
					.filter(a -> a.getDashboardId().doubleValue() == dashboardDTO.getId().doubleValue()).toList();
			
			boolean hasRight=false;
			
			for(DashboardRoleInfoDTO role:roles) {
				if(this.commonService.getRoles().contains(role.getRoleCode())) {
					hasRight=true;
					break;
				}
			}
			
			if(!hasRight) {
				continue;
			}
			dashboards.getChildren().add(dashboardMenuDTO);
		}

		return dashboards;
	}

	@Override
	public DashboardResultDTO getDefaultDashboard() {
		TableParameterDTO tableParameterDTO=new TableParameterDTO();
		tableParameterDTO.getTableFilters().add(new TableFilter("appUser",SearchOperation.equals,String.valueOf(commonService.getAppUser().getId()),null));
		List<DefaultDashboard> defaultDashboards=this.datatableService.findAll(tableParameterDTO, DefaultDashboard.class);
		if(defaultDashboards.isEmpty()) {
			return null;
		}else {
			return this.previewDashboardService.getDashboardData(defaultDashboards.get(0).getDashboard().getId());
		}
	}

}
