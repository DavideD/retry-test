package org.acme;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.NonBlocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * @author Sławomir Feliks
 * @version 1.0.0
 */
@ApplicationScoped
public class SchedulerTest {

  @Inject
  SchedulerService schedulerService;

  @Scheduled(cron = "0/10 * * * * ? *")
  @NonBlocking
  public void cronJob() {
    Log.info("Starting job");
    schedulerService.retrieveDataAndSaveInLocalDB()
        .subscribe()
        .with((a) -> Log.info("success"));
  }
}
