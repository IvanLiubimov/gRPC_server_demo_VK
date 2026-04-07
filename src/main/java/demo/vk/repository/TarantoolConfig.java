package demo.vk.repository;

import org.tarantool.SocketChannelProvider;
import org.tarantool.TarantoolClientConfig;
import org.tarantool.TarantoolClientImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;

public class TarantoolConfig {

    public static final int TESTER_SPACE_ID = 513;

    public static void main(String[] args) throws IOException, InterruptedException {

        SocketChannelProvider socketChannelProvider = new SocketChannelProvider() {
            @Override
            public SocketChannel get(int retryNumber, Throwable lastError) {
                if (lastError != null) lastError.printStackTrace();
                try {
                    return SocketChannel.open(new InetSocketAddress("localhost", 3301));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };

        TarantoolClientConfig config = new TarantoolClientConfig();
        config.username = "test";
        config.password = "test";
        config.initTimeoutMillis = 1000;

        TarantoolClientImpl client = new TarantoolClientImpl(socketChannelProvider, config);

        List<Object> tuple = Arrays.asList(1, "hello world");
        client.syncOps().insert(TESTER_SPACE_ID, tuple);


        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) (List<?>) client.syncOps().select(
                TESTER_SPACE_ID,
                0,
                Arrays.asList(1),
                0,
                1000,
                2
        );

        System.out.println("Result from Tarantool: " + result);

        client.close();
    }
}