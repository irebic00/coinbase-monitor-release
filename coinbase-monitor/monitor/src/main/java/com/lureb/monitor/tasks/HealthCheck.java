package com.lureb.monitor.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class HealthCheck implements HealthIndicator {

  private final Logger LOGGER = LogManager.getLogger(HealthCheck.class);

  @Autowired private Liveness liveness;

  @Override
  public Health health() {
    if (healthy()) {
      LOGGER.debug("Liveness probe successful");
      return Health.up().build();
    }

    LOGGER.error("Liveness probe not successful");
    return Health.down().build();
  }

  private boolean healthy() {
    return liveness.amIAlive();
  }
}
