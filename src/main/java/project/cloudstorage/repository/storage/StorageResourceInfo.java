package project.cloudstorage.repository.storage;

public record StorageResourceInfo(String storagePath, long size, boolean directory) {
}