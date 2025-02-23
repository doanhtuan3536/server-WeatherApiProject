package com.skyapi.weatherforecast.realtime;

import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.location.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RealtimeWeatherService {

    private RealtimeWeatherRepository realtimeWeatherRepo;
    private LocationRepository locationRepository;

    public RealtimeWeatherService(RealtimeWeatherRepository realtimeWeatherRepo, LocationRepository locationRepository) {
        this.realtimeWeatherRepo = realtimeWeatherRepo;
        this.locationRepository = locationRepository;
    }

    public RealtimeWeather getByLocation(Location location){
        String countryCode = location.getCountryCode();
        String cityName = location.getCityName();

        RealtimeWeather realtimeWeather = realtimeWeatherRepo.findByCountryCodeAndCity(countryCode, cityName);

        if(realtimeWeather == null){
            throw new LocationNotFoundException(countryCode, cityName);
        }

        return realtimeWeather;
    }

    public RealtimeWeather getByLocationCode(String locationCode) {
        RealtimeWeather realtimeWeather = realtimeWeatherRepo.findByLocationCode(locationCode);

        if (realtimeWeather == null) {
            throw new LocationNotFoundException(locationCode);
        }

        return realtimeWeather;
    }

    public RealtimeWeather update(String locationCode, RealtimeWeather realtimeWeather) {
        Location location = locationRepository.findByCode(locationCode);

        if(location == null){
            throw new LocationNotFoundException(locationCode);
        }
        realtimeWeather.setLocation(location);
        realtimeWeather.setLastUpdated(new Date());

        if(location.getRealtimeWeather() == null){
            location.setRealtimeWeather(realtimeWeather);
            Location updatedLocation = locationRepository.save(location);
            return updatedLocation.getRealtimeWeather();
        }

        return realtimeWeatherRepo.save(realtimeWeather);
    }
}
