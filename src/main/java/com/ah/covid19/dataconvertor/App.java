package com.ah.covid19.dataconvertor;

import com.ah.covid19.dataconvertor.model.Covid19Case;
import com.ah.covid19.dataconvertor.model.Location;

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
    private static final String timeseries_date_format = CONFIG.getProperty("timeseries_date_format");

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        //Map<Location, List<Case>> timeSeriesCSVFileToMap = CSVUtils.readTimeSeriesCSVFileToMap(new File(confirmed_case_timeseries_csv_filepath), timeseries_date_format);
        //GoogleSheetUtils.uploadCsvToGoogleSheet(google_spreadsheet_id, timeSeriesCSVFileToMap, "Confirmed!A:ZZ");

        GregorianCalendar startDate = new GregorianCalendar();
        startDate.set(2020,0,22);

        GregorianCalendar endDate = new GregorianCalendar();
        endDate.add(Calendar.DAY_OF_MONTH, -1);
        Map<Location, List<Covid19Case>> dailyReportCSVFileToMap = CSVUtils.readDailyReportCSVFileToMap(daily_reports_csv_filepath, startDate, endDate, daily_reports_filename_pattern);
        Map<Location, List<Covid19Case>> g75eyeReport = CSVUtils.generateG7DailyReport(dailyReportCSVFileToMap);
        GoogleSheetUtils.uploadConfirmedCaseToGoogleSheet(google_spreadsheet_id, dailyReportCSVFileToMap, "Confirmed!A:ZZ");
        GoogleSheetUtils.uploadDailyNewCaseToGoogleSheet(google_spreadsheet_id, dailyReportCSVFileToMap, "Daily New Cases!A:ZZ");
        GoogleSheetUtils.uploadDeathCaseToGoogleSheet(google_spreadsheet_id, dailyReportCSVFileToMap, "Death!A:ZZ");
        GoogleSheetUtils.uploadDailyNewDeathToGoogleSheet(google_spreadsheet_id, dailyReportCSVFileToMap, "Daily New Death!A:ZZ");
        GoogleSheetUtils.uploadDailyConfirmedG75EToGoogleSheet(google_spreadsheet_id, g75eyeReport, "G7E5!A:ZZ");
        GoogleSheetUtils.uploadDailyNewConfirmedG75EToGoogleSheet(google_spreadsheet_id, g75eyeReport, "G7E5!A10:ZZ");
    }
}
