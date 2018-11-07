package nl.vaneijndhoven.reactivestreams.spoilers;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.reactivex.Flowable;
import reactor.core.publisher.Flux;

public class BasicUsage {

    public static void main(String[] args) throws InterruptedException {
        rxDemo();
//        reactorDemo();
//        akkaStreamsDemo();

//        Thread.sleep(500);
    }

    public static void rxDemo() {
        Flowable.range(0, 100)
                .filter(value -> value % 2 == 0) // Only keep even items
                .groupBy(value -> value / 10)   // Group them
                .flatMap(group -> group.toList().toFlowable()) //Operate on groups
                .doOnNext(value -> System.out.println(Thread.currentThread().getName() + " value: " + value))
//                .subscribeOn(io.reactivex.schedulers.Schedulers.newThread())
                .subscribe();
    }

    public static void reactorDemo() {
        Flux.range(0, 100)
                .filter(value -> value % 2 == 0) // Only keep even items
                .groupBy(value -> value / 10)   // Group them
                .flatMap(Flux::collectList) //Operate on groups
                .doOnNext(value -> System.out.println(Thread.currentThread().getName() + " value: " + value))
//                .subscribeOn(reactor.core.scheduler.Schedulers.single())
                .subscribe();
    }

    public static void akkaStreamsDemo() {
        Source.range(0, 99)
                .filter(value -> value % 2 == 0) // Only keep even items
                .groupBy(10, value -> value / 10)   // Group them
                // SubStreams from here
                .grouped(100)
                .mergeSubstreams()
                // Back to one stream
                .map(value ->
                {
                    System.out.println(Thread.currentThread().getName() + " value: " + value);
                    return value;
                }).runWith(Sink.ignore(), ActorMaterializer.create(ActorSystem.create()));
    }

}
