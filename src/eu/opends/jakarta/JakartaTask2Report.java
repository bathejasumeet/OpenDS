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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;

import eu.opends.drivingTask.settings.SettingsLoader.Setting;
import eu.opends.main.SimulationDefaults;
import eu.opends.main.Simulator;
import eu.opends.tools.Util;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JaxenXmlDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/**
 * 
 * @author Rafael Math
 */
public class JakartaTask2Report {
	private String dataFileName = "JakartaTask2ReportData.xml";
	private String reportFileName = "JakartaTask2Report.pdf";
	private String outputFolder;
	BufferedWriter bw;

	public void generateReport(String driverName, boolean front30to35, boolean front35to50, boolean frontLessThan30,
			boolean side2to5, boolean sideLessThan2, boolean idealLine, int score) {
		try {
			outputFolder = Simulator.getOutputFolder();
			Util.makeDirectory(outputFolder);

			bw = new BufferedWriter(new FileWriter(outputFolder + "/" + dataFileName));
			System.out.println(outputFolder);
			bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			bw.write("<report>\n");

			bw.write("\t<driverName>" + driverName + "</driverName>\n");
			bw.write("\t<front30to35>" + front30to35 + "</front30to35>\n");
			bw.write("\t<front35to50>" + front35to50 + "</front35to50>\n");
			bw.write("\t<frontLess30>" + frontLessThan30 + "</frontLess30>\n");
			bw.write("\t<side2to5>" + side2to5 + "</side2to5>\n");
			bw.write("\t<sideLessThan2>" + sideLessThan2 + "</sideLessThan2>\n");
			bw.write("\t<idealLine>" + idealLine + "</idealLine>\n");
			bw.write("\t<score>" + score + "</score>\n");

			bw.write("</report>\n");
			bw.close();

			// open XML data source
			JRDataSource dataSource = new JaxenXmlDataSource(new File(outputFolder + "/" + dataFileName), "report");

			// get report template for reaction measurement
			InputStream inputStream = new FileInputStream("assets/JasperReports/templates/JakartaTask2Template.jrxml");
			JasperDesign design = JRXmlLoader.load(inputStream);
			JasperReport report = JasperCompileManager.compileReport(design);

			// fill report with parameters and data

			JasperPrint print = JasperFillManager.fillReport(report, new HashMap<String, Object>(), dataSource);

			// create PDF file
			long start = System.currentTimeMillis();
			JasperExportManager.exportReportToPdfFile(print, outputFolder + "/" + reportFileName);
			System.out.println("PDF creation time : " + (System.currentTimeMillis() - start) + " ms");

			// open PDF file
			boolean suppressPDF = Simulator.getSettingsLoader().getSetting(Setting.Analyzer_suppressPDFPopup,
					SimulationDefaults.Analyzer_suppressPDFPopup);

			if (!suppressPDF)
				Util.open(outputFolder + "/" + reportFileName);

		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
