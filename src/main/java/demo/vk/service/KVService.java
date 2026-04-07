package demo.vk.service;

import demo.vk.KeyValue;

import java.util.stream.Stream;

public interface KVService {
    void put(String key, byte[] value);
    byte[] get(String key);
    void delete(String key);
    Stream<KeyValue> range(String keySince, String keyTo);
    long count();
}