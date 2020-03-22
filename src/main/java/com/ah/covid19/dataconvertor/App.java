package com.ah.covid19.dataconvertor;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class App {
    private static final Properties config = new Properties();

    private static final int column_province = 0;
    private static final int column_country = 1;
    private static final int column_latitude = 2;
    private static final int column_longitude = 3;

    static {
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
            }
            //load a properties file from class path, inside static method
            config.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static final String confirmed_case_timeseries_csv_filepath = config.getProperty("confirmed_case_timeseries_csv_filepath");
    private static final String death_case_timeseries_csv_filepath = config.getProperty("death_case_timeseries_csv_filepath");
    private static final String recovered_case_timeseries_csv_filepath = config.getProperty("recovery_case_timeseries_csv_filepath");

    public static JSONObject readCsvFile(String filepath) {
        File csvFile = new File(filepath);
        JSONArray locationArray = new JSONArray();

        try (CSVReader csvReader = new CSVReader(new FileReader(csvFile))) {
            String[] parsedDateLine = csvReader.readNext();
            String[] parsedLine;
            while ((parsedLine = csvReader.readNext()) != null) {
                JSONObject location = new JSONObject();
                location.put("country", parsedLine[column_country]);

                if (StringUtils.isNotBlank(parsedLine[column_province])) {
                    location.put("province", parsedLine[column_province]);
                }

                JSONObject geography = new JSONObject();
                geography.put("latitude", Float.parseFloat(parsedLine[column_latitude]));
                geography.put("longitude", Float.parseFloat(parsedLine[column_longitude]));
                location.put("geography", geography);

                JSONArray accumulatedCases = new JSONArray();
                int previousDayAccumulated = 0;
                for (int i=column_longitude+1; i<parsedLine.length-1; i++) {
                    JSONObject dailyCase = new JSONObject();
                    dailyCase.put("date", parsedDateLine[i]);
                    int todayAccumulated = Integer.parseInt(parsedLine[i]);
                    dailyCase.put("new", todayAccumulated - previousDayAccumulated);
                    dailyCase.put("accumulated", todayAccumulated);
                    accumulatedCases.put(dailyCase);
                    previousDayAccumulated = todayAccumulated;
                }
                location.put("cases", accumulatedCases);
                locationArray.put(location);
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }

        JSONObject result = new JSONObject();
        result.put("locations", locationArray);
        return result;
    }


    public static void writeToFile(JSONObject json) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.json"))) {
            writer.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JSONObject confirmed = App.readCsvFile(App.confirmed_case_timeseries_csv_filepath);
        JSONObject death = App.readCsvFile(App.death_case_timeseries_csv_filepath);
        JSONObject recovered = App.readCsvFile(App.recovered_case_timeseries_csv_filepath);

        JSONObject output = new JSONObject();
        output.put("confirmed", confirmed);
        output.put("deaths", death);
        output.put("recovered", recovered);
        App.writeToFile(output);
    }
}
