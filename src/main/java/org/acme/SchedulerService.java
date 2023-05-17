package org.acme;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * @author Sławomir Feliks
 * @version 1.0.0
 */
@ApplicationScoped
public class SchedulerService {

	@WithSession
	public Uni<Void> retrieveDataAndSaveInLocalDB() {
		final AtomicInteger counter = new AtomicInteger();
		return Uni.createFrom()
				// This will throw an exception a couple of times to simulate an error
				.deferred( () -> Uni.createFrom().item( this.somethingDeferred( counter ) ) )
				.invoke( () -> Log.info( "Detached object created" ) )
				.chain( testEntity -> getTestEntities()
						.map( listAll -> {
							Log.info( "Existing entities: " + listAll );
							return testEntity;
						} )
				)
				.onFailure()
				.retry()
				.withBackOff( Duration.ofSeconds( 3 ), Duration.ofMinutes( 5 ) )
				.atMost( 3 )
				.call( this::persistEntity )
				.invoke( testEntity -> Log.info( testEntity + " persisted!" ) )
				.replaceWithVoid();
	}

	private TestEntity somethingDeferred(AtomicInteger counter) {
		int value = counter.incrementAndGet();
		// Fail the first few times
		if ( value < 3 ) {
			Log.error( "Error " + value );
			throw new RuntimeException( "Not ready!" );
		}
		Log.info( "Creating TestEntity" );
		return new TestEntity();
	}

	public Uni<List<TestEntity>> getTestEntities() {
		Log.info( "Listing all existing entities" );
		return TestEntity.listAll();
	}

	public Uni<Void> persistEntity(TestEntity testEntity) {
		Log.info( "Persisting: " + testEntity );
		return TestEntity.persist( testEntity );
	}
}
