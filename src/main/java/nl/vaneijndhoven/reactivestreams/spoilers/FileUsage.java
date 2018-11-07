package nl.vaneijndhoven.reactivestreams.spoilers;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FileUsage {

    public static final Path file = Paths.get("/Users/jpoint/Repositories/reactivestreams-demo/src/main/resources/listings.csv");

    public static void main(String[] args) throws InterruptedException {
        useImplementation();

        Thread.sleep(500);
    }

    private static void tryWithResources() {
        try (Stream<String> lines = Files.lines(Paths.get("/Users/jpoint/Repositories/reactivestreams-demo/src/main/resources/listings.csv"))) {

            Flux.fromStream(lines)
                    .subscribeOn(Schedulers.elastic()) // run on IO thread
                    .doOnNext(line -> System.out.println(line))
            .subscribe();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Finalizing");
        }
    }

    private static void useImplementation() {
        Flux.using(() -> Files.lines(Paths.get("/Users/jpoint/Repositories/reactivestreams-demo/src/main/resources/listings.csv")),
                Flux::fromStream,
                Stream::close)
                .subscribeOn(Schedulers.elastic())
                .doOnNext(line -> System.out.println(line))
                .subscribe();
    }

}
