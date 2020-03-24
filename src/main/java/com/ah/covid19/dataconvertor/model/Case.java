package com.ah.covid19.dataconvertor.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Case {
    private final int accumulatedCase;
    private final int dailyNewCase;
    private final String date;

    public Case(int dailyNewCase, int accumulatedCase, String date) {
        this.accumulatedCase = accumulatedCase;
        this.dailyNewCase = dailyNewCase;
        this.date = date;
    }

    public int getDailyNewCase() {
        return dailyNewCase;
    }

    public int getAccumulatedCase() {
        return accumulatedCase;
    }

    public String getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Case aCase = (Case) o;

        return new EqualsBuilder()
                .append(dailyNewCase, aCase.dailyNewCase)
                .append(date, aCase.date)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(dailyNewCase)
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

        public Case build() {
            return new Case(this.dailyNewCase, this.accumulatedCase, this.date);
        }
    }
}
