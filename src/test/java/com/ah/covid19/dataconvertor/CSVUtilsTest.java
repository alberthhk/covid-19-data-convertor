package com.ah.covid19.dataconvertor;

import com.ah.covid19.dataconvertor.model.Covid19Case;
import com.ah.covid19.dataconvertor.model.Location;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVUtilsTest {

    @Test
    public void getStandardizedLocation_shouldReturnDefault() {
        //Given
        Location expectedAustralia = new Location.LocationBuilder().country("Australia").province("New South Wales").build();
        Location expectedFrance = new Location.LocationBuilder().country("France").province("France").build();

        //When
        Location testAustralia = CSVUtils.getStandardizedLocation("Australia", "New South Wales");
        Location testFrance = CSVUtils.getStandardizedLocation("France", null);

        //Then
        assertEquals(expectedAustralia, testAustralia);
        assertEquals(expectedFrance, testFrance);
    }

    @Test
    public void accumulateDailyCase_shouldAccumulate_targetIsEmpty() throws ParseException {
        //Given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<Covid19Case> target = new ArrayList<>();

        List<Covid19Case> source1 = Arrays.asList(
                new Covid19Case.CaseBuilder().confirmedCase(5).dailyNewConfirmedCase(5).date(simpleDateFormat.parse("2020-03-01")).build(),
                new Covid19Case.CaseBuilder().confirmedCase(9).dailyNewConfirmedCase(4).date(simpleDateFormat.parse("2020-03-02")).build(),
                new Covid19Case.CaseBuilder().confirmedCase(20).dailyNewConfirmedCase(11).date(simpleDateFormat.parse("2020-03-03")).build());
        List<Covid19Case> source2 = Arrays.asList(
                new Covid19Case.CaseBuilder().confirmedCase(18).dailyNewConfirmedCase(18).date(simpleDateFormat.parse("2020-02-29")).build(),
                new Covid19Case.CaseBuilder().confirmedCase(27).dailyNewConfirmedCase(9).date(simpleDateFormat.parse("2020-03-01")).build(),
                new Covid19Case.CaseBuilder().confirmedCase(37).dailyNewConfirmedCase(10).date(simpleDateFormat.parse("2020-03-02")).build(),
                new Covid19Case.CaseBuilder().confirmedCase(55).dailyNewConfirmedCase(18).date(simpleDateFormat.parse("2020-03-03")).build());

        //When
        CSVUtils.accumulateDailyCase(target, source1);
        CSVUtils.accumulateDailyCase(target, source2);

        //Then
        assertEquals(4, target.size());
        assertEquals(18, target.get(0).getConfirmedCase());
        assertEquals(18, target.get(0).getDailyNewConfirmedCase());
        assertEquals(simpleDateFormat.parse("2020-02-29"), target.get(0).getDate());
        assertEquals(5+27, target.get(1).getConfirmedCase());
        assertEquals(5+9, target.get(1).getDailyNewConfirmedCase());
        assertEquals(simpleDateFormat.parse("2020-03-01"), target.get(1).getDate());
    }
}
