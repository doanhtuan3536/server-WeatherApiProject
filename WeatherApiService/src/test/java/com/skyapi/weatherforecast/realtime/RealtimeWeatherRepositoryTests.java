package com.skyapi.weatherforecast.realtime;

import com.skyapi.weatherforecast.common.RealtimeWeather;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class RealtimeWeatherRepositoryTests {
    @Autowired
    private RealtimeWeatherRepository repo;

    @Test
    public void testUpdate(){
        String locationCode = "DANA_VN";

        RealtimeWeather realtimeWeather = repo.findById(locationCode).get();
        realtimeWeather.setTemperature(-2);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setPrecipitation(42);
        realtimeWeather.setStatus("Snowy");
        realtimeWeather.setWindSpeed(12);
        realtimeWeather.setLastUpdated(new Date());

        RealtimeWeather updatedRealtimeWeather = repo.save(realtimeWeather);

        assertThat(updatedRealtimeWeather.getHumidity()).isEqualTo(32);
    }

    @Test
    public void testFindByCountryCodeAndCityNotFound(){
        String countryCode = "JP";
        String cityName = "Tokyo";

        RealtimeWeather realtimeWeather = repo.findByCountryCodeAndCity(countryCode, cityName);
        assertThat(realtimeWeather).isNull();
    }

    @Test
    public void testFindByCountryCodeAndCityFound(){
        String countryCode = "VN";
        String cityName = "Da Nang";

        RealtimeWeather realtimeWeather = repo.findByCountryCodeAndCity(countryCode, cityName);
        assertThat(realtimeWeather).isNotNull();
        assertThat(realtimeWeather.getLocation().getCityName()).isEqualTo(cityName);
    }
    @Test
    public void testFindByLocationNotFound() {
        String locationCode = "ABCXYZ";
        RealtimeWeather realtimeWeather = repo.findByLocationCode(locationCode);

        assertThat(realtimeWeather).isNull();
    }

    @Test
    public void testFindByTrashedLocationNotFound() {
        String locationCode = "NYC_USA";
        RealtimeWeather realtimeWeather = repo.findByLocationCode(locationCode);

        assertThat(realtimeWeather).isNull();
    }

    @Test
    public void testFindByLocationFound() {
        String locationCode = "DELHI_IN";
        RealtimeWeather realtimeWeather = repo.findByLocationCode(locationCode);

        assertThat(realtimeWeather).isNotNull();
        assertThat(realtimeWeather.getLocationCode()).isEqualTo(locationCode);
    }
}
