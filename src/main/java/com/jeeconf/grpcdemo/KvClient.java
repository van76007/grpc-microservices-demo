package com.jeeconf.grpcdemo;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import com.jeeconf.grpcdemo.WeatherServiceGrpc.WeatherServiceFutureStub;
import io.grpc.ManagedChannel;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalTime;

public class KvClient {
  private final ManagedChannel channel;
  private AtomicLong rpcCount = new AtomicLong();
  private final Semaphore limiter = new Semaphore(1);

  public KvClient(ManagedChannel channel) {
    this.channel = channel;
  }

  public long getRpcCount() {
    return rpcCount.get();
  }

  public void doClientWork(AtomicBoolean done) throws InterruptedException {
    WeatherServiceFutureStub stub = WeatherServiceGrpc.newFutureStub(channel);
    while(!done.get()) {
      // Call server
      limiter.acquire();

      WeatherRequest request = WeatherRequest.newBuilder()
          .setCoordinates(Coordinates.newBuilder().setLatitude(KyivCoordinates.LATITUDE)
              .setLongitude(KyivCoordinates.LONGITUDE)).build();
      ListenableFuture<WeatherResponse> res = stub.getCurrent(request);

      res.addListener(() ->  {
        rpcCount.incrementAndGet();
        limiter.release();
      }, MoreExecutors.directExecutor());
      Futures.addCallback(res, new FutureCallback<WeatherResponse>() {
        @Override
        public void onSuccess(WeatherResponse response) {
          // System.out.println("Async client. Current weather for %s: %s.%n", request, response);
          System.out.println("Got response at " + LocalTime.now());
        }

        @Override
        public void onFailure(Throwable t) {
          Status status = Status.fromThrowable(t);
          System.out.println("On Failure " + status.getCode());
        }
      });
    }
  }
}