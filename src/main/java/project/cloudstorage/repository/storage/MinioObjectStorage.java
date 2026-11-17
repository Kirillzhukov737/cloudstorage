package project.cloudstorage.repository.storage;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import org.springframework.stereotype.Repository;
import project.cloudstorage.config.MinioProperties;
import project.cloudstorage.exceptions.storage.MinioStorageException;
import project.cloudstorage.exceptions.storage.ResourceNotFoundException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MinioObjectStorage implements ObjectStorage {

    private static final String OBJECT_NOT_FOUND_ERROR = "NoSuchKey";

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioObjectStorage(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.bucketName = minioProperties.bucket();
    }

    @Override
    public StorageResourceInfo getObjectInfo(String objectName) {

        StatObjectResponse objectInfo;

        try {
            StatObjectArgs statObjectArgs = StatObjectArgs.builder().bucket(bucketName).object(objectName).build();

            objectInfo = minioClient.statObject(statObjectArgs);

        } catch (ErrorResponseException ex) {
            String minioErrorCode = ex.errorResponse().code();

            if (OBJECT_NOT_FOUND_ERROR.equals(minioErrorCode)) {
                throw new ResourceNotFoundException("Resource not found");
            }

            throw new MinioStorageException("Can not get object info", ex);
        } catch (MinioException ex) {
            throw new MinioStorageException("Can not get object info", ex);
        }

        boolean directory = objectInfo.object().endsWith("/");

        return new StorageResourceInfo(objectInfo.object(), objectInfo.size(), directory);
    }

    @Override
    public InputStream getObject(String objectName) {

        InputStream objectStream;

        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucketName).object(objectName).build();

            objectStream = minioClient.getObject(getObjectArgs);

        } catch (ErrorResponseException ex) {
            String minioErrorCode = ex.errorResponse().code();

            if (OBJECT_NOT_FOUND_ERROR.equals(minioErrorCode)) {
                throw new ResourceNotFoundException("Resource not found");

            }
            throw new MinioStorageException("Can not get object", ex);

        } catch (MinioException ex) {
            throw new MinioStorageException("Can not get object", ex);
        }
        return objectStream;
    }

    @Override
    public void putObject(String objectName, InputStream inputStream, long objectSize) {
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucketName).object(objectName)
                    .stream(inputStream, objectSize, -1L).build();

            minioClient.putObject(putObjectArgs);

        } catch (MinioException ex) {
            throw new MinioStorageException("Can not put object", ex);
        }
    }

    @Override
    public void removeObject(String objectName) {
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build();

            minioClient.removeObject(removeObjectArgs);

        } catch (MinioException ex) {
            throw new MinioStorageException("Cannot remove object", ex);
        }
    }

    @Override
    public void copyObject(String sourceObjectName, String targetObjectName) {
        try {
            SourceObject sourceObject = SourceObject.builder().bucket(bucketName).object(sourceObjectName).build();

            CopyObjectArgs copyObjectArgs = CopyObjectArgs.builder().bucket(bucketName)
                    .object(targetObjectName).source(sourceObject).build();

            minioClient.copyObject(copyObjectArgs);

        } catch (ErrorResponseException ex) {
            String minioErrorCode = ex.errorResponse().code();

            if (OBJECT_NOT_FOUND_ERROR.equals(minioErrorCode)) {
                throw new ResourceNotFoundException("Resource not found");
            }
            throw new MinioStorageException("Can not copy object", ex);

        } catch (MinioException ex) {
            throw new MinioStorageException("Can not copy object", ex);
        }
    }

    @Override
    public List<StorageResourceInfo> listObjects(String prefix, boolean recursive) {

        List<StorageResourceInfo> objects = new ArrayList<>();

        try {
            ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder().bucket(bucketName)
                    .prefix(prefix).recursive(recursive).build();

            Iterable<Result<Item>> results = minioClient.listObjects(listObjectsArgs);


            for (Result<Item> result : results) {
                Item item = result.get();

                StorageResourceInfo storageResourceInfo = toStorageObject(item);

                objects.add(storageResourceInfo);
            }
        } catch (MinioException ex) {
            throw new MinioStorageException("Can not list objects", ex);
        }

        return objects;
    }

    private StorageResourceInfo toStorageObject(Item item) {

        boolean directory;

        if (item.isDir()) {
            directory = true;
        } else {
            directory = item.objectName().endsWith("/");
        }
        return new StorageResourceInfo(item.objectName(), item.size(), directory);
    }
}