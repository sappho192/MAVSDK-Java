/* I've changed official RunMission.java in MAVSDK-Java (v1.3.1) example a bit
*  because the example is just uploading & downloading mission.
*  The same example in C++ SDK demonstrates not only uploading but executing mission so I applied similar logic to this Java example code.
*/

package io.mavsdk.example;

import io.mavsdk.System;
import io.mavsdk.mission.Mission;
import io.mavsdk.telemetry.Telemetry;
import io.reactivex.CompletableSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunMission {
  private static final Logger logger = LoggerFactory.getLogger(RunMission.class);

  public static void main(String[] args) {
    logger.debug("Starting example: mission...");

    String SERVER_IP = "172.0.0.1";
    int SERVER_PORT = 50051;
    System drone = new System(SERVER_IP, SERVER_PORT);

    drone.getTelemetry().getFlightMode()
            .doOnNext(flightMode -> {
              logger.debug("flight mode: " + flightMode);
              var position = drone.getTelemetry().getPosition().blockingFirst();
              logger.debug(String.format("Position: Altitude %f / Latitude %f / Longitude %f",
                      position.getAbsoluteAltitudeM(), position.getLatitudeDeg(), position.getLongitudeDeg()));
            })
            .subscribe();
    drone.getTelemetry().getArmed()
            .doOnNext(armed -> {
              if(!armed) {
                logger.debug("Disarmed");
              }
            })
            .subscribe();
    drone.getTelemetry().getLandedState()
            .doOnNext(landed -> {
              switch (landed) {
                case UNKNOWN:
                  break;
                case ON_GROUND:
                  logger.debug("Drone: ON_GROUND");
                  break;
                case TAKING_OFF:
                  logger.debug("Drone: TAKING_OFF");
                  break;
                case LANDING:
                  logger.debug("Drone: LANDING");
                  break;
              }
            })
            .subscribe();
//    drone.getTelemetry().getPosition()
//            .doOnNext(position -> logger.debug(String.format("Alt: %s, Long: %f, Lat: %f", position.getAbsoluteAltitudeM(), position.getLongitudeDeg(), position.getLatitudeDeg())))
//            .subscribe();

    List<Mission.MissionItem> cycle = new ArrayList<>();
    cycle.add(generateMissionItem(47.398170327054473, 8.5456490218639658, 10.0f, 5.0f, false, 20.0f, 60.0f, Mission.MissionItem.CameraAction.NONE));
    cycle.add(generateMissionItem(47.398241338125118, 8.5455360114574432, 10.0f, 2.0f, true, 0.0f, -60.0f, Mission.MissionItem.CameraAction.NONE));
    cycle.add(generateMissionItem(47.398139363821485, 8.5453846156597137, 10.0f, 5.0f, true, -45.0f, 0.0f, Mission.MissionItem.CameraAction.NONE));
    cycle.add(generateMissionItem(47.398058617228855, 8.5454618036746979, 10.0f, 2.0f, false, -90.0f, 30.0f, Mission.MissionItem.CameraAction.NONE));
    cycle.add(generateMissionItem(47.398100366082858, 8.5456969141960144, 10.0f, 5.0f, false, -45.0f, -30.0f, Mission.MissionItem.CameraAction.NONE));
    cycle.add(generateMissionItem(47.398001890458097, 8.545557618141174, 10.0f, 5.0f, false, 0.0f, 0.0f, Mission.MissionItem.CameraAction.NONE));

    List<Mission.MissionItem> missionItems = new ArrayList<>();
    for (int i = 0; i < 1; i++) {
      missionItems.addAll(cycle);
    }

    Mission.MissionPlan missionPlan = new Mission.MissionPlan(missionItems);
    logger.debug("About to upload " + missionItems.size() + " mission items");

    CountDownLatch latch = new CountDownLatch(1);
    drone.getMission()
            .setReturnToLaunchAfterMission(true)
            .andThen(drone.getMission().uploadMission(missionPlan)
                    .doOnComplete(() -> logger.debug("Upload succeeded")))
            .andThen(drone.getAction().arm().doOnComplete(() -> logger.debug("Armed")))
            .andThen(drone.getMission().startMission().doOnComplete(() -> logger.debug("Starting mission...")))
            .andThen(drone.getMission().downloadMission()
                    .doOnSubscribe(disposable -> logger.debug("Downloading mission"))
                    .doAfterSuccess(disposable -> logger.debug("Mission downloaded")))
            .toCompletable()
            .andThen((CompletableSource) cs -> latch.countDown())
            .subscribe();

    try {
      latch.await();
    } catch (InterruptedException ignored) {
      // This is expected
    }
  }

  public static Mission.MissionItem generateMissionItem(
          double latitudeDeg, double longitudeDeg,
          float relativeAltitudeM, float speedMSS, boolean isFlyThrough,
          float gimbalPitchDeg, float gimbalYawDeg,
          Mission.MissionItem.CameraAction cameraAction) {
    return new Mission.MissionItem(
            latitudeDeg,
            longitudeDeg,
            relativeAltitudeM,
            speedMSS,
            isFlyThrough,
            gimbalPitchDeg,
            gimbalYawDeg,
            cameraAction,
            Float.NaN,
            Double.NaN,
            Float.NaN,
            Float.NaN,
            Float.NaN);
  }
}
