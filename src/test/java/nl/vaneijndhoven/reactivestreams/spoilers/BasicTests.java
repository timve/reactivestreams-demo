package nl.vaneijndhoven.reactivestreams.spoilers;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Source;
import akka.stream.testkit.javadsl.TestSink;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class BasicTests {

    @Test
    public void testStepVerifier() {
        Flux<Integer> range = Flux.range(0, 10);

        StepVerifier.create(range)
                .expectNextCount(10)
                .verifyComplete();
    }

    @Test
    public void testTestSubscriber() {
        Flowable<Integer> range = Flowable.range(0, 10);

        TestSubscriber<Object> test = TestSubscriber.create();
        range.subscribe(test);

        test.assertNoErrors();
        test.assertValueCount(10);
        test.assertComplete();
    }

    @Test
    public void testTestSink() {
        ActorSystem system = ActorSystem.create();

        Source.range(0, 9).runWith(TestSink.probe(system), ActorMaterializer.create(system))
                .request(100)
                .expectNext(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
                .expectComplete();
    }

}
