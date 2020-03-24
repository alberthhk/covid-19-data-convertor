package com.ah.covid19.dataconvertor.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Covid19Case {
    private final int confirmedCase;
    private final int dailyNewConfirmedCase;
    private final String date;

    public Covid19Case(int dailyNewCase, int confirmedCase, String date) {
        this.confirmedCase = confirmedCase;
        this.dailyNewConfirmedCase = dailyNewCase;
        this.date = date;
    }

    public int getDailyNewConfirmedCase() {
        return dailyNewConfirmedCase;
    }

    public int getConfirmedCase() {
        return confirmedCase;
    }

    public String getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Covid19Case aCovid19Case = (Covid19Case) o;

        return new EqualsBuilder()
                .append(dailyNewConfirmedCase, aCovid19Case.dailyNewConfirmedCase)
                .append(date, aCovid19Case.date)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(dailyNewConfirmedCase)
                .append(date)
                .toHashCode();
    }

    public static class CaseBuilder {
        private int dailyNewCase;
        private int accumulatedCase;
        private String date;

        public CaseBuilder accumulatedCase(int accumulatedCase) {
            this.accumulatedCase = accumulatedCase;
            return this;
        }

        public CaseBuilder dailyNewCase(int dailyNewCase) {
            this.dailyNewCase = dailyNewCase;
            return this;
        }

        public CaseBuilder date(String date) {
            this.date = date;
            return this;
        }

        public Covid19Case build() {
            return new Covid19Case(this.dailyNewCase, this.accumulatedCase, this.date);
        }
    }
}
