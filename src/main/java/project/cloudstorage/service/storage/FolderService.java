package project.cloudstorage.service.storage;

import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import project.cloudstorage.dto.resource.ResourceResponseDto;
import project.cloudstorage.exceptions.storage.MinioStorageException;
import project.cloudstorage.mapper.ResourceMapper;
import project.cloudstorage.repository.storage.DirectoryStorage;
import project.cloudstorage.repository.storage.ObjectStorage;
import project.cloudstorage.repository.storage.StorageResourceInfo;
import project.cloudstorage.service.user.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FolderService {

    private final UserService userService;
    private final DirectoryStorage directoryStorage;
    private final ObjectStorage objectStorage;
    private final ResourceMapper resourceMapper;
    private final ResourceValidator resourceValidator;
    private final ResourcePathHelper resourcePathHelper;

    public FolderService(UserService userService, DirectoryStorage directoryStorage, ObjectStorage objectStorage,
                         ResourceMapper resourceMapper, ResourceValidator resourceValidator,
                         ResourcePathHelper resourcePathHelper) {
        this.userService = userService;
        this.directoryStorage = directoryStorage;
        this.objectStorage = objectStorage;
        this.resourceMapper = resourceMapper;
        this.resourceValidator = resourceValidator;
        this.resourcePathHelper = resourcePathHelper;
    }

    public List<ResourceResponseDto> getFolderContent(String username, String path) {

        resourceValidator.validateFolderPath(path, true);

        Integer userId = userService.getUserIdByUsername(username);
        String storagePath = resourcePathHelper.buildStoragePath(userId, path);

        if (!path.isEmpty()) {
            directoryStorage.getDirectoryInfo(storagePath);
        }

        List<StorageResourceInfo> resources = directoryStorage.listDirectory(storagePath, false);
        List<ResourceResponseDto> folderContent = new ArrayList<>();

        for (StorageResourceInfo resource : resources) {
            ResourceResponseDto resourceDto = resourceMapper.toDto(userId, resource);
            folderContent.add(resourceDto);
        }

        return List.copyOf(folderContent);
    }

    public ResourceResponseDto createFolder(String username, String path) {

        resourceValidator.validateFolderPath(path, false);

        Integer userId = userService.getUserIdByUsername(username);

        validateParentFolderExist(userId, path);

        String storagePath = resourcePathHelper.buildStoragePath(userId, path);
        StorageResourceInfo createdDirectory = directoryStorage.createDirectory(storagePath);

        return resourceMapper.toDto(userId, createdDirectory);
    }

    public void removeFolder(String username, String path) {

        resourceValidator.validateFolderPath(path, false);

        Integer userId = userService.getUserIdByUsername(username);
        String storagePath = resourcePathHelper.buildStoragePath(userId, path);

        directoryStorage.removeDirectory(storagePath);
    }

    public ResourceResponseDto moveFolder(String username, String sourcePath, String targetPath) {

        resourceValidator.validateFolderPath(sourcePath, false);
        resourceValidator.validateFolderPath(targetPath, false);

        Integer userId = userService.getUserIdByUsername(username);

        validateParentFolderExist(userId, targetPath);

        String sourceStoragePath = resourcePathHelper.buildStoragePath(userId, sourcePath);

        String targetStoragePath = resourcePathHelper.buildStoragePath(userId, targetPath);

        StorageResourceInfo movedDirectory = directoryStorage.moveDirectory(sourceStoragePath, targetStoragePath);

        return resourceMapper.toDto(userId, movedDirectory);
    }

    public void downloadFolder(String username, String path, OutputStream outputStream) {

        resourceValidator.validateFolderPath(path, false);

        Integer userId = userService.getUserIdByUsername(username);
        String storagePath = resourcePathHelper.buildStoragePath(userId, path);

        directoryStorage.getDirectoryInfo(storagePath);

        List<StorageResourceInfo> objects = directoryStorage.listDirectory(storagePath, true);

        String folderName = resourcePathHelper.extractNameFromPath(path);

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(StreamUtils.nonClosing(outputStream))) {
            ZipEntry rootDirectory = new ZipEntry(folderName + "/");

            zipOutputStream.putNextEntry(rootDirectory);
            zipOutputStream.closeEntry();

            for (StorageResourceInfo object : objects) {
                String relativePath = object.storagePath().substring(storagePath.length());

                String zipPath = folderName + "/" + relativePath;
                ZipEntry zipEntry = new ZipEntry(zipPath);

                zipOutputStream.putNextEntry(zipEntry);

                if (!object.directory()) {
                    try (InputStream inputStream = objectStorage.getObject(object.storagePath())) {
                        inputStream.transferTo(zipOutputStream);
                    }
                }

                zipOutputStream.closeEntry();
            }
        } catch (IOException ex) {
            throw new MinioStorageException("Can not create ZIP file " + path, ex);
        }
    }

    private void validateParentFolderExist(Integer userId, String path) {

        String parentPath = resourcePathHelper.extractParentPath(path);

        if (parentPath.isEmpty()) {
            return;
        }

        String parentStoragePath = resourcePathHelper.buildStoragePath(userId, parentPath);

        directoryStorage.getDirectoryInfo(parentStoragePath);
    }
}