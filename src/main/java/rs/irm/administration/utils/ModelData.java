package rs.irm.administration.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import rs.irm.administration.dto.DashboardDTO;
import rs.irm.administration.dto.DashboardRoleInfoDTO;
import rs.irm.administration.dto.ModelColumnDTO;
import rs.irm.administration.dto.ModelDTO;
import rs.irm.administration.dto.ModelJasperReportDTO;
import rs.irm.administration.dto.ModelProcedureDTO;
import rs.irm.administration.dto.ReportDTO;
import rs.irm.administration.dto.ReportGroupDTO;
import rs.irm.administration.dto.ReportGroupRolesDTO;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.administration.dto.ReportJobSessionDTO;

public class ModelData {

	public static List<ModelDTO> listModelDTOs = new ArrayList<>();
	public static List<ModelColumnDTO> listColumnDTOs=new ArrayList<>();
	public static List<ModelJasperReportDTO> listModelJasperReportDTOs=new ArrayList<>();
	public static List<ReportGroupDTO> listReportGroupDTOs=new ArrayList<>();
	public static List<ReportGroupRolesDTO> listReportGroupRolesDTOs=new ArrayList<>();
	public static List<ReportDTO> listReportDTOs=new ArrayList<>();
	public static List<DashboardDTO> listDashboardDTOs=new ArrayList<>();
	public static List<DashboardRoleInfoDTO> dashboardRoleDtos=new ArrayList<>();
	public static List<ReportJobDTO> listReportJobDTOs=new ArrayList<>();
	public static LinkedHashMap<Long, ReportJobSessionDTO> reportJobStates=new LinkedHashMap<>();
	public static List<ModelProcedureDTO> modelProcedureDTOs=new ArrayList<>();
}
