package demo.vk.service;

import demo.vk.exceptions.NotFoundException;

import demo.vk.KeyValue;
import demo.vk.exceptions.ValidationExeption;
import demo.vk.repository.KVRepository;
import demo.vk.repository.KVRepositoryImpl;

import java.util.stream.Stream;

public class KVBusinessServiceImpl implements KVService {

    private final KVRepository kvRepository = new KVRepositoryImpl();

    @Override
    public void put(String key, byte[] value) {
        keyValidation(key);
        kvRepository.put(key, value);

    }

    @Override
    public void delete(String key) {
        keyValidation(key);
        kvRepository.delete(key);
    }

    @Override
    public Stream<KeyValue> range(String keySince, String keyTo) {

        keyValidation(keySince);
        keyValidation(keyTo);
        if (keySince.compareTo(keyTo) > 0) {
            throw new ValidationExeption("keySince must be <= keyTo");
        }

        return kvRepository.range(keySince, keyTo);
    }

    @Override
    public long count() {
        return kvRepository.count();
    }

    @Override
    public byte[] get(String key) {
        keyValidation(key);
        return kvRepository.get(key)
                .orElseThrow(() -> new NotFoundException("Key not found: " + key));

    }

    private void keyValidation(String key) {
        if (key == null || key.isBlank()) {
            throw new ValidationExeption("Key must not be empty");
        }
    }
}
