package demo.vk.repository;

import demo.vk.KeyValue;

import java.util.Optional;
import java.util.stream.Stream;

public interface KVRepository {

    void put(String key, byte[] value);

    Optional<byte[]> get(String key);

    void delete(String key);

    Stream<KeyValue> range(String keySince, String keyTo);

    long count();

}
