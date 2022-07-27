package com.jeeconf.grpcdemo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientRunner {

  private static final long DURATION_SECONDS = 180;
  private ManagedChannel channel;

  public static void main(String[] args) {
    ClientRunner runner = new ClientRunner();
    try {
      runner.runClient();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void runClient() throws InterruptedException {
    if (channel != null) {
      throw new IllegalStateException("Already started");
    }

    channel = ManagedChannelBuilder.forTarget("dns:///localhost:8090").usePlaintext(true).build();
    // channel = NettyChannelBuilder.forAddress("localhost", 8090).usePlaintext(true).build();

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    try {
      AtomicBoolean done = new AtomicBoolean();
      KvClient client = new KvClient(channel, 200);
      System.out.println("Starting");
      scheduler.schedule(() -> done.set(true), DURATION_SECONDS, TimeUnit.SECONDS);
      client.doClientWork(done);
      double qps = (double) client.getRpcCount() / DURATION_SECONDS;
      System.out.println("Did " + qps + " RPCs/s");
    } finally {
      scheduler.shutdownNow();
      channel.shutdownNow();
    }
  }
}