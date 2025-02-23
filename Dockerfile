FROM maven:3-openjdk-17 AS build
WORKDIR /app

COPY . .
RUN mvn clean package -DskipTests


# Run stage

FROM openjdk:17-jdk-slim
WORKDIR /app

COPY src/resources/ip2locdb/IP2LOCATION-LITE-DB3.BIN /app/ip2locdb/IP2LOCATION-LITE-DB3.BIN

COPY --from=build /app/WeatherApiService/target/WeatherApiService-1.0.0.jar drcomputer.jar
EXPOSE 8080 

ENTRYPOINT ["java","-jar","drcomputer.jar"]