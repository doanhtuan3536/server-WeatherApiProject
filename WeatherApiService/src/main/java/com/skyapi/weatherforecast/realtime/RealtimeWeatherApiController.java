package com.skyapi.weatherforecast.realtime;

import com.skyapi.weatherforecast.CommonUtility;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/realtime")
public class RealtimeWeatherApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RealtimeWeatherApiController.class);

    private GeolocationService locationService;
    private RealtimeWeatherService realtimeWeatherService;
    private ModelMapper modelMapper;

    public RealtimeWeatherApiController(GeolocationService locationService, RealtimeWeatherService realtimeWeatherService, ModelMapper modelMapper) {
        this.locationService = locationService;
        this.realtimeWeatherService = realtimeWeatherService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<?> getRealtimeWeatherByIPAddress(HttpServletRequest request){
        String ipAddress = CommonUtility.getIPAddress(request);
        Location locationFromIp = locationService.getLocation(ipAddress);
        RealtimeWeather realtimeWeather = realtimeWeatherService.getByLocation(locationFromIp);

        RealtimeWeatherDTO dto = modelMapper.map(realtimeWeather, RealtimeWeatherDTO.class);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{locationCode}")
    public ResponseEntity<?> getRealtimeWeatherByLocationCode(@PathVariable("locationCode") String locationCode) {
        RealtimeWeather realtimeWeather = null;
        realtimeWeather = realtimeWeatherService.getByLocationCode(locationCode);

        return ResponseEntity.ok(entity2DTO(realtimeWeather));
    }

    @PutMapping("/{locationCode}")
    public ResponseEntity<?> updateRealtimeWeather(@PathVariable("locationCode") String locationCode,
                                                   @RequestBody @Valid RealtimeWeather realtimeWeatherInRequest) {
        realtimeWeatherInRequest.setLocationCode(locationCode);
        System.out.println(realtimeWeatherInRequest);
        RealtimeWeather updatedRealtimeWeather = realtimeWeatherService.update(locationCode, realtimeWeatherInRequest);

        System.out.println(updatedRealtimeWeather);

        return ResponseEntity.ok(entity2DTO(updatedRealtimeWeather));

    }

    private RealtimeWeatherDTO entity2DTO(RealtimeWeather realtimeWeather){
        return modelMapper.map(realtimeWeather, RealtimeWeatherDTO.class);
    }
}
