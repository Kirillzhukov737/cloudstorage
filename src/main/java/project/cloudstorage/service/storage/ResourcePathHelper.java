package project.cloudstorage.service.storage;

import org.springframework.stereotype.Component;
import project.cloudstorage.exceptions.storage.InvalidResourcePathException;

@Component
public class ResourcePathHelper {

    private static final String USER_STORAGE_ROOT_TEMPLATE = "user-%d-files/";

    private final ResourceValidator resourceValidator;

    public ResourcePathHelper(ResourceValidator resourceValidator) {
        this.resourceValidator = resourceValidator;
    }

    public String buildStoragePath(Integer userId, String path) {

        resourceValidator.validateUserPath(path);

        return buildUserRoot(userId) + path;
    }

    public String buildUserPath(Integer userId, String storagePath) {

        if (storagePath == null) {
            throw new InvalidResourcePathException("Storage path can not be null");
        }

        String userRoot = buildUserRoot(userId);

        if (!storagePath.startsWith(userRoot)) {
            throw new InvalidResourcePathException("Resource does not belong to the user");
        }

        return storagePath.substring(userRoot.length());
    }

    public String extractParentPath(String path) {

        String pathWithoutSlashAtEnd = removeSlashAtEnd(path);

        int lastSlashIndex = pathWithoutSlashAtEnd.lastIndexOf('/');

        if (lastSlashIndex < 0) {
            return "";
        }

        return pathWithoutSlashAtEnd.substring(0, lastSlashIndex + 1);
    }

    public String extractNameFromPath(String path) {

        String pathWithoutSlashAtEnd = removeSlashAtEnd(path);

        int lastSlashIndex = pathWithoutSlashAtEnd.lastIndexOf('/');

        return pathWithoutSlashAtEnd.substring(lastSlashIndex + 1);
    }

    private String removeSlashAtEnd(String path) {
        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }

        return path;
    }

    private String buildUserRoot(Integer userId) {
        if (userId == null) {
            throw new IllegalStateException("User id can not be null");
        }

        return USER_STORAGE_ROOT_TEMPLATE.formatted(userId);
    }
}