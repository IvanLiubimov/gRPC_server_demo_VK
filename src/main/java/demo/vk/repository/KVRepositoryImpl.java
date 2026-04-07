package demo.vk.repository;

import demo.vk.KeyValue;
import demo.vk.exceptions.NotFoundException;
import org.tarantool.SocketChannelProvider;
import org.tarantool.TarantoolClientConfig;
import org.tarantool.TarantoolClientImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class KVRepositoryImpl implements KVRepository {

    private static final int KV_SPACE_ID = 514; // задаём ID space kv_space
    private final TarantoolClientImpl client;

    public KVRepositoryImpl() {
        SocketChannelProvider provider = (retry, lastError) -> {
            if (lastError != null) lastError.printStackTrace();
            try {
                return SocketChannel.open(new InetSocketAddress("localhost", 3301));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        };

        TarantoolClientConfig config = new TarantoolClientConfig();
        config.username = "test";
        config.password = "test";
        config.initTimeoutMillis = 1000;

        client = new TarantoolClientImpl(provider, config);
    }

    @Override
    public void put(String key, byte[] value) {
        List<Object> tuple = Arrays.asList(key, value);
        client.syncOps().replace(KV_SPACE_ID, tuple);
    }


    @Override
    public Optional<byte[]> get(String key) {
        List<?> result = client.syncOps().select(KV_SPACE_ID, 0, Arrays.asList(key), 0, 1, 0);
        if (result.isEmpty()) return Optional.empty();

        List<Object> tuple = (List<Object>) result.get(0);
        return Optional.of((byte[]) tuple.get(1));
    }

    @Override
    public void delete(String key) {
        List<?> deleted = client.syncOps().delete(KV_SPACE_ID, Arrays.asList(key));
        if (deleted.isEmpty()) {
            throw new NotFoundException("Key not found: " + key);
        }
    }

    @Override
    public Stream<KeyValue> range(String keySince, String keyTo) {
        @SuppressWarnings("unchecked")
        List<List<Object>> result = (List<List<Object>>) (List<?>) client.syncOps().select(
                KV_SPACE_ID,
                0,
                Arrays.asList(keySince),
                0,
                1000,
                2
        );

        return result.stream()
                .map(tuple -> new KeyValue((String) tuple.get(0), (byte[]) tuple.get(1)))
                .filter(kv -> kv.getKey().compareTo(keyTo) <= 0);
    }

    @Override
    public long count() {
        List<?> tuples = client.syncOps().select(
                KV_SPACE_ID,
                0,
                Arrays.asList(),
                0,
                Integer.MAX_VALUE,
                2
        );
        return tuples.size();
    }
}
