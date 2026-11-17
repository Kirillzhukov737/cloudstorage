package project.cloudstorage.repository.storage;

import java.util.List;

public interface DirectoryStorage {

    StorageResourceInfo getDirectoryInfo(String storagePath);

    StorageResourceInfo createDirectory(String storagePath);

    void removeDirectory(String storagePath);

    StorageResourceInfo moveDirectory(String sourceStoragePath, String targetStoragePath);

    List<StorageResourceInfo> listDirectory(String storagePath, boolean recursive);
}