package com.jeeconf.grpcdemo;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Starts gRPC server with {@link WeatherService}.
 */
public class WeatherServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        /*
        Server grpcServer = NettyServerBuilder.forPort(8090)
                .addService(new WeatherService()).build()
                .start();
         */

        // Custom executor
        /*
        Server grpcServer = NettyServerBuilder.forPort(8090)
                .executor(Executors.newFixedThreadPool(100))
                .addService(new WeatherService()).build()
                .start();
         */

        Server grpcServer = configureExecutor()
                .addService(new WeatherService()).build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(grpcServer::shutdown));
        grpcServer.awaitTermination();
    }

    private static NettyServerBuilder configureExecutor() {
        NettyServerBuilder sb = NettyServerBuilder.forPort(8090);

        String threads = System.getenv("JVM_EXECUTOR_THREADS");
        int i_threads = Runtime.getRuntime().availableProcessors();
        if (threads != null && !threads.isEmpty()) {
            i_threads = Integer.parseInt(threads);
        }
        String value = System.getenv().getOrDefault("JVM_EXECUTOR_TYPE", "workStealing");
        System.out.println("Number of threads " + i_threads + " and executor style=" + value);

        if (Objects.equals(value, "direct")) {
            sb = sb.directExecutor();
        } else if (Objects.equals(value, "single")) {
            sb = sb.executor(Executors.newSingleThreadExecutor());
        } else if (Objects.equals(value, "fixed")) {
            sb = sb.executor(Executors.newFixedThreadPool(i_threads));
        } else if (Objects.equals(value, "workStealing")) {
            sb = sb.executor(Executors.newWorkStealingPool(100));
        } else if (Objects.equals(value, "cached")) {
            sb = sb.executor(Executors.newCachedThreadPool());
        }
        return sb;
    }
}
