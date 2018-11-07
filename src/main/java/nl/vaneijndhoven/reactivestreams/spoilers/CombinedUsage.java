package nl.vaneijndhoven.reactivestreams.spoilers;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Source;
import akka.stream.scaladsl.Sink;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.List;

public class CombinedUsage {

    public static void main(String[] args) {
        ActorMaterializer materializer = ActorMaterializer.create(ActorSystem.create());

        // Note that rxJava and reactor parts observe/publish on their own threads to show
        // crossing threads, when these are removed everything will be run on the publishing thread (akka streams)

        // Create items with Akka Streams
        Publisher<Integer> akkaStreamsPublisher = Source.range(0, 10).map(x -> {
            printThread("akka");
            return x;
        }).runWith(Sink.asPublisher(true), materializer);

        // Manipulate with rxJava
        Flowable<List<Integer>> rxPublisher = Flowable.fromPublisher(akkaStreamsPublisher)
                .observeOn(io.reactivex.schedulers.Schedulers.newThread())
                .doOnNext(x -> printThread("rx"))
                .filter(value -> value % 2 == 0) // Only keep even items
                .groupBy(value -> value % 10)// Group them
                .flatMap(group -> group.toList().toFlowable());

        // Output with reactor
        Flux<List<Integer>> reactorPublisher = Flux.from(rxPublisher)
                .publishOn(reactor.core.scheduler.Schedulers.single())
                .doOnNext(x -> printThread("reactor"))
                .doOnNext(value -> System.out.println(" value: " + value));

        reactorPublisher.subscribe();
    }

    private static void printThread(String step) {
        System.out.println(step + " runs on: " + Thread.currentThread().getName());
    }

}
