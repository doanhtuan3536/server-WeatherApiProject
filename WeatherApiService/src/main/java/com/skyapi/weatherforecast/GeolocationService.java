package com.skyapi.weatherforecast;

import com.ip2location.IP2Location;
import com.ip2location.IPResult;
import com.skyapi.weatherforecast.common.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class GeolocationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeolocationService.class);
    private String DBPath = "/ip2locdb/IP2LOCATION-LITE-DB3.BIN";
    private IP2Location ipLocator = new IP2Location();
    public GeolocationService() {
//        try {
////            DBPath = getClass().getClassLoader().getResource("ip2locdb/IP2LOCATION-LITE-DB3.BIN").getPath();
////            System.out.println(getClass().getClassLoader().getResource("ip2locdb/IP2LOCATION-LITE-DB3.BIN").getPath());
////            InputStream inputStream = getClass().getResourceAsStream(DBPath);
////            byte[] data = inputStream.readAllBytes();
////            ipLocator.Open(data);
////            inputStream.close();
//            URL resource = getClass().getClassLoader().getResource("ip2locdb/IP2LOCATION-LITE-DB3.BIN");
//            if (resource == null) {
//                throw new IOException("Database file not found in classpath");
//            }
//
//            DBPath = resource.getPath(); // Get the absolute path to the file
//
//            ipLocator.Open(DBPath); // Open the database using the file path
//        } catch (IOException e) {
//            LOGGER.error(e.getMessage(), e);
//        }
        try (InputStream inputStream = getClass().getResourceAsStream(DBPath)) {

            if (inputStream == null) {
                throw new IOException("Database file not found in classpath: " + DBPath);
            }

            long fileSize = inputStream.available(); // Get file size
            byte[] data = new byte[(int) fileSize]; // Allocate exact size buffer

            int offset = 0;
            int bytesRead;
            while ((bytesRead = inputStream.read(data, offset, data.length - offset)) != -1) {
                offset += bytesRead;
            }
            System.out.println(data.length);
            ipLocator.Open(data); // Load into IP2Location

            LOGGER.info("IP2Location database successfully loaded.");

        } catch (IOException e) {
            LOGGER.error("Failed to load IP2Location database", e);
        }
    }

    public Location getLocation(String ipAddress) throws GeolocationException {
        try {
            IPResult result = ipLocator.IPQuery(ipAddress);

            if(!"OK".equals(result.getStatus())){
                throw new GeolocationException("Geolocation failed with status: " + result.getStatus());
            }

            return new Location(result.getCity(), result.getRegion(), result.getCountryLong(), result.getCountryShort());
        } catch (IOException e) {
            throw new GeolocationException("Error querying IP database", e);
        }

    }
}
