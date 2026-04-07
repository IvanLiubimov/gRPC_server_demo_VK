package demo.vk.exceptions;

public class ValidationExeption extends RuntimeException {
    public ValidationExeption(String message) {
        super(message);
    }
}
