package demo.vk;

public class KeyValue {

    private String key;
    private byte[] value;

    public KeyValue (String key, byte[] value){
        this.key = key;
        this.value = value;
    }

    public byte[] getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}
