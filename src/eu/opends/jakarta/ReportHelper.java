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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.math.FastMath;

import eu.opends.car.Car;
import eu.opends.jasperReport.JasperReport;
import eu.opends.main.Simulator;

/**
 * Class for handling database connection and logging to database
 * 
 * @author Rafael Math
 */
public class ReportHelper extends JasperReport {

	/**
	 * Constructor, that creates database connection and prepared statement for
	 * fast query execution
	 */
	public ReportHelper(String reportTemplate, String url, String user, String pass, String table,
			boolean useAdditionalTable, float maxDeviation) {
		super(reportTemplate, url, user, pass, table, useAdditionalTable);

		try {

			// Creating prepared statement for faster query execution all "?"
			// then have to be assigned some value using
			// statement.set[Float,Int,Long,String,etc]
			statement = connection.prepareStatement(getInsertStatement(table));

			PreparedStatement clearStatement = connection.prepareStatement("TRUNCATE TABLE " + table);
			clearStatement.executeUpdate();

			if (useAdditionalTable) {
				String additionalTable = table + "_" + Simulator.getOutputFolder().replace("analyzerData/", "");

				// create new table
				String createStatement = getCreateStatement(additionalTable);
				PreparedStatement newTableStatement = connection.prepareStatement(createStatement);
				newTableStatement.executeUpdate();

				additionalStatement = connection.prepareStatement(getInsertStatement(additionalTable));
			}

			parameters.put("maxDeviation", new Float(FastMath.abs(maxDeviation)));

		} catch (Exception e) {

			e.getStackTrace();
		}
	}

	private String getInsertStatement(String table) {
		return "INSERT INTO `" + table + "` (timestamp, translation) VALUES (?, ?);";
	}

	private String getCreateStatement(String table) {
		return "CREATE TABLE IF NOT EXISTS `" + table + "` (" + "`timestamp` bigint(13) default NULL,"
				+ "`translation` varchar(100) default NULL,";
	}

	// Method, which writes record to database. It assigns to each "?" in
	// prepared statement definite value and then executes
	// update inserting record in database
	public void addDataSet(long timestamp, String translation) {
		try {
			statement.setLong(1, timestamp);
			statement.setString(2, translation);

			statement.executeUpdate();

			if (useAdditionalTable) {
				additionalStatement.setLong(1, timestamp);
				additionalStatement.setString(2, translation);
				additionalStatement.executeUpdate();
			}

		} catch (SQLException ex) {

			Logger.getLogger(JasperReport.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void recordTranslation(String translation) {
		HashMap<Long, String> trail = new HashMap<>();
		long timestamp = new Date().getTime();
		trail.put(timestamp, translation);
	}

}
