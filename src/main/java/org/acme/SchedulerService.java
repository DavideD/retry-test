package org.acme;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.List;

/**
 * @author Sławomir Feliks
 * @version 1.0.0
 */
@ApplicationScoped
public class SchedulerService {



  @WithSession
  public Uni<Void> retrieveDataAndSaveInLocalDB() {
    final String aa = "dsadsdas";
    return Uni.createFrom()
        .deferred(() -> Uni.createFrom()
            .item(this::somethingDeferred))
        .map(a -> getTestEntities())
        .invoke(a -> {
          if (true) throw new RuntimeException("Test Runtime");})
        .onFailure()
        .retry()
        .withBackOff(Duration.ofSeconds(3), Duration.ofMinutes(5))
        .atMost(3)
        .map(a -> a)
        .flatMap(TestEntity::persist)
        .invoke(() -> Log.info(aa))
        .invoke(() -> Log.info("logged info"));
  }

  private Object somethingDeferred()  {
    try {
      Log.info("Before sleep");
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return new TestEntity();
  }

  private Uni<List<TestEntity>> getTestEntities() {
    Log.info("Trying to connect to db");
    Uni<List<TestEntity>> listUni = TestEntity.listAll();
    TestEntity.persist(listUni);
    return listUni;
  }



}
