package com.ah.covid19.dataconvertor;

import com.ah.covid19.dataconvertor.model.Covid19Case;
import com.ah.covid19.dataconvertor.model.Location;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class CSVUtils {
    private static final Logger logger = LogManager.getLogger(GoogleSheetUtils.class);

    private static final int COLUMN_PROVINCE = 0;
    private static final int COLUMN_COUNTRY = 1;
    private static final int COLUMN_LATITUDE = 2;
    private static final int COLUMN_LONGITUDE = 3;

    private static final int COLUMN_DAILY_REPORT_PROVINCE = 0;
    private static final int COLUMN_DAILY_REPORT_COUNTRY = 1;
    private static final int COLUMN_DAILY_REPORT_LASTUPDATE = 2;
    private static final int COLUMN_DAILY_REPORT_CONFIRMED = 3;
    private static final int COLUMN_DAILY_REPORT_DEATH = 4;
    private static final int COLUMN_DAILY_REPORT_RECOVERED = 5;

    private static final int NEW_COLUMN_DAILY_REPORT_PROVINCE = 2;
    private static final int NEW_COLUMN_DAILY_REPORT_COUNTRY = 3;
    private static final int NEW_COLUMN_DAILY_REPORT_LASTUPDATE = 4;
    private static final int NEW_COLUMN_DAILY_REPORT_CONFIRMED = 7;
    private static final int NEW_COLUMN_DAILY_REPORT_DEATH = 8;

    private static final Date newFormatDate = new GregorianCalendar(2020, Calendar.MARCH, 22).getTime();

    public static Location getStandardizedLocation(final String country, final String province) {
        String countryName = StringUtils.trim(country);
        String provinceName = StringUtils.trim(province);

        switch (countryName) {
            case "Austria":
                return new Location.LocationBuilder().country("Austria").build();
            case "China":
                if ("Hong Kong".equals(provinceName))
                    return new Location.LocationBuilder().country("Hong Kong").province("Hong Kong").build();
                else break;
            case "Mainland China":
                return new Location.LocationBuilder().country("China").province(provinceName).build();
            case "UK":
                return new Location.LocationBuilder().country("United Kingdom").province("United Kingdom").build();
            case "United Kingdom":
                return new Location.LocationBuilder().country("United Kingdom").province("UK".equals(provinceName) || StringUtils.isEmpty(provinceName) ? "United Kingdom" : provinceName).build();
            case "Australia":
                return new Location.LocationBuilder().country("Australia").province(StringUtils.isEmpty(provinceName) ? "New South Wales" : provinceName).build();
            case "Denmark":
                return new Location.LocationBuilder().country("Denmark").province(StringUtils.isEmpty(provinceName) ? "Denmark" : provinceName).build();
            case "France":
                return new Location.LocationBuilder().country("France").province(StringUtils.isEmpty(provinceName) ? "France" : provinceName).build();
            case "Macao SAR":
                return new Location.LocationBuilder().country("Macao").province("Macao").build();
            case "Hong Kong SAR":
                return new Location.LocationBuilder().country("Hong Kong").province("Hong Kong").build();
            case "Netherlands":
                return new Location.LocationBuilder().country("Netherlands").province(StringUtils.isEmpty(provinceName) ? "Netherlands" : provinceName).build();
            case "Taipei and environs":
            case "Taiwan*":
                return new Location.LocationBuilder().country("Taiwan").province("Taiwan").build();
            case "Korea, South":
            case "Republic of Korea":
                return new Location.LocationBuilder().country("South Korea").province(provinceName).build();
        }
        return new Location.LocationBuilder().country(countryName).province(provinceName).build();
    }

    public static Location readDailyReportCSVLocation(final String[] parsedLine, final Date fileDate) {
        if (newFormatDate.compareTo(fileDate) < 0) { //After newFormatDate, use the new format
            return getStandardizedLocation(parsedLine[NEW_COLUMN_DAILY_REPORT_COUNTRY], parsedLine[NEW_COLUMN_DAILY_REPORT_PROVINCE]);
        } else { //before that, use the old format
            return getStandardizedLocation(parsedLine[COLUMN_DAILY_REPORT_COUNTRY], parsedLine[COLUMN_DAILY_REPORT_PROVINCE]);
        }
    }

    public static int readDailyReportCSVConfirmedValue(final String[] parsedLine, final Date fileDate) {
        if (newFormatDate.compareTo(fileDate) < 0) { //After newFormatDate, use the new format
            return StringUtils.isEmpty(parsedLine[NEW_COLUMN_DAILY_REPORT_CONFIRMED]) ? 0 : Integer.valueOf(parsedLine[NEW_COLUMN_DAILY_REPORT_CONFIRMED]);
        } else { //before that, use the old format
            return StringUtils.isEmpty(parsedLine[COLUMN_DAILY_REPORT_CONFIRMED]) ? 0 : Integer.valueOf(parsedLine[COLUMN_DAILY_REPORT_CONFIRMED]);
        }
    }

    public static int readDailyReportCSVDeathValue(final String[] parsedLine, final Date fileDate) {
        if (newFormatDate.compareTo(fileDate) < 0) { //After newFormatDate, use the new format
            return StringUtils.isEmpty(parsedLine[NEW_COLUMN_DAILY_REPORT_DEATH]) ? 0 : Integer.valueOf(parsedLine[NEW_COLUMN_DAILY_REPORT_DEATH]);
        } else { //before that, use the old format
            return StringUtils.isEmpty(parsedLine[COLUMN_DAILY_REPORT_DEATH]) ? 0 : Integer.valueOf(parsedLine[COLUMN_DAILY_REPORT_DEATH]);
        }
    }

    public static String readDailyReportCSVLastUpdate(final String[] parsedLine, final Date fileDate) {
        if (newFormatDate.compareTo(fileDate) < 0) { //After newFormatDate, use the new format
            return parsedLine[NEW_COLUMN_DAILY_REPORT_LASTUPDATE];
        } else { //before that, use the old format
            return parsedLine[COLUMN_DAILY_REPORT_LASTUPDATE];
        }
    }

    public static Map<Location, List<Covid19Case>> readDailyReportCSVFileToMap(final String dailyReportFileFolder, final Calendar startDate, final Calendar endDate, final String fileNameDatePattern) {
        final Map<Location, List<Covid19Case>> result = new TreeMap<>();
        final SimpleDateFormat fileNameDateFormat = new SimpleDateFormat(fileNameDatePattern);
        int expectedDays = 0;
        final int preferredArraySize = Long.valueOf(ChronoUnit.DAYS.between(startDate.toInstant(), endDate.toInstant())).intValue();
        final List<Covid19Case> zeroCasesList = new ArrayList<>(preferredArraySize);

        while (endDate.compareTo(startDate) >= 0) {
            expectedDays++;
            Date date = startDate.getTime();
            File csvFile = new File(dailyReportFileFolder + "/" + fileNameDateFormat.format(startDate.getTime()) + ".csv");

            try (CSVReader csvReader = new CSVReader(new FileReader(csvFile))) {
                //skip header
                csvReader.readNext();

                //reading the daily report
                String[] parsedLine;
                while ((parsedLine = csvReader.readNext()) != null) {

                    Location location = readDailyReportCSVLocation(parsedLine, date);
                    int confirmedValue = readDailyReportCSVConfirmedValue(parsedLine, date);
                    int deathValue = readDailyReportCSVDeathValue(parsedLine, date);
                    String lastUpdate = readDailyReportCSVLastUpdate(parsedLine, date);

                    if (confirmedValue <= 0) continue;

                    if (result.containsKey(location)) {
                        List<Covid19Case> target = result.get(location);

                        if (target.size() == expectedDays) {
                            //already added date record. proceed to modify
                            Covid19Case todayCase = target.get(expectedDays-1);
                            Covid19Case yesterdayCase = target.get(expectedDays-2);
                            int accumulatedConfirmedCase = todayCase.getConfirmedCase() + confirmedValue;
                            int dailyNewCase = accumulatedConfirmedCase - yesterdayCase.getConfirmedCase();
                            int accumulatedDeathCase = todayCase.getDeathCase() + deathValue;
                            int dailyNewDeath = accumulatedDeathCase - yesterdayCase.getDeathCase();

                            todayCase.setConfirmedCase(accumulatedConfirmedCase);
                            todayCase.setDailyNewConfirmedCase(dailyNewCase);
                            todayCase.setDeathCase(accumulatedDeathCase);
                            todayCase.setDailyNewDeathCase(dailyNewDeath);
                            todayCase.setLastUpdate(lastUpdate);
                        } else {
                            //to add today's record
                            Covid19Case previousDayCovid19Case = target.get(target.size() - 1);
                            target.add(new Covid19Case.CaseBuilder()
                                    .date(date)
                                    .confirmedCase(confirmedValue)
                                    .dailyNewConfirmedCase(confirmedValue - previousDayCovid19Case.getConfirmedCase())
                                    .deathCase(deathValue)
                                    .dailyNewDeathCase(deathValue - previousDayCovid19Case.getDeathCase())
                                    .lastUpdate(lastUpdate)
                                    .build());
                        }

                    } else {

                        //new location found
                        Covid19Case confirmedCovid19Case = new Covid19Case.CaseBuilder()
                                .date(date)
                                .confirmedCase(confirmedValue)
                                .dailyNewConfirmedCase(confirmedValue)
                                .build();

                        List<Covid19Case> target = new ArrayList<>(zeroCasesList);
                        target.add(confirmedCovid19Case);
                        result.put(location, target);

                    }
                }
            } catch(IOException | CsvValidationException e) {
                e.printStackTrace();
                return null;
            }

            //append empty case for location without daily update
            appendCaseToLocationWithoutUpdate(result, expectedDays, date);

            zeroCasesList.add(new Covid19Case.CaseBuilder().date(date).confirmedCase(0).build());
            startDate.add(Calendar.DAY_OF_MONTH, 1);
        }
        return result;
    }

    public static Map<Location, List<Covid19Case>> generateG7DailyReport(final Map<Location, List<Covid19Case>> dailyReportCSVFileToMap) {
        Map<Location, List<Covid19Case>> result = new TreeMap<>();
        List<Covid19Case> usAccumulatedList = new ArrayList<>();

        dailyReportCSVFileToMap.entrySet().stream()
                .filter(e -> "US".equals(e.getKey().getCountry()))
                .forEach(e -> accumulateDailyCase(usAccumulatedList, e.getValue()));

        result.put(new Location.LocationBuilder().country("US").province("").build(), usAccumulatedList);

        List<Covid19Case> canadaAccumulatedList = new ArrayList<>();
        dailyReportCSVFileToMap.entrySet().stream()
                .filter(e -> "Canada".equals(e.getKey().getCountry()))
                .forEach(e -> accumulateDailyCase(canadaAccumulatedList, e.getValue()));

        result.put(new Location.LocationBuilder().country("Canada").province("").build(), canadaAccumulatedList);

        dailyReportCSVFileToMap.entrySet().stream()
                .filter(e -> "United Kingdom".equals(e.getKey().getCountry()) && "United Kingdom".equals(e.getKey().getProvince()))
                .findFirst()
                .ifPresent(e -> result.put(new Location.LocationBuilder().country("UK").province("").build(), e.getValue()));


        dailyReportCSVFileToMap.entrySet().stream()
                .filter(e -> "France".equals(e.getKey().getCountry()) && "France".equals(e.getKey().getProvince()))
                .findFirst()
                .ifPresent(e -> result.put(new Location.LocationBuilder().country("France").province("").build(), e.getValue()));

        dailyReportCSVFileToMap.entrySet().stream()
                .filter(e -> "Italy".equals(e.getKey().getCountry()))
                .findFirst()
                .ifPresent(e -> result.put(new Location.LocationBuilder().country("Italy").province("").build(), e.getValue()));

        List<Covid19Case> australiaAccumulatedList = new ArrayList<>();
        dailyReportCSVFileToMap.entrySet().stream()
                .filter(e -> "Australia".equals(e.getKey().getCountry()))
                .forEach(e -> accumulateDailyCase(australiaAccumulatedList, e.getValue()));

        result.put(new Location.LocationBuilder().country("Australia").province("").build(), australiaAccumulatedList);

        return result;
    }

    public static List<Covid19Case> accumulateDailyCase(final List<Covid19Case> target, final List<Covid19Case> source) {
        for (Covid19Case c : source) {
            Optional<Covid19Case> targetCase = target.stream()
                    .filter(e -> c.getDate().equals(e.getDate()))
                    .findFirst();

            if (targetCase.isPresent()) {
                targetCase.get().setConfirmedCase(targetCase.get().getConfirmedCase() + c.getConfirmedCase());
                targetCase.get().setDailyNewConfirmedCase(targetCase.get().getDailyNewConfirmedCase() + c.getDailyNewConfirmedCase());
            } else {
                target.add(new Covid19Case.CaseBuilder()
                        .date(c.getDate())
                        .confirmedCase(c.getConfirmedCase())
                        .dailyNewConfirmedCase(c.getDailyNewConfirmedCase())
                        .build());
                Collections.sort(target);
            }
        }
        return target;
    }

    public static Map<Location, List<Covid19Case>> appendCaseToLocationWithoutUpdate(final Map<Location, List<Covid19Case>> caseMap, final int expectedDays, final Date date) {
        if (expectedDays < 2) return caseMap;

        caseMap.values().stream()
                .filter(l -> l.size() < expectedDays)
                .forEach(list -> {
                    list.add(new Covid19Case.CaseBuilder()
                            .date(date)
                            .confirmedCase(list.get(expectedDays - 2).getConfirmedCase())
                            .dailyNewConfirmedCase(0)
                            .build());
                });

        return caseMap;
    }

    public static Map<Location, List<Covid19Case>> readTimeSeriesCSVFileToMap(final File csvFile, final String timeSeriesDateFormat) {
        final Map<Location, List<Covid19Case>> result = new TreeMap<>();
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeSeriesDateFormat);
        try (CSVReader csvReader = new CSVReader(new FileReader(csvFile))) {
            String[] parsedDateLine = csvReader.readNext();

            String[] parsedLine;
            while ((parsedLine = csvReader.readNext()) != null) {
                Location location = new Location.LocationBuilder()
                        .country(parsedLine[COLUMN_COUNTRY])
                        .province(parsedLine[COLUMN_PROVINCE])
                        .longitude(parsedLine[COLUMN_LONGITUDE])
                        .latitude(parsedLine[COLUMN_LATITUDE])
                        .build();

                List<Covid19Case> accumulatedCovid19Cases = new ArrayList<>();
                for (int i = COLUMN_LONGITUDE +1; i<parsedLine.length-1; i++) {
                    accumulatedCovid19Cases.add(new Covid19Case.CaseBuilder()
                            .date(simpleDateFormat.parse(parsedDateLine[i]))
                            .confirmedCase(Integer.valueOf(parsedLine[i]))
                            .build()
                    );
                }

                if (result.containsKey(location)) {
                    //TODO: to verify each value of both to try to correct the value
                    logger.error("Duplicated locaation found in csv " + location);;
                } else {
                    result.put(location, accumulatedCovid19Cases);
                }
            }
        } catch(IOException | CsvValidationException | ParseException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
}
