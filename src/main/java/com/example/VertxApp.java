package com.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.Pump;
import io.vertx.ext.reactivestreams.ReactiveReadStream;
import rx.Observable;
import rx.RxReactiveStreams;

public class VertxApp extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        vertx.createHttpServer()
                .requestHandler(r -> {
                    ReactiveReadStream<Buffer> rrs = ReactiveReadStream.readStream();
                    HttpServerResponse response = r.response();

                    // Subscribe the read stream to the publisher
                    RxReactiveStreams
                            .toPublisher(
                                    Observable
                                            .defer(() -> Observable.just("Hello World"))
                                            .map(Buffer::buffer)
                                            .doOnTerminate(response::end)
                            )
                            .subscribe(rrs);

                    // Pump from the read stream to the http response
                    response.setStatusCode(200);
                    response.setChunked(true);
                    response.putHeader("Content-Type", "text/plain");
                    Pump.pump(rrs, response).start();
                })
                .listen(8080);

    }

    public static void main(final String[] args) {
        Launcher.executeCommand("run", VertxApp.class.getName());
    }
}
