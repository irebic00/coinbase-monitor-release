package com.lureb.monitor.tasks;

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class Liveness {
  private final Logger LOGGER = LogManager.getLogger(Liveness.class);

  private Date lastCalled;
  private long timeThreshold;
  private long hardcodedTimeout = 10;

  /** Private constructor */
  public Liveness() {
    lastCalled = new Date(System.currentTimeMillis());
  }

  public synchronized void iAmAlive() {
    lastCalled.setTime(System.currentTimeMillis());
  }

  public boolean amIAlive() {
    Date currentTime = new Date(System.currentTimeMillis());
    long difference = currentTime.getTime() - lastCalled.getTime();

    if (difference < hardcodedTimeout * 1000l) {
      return true;
    }
    return false;
  }

  public long getTimeThreshold() {
    return timeThreshold;
  }

  public void setTimeThreshold(long timeThreshold) {
    this.timeThreshold = timeThreshold;
  }
}
