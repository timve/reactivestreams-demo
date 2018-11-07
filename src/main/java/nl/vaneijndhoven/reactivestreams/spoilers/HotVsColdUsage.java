package nl.vaneijndhoven.reactivestreams.spoilers;

import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class HotVsColdUsage {

    public static final int START_DELAY = 20;

    public static void main(String[] args) throws InterruptedException {
        coldExample();
//        hotExample();

        Thread.sleep(500);
    }

    static void coldExample() throws InterruptedException {
        Flux<String> source = createProducer();

        source
                .doOnNext(d -> System.out.println("Subscriber 1: "+d))
                .count()
                .subscribe(d -> System.out.println("Subscriber 1 received: "+d+" items"));

        Thread.sleep(START_DELAY);
        // Subscriber 1 and 2 will receive the same items.
        source
                .doOnNext(d -> System.out.println("Subscriber 2: "+d))
                .count()
                .subscribe(d -> System.out.println("Subscriber 2 received: "+d+" items"));;
    }

    static void hotExample() throws InterruptedException {
        Flux<String> source = createProducer();

        ConnectableFlux<String> connectable = source.publish();
        connectable
                .doOnNext(d -> System.out.println("Subscriber 1: "+d))
                .count()
                .subscribe(d -> System.out.println("Subscriber 1 received: "+d+" items"));

        // This makes the flux hot.
        connectable.connect();

        Thread.sleep(START_DELAY);

        // Subscriber 2 will receive less items than subscriber 1.
        connectable
                .doOnNext(d -> System.out.println("Subscriber 2: "+d))
                .count()
                .subscribe(d -> System.out.println("Subscriber 2 received: "+d+" items"));;
    }

    private static Flux<String> createProducer() {
        return Flux.range(0, 100)
                .map(String::valueOf)
                .subscribeOn(Schedulers.elastic());
    }

}
