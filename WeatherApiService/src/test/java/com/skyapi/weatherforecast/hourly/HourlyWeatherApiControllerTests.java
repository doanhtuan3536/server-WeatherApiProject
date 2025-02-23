package com.skyapi.weatherforecast.hourly;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HourlyWeatherApiController.class)
public class HourlyWeatherApiControllerTests {
    private static final String END_POINT_PATH = "/v1/hourly";
    private static final String X_CURRENT_HOUR = "X-Current-Hour";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private HourlyWeatherService hourlyWeatherService;
    @MockBean private GeolocationService geolocationService;

    @Test
    public void testGetByIPShouldReturn400BadRequestBecauseNoHeaderXCurrentHour() throws Exception {
        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testGetByIPShouldReturn400BadRequestBecauseGeolocationException() throws Exception {
        GeolocationException ex = new GeolocationException("Geolocation error");
        when(geolocationService.getLocation(Mockito.anyString())).thenThrow(ex);

        mockMvc.perform(get(END_POINT_PATH).header(X_CURRENT_HOUR, "9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", is(ex.getMessage())))
                .andDo(print());
    }

    @Test
    public void testGetByIPShouldReturn204NoContent() throws Exception {
        int currentHour = 9;
        Location location = new Location().code("LACA_USA");

        when(geolocationService.getLocation(Mockito.anyString())).thenReturn(location);
        when(hourlyWeatherService.getByLocation(location, currentHour)).thenReturn(new ArrayList<>());
        mockMvc.perform(get(END_POINT_PATH).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void testGetByIPShouldReturn200OK() throws Exception {
        int currentHour = 9;
        Location location = new Location();
        location.setCode("LACA_USA");
        location.setCityName("Los Angeles");
        location.setRegionName("California");
        location.setCountryCode("US");
        location.setCountryName("United States of America");

        HourlyWeather forecast1 = new HourlyWeather().id(location, 10)
                .hourOfDay(10)
                .temperature(15)
                .precipitation(60)
                .status("Sunny");

        HourlyWeather forecast2 = new HourlyWeather().id(location, 10)
                .hourOfDay(11)
                .temperature(13)
                .precipitation(17)
                .status("Cloudy");

        when(geolocationService.getLocation(Mockito.anyString())).thenReturn(location);
        when(hourlyWeatherService.getByLocation(location, currentHour)).thenReturn(List.of(forecast1, forecast2));

        String expectedLocation = location.toString();
        mockMvc.perform(get(END_POINT_PATH).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andExpect(jsonPath("$.hourly_forecast[0].hour_of_day", is(10)))
                .andDo(print());
    }

    @Test
    public void testGetByCodeShouldReturn400BadRequest() throws Exception {
        String locationCode = "DELHI_IN";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        mockMvc.perform(get(requestURI))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testGetByCodeShouldReturn404NotFound() throws Exception {
        int currentHour = 9;
        String locationCode = "DELHI_IN";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        LocationNotFoundException ex = new LocationNotFoundException(locationCode);
        when(hourlyWeatherService.getByLocationCode(locationCode, currentHour)).thenThrow(ex);

        mockMvc.perform(get(requestURI).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]", is(ex.getMessage())))
                .andDo(print());
    }

    @Test
    public void testGetByCodeShouldReturn204NoContent() throws Exception {
        int currentHour = 9;
        String locationCode = "DANA_VN";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        when(hourlyWeatherService.getByLocationCode(locationCode, currentHour)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(requestURI).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void testGetByCodeShouldReturn200OK() throws Exception {
        int currentHour = 9;
        String locationCode = "DELHI_IN";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        Location location = new Location();
        location.setCode(locationCode);
        location.setCityName("New York City");
        location.setRegionName("New York");
        location.setCountryCode("US");
        location.setCountryName("United States of America");

        HourlyWeather forecast1 = new HourlyWeather()
                .location(location)
                .hourOfDay(10)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");

        HourlyWeather forecast2 = new HourlyWeather()
                .location(location)
                .hourOfDay(11)
                .temperature(15)
                .precipitation(60)
                .status("Sunny");

        var hourlyForecast = List.of(forecast1, forecast2);

        when(hourlyWeatherService.getByLocationCode(locationCode, currentHour)).thenReturn(hourlyForecast);

        mockMvc.perform(get(requestURI).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.location", is(location.toString())))
                .andExpect(jsonPath("$.hourly_forecast[0].hour_of_day", is(10)))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseNoData() throws Exception {
        String requestURI = END_POINT_PATH + "/NYC_USA";

        List<HourlyWeatherDTO> listDTO = Collections.emptyList();

        String requestBody = objectMapper.writeValueAsString(listDTO);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", is("Hourly forecast data can not be empty")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseInvalidData() throws Exception {
        String requestURI = END_POINT_PATH + "/NYC_USA";

        HourlyWeatherDTO dto1 = new HourlyWeatherDTO()
                .hourOfDay(10)
                .temperature(133)
                .precipitation(70)
                .status("Cloudy");

        HourlyWeatherDTO dto2 = new HourlyWeatherDTO()
                .hourOfDay(11)
                .temperature(15)
                .precipitation(60)
                .status("Sunny");

        List<HourlyWeatherDTO> listDTO =List.of(dto1, dto2);
        String requestBody = objectMapper.writeValueAsString(listDTO);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", containsString("Temperature must be in the range -50 to 50")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn404NotFound() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        HourlyWeatherDTO dto1 = new HourlyWeatherDTO()
                .hourOfDay(10)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");

        List<HourlyWeatherDTO> listDTO =List.of(dto1);
        String requestBody = objectMapper.writeValueAsString(listDTO);

        LocationNotFoundException ex = new LocationNotFoundException(locationCode);
        Mockito.when(hourlyWeatherService.updateByLocationCode(Mockito.eq(locationCode), Mockito.anyList())).thenThrow(ex);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]", is(ex.getMessage())))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn200OK() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        HourlyWeatherDTO dto1 = new HourlyWeatherDTO()
                .hourOfDay(10)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");

        HourlyWeatherDTO dto2 = new HourlyWeatherDTO()
                .hourOfDay(11)
                .temperature(15)
                .precipitation(60)
                .status("Sunny");

        Location location = new Location();
        location.setCode(locationCode);
        location.setCityName("New York City");
        location.setRegionName("New York");
        location.setCountryCode("US");
        location.setCountryName("United States of America");

        HourlyWeather forecast1 = new HourlyWeather()
                .location(location)
                .hourOfDay(10)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");

        HourlyWeather forecast2 = new HourlyWeather()
                .location(location)
                .hourOfDay(11)
                .temperature(15)
                .precipitation(60)
                .status("Sunny");

        List<HourlyWeatherDTO> listDTO =List.of(dto1, dto2);
        var hourlyForecast =List.of(forecast1, forecast2);
        String requestBody = objectMapper.writeValueAsString(listDTO);

        Mockito.when(hourlyWeatherService.updateByLocationCode(Mockito.eq(locationCode), Mockito.anyList())).thenReturn(hourlyForecast);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location", is(location.toString())))
                .andExpect(jsonPath("$.hourly_forecast[0].hour_of_day", is(10)))
                .andDo(print());
    }
}
