package nl.vaneijndhoven.reactivestreams.spoilers;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.util.ByteString;
import io.reactivex.Flowable;
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
        tryWithResources();
//        tryWithResources2();
//        useRxImplementation();
//        useReactorImplementation();
//        useAkkaImplementation();

        Thread.sleep(5000);
    }

    private static void tryWithResources() {
        try (Stream<String> lines = Files.lines(file)) {

            Flux.fromStream(lines)
                    .subscribeOn(Schedulers.elastic()) // run on IO thread
                    .doOnNext(System.out::println)
            .subscribe();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Finalizing");
        }
    }

    private static void tryWithResources2() {
        // Use try-with-resources block within supplier
        Flux.fromStream(() -> {
            try (Stream<String> lines = Files.lines(file)) {
                return lines;
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                System.out.println("Finalizing");
            }
        })
                .subscribeOn(Schedulers.elastic()) // run on IO thread
                .doOnNext(System.out::println)
                .subscribe();
    }

    private static void useReactorImplementation() {
        Flux.using(() -> Files.lines(file),
                Flux::fromStream,
                Stream::close)
                .subscribeOn(reactor.core.scheduler.Schedulers.elastic())
                .doOnNext(System.out::println)
                .subscribe();
    }

    private static void useRxImplementation() {
        Flowable.using(() -> Files.lines(file),
                Flux::fromStream,
                Stream::close)
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .doOnNext(System.out::println)
                .subscribe();
    }

    private static void useAkkaImplementation() {
        FileIO.fromPath(file)
                .via(Framing.delimiter(ByteString.fromString("\n"), 100000))
                .map(ByteString::utf8String)
                .runForeach(System.out::println, ActorMaterializer.create(ActorSystem.create()));
    }

}
