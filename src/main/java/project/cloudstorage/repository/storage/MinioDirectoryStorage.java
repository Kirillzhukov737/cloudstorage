package project.cloudstorage.repository.storage;

import org.springframework.stereotype.Repository;
import project.cloudstorage.exceptions.storage.DirectoryCopyException;
import project.cloudstorage.exceptions.storage.InvalidResourcePathException;
import project.cloudstorage.exceptions.storage.ResourceAlreadyExistsException;
import project.cloudstorage.exceptions.storage.ResourceNotFoundException;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MinioDirectoryStorage implements DirectoryStorage {

    private final ObjectStorage objectStorage;

    public MinioDirectoryStorage(ObjectStorage objectStorage) {
        this.objectStorage = objectStorage;
    }

    @Override
    public StorageResourceInfo getDirectoryInfo(String storagePath) {

        validateDirectoryPath(storagePath);

        if (!existsDirectory(storagePath)) {
            throw new ResourceNotFoundException("Directory not found");
        }

        return buildDirectoryInfo(storagePath);
    }

    @Override
    public StorageResourceInfo createDirectory(String storagePath) {

        validateDirectoryPath(storagePath);

        if (existsDirectory(storagePath)) {
            throw new ResourceAlreadyExistsException("Directory already exists");
        }

        return createEmptyDirectoryObject(storagePath);
    }

    @Override
    public void removeDirectory(String storagePath) {

        validateDirectoryPath(storagePath);
        getDirectoryInfo(storagePath);

        removeDirectoryObjects(storagePath);
    }

    @Override
    public StorageResourceInfo moveDirectory(String sourceStoragePath, String targetStoragePath) {

        validateDirectoryPath(sourceStoragePath);
        validateDirectoryPath(targetStoragePath);

        getDirectoryInfo(sourceStoragePath);

        if (targetStoragePath.startsWith(sourceStoragePath)) {
            throw new InvalidResourcePathException("Directory can't be moved inside itself");
        }

        if (existsDirectory(targetStoragePath)) {
            throw new ResourceAlreadyExistsException("Directory already exists");
        }

        List<StorageResourceInfo> sourceObjects = objectStorage.listObjects(sourceStoragePath, true);

        createEmptyDirectoryObject(targetStoragePath);

        try {
            copyDirectoryObjects(sourceObjects, sourceStoragePath, targetStoragePath);

        } catch (RuntimeException ex) {

            DirectoryCopyException directoryCopyException = new DirectoryCopyException("Cannot copy directory", ex);

            try {
                removeDirectory(targetStoragePath);

            } catch (RuntimeException rollbackException) {
                directoryCopyException.addSuppressed(rollbackException);
            }
            throw directoryCopyException;
        }

        removeDirectory(sourceStoragePath);

        return buildDirectoryInfo(targetStoragePath);
    }

    @Override
    public List<StorageResourceInfo> listDirectory(String storagePath, boolean recursive) {

        validateDirectoryPath(storagePath);

        List<StorageResourceInfo> objects = objectStorage.listObjects(storagePath, recursive);
        List<StorageResourceInfo> directoryContent = new ArrayList<>();

        for (StorageResourceInfo object : objects) {
            if (!storagePath.equals(object.storagePath())) {
                directoryContent.add(object);
            }
        }

        return List.copyOf(directoryContent);
    }

    private boolean existsDirectory(String storagePath) {

        boolean directoryExists;

        try {
            StorageResourceInfo object = objectStorage.getObjectInfo(storagePath);

            directoryExists = object.directory();

        } catch (ResourceNotFoundException ex) {
            List<StorageResourceInfo> directoryObjects = objectStorage.listObjects(storagePath, false);

            directoryExists = !directoryObjects.isEmpty();
        }
        return directoryExists;
    }

    private StorageResourceInfo buildDirectoryInfo(String storagePath) {
        return new StorageResourceInfo(storagePath, 0L, true);
    }

    private void validateDirectoryPath(String storagePath) {

        if (storagePath == null || storagePath.isBlank() || !storagePath.endsWith("/")) {
            throw new InvalidResourcePathException("Directory path is invalid");
        }
    }

    private StorageResourceInfo createEmptyDirectoryObject(String storagePath) {

        byte[] emptyContent = new byte[0];

        objectStorage.putObject(storagePath, new ByteArrayInputStream(emptyContent), emptyContent.length);

        return buildDirectoryInfo(storagePath);
    }

    private void copyDirectoryObjects(List<StorageResourceInfo> sourceObjects,
                                      String sourceStoragePath, String targetStoragePath) {

        for (StorageResourceInfo sourceObject : sourceObjects) {
            String sourceObjectPath = sourceObject.storagePath();

            if (!sourceObjectPath.equals(sourceStoragePath)) {

                String pathInsideSourceDirectory = sourceObjectPath.substring(sourceStoragePath.length());

                String targetObjectPath = targetStoragePath + pathInsideSourceDirectory;

                objectStorage.copyObject(sourceObjectPath, targetObjectPath);
            }
        }
    }

    private void removeDirectoryObjects(String storagePath) {

        List<StorageResourceInfo> objects = objectStorage.listObjects(storagePath, true);

        for (StorageResourceInfo object : objects) {
            String objectPath = object.storagePath();

            if (!objectPath.equals(storagePath)) {
                objectStorage.removeObject(objectPath);
            }
        }
        objectStorage.removeObject(storagePath);
    }
}