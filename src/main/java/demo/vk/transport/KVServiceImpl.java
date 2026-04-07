package demo.vk.transport;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import demo.vk.KeyValue;
import demo.vk.exceptions.NotFoundException;
import demo.vk.service.KVBusinessServiceImpl;
import demo.vk.service.KVService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ru.kv.grpc.KVProto;
import ru.kv.grpc.KVServiceGrpc;

import java.util.stream.Stream;

public class KVServiceImpl extends KVServiceGrpc.KVServiceImplBase {

    private final KVService kvService = new KVBusinessServiceImpl();

    @Override
    public void get(KVProto.GetRequest request, StreamObserver<KVProto.GetReply> responseObserver) {
        try {
            String key = request.getKey();
            byte[] value = kvService.get(key);

            KVProto.GetReply reply = KVProto.GetReply.newBuilder()
                    .setValue(ByteString.copyFrom(value))
                    .build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();

        } catch (NotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException()
            );
        }
    }

    @Override
    public void put(KVProto.PutRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String key = request.getKey();
            byte[] value = request.getValue().toByteArray();
            kvService.put(key, value);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void delete(KVProto.DeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            String key = request.getKey();

            kvService.delete(key);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (NotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException()
            );
        }
    }

    @Override
    public void range(KVProto.RangeRequest request, StreamObserver<KVProto.KeyValue> responseObserver) {
        try {
            String keySince = request.getKeySince();
            String keyTo = request.getKeyTo();

            Stream<KeyValue> stream = kvService.range(keySince, keyTo);

            stream.forEach(kv -> {
                KVProto.KeyValue response = KVProto.KeyValue.newBuilder()
                        .setKey(kv.getKey())
                        .setValue(ByteString.copyFrom(kv.getValue()))
                        .build();

                responseObserver.onNext(response);
            });

            responseObserver.onCompleted();
        } catch (NotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException()
            );
        }
    }

    @Override
    public void count(KVProto.CountRequest request, StreamObserver<KVProto.CountReply> responseObserver) {
        try {
            long count = kvService.count();

            KVProto.CountReply reply = KVProto.CountReply.newBuilder()
                    .setMessage(count)
                    .build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
