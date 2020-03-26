package com.ah.covid19.dataconvertor;

import com.ah.covid19.dataconvertor.model.Covid19Case;
import com.ah.covid19.dataconvertor.model.Location;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GoogleSheetUtils {

    private static final Logger logger = LogManager.getLogger(GoogleSheetUtils.class);

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    private static final int COLUMN_PROVINCE = 0;
    private static final int COLUMN_COUNTRY = 1;
    private static final int COLUMN_LATITUDE = 2;
    private static final int COLUMN_LONGITUDE = 3;

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        //setup google worksheet api connection
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("credentials.json")) {
            if (input == null) {
                System.out.println("Sorry, unable to find credentials.json");
                System.exit(1);
            }
            final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(input));

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        }
    }

    public static void uploadConfirmedCaseToGoogleSheet(final String g_spreadsheet_id, final Map<Location, List<Covid19Case>> locationAccumCasesMap, final String range) throws GeneralSecurityException, IOException {
        uploadCsvToGoogleSheet(g_spreadsheet_id, locationAccumCasesMap, range, CaseType.CONFIRMED);
    }

    public static void uploadDailyNewCaseToGoogleSheet(final String g_spreadsheet_id, final Map<Location, List<Covid19Case>> locationAccumCasesMap, final String range) throws GeneralSecurityException, IOException {
        uploadCsvToGoogleSheet(g_spreadsheet_id, locationAccumCasesMap, range, CaseType.DAILYNEWCONFIRMED);
    }

    public static void uploadDeathCaseToGoogleSheet(final String g_spreadsheet_id, final Map<Location, List<Covid19Case>> locationAccumCasesMap, final String range) throws GeneralSecurityException, IOException {
        uploadCsvToGoogleSheet(g_spreadsheet_id, locationAccumCasesMap, range, CaseType.DEATH);
    }

    public static void uploadDailyNewDeathToGoogleSheet(final String g_spreadsheet_id, final Map<Location, List<Covid19Case>> locationAccumCasesMap, final String range) throws GeneralSecurityException, IOException {
        uploadCsvToGoogleSheet(g_spreadsheet_id, locationAccumCasesMap, range, CaseType.DAILYNEWDEATH);
    }

    public static void uploadDailyConfirmedG75EToGoogleSheet(final String g_spreadsheet_id, final Map<Location, List<Covid19Case>> locationAccumCasesMap, final String range) throws GeneralSecurityException, IOException {
        uploadCsvToGoogleSheet(g_spreadsheet_id, locationAccumCasesMap, range, CaseType.CONFIRMED);
    }

    public static void uploadDailyNewConfirmedG75EToGoogleSheet(final String g_spreadsheet_id, final Map<Location, List<Covid19Case>> locationAccumCasesMap, final String range) throws GeneralSecurityException, IOException {
        uploadCsvToGoogleSheet(g_spreadsheet_id, locationAccumCasesMap, range, CaseType.DAILYNEWCONFIRMED);
    }

    private static void uploadCsvToGoogleSheet(final String g_spreadsheet_id, final Map<Location, List<Covid19Case>> locationAccumCasesMap, final String range, final CaseType type) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, GoogleSheetUtils.JSON_FACTORY, GoogleSheetUtils.getCredentials(HTTP_TRANSPORT))
                .setApplicationName("COVID-19")
                .build();

        //search all the location in the google sheet
        final Set<Location> locationSet = locationAccumCasesMap.keySet();
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final int numOfDays = locationAccumCasesMap.entrySet().stream().findFirst().get().getValue().size();

        List<List<Object>> valuesToGSheet = new ArrayList<>(locationAccumCasesMap.size() + 1);
        List<Object> headerRow = new ArrayList<>(numOfDays);

        for (Location location : locationSet) {
            //initialize headerRow if empty
            if (headerRow.isEmpty()) {
                headerRow.add("Country");
                headerRow.add("Province");
                for (Covid19Case c : locationAccumCasesMap.get(location)) {
                    headerRow.add(c.getDate().toString());
                }
                valuesToGSheet.add(headerRow);
            }

            List<Object> row = new ArrayList<>(numOfDays);
            row.add(location.getCountry());
            row.add(location.getProvince());
            for (Covid19Case c : locationAccumCasesMap.get(location)) {
                row.add(getCaseNumber(c, type));
            }
            valuesToGSheet.add(row);
        }
        ValueRange body = new ValueRange().setValues(valuesToGSheet);
        UpdateValuesResponse result =
                service.spreadsheets().values().update(g_spreadsheet_id, range, body)
                        .setValueInputOption("RAW")
                        .execute();

        logger.info("{} cells updated.", result.getUpdatedCells());
    }

    public static int getCaseNumber(Covid19Case c, CaseType type) {
        switch (type) {
            case CONFIRMED:
                return c.getConfirmedCase();
            case DAILYNEWCONFIRMED:
                return c.getDailyNewConfirmedCase();
            case DEATH:
                return c.getDeathCase();
            case DAILYNEWDEATH:
                return c.getDailyNewDeathCase();
            default:
                return c.getConfirmedCase();
        }
    }

    private enum CaseType {
        CONFIRMED, DAILYNEWCONFIRMED, DEATH, DAILYNEWDEATH, RECOVERED, G75E;
    }
}
