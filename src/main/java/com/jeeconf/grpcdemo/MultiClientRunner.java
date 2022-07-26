package com.jeeconf.grpcdemo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiClientRunner {
    public static void main(String[] args)
    {
        CountDownLatch latch = new CountDownLatch(1);
        MyThread t1 = new MyThread(latch);
        MyThread t2 = new MyThread(latch);
        new Thread(t1).start();
        new Thread(t2).start();
        latch.countDown();          //This will inform all the threads to start
    }
}

class MyThread implements Runnable
{
    CountDownLatch latch;
    public MyThread(CountDownLatch latch)
    {
        this.latch = latch;
    }
    @Override
    public void run()
    {
        try
        {
            latch.await();          //The thread keeps waiting till it is informed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ManagedChannel channel = ManagedChannelBuilder.forTarget("dns:///localhost:8090").usePlaintext(true).build();
        
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        long DURATION_SECONDS = 120;
        try {
            AtomicBoolean done = new AtomicBoolean();
            KvClient client = new KvClient(channel, 100);
            System.out.println("Starting");
            scheduler.schedule(() -> done.set(true), DURATION_SECONDS, TimeUnit.SECONDS);
            client.doClientWork(done);
            double qps = (double) client.getRpcCount() / DURATION_SECONDS;
            System.out.println("Did " + qps + " RPCs/s");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            scheduler.shutdownNow();
            channel.shutdownNow();
        }

    }
}
