package com.skyapi.weatherforecast.hourly;

import com.skyapi.weatherforecast.CommonUtility;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/hourly")
@Validated
public class HourlyWeatherApiController {

    private HourlyWeatherService hourlyWeatherService;
    private GeolocationService locationService;
    private ModelMapper modelMapper;

    public HourlyWeatherApiController(HourlyWeatherService hourlyWeatherService, GeolocationService locationService, ModelMapper modelMapper) {
        this.hourlyWeatherService = hourlyWeatherService;
        this.locationService = locationService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<?> listHourlyForecastByIPAddress(HttpServletRequest request){
        String ipAddress = CommonUtility.getIPAddress(request);

        try {
            int currentHour = Integer.parseInt(request.getHeader("X-Current-Hour"));

            Location locationFromIP = locationService.getLocation(ipAddress);

            List<HourlyWeather> hourlyForecast = hourlyWeatherService.getByLocation(locationFromIP, currentHour);

            if(hourlyForecast.isEmpty()){
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(listEntity2DTO(hourlyForecast));
        }
        catch (NumberFormatException  e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{locationCode}")
    public ResponseEntity<?> listHourlyForecastByLocationCode(
            @PathVariable("locationCode") String locationCode, HttpServletRequest request) {

        try {
            int currentHour = Integer.parseInt(request.getHeader("X-Current-Hour"));

            List<HourlyWeather> hourlyForecast = hourlyWeatherService.getByLocationCode(locationCode, currentHour);

            if (hourlyForecast.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            HourlyWeatherListDTO dto = listEntity2DTO(hourlyForecast);

            return ResponseEntity.ok(dto);

        } catch (NumberFormatException ex) {

            return ResponseEntity.badRequest().build();

        }
    }

    @PutMapping("/{locationCode}")
    public ResponseEntity<?> updateHourlyForecast(@PathVariable("locationCode") String locationCode,
                                                  @RequestBody @Valid List<HourlyWeatherDTO> listDTO) throws BadRequestException {
        if(listDTO.isEmpty()){
            throw new BadRequestException("Hourly forecast data can not be empty");
        }

        listDTO.forEach(System.out::println);

        List<HourlyWeather> listHourlyWeather = listDTO2ListEntity(listDTO);

        listHourlyWeather.forEach(System.out::println);
        List<HourlyWeather> updatedHourlyWeather = hourlyWeatherService.updateByLocationCode(locationCode, listHourlyWeather);

        return ResponseEntity.ok(listEntity2DTO(updatedHourlyWeather));
    }

    private List<HourlyWeather> listDTO2ListEntity(List<HourlyWeatherDTO> listDTO){
        List<HourlyWeather> listEntity = new ArrayList<>();

        listDTO.forEach(dto -> {
            listEntity.add(modelMapper.map(dto, HourlyWeather.class));
        });

        return listEntity;
    }

    private HourlyWeatherListDTO listEntity2DTO(List<HourlyWeather> hourlyForecast){
        Location location = hourlyForecast.get(0).getId().getLocation();
        HourlyWeatherListDTO listDTO = new HourlyWeatherListDTO();
        listDTO.setLocation(location.toString());

        hourlyForecast.forEach(hourlyWeather -> {
            HourlyWeatherDTO dto = modelMapper.map(hourlyWeather, HourlyWeatherDTO.class);
            listDTO.addWeatherHourlyDTO(dto);
        });

        return listDTO;
    }
}
