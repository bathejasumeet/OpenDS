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

public class JakartaTask8Report {
	private String dataFileName = "JakartaTask8ReportData.xml";
	private String reportFileName = "JakartaTask8Report.pdf";
	private String outputFolder;
	BufferedWriter bw;

	public void generateReport(String driverName, boolean score40kmh, boolean score20kmh, boolean score) {
		try {
			outputFolder = Simulator.getOutputFolder();
			Util.makeDirectory(outputFolder);

			bw = new BufferedWriter(new FileWriter(outputFolder + "/" + dataFileName));
			System.out.println(outputFolder);
			bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			bw.write("<report>\n");

			bw.write("\t<driverName>" + driverName + "</driverName>\n");
			bw.write("\t<score40kmh>" + score40kmh + "</score40kmh>\n");
			bw.write("\t<score20kmh>" + score20kmh + "</score20kmh>\n");
			bw.write("\t<score>" + score + "</score>\n");

			bw.write("</report>\n");
			bw.close();

			// open XML data source
			JRDataSource dataSource = new JaxenXmlDataSource(new File(outputFolder + "/" + dataFileName), "report");

			// get report template for reaction measurement
			InputStream inputStream = new FileInputStream("assets/JasperReports/templates/JakartaTask8Template.jrxml");
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
