package nl.vaneijndhoven.reactivestreams.demo;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

public class Spoilers {

    public static void main(String[] args) throws InterruptedException {
        hotCold();

        Thread.sleep(5000);
    }

    public static void rxDemo() {
        Flowable.range(0, 100)
//                .subscribeOn(Schedulers.newThread())
                .filter(value -> value % 2 == 0)
                .groupBy(value -> value % 10)
                .flatMap(group -> group.toList().toFlowable())
                .doOnNext(value -> System.out.println(Thread.currentThread().getName() + " value: " + value))
                .subscribe();
    }

    public static void reactorDemo() {
//        try (Stream<String> lines = Files.lines(Paths.get("/Users/jpoint/Repositories/reactivestreams-demo/src/main/resources/listings.csv"))) {
//
//            Flux.fromStream(lines)
//                    .subscribeOn(Schedulers.elastic())
//                    .doOnNext(line -> System.out.println(line))
//            .subscribe();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Flux.using(() -> Files.lines(Paths.get("/Users/jpoint/Repositories/reactivestreams-demo/src/main/resources/listings.csv")),
                Flux::fromStream,
                Stream::close)
                .subscribeOn(Schedulers.elastic())
                .doOnNext(line -> System.out.println(line))
                .subscribe();

    }

    public static void hotCold() throws InterruptedException {
        Flux<String> source =
//                Flux.fromIterable(Arrays.asList("ram", "sam", "dam", "lam"))
        Flux.range(0, 100)
                .map(String::valueOf)
                .subscribeOn(Schedulers.elastic())
                .doOnNext(System.out::println)
//                .filter(s -> s.startsWith("l"))
                .map(String::toUpperCase);

//        ConnectableFlux<String> connectable = source.publish();
        source.subscribe(d -> System.out.println("Subscriber 1: "+d));
//        connectable.connect();
        Thread.sleep(20);
        source.subscribe(d -> System.out.println("Subscriber 2: "+d));
    }

}
