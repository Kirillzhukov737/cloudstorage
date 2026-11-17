package project.cloudstorage.repository.storage;

import java.io.InputStream;
import java.util.List;

public interface ObjectStorage {

    StorageResourceInfo getObjectInfo(String storagePath);

    InputStream getObject(String storagePath);

    void putObject(String storagePath, InputStream inputStream, long objectSize);

    void removeObject(String storagePath);

    void copyObject(String sourceStoragePath, String targetStoragePath);

    List<StorageResourceInfo> listObjects(String prefix, boolean recursive);
}