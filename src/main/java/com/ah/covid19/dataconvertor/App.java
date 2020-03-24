package com.ah.covid19.dataconvertor;

import com.ah.covid19.dataconvertor.model.Case;
import com.ah.covid19.dataconvertor.model.Location;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.*;

public class App {
    private static final Properties CONFIG = new Properties();

    static {
        //setup to load the covid-19 data from CSSE-COVID-19 local git repo
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                System.exit(1);
            }
            //load a properties file from class path, inside static method
            CONFIG.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static final String confirmed_case_timeseries_csv_filepath = CONFIG.getProperty("confirmed_case_timeseries_csv_filepath");
    private static final String death_case_timeseries_csv_filepath = CONFIG.getProperty("death_case_timeseries_csv_filepath");
    private static final String recovered_case_timeseries_csv_filepath = CONFIG.getProperty("recovery_case_timeseries_csv_filepath");
    private static final String daily_reports_csv_filepath = CONFIG.getProperty("daily_reports_csv_filepath");

    private static final String google_spreadsheet_id = CONFIG.getProperty("google_spreadsheet_id");

    private static final String daily_reports_filename_pattern = CONFIG.getProperty("daily_reports_filename_pattern");

    public static void main(String[] args) throws GeneralSecurityException, IOException {

        /**
        JSONObject confirmed = Utils.convertCSVFiletoJSONObject(new File(App.confirmed_case_timeseries_csv_filepath));
        JSONObject death = Utils.convertCSVFiletoJSONObject(new File(App.death_case_timeseries_csv_filepath));
        JSONObject recovered = Utils.convertCSVFiletoJSONObject(new File(App.recovered_case_timeseries_csv_filepath));

        JSONObject output = new JSONObject();
        output.put("confirmed", confirmed);
        output.put("deaths", death);
        output.put("recovered", recovered);
        Utils.writeToFile(output);
         **/
        Map<Location, List<Case>> timeSeriesCSVFileToMap = CSVHelper.readTimeSeriesCSVFileToMap(new File(confirmed_case_timeseries_csv_filepath));
        //GoogleSheetHelper.uploadCsvToGoogleSheet(google_spreadsheet_id, timeSeriesCSVFileToMap, "Confirmed!A:ZZ");

        GregorianCalendar startDate = new GregorianCalendar();
        startDate.set(2020,0,22);

        GregorianCalendar endDate = new GregorianCalendar();
        endDate.add(Calendar.DAY_OF_MONTH, -1);
        Map<Location, List<Case>> dailyReportCSVFileToMap = CSVHelper.readDailyReportCSVFileToMap(daily_reports_csv_filepath, startDate, endDate, daily_reports_filename_pattern);

        GoogleSheetHelper.uploadCsvToGoogleSheet(google_spreadsheet_id, dailyReportCSVFileToMap, "Daily!A:ZZ");
    }
}
