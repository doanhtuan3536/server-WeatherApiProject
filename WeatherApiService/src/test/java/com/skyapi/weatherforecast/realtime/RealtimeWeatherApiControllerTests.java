package com.skyapi.weatherforecast.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RealtimeWeatherApiController.class)
public class RealtimeWeatherApiControllerTests {
    private static final String END_POINT_PATH = "/v1/realtime";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean RealtimeWeatherService realtimeWeatherService;

    @MockBean
    GeolocationService locationService;

    @Test
    public void testGetShouldReturnStatus400BadRequest() throws Exception {
        GeolocationException ex = new GeolocationException("Geolocation error");
        Mockito.when(locationService.getLocation(Mockito.anyString())).thenThrow(ex);

        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testGetShouldReturnStatus404NotFound() throws Exception {
        Location location = new Location();
        location.setCountryCode("US");
        location.setCountryCode("Tampa");
        LocationNotFoundException ex = new LocationNotFoundException(location.getCountryCode(), location.getCityName());
        Mockito.when(locationService.getLocation(Mockito.anyString())).thenReturn(location);
        Mockito.when(realtimeWeatherService.getByLocation(location)).thenThrow(ex);

        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testGetShouldReturnStatus200OK() throws Exception {
        Location location = new Location();
        location.setCode("SFCA_USA");
        location.setCityName("San Franciso");
        location.setRegionName("California");
        location.setCountryName("United States of America");
        location.setCountryCode("US");

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setTemperature(12);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setLastUpdated(new Date());
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(5);

        realtimeWeather.setLocation(location);
        location.setRealtimeWeather(realtimeWeather);

        Mockito.when(locationService.getLocation(Mockito.anyString())).thenReturn(location);

        Mockito.when(realtimeWeatherService.getByLocation(location)).thenReturn(realtimeWeather);

        String expectedLocation = location.getCityName() + ", " + location.getRegionName() + ", " + location.getCountryName();
        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andDo(print());
    }
    @Test
    public void testGetByLocationCodeShouldReturnStatus404NotFound() throws Exception {
        String locationCode = "ABC_US";

        LocationNotFoundException ex = new LocationNotFoundException(locationCode);
        Mockito.when(realtimeWeatherService.getByLocationCode(locationCode)).thenThrow(ex);

        String requestURI = END_POINT_PATH + "/" + locationCode;

        mockMvc.perform(get(requestURI))
                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.errors[0]", is(ex.getMessage())))
                .andDo(print());
    }
    @Test
    public void testGetByLocationCodeShouldReturnStatus200OK() throws Exception {
        String locationCode = "SFCA_USA";

        Location location = new Location();
        location.setCode(locationCode);
        location.setCityName("San Franciso");
        location.setRegionName("California");
        location.setCountryName("United States of America");
        location.setCountryCode("US");

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setTemperature(12);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setLastUpdated(new Date());
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(5);

        realtimeWeather.setLocation(location);
        location.setRealtimeWeather(realtimeWeather);


        Mockito.when(realtimeWeatherService.getByLocationCode(locationCode)).thenReturn(realtimeWeather);

        String expectedLocation = location.getCityName() + ", " + location.getRegionName() + ", " + location.getCountryName();

        String requestURI = END_POINT_PATH + "/" + locationCode;

        mockMvc.perform(get(requestURI))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.location", is(expectedLocation)))
//                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/realtime/" + locationCode)))
//                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
//                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
//                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequest() throws Exception {
        String locationCode = "ABC_US";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setTemperature(120);
        realtimeWeather.setHumidity(132);
//        realtimeWeather.setLastUpdated(new Date());
        realtimeWeather.setPrecipitation(188);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(500);

        String bodyContent = mapper.writeValueAsString(realtimeWeather);

//        Mockito.when(realtimeWeatherService.update(locationCode, realtimeWeather)).thenThrow(LocationNotFoundException.class);

        mockMvc.perform(put(requestURI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyContent))
                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors[0]", is(ex.getMessage())))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn404NotFound() throws Exception {
        String locationCode = "ABC_US";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setTemperature(12);
        realtimeWeather.setHumidity(32);
//        realtimeWeather.setLastUpdated(new Date());
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(5);
        realtimeWeather.setLocationCode(locationCode);

        String bodyContent = mapper.writeValueAsString(realtimeWeather);
        System.out.println(bodyContent);

        LocationNotFoundException ex = new LocationNotFoundException(locationCode);
        Mockito.when(realtimeWeatherService.update(locationCode, realtimeWeather)).thenThrow(ex);

        mockMvc.perform(put(requestURI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyContent))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]", is(ex.getMessage())))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn200OK() throws Exception {
        String locationCode = "SFCA_US";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setTemperature(12);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setLastUpdated(new Date());
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(5);

        Location location = new Location();
        location.setCode(locationCode);
        location.setCityName("San Franciso");
        location.setRegionName("California");
        location.setCountryName("United States of America");
        location.setCountryCode("US");

        realtimeWeather.setLocation(location);
        location.setRealtimeWeather(realtimeWeather);

        String bodyContent = mapper.writeValueAsString(realtimeWeather);

        Mockito.when(realtimeWeatherService.update(locationCode, realtimeWeather)).thenReturn(realtimeWeather);


        String expectedLocation = location.getCityName() + ", " + location.getRegionName() + ", " + location.getCountryName();

        mockMvc.perform(put(requestURI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location", is(expectedLocation)))
//                .andExpect(jsonPath("$.errors[0]", is(ex.getMessage())))
                .andDo(print());
    }
}
