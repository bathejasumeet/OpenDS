/*
*  This file is part of OpenDS (Open Source Driving Simulator).
*  Copyright (C) 2016 Rafael Math
*
*  OpenDS is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  OpenDS is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with OpenDS. If not, see <http://www.gnu.org/licenses/>.
*/
package eu.opends.jakarta;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.opends.drivingTask.settings.SettingsLoader.Setting;
import eu.opends.main.SimulationDefaults;
import eu.opends.main.Simulator;
import eu.opends.tools.Util;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

public class JakartaReport {
	protected String reportTemplate;
	protected String driverName;
	protected int score;

	protected String outputFolder;
	protected String fileName = "JakartaReport.pdf";
	protected boolean createReport = true;
	protected boolean openReport = true;
	protected Map<String, Object> parameters;

	public JakartaReport(String reportTemplate, String driverName, int score) {
		this.reportTemplate = reportTemplate;
		this.outputFolder = Simulator.getOutputFolder();

		String tempFileName = Simulator.getSettingsLoader().getSetting(Setting.Analyzer_fileName, "");
		if (tempFileName != null && !tempFileName.isEmpty())
			fileName = tempFileName;

		boolean suppressOpen = Simulator.getSettingsLoader().getSetting(Setting.Analyzer_suppressPDFPopup,
				SimulationDefaults.Analyzer_suppressPDFPopup);

		openReport = !suppressOpen;

		this.parameters = new HashMap<String, Object>();
	}

	public void createPDF() {
		try {

			boolean reportCreated = false;

			if (createReport)
				reportCreated = createReport();

			if (reportCreated && openReport)
				Util.open(outputFolder + "/" + fileName);

		} catch (Exception ex) {
			Logger.getLogger(JakartaReport.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private boolean createReport() {
		boolean success = false;

		try {
			// get report template for driver or passenger task
			InputStream reportStream = new FileInputStream(reportTemplate);

			// fill report with parameters and data from database
			JasperPrint print = JasperFillManager.fillReport(reportStream, parameters);

			// create PDF file
			long start = System.currentTimeMillis();
			JasperExportManager.exportReportToPdfFile(print, outputFolder + "/" + fileName);
			System.out.println("PDF creation time : " + (System.currentTimeMillis() - start) + " ms");

			success = true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return success;
	}

}
