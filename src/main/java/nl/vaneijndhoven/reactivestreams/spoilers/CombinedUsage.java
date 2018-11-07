package nl.vaneijndhoven.reactivestreams.spoilers;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.JavaFlowSupport;
import akka.stream.javadsl.Source;
import akka.stream.scaladsl.Sink;
import hu.akarnokd.rxjava2.interop.FlowInterop;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public class CombinedUsage {

    public static void main(String[] args) {
        integrationUsingReactiveStreams();
//        integrationUsingJava9Flow();
    }

    private static void integrationUsingReactiveStreams() {
        ActorMaterializer materializer = ActorMaterializer.create(ActorSystem.create());

        // Note that rxJava and reactor parts observe/publish on their own threads to show
        // crossing threads, when these are removed everything will be run on the publishing thread (akka streams)

        // Create items with Akka Streams
        Publisher<Integer> akkaStreamsPublisher = Source.range(0, 9).map(value -> {
            printThread("akka");
            return value;
        }).runWith(Sink.asPublisher(true), materializer);

        // Manipulate with rxJava
        Flowable<List<Integer>> rxPublisher = Flowable.fromPublisher(akkaStreamsPublisher)
                .observeOn(io.reactivex.schedulers.Schedulers.newThread())
                .doOnNext(x -> printThread("rx"))
                .filter(value -> value % 2 == 0) // Only keep even items
                .groupBy(value -> value / 5)// Group them
                .flatMap(group -> group.toList().toFlowable());

        // Output with reactor
        Flux<List<Integer>> reactorPublisher = Flux.from(rxPublisher)
                .publishOn(reactor.core.scheduler.Schedulers.single())
                .doOnNext(x -> printThread("reactor"))
                .doOnNext(value -> System.out.println(" value: " + value));

        reactorPublisher.subscribe();
    }

    private static void integrationUsingJava9Flow() {
        ActorMaterializer materializer = ActorMaterializer.create(ActorSystem.create());

        // Note that rxJava and reactor parts observe/publish on their own threads to show
        // crossing threads, when these are removed everything will be run on the publishing thread (akka streams)

        // Create items with Akka Streams
        // Use JavaFlowSupport to adapt Akka Stream types from/to Java 9 Flow interfaces
        Flow.Publisher<Integer> akkaStreamsPublisher = Source.range(0, 9).map(value -> {
            printThread("akka");
            return value;
        }).runWith(JavaFlowSupport.Sink.asPublisher(AsPublisher.WITH_FANOUT), materializer);

        // Manipulate with rxJava
        // Use FlowInterop to adapt RxJava types from/to Java 9 Flow interfaces
        Flow.Publisher<List<Integer>> rxPublisher = FlowInterop.toFlowPublisher(FlowInterop.fromFlowPublisher(akkaStreamsPublisher)
                .observeOn(Schedulers.newThread())
                .doOnNext(x -> printThread("rx"))
                .filter(value -> value % 2 == 0) // Only keep even items
                .groupBy(value -> value / 5)// Group them
                .flatMap(group -> group.toList().toFlowable()));

        // Output with reactor
        // Use JdkFlowAdapter to adapt Reactor types from/to Java 9 Flow interfaces
        Flux<List<Integer>> reactorPublisher = JdkFlowAdapter.flowPublisherToFlux(rxPublisher)
                .publishOn(reactor.core.scheduler.Schedulers.single())
                .doOnNext(x -> printThread("reactor"))
                .doOnNext(value -> System.out.println(" value: " + value));

        reactorPublisher.subscribe();
    }

    private static void printThread(String step) {
        System.out.println(step + " runs on: " + Thread.currentThread().getName());
    }

}
