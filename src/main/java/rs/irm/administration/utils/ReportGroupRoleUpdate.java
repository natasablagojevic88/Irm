package rs.irm.administration.utils;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import rs.irm.administration.dto.ReportGroupRoleDTO;
import rs.irm.administration.entity.ReportGroup;
import rs.irm.administration.entity.ReportGroupRole;
import rs.irm.administration.entity.Role;
import rs.irm.common.exceptions.CommonException;
import rs.irm.database.dto.TableParameterDTO;
import rs.irm.database.enums.SearchOperation;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.database.utils.TableFilter;

public class ReportGroupRoleUpdate implements ExecuteMethod {

	private ReportGroupRoleDTO reportGroupRoleDTO;
	private Long reportGroupId;
	private HttpServletRequest httpServletRequest;
	private DatatableService datatableService;

	public ReportGroupRoleUpdate(ReportGroupRoleDTO reportGroupRoleDTO, Long reportGroupId,
			HttpServletRequest httpServletRequest) {
		this.reportGroupRoleDTO = reportGroupRoleDTO;
		this.reportGroupId = reportGroupId;
		this.httpServletRequest = httpServletRequest;
		this.datatableService = new DatatableServiceImpl(this.httpServletRequest);
	}

	@Override
	public void execute(Connection connection) {
		ReportGroup reportGroup = this.datatableService.findByExistingId(reportGroupId, ReportGroup.class, connection);

		TableParameterDTO tableParameterDTO = new TableParameterDTO();
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("reportGroup", SearchOperation.equals, String.valueOf(reportGroup.getId()), null));
		tableParameterDTO.getTableFilters()
				.add(new TableFilter("role", SearchOperation.equals, String.valueOf(reportGroupRoleDTO.getId()), null));

		List<ReportGroupRole> list = this.datatableService.findAll(tableParameterDTO, ReportGroupRole.class,
				connection);

		if (reportGroupRoleDTO.getHasRight() && (!list.isEmpty())) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "groupAlredyHasRole", null);
		}

		if ((!reportGroupRoleDTO.getHasRight()) && list.isEmpty()) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "groupDoesntHaveRole", null);
		}

		if (reportGroupRoleDTO.getHasRight()) {

			Role role = new Role(reportGroupRoleDTO.getId());
			ReportGroupRole reportGroupRole = new ReportGroupRole();
			reportGroupRole.setId(0L);
			reportGroupRole.setReportGroup(reportGroup);
			reportGroupRole.setRole(role);

			reportGroupRole = this.datatableService.save(reportGroupRole, connection);
		} else {
			this.datatableService.delete(list.get(0), connection);
		}

	}

}
