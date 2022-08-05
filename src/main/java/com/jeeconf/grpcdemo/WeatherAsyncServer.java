package com.jeeconf.grpcdemo;

import com.jeeconf.grpcdemo.dependencies.*;
import com.jeeconf.grpcdemo.dependencies.HumidityServiceGrpc.HumidityServiceFutureStub;
import com.jeeconf.grpcdemo.dependencies.TemperatureServiceGrpc.TemperatureServiceFutureStub;
import com.jeeconf.grpcdemo.dependencies.WindServiceGrpc.WindServiceFutureStub;
import io.grpc.Server;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * Starts weather, temperature, humidity and wind servers.
 */
public class WeatherAsyncServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        String host = "localhost";
        int temperaturePort = 8081;
        int humidityPort = 8082;
        int windPort = 8083;

        Server temperatureServer = NettyServerBuilder.forPort(temperaturePort)
                .addService(new TemperatureService()).build().start();
        Server humidityServer = NettyServerBuilder.forPort(humidityPort)
                .addService(new HumidityService()).build().start();
        Server windServer = NettyServerBuilder.forPort(windPort).addService(new WindService()).build().start();

        TemperatureServiceFutureStub temperatureStub =
                TemperatureServiceGrpc.newFutureStub(NettyChannelBuilder.forAddress(host, temperaturePort).usePlaintext(true).build());
        HumidityServiceFutureStub humidityStub =
                HumidityServiceGrpc.newFutureStub(NettyChannelBuilder.forAddress(host, humidityPort).usePlaintext(true).build());
        WindServiceFutureStub windStub =
                WindServiceGrpc.newFutureStub(NettyChannelBuilder.forAddress(host, windPort).usePlaintext(true).build());

        WeatherAsyncService weatherService = new WeatherAsyncService(temperatureStub, humidityStub, windStub);
        Server weatherServer = configureExecutor().addService(weatherService).build().start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            weatherServer.shutdownNow();
            temperatureServer.shutdownNow();
            humidityServer.shutdownNow();
            windServer.shutdownNow();
        }));

        weatherServer.awaitTermination();
    }

    private static NettyServerBuilder configureExecutor() {
        NettyServerBuilder sb = NettyServerBuilder.forPort(8090);

        String threads = System.getenv("JVM_EXECUTOR_THREADS");
        int i_threads = Runtime.getRuntime().availableProcessors();
        if (threads != null && !threads.isEmpty()) {
            i_threads = Integer.parseInt(threads);
        }
        // In principle, number of threads should be equal to number of CPUs but let try
        i_threads = i_threads * 16;
        String value = System.getenv().getOrDefault("JVM_EXECUTOR_TYPE", "fixed");
        System.out.println("Number of threads " + i_threads + " and executor style=" + value);

        if (Objects.equals(value, "direct")) {
            sb = sb.directExecutor();
        } else if (Objects.equals(value, "single")) {
            sb = sb.executor(Executors.newSingleThreadExecutor());
        } else if (Objects.equals(value, "fixed")) {
            sb = sb.executor(Executors.newFixedThreadPool(i_threads));
        } else if (Objects.equals(value, "workStealing")) {
            sb = sb.executor(Executors.newWorkStealingPool(i_threads));
        } else if (Objects.equals(value, "cached")) {
            sb = sb.executor(Executors.newCachedThreadPool());
        }
        return sb;
    }
}
