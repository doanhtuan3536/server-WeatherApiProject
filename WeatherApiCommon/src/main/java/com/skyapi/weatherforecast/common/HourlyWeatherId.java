package com.skyapi.weatherforecast.common;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class HourlyWeatherId implements Serializable {
    private int hourOfDay;

    @ManyToOne
    @JoinColumn(name = "location_code")
    private Location location;

    public HourlyWeatherId() {
    }

    public HourlyWeatherId(int hourOfDay, Location location) {
        this.hourOfDay = hourOfDay;
        this.location = location;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HourlyWeatherId that = (HourlyWeatherId) o;
        return hourOfDay == that.hourOfDay && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hourOfDay, location);
    }
}
