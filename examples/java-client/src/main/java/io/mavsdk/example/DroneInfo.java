package io.mavsdk.example;

import io.mavsdk.System;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneInfo {
    private static final Logger logger = LoggerFactory.getLogger(DroneInfo.class);

    public static void main(String[] args) {
        logger.debug("Starting example: Drone information...");

        String SERVER_IP = "3.35.230.219";
        int SERVER_PORT = 50051;
        System drone = new System(SERVER_IP, SERVER_PORT);

        var info = drone.getInfo();
        var product = info.getProduct().blockingGet();
        logger.debug("Product ID: " + product.getProductId());
        logger.debug("Product Name: " + product.getProductName());
        logger.debug("Vendor ID: " + product.getVendorId());
        logger.debug("Vendor Name: " + product.getVendorName());

        var telemetry = drone.getTelemetry();
        var battery = telemetry.getBattery().blockingFirst();
        logger.debug("Battery percent: " + battery.getRemainingPercent());
        logger.debug("Battery voltage: " + battery.getVoltageV());

        var position = telemetry.getPosition().blockingFirst();
        logger.debug("Latitude degree: " + position.getLatitudeDeg());
        logger.debug("Longitude degree: " + position.getLongitudeDeg());
        logger.debug("Absolute altitude (meter): " + position.getAbsoluteAltitudeM());
        logger.debug("Relative altitude (meter): " + position.getRelativeAltitudeM());

        // not working under blocking condition
//        var groundTruth = telemetry.getGroundTruth().blockingFirst();
//        logger.debug("Latitude degree (GroundTruth): " + groundTruth.getLatitudeDeg());
//        logger.debug("Longitude degree (GroundTruth): " + groundTruth.getLongitudeDeg());
//        logger.debug("Absolute altitude meter (GroundTruth): " + groundTruth.getAbsoluteAltitudeM());

        var fixedWingMetrics = telemetry.getFixedwingMetrics().blockingFirst();
        logger.debug("Indicated Airspeed (IAS) in m/s: " + fixedWingMetrics.getAirspeedMS());
        logger.debug("Throttle percentage: " + fixedWingMetrics.getThrottlePercentage());
        logger.debug("Climb rate (m/s): " + fixedWingMetrics.getClimbRateMS());

        var odometry = telemetry.getOdometry().blockingFirst();
        logger.debug(String.format("Velocity body (m/s): (%f,%f,%f)",
                odometry.getVelocityBody().getXMS(),
                odometry.getVelocityBody().getYMS(),
                odometry.getVelocityBody().getZMS()));
        logger.debug(String.format("Angular velocity body PYR (rad/s): (%f,%f,%f)",
                odometry.getAngularVelocityBody().getPitchRadS(),
                odometry.getAngularVelocityBody().getYawRadS(),
                odometry.getAngularVelocityBody().getRollRadS()));
        logger.debug("Position body (m/s): " + odometry.getPositionBody());

        var health = telemetry.getHealth().blockingFirst();
        logger.debug("Calibration (Gyrometer): " + health.getIsGyrometerCalibrationOk());
        logger.debug("Calibration (Accelerometer): " + health.getIsAccelerometerCalibrationOk());
        logger.debug("Calibration (Magnetometer): " + health.getIsMagnetometerCalibrationOk());
        logger.debug("Local position is good enough?: " + health.getIsLocalPositionOk());
        logger.debug("Global position is good enough?: " + health.getIsGlobalPositionOk());
        logger.debug("Home position is good enough?: " + health.getIsHomePositionOk());
        logger.debug("Armable: " + health.getIsArmable());

        var allHealth = telemetry.getHealthAllOk().blockingFirst();
        logger.debug("All health is OK?: " + allHealth);

        // not working
//        var statusText = telemetry.getStatusText().blockingFirst();
//        logger.debug("Status text: " + statusText.getText());

        drone.dispose();
        java.lang.System.exit(0);
    }
}
