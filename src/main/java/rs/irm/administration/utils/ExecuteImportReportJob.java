package rs.irm.administration.utils;

import java.io.File;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import jakarta.ws.rs.WebApplicationException;
import rs.irm.administration.dto.ReportJobDTO;
import rs.irm.common.exceptions.CommonException;
import rs.irm.common.exceptions.ExcelRowException;
import rs.irm.common.service.CommonService;
import rs.irm.common.service.impl.CommonServiceImpl;
import rs.irm.database.service.DatatableService;
import rs.irm.database.service.impl.DatatableServiceImpl;
import rs.irm.database.utils.ExecuteMethod;
import rs.irm.preview.utils.ExcelImport;

public class ExecuteImportReportJob implements ExecuteMethod {

	private ReportJobDTO reportJobDTO;
	private DatatableService datatableService;
	private CommonService commonService;

	public ExecuteImportReportJob(ReportJobDTO reportJobDTO) {
		this.reportJobDTO = reportJobDTO;
		this.datatableService = new DatatableServiceImpl();
		this.commonService = new CommonServiceImpl(null);
	}

	@Override
	public void execute(Connection connection) {

		File filePath = new File(this.reportJobDTO.getFilePath());

		if (!(filePath.exists() && filePath.isDirectory())) {
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noPath:" + this.reportJobDTO.getFilePath(),
					null);
		}

		File[] listFiles = filePath.listFiles();

		List<File> importFiles = new ArrayList<>();

		for (File file : Arrays.asList(listFiles)) {
			if (!file.isFile()) {
				continue;
			}

			if (commonService.hasText(reportJobDTO.getFileName())) {
				if (!file.getName().startsWith(reportJobDTO.getFileName())) {
					continue;
				}
			}
			importFiles.add(file);

		}

		File inprogressPath = new File(filePath.getAbsolutePath() + "/inprogress");
		if (!(inprogressPath.exists() && inprogressPath.isDirectory())) {
			inprogressPath.mkdirs();
		}

		File backupPath = new File(filePath.getAbsolutePath() + "/backup");
		if (!(backupPath.exists() && backupPath.isDirectory())) {
			backupPath.mkdirs();
		}

		File errorPath = new File(filePath.getAbsolutePath() + "/error");
		if (!(errorPath.exists() && errorPath.isDirectory())) {
			errorPath.mkdirs();
		}

		Long parentId = -1L;
		try {
			if (commonService.hasText(reportJobDTO.getParentQuery())) {
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(reportJobDTO.getParentQuery());

				if (resultSet.next()) {
					if (resultSet.getObject(1) == null) {
						throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noParent", null);
					} else {
						parentId = Long.valueOf(resultSet.getObject(1).toString());
					}
				} else {
					throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, "noParent", null);
				}

			}

		} catch (Exception e) {
			throw new WebApplicationException(e);
		}

		LinkedHashMap<String, String> errorFiles = new LinkedHashMap<String, String>();

		for (File file : importFiles) {
			String fileName = file.getName();
			String inprogressFile = inprogressPath.getAbsolutePath() + "/" + file.getName();
			String backupFile = backupPath.getAbsolutePath() + "/" + file.getName();
			String errorFile = errorPath.getAbsolutePath() + "/" + file.getName();
			try {

				Files.move(Paths.get(file.getAbsolutePath()), Paths.get(inprogressFile),
						StandardCopyOption.REPLACE_EXISTING);

				File importFile = new File(inprogressFile);

				try {
					ExcelImport excelImport = new ExcelImport(null, reportJobDTO.getModelId(), parentId, importFile);
					this.datatableService.executeMethodWithReturn(excelImport);
					Files.move(Paths.get(importFile.getAbsolutePath()), Paths.get(backupFile),
							StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception e) {
					Files.move(Paths.get(importFile.getAbsolutePath()), Paths.get(errorFile),
							StandardCopyOption.REPLACE_EXISTING);
					throw e;
				}

			} catch (Exception e) {

				Exception lastException = e;
				while (lastException.getCause() != null) {
					lastException = (Exception) lastException.getCause();
				}
				
				if(lastException.getClass().getCanonicalName().equals(ExcelRowException.class.getCanonicalName())){
					ExcelRowException excelRowException=(ExcelRowException)lastException;
					errorFiles.put(fileName,excelRowException.getRow()+": "+ lastException.getMessage());
				}else {
					errorFiles.put(fileName, lastException.getMessage());
				}

				

			}
		}
		
		if(!errorFiles.isEmpty()) {
			String error="";
			
			Iterator<String> iterator=errorFiles.keySet().iterator();
			
			while(iterator.hasNext()) {
				String fileName=iterator.next();
				error+=fileName+":"+errorFiles.get(fileName)+"  ";
			}
			
			throw new CommonException(HttpURLConnection.HTTP_BAD_REQUEST, error, null);
		}

	}

}
