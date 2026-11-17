package project.cloudstorage.exceptions.storage;

public class MinioStorageException extends RuntimeException {
    public MinioStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
