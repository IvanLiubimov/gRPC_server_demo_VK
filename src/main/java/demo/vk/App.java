package demo.vk;

import demo.vk.transport.KVServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class App {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(9090)
                .addService(new KVServiceImpl())
                .build()
                .start();
        System.out.println("Server started at port 9090");
        server.awaitTermination();
    }
}