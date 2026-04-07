import com.google.protobuf.ByteString;
import demo.vk.transport.KVServiceImpl;
import io.grpc.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.kv.grpc.KVProto;
import ru.kv.grpc.KVServiceGrpc;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KVServiceIntegrationTest {

    private static Server server;
    private static ManagedChannel channel;
    private static KVServiceGrpc.KVServiceBlockingStub stub;

    @BeforeAll
    static void setUp() throws Exception {

        server = ServerBuilder.forPort(9091)
                .addService(new KVServiceImpl())
                .build()
                .start();

        channel = ManagedChannelBuilder.forAddress("localhost", 9091)
                .usePlaintext()
                .build();

        stub = KVServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    static void tearDown() {
        if (channel != null) {
            channel.shutdown();
        }
        if (server != null) {
            server.shutdown();
        }
    }

    @Test
    void testPutAndGet() {

        stub.put(KVProto.PutRequest.newBuilder()
                .setKey("test")
                .setValue(ByteString.copyFromUtf8("hello"))
                .build());

        KVProto.GetReply reply = stub.get(
                KVProto.GetRequest.newBuilder()
                        .setKey("test")
                        .build()
        );

        assertEquals("hello", new String(reply.getValue().toByteArray()));
    }

    @Test
    void testDeleteExistingKey() {
        String key = "3";

        stub.put(KVProto.PutRequest.newBuilder()
                .setKey(key)
                .setValue(ByteString.copyFromUtf8("value3"))
                .build());

        KVProto.GetReply before = stub.get(KVProto.GetRequest.newBuilder().setKey(key).build());
        assertNotNull(before.getValue());

        stub.delete(KVProto.DeleteRequest.newBuilder().setKey(key).build());

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () -> {
            stub.get(KVProto.GetRequest.newBuilder().setKey(key).build());
        });
        assertEquals(io.grpc.Status.Code.NOT_FOUND, ex.getStatus().getCode());
    }

    @Test
    void testDeleteNonExistingKey() {
        String key = "non_existing_key";

        StatusRuntimeException e = assertThrows(StatusRuntimeException.class,
                () -> stub.delete(KVProto.DeleteRequest.newBuilder().setKey(key).build()));
        assertTrue(e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND);
    }


    @Test
    void testCount() {
        String key = "countKey_" + System.currentTimeMillis(); // уникальный ключ для теста

        long before = stub.count(KVProto.CountRequest.newBuilder().build())
                .getMessage();

        stub.put(KVProto.PutRequest.newBuilder()
                        .setKey(key)
                        .setValue(ByteString.copyFromUtf8("1"))
                        .build());

        long after = stub.count(KVProto.CountRequest.newBuilder().build())
                .getMessage();

        assertEquals(before + 1, after);
    }

    @Test
    void testRange() {
        stub.put(KVProto.PutRequest.newBuilder()
                .setKey("a")
                .setValue(ByteString.copyFromUtf8("1"))
                .build());

        stub.put(KVProto.PutRequest.newBuilder()
                .setKey("b")
                .setValue(ByteString.copyFromUtf8("2"))
                .build());

        stub.put(KVProto.PutRequest.newBuilder()
                .setKey("c")
                .setValue(ByteString.copyFromUtf8("3"))
                .build());

        var iterator = stub.range(KVProto.RangeRequest.newBuilder()
                .setKeySince("a")
                .setKeyTo("c")
                .build());

        List<String> expectedKeys = List.of("a", "b", "c");
        List<String> actualKeys = new ArrayList<>();

        while (iterator.hasNext()) {
            var kv = iterator.next();
            actualKeys.add(kv.getKey());
        }

        Collections.sort(actualKeys);

        assertEquals(expectedKeys, actualKeys);
    }
}


