package nl.vaneijndhoven.reactivestreams.spoilers;

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

}
