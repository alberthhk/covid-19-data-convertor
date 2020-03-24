package com.ah.covid19.dataconvertor.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Location implements Comparable<Location> {
    private final String country;
    private final String province;
    private final float longitude;
    private final float latitude;

    public Location(String country, String province, float longitude, float latitude) {
        this.country = country;
        this.province = province;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getCountry() {
        return country;
    }

    public String getProvince() {
        return province;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        return new EqualsBuilder()
                .append(country, location.country)
                .append(province, location.province)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(country)
                .append(province)
                .toHashCode();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Location{");
        sb.append("country='").append(country).append('\'');
        sb.append(", province='").append(province).append('\'');
        sb.append(", longitude=").append(longitude);
        sb.append(", langitude=").append(latitude);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Location target) {
        if (this.country.equals(target.country)) {
            return StringUtils.compare(this.province, target.province);
        }
        return StringUtils.compare(this.country, target.country);
    }

    public static class LocationBuilder {
        private String country;
        private String province;
        private float longitude;
        private float latitude;

        public LocationBuilder country(String country) {
            this.country = country;
            return this;
        }

        public LocationBuilder province(String province) {
            this.province = province;
            return this;
        }

        public LocationBuilder longitude(String longitude) {
            this.longitude = Float.parseFloat(longitude);
            return this;
        }

        public LocationBuilder latitude(String latitude) {
            this.latitude = Float.parseFloat(latitude);
            return this;
        }

        public Location build() {
            return new Location(this.country, this.province, this.longitude, this.latitude);
        }


    }
}
