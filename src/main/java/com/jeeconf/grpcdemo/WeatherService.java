package com.jeeconf.grpcdemo;

import com.jeeconf.grpcdemo.WeatherServiceGrpc.WeatherServiceImplBase;
import io.grpc.stub.StreamObserver;

import static com.jeeconf.grpcdemo.Temperature.Units.CELSUIS;
import java.util.concurrent.TimeUnit;

/**
 * Returns hard-coded weather response.
 */
public class WeatherService extends WeatherServiceImplBase {
    private static final long DELAY_MILLIS = 5;

    @Override
    public void getCurrent(WeatherRequest request, StreamObserver<WeatherResponse> responseObserver) {
        WeatherResponse response = WeatherResponse.newBuilder()
                .setTemperature(Temperature.newBuilder().setUnits(CELSUIS).setDegrees(20.f))
                .setHumidity(Humidity.newBuilder().setValue(.65f))
                .build();
        simulateWork(DELAY_MILLIS);
        // Reply
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void simulateWork(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
