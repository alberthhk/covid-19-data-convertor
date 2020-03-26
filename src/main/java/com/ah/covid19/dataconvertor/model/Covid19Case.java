package com.ah.covid19.dataconvertor.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

public class Covid19Case implements Comparable<Covid19Case> {
    private int confirmedCase;
    private int dailyNewConfirmedCase;
    private int deathCase;
    private int dailyNewDeathCase;
    private int recovery;
    private Date date;
    private String lastUpdate;

    public Covid19Case(int dailyNewConfirmedCaseCase, int confirmedCase, int dailyNewDeathCase, int deathCase, Date date, String lastUpdate) {
        this.confirmedCase = confirmedCase;
        this.dailyNewConfirmedCase = dailyNewConfirmedCaseCase;
        this.deathCase = deathCase;
        this.dailyNewDeathCase = dailyNewDeathCase;
        this.date = date;
        this.lastUpdate = lastUpdate;
    }

    public int getDailyNewConfirmedCase() {
        return dailyNewConfirmedCase;
    }

    public int getConfirmedCase() {
        return confirmedCase;
    }

    public Date getDate() {
        return date;
    }

    public void setConfirmedCase(int confirmedCase) {
        this.confirmedCase = confirmedCase;
    }

    public void setDailyNewConfirmedCase(int dailyNewConfirmedCase) {
        this.dailyNewConfirmedCase = dailyNewConfirmedCase;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getDeathCase() {
        return deathCase;
    }

    public void setDeathCase(int deathCase) {
        this.deathCase = deathCase;
    }

    public int getDailyNewDeathCase() {
        return dailyNewDeathCase;
    }

    public void setDailyNewDeathCase(int dailyNewDeathCase) {
        this.dailyNewDeathCase = dailyNewDeathCase;
    }

    public int getRecovery() {
        return recovery;
    }

    public void setRecovery(int recovery) {
        this.recovery = recovery;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
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

    @Override
    public int compareTo(Covid19Case o) {
        return this.date.compareTo(o.date);
    }

    public static class CaseBuilder {
        private int confirmedCase;
        private int dailyNewConfirmedCase;
        private int deathCase;
        private int dailyNewDeathCase;
        private Date date;
        private String lastUpdate;

        public CaseBuilder confirmedCase(int confirmedCase) {
            this.confirmedCase = confirmedCase;
            return this;
        }

        public CaseBuilder dailyNewConfirmedCase(int dailyNewConfirmedCase) {
            this.dailyNewConfirmedCase = dailyNewConfirmedCase;
            return this;
        }

        public CaseBuilder deathCase(int deathCase) {
            this.deathCase = deathCase;
            return this;
        }

        public CaseBuilder dailyNewDeathCase(int dailyNewDeathCase) {
            this.dailyNewDeathCase = dailyNewDeathCase;
            return this;
        }

        public CaseBuilder date(Date date) {
            this.date = date;
            return this;
        }

        public CaseBuilder lastUpdate(String lastUpdate) {
            this.lastUpdate = lastUpdate;
            return this;
        }

        public Covid19Case build() {
            return new Covid19Case(this.dailyNewConfirmedCase, this.confirmedCase, this.dailyNewDeathCase, this.deathCase, this.date, this.lastUpdate);
        }
    }
}
