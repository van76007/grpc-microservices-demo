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

Client runner will run KvClient for 30s. This client send concurrent RPC up to a limit
using semaphore [Ref](https://grpc.io/blog/optimizing-grpc-part-1/)
```
./gradlew runAsyncServer 

./gradlew clientRunner

```