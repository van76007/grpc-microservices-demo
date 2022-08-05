# grpc-microservices-demo

grpc-java demo.

[gRPC repository](https://github.com/grpc/grpc)

[gRPC Java repository](https://github.com/grpc/grpc-java)

[Original repo](https://github.com/alxbnet/grpc-microservices-demo)

[Talk](https://fr.slideshare.net/borisovalex/enabling-googley-microservices-with-grpc-at-jdkio-2017)

### Running services

#### Unary

```
./gradlew runServer
```

```
./gradlew runClient
```

#### Streaming

```
./gradlew streamingServer
```

```
./gradlew streamingClient
```

### Async server

```
./gradlew runAsyncServer

./gradlew runAsyncClient
```

### Client runner

Client runner will run KvClient for 30s. This client sends concurrent RPC up to a limit
using semaphore [Ref](https://grpc.io/blog/optimizing-grpc-part-1/)
```
Start sync server
./gradlew runServer

OR async server
./gradlew runAsyncServer 

Start 1 client
./gradlew clientRunner

OR start 2 cliens concurrently
./gradlew multiClientsRunner

```

### Result
Server listening in port 8090. Wireshark should filter by that port, i.e. `tcp.port == 8090`

1. clientRunner(semaphore=200) + WeatherServer(default executors, no limit):
   Did 12722.758333333333 RPCs/s. Did 8650.075 RPCs/s
   Wireshark I/O: 1250 packet/s
   Peak 215 threads on server
Note that if stub is global variable in KvClient: Did 7482.383333333333 RPCs/s
Each HTTP2 packet has 10 streams.

2. clientRunner(semaphore=200) + WeatherAsyncServer(default executors, no limit) block on WindProvider
   Did 3466.7833333333333 RPCs/s
   Wireshark I/O: 1250 packet/s
   Peak 215 threads

3. clientRunner(semaphore=200) + WeatherAsyncServer(default executors, no limit) block on WeatherAsyncservice
   Did 4873.791666666667 RPCs/s

4. multiClientsRunner(semaphore=100) + WeatherServer:
   1 client Did 3595.3333333333335 RPCs/s. Other Did 3582.4333333333334 RPCs/s
   Wireshark I/O: 1500 packet/s 
   Peak 220 threads on server

5. clientRunner(semaphore=200) + WeatherServer(fixed thread pool = 100):
   Did 8795.791666666666 RPCs/s. Peak 115 threads on server. Wireshark I/O: 1500 packets/s
   Did 1064.1166666666666 RPCs/s if the server uses fixed thread pool = 10. Peak 25 threads. Wireshark I/O: 2000 packets/s
   Did Did 21806.077777777777 RPCs/s if the server uses fixed thread pool = 512. Peak 525 threads. Wireshark I/O: 30000 packets/s
   Tested on 8-core CPU machine so should not have set the number of threads > 8

6. clientRunner(semaphore=200) + WeatherServer(workstealing pool = 8)
   Did 1387.7833333333333 RPCs/s
   Wireshark I/O: 2000 packet/s

7. Set parallelism higher than number of CPU = 8:
   clientRunner(semaphore=200) + WeatherServer(workstealing pool = 100)
   Did 9651.891666666666 RPCs/s
   Wireshark I/O: 3200 packet/s

8. Note that we do not limit the numbers of threads on the client!