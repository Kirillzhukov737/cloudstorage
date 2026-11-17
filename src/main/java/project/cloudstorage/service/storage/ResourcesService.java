package project.cloudstorage.service.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.cloudstorage.dto.resource.ResourceResponseDto;
import project.cloudstorage.exceptions.storage.InvalidResourcePathException;
import project.cloudstorage.exceptions.storage.MinioStorageException;
import project.cloudstorage.exceptions.storage.ResourceAlreadyExistsException;
import project.cloudstorage.exceptions.storage.ResourceNotFoundException;
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
import java.util.Locale;

@Service
public class ResourcesService {

    private final UserService userService;
    private final ObjectStorage objectStorage;
    private final DirectoryStorage directoryStorage;
    private final ResourceMapper resourceMapper;
    private final FolderService folderService;
    private final ResourceValidator resourceValidator;
    private final ResourcePathHelper resourcePathHelper;

    public ResourcesService(UserService userService, ObjectStorage objectStorage, DirectoryStorage directoryStorage,
                            ResourceMapper resourceMapper, FolderService folderService,
                            ResourceValidator resourceValidator, ResourcePathHelper resourcePathHelper) {

        this.userService = userService;
        this.objectStorage = objectStorage;
        this.directoryStorage = directoryStorage;
        this.resourceMapper = resourceMapper;
        this.folderService = folderService;
        this.resourceValidator = resourceValidator;
        this.resourcePathHelper = resourcePathHelper;
    }

    public ResourceResponseDto getResources(String username, String path) {

        resourceValidator.validateResourcePath(path);

        Integer userId = userService.getUserIdByUsername(username);
        String storagePath = resourcePathHelper.buildStoragePath(userId, path);

        StorageResourceInfo resourceInfo;

        if (path.endsWith("/")) {
            resourceInfo = directoryStorage.getDirectoryInfo(storagePath);
        } else {
            resourceInfo = objectStorage.getObjectInfo(storagePath);
        }

        return resourceMapper.toDto(userId, resourceInfo);
    }

    public void removeResource(String username, String path) {

        resourceValidator.validateResourcePath(path);

        if (path.endsWith("/")) {
            folderService.removeFolder(username, path);
            return;
        }

        Integer userId = userService.getUserIdByUsername(username);
        String storagePath = resourcePathHelper.buildStoragePath(userId, path);

        objectStorage.getObjectInfo(storagePath);
        objectStorage.removeObject(storagePath);
    }

    public ResourceResponseDto moveResources(String username, String sourcePath, String targetPath) {

        resourceValidator.validateResourcePath(sourcePath);
        resourceValidator.validateResourcePath(targetPath);

        boolean sourceIsDirectory = sourcePath.endsWith("/");
        boolean targetIsDirectory = targetPath.endsWith("/");

        if (sourceIsDirectory != targetIsDirectory) {
            throw new InvalidResourcePathException("Source and target paths are not compatible");
        }

        if (sourceIsDirectory) {
            return folderService.moveFolder(username, sourcePath, targetPath);
        }

        Integer userId = userService.getUserIdByUsername(username);

        validateParentDirectoryExists(userId, targetPath);

        String sourceStoragePath = resourcePathHelper.buildStoragePath(userId, sourcePath);

        String targetStoragePath = resourcePathHelper.buildStoragePath(userId, targetPath);

        objectStorage.getObjectInfo(sourceStoragePath);

        if (objectExists(targetStoragePath)) {
            throw new ResourceAlreadyExistsException("Resource already exists " + targetPath);
        }

        objectStorage.copyObject(sourceStoragePath, targetStoragePath);
        objectStorage.removeObject(sourceStoragePath);

        StorageResourceInfo movedObject = objectStorage.getObjectInfo(targetStoragePath);

        return resourceMapper.toDto(userId, movedObject);
    }

    public List<ResourceResponseDto> searchResources(String username, String query) {

        resourceValidator.validateQuery(query);

        Integer userId = userService.getUserIdByUsername(username);
        String userStorageRoot = resourcePathHelper.buildStoragePath(userId, "");

        List<StorageResourceInfo> objects = directoryStorage.listDirectory(userStorageRoot, true);

        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        List<ResourceResponseDto> foundResources = new ArrayList<>();

        for (StorageResourceInfo object : objects) {
            ResourceResponseDto resource = resourceMapper.toDto(userId, object);
            String normalizedResourceName = resource.name().toLowerCase(Locale.ROOT);

            if (normalizedResourceName.contains(normalizedQuery)) {
                foundResources.add(resource);
            }
        }

        return foundResources;
    }

    public List<ResourceResponseDto> uploadResources(String username, String directoryPath, List<MultipartFile> files) {

        resourceValidator.validateUploadRequest(directoryPath, files);

        Integer userId = userService.getUserIdByUsername(username);

        validateUploadDirectoryExists(userId, directoryPath);

        List<ResourceResponseDto> uploadedResources = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalFileName = file.getOriginalFilename();

            resourceValidator.validateOriginalFileName(originalFileName);

            String filePath = directoryPath + originalFileName;
            String fileStoragePath =
                    resourcePathHelper.buildStoragePath(userId, filePath);

            if (objectExists(fileStoragePath)) {
                throw new ResourceAlreadyExistsException("Resource already exists " + filePath);
            }

            uploadFile(file, fileStoragePath);

            StorageResourceInfo uploadedObject = objectStorage.getObjectInfo(fileStoragePath);

            ResourceResponseDto uploadedResource = resourceMapper.toDto(userId, uploadedObject);

            uploadedResources.add(uploadedResource);
        }

        return uploadedResources;
    }

    public void downloadResource(String username, String path, OutputStream outputStream) {

        resourceValidator.validateResourcePath(path);

        if (path.endsWith("/")) {
            folderService.downloadFolder(username, path, outputStream);
            return;
        }

        Integer userId = userService.getUserIdByUsername(username);
        String storagePath = resourcePathHelper.buildStoragePath(userId, path);

        objectStorage.getObjectInfo(storagePath);

        try (InputStream inputStream = objectStorage.getObject(storagePath)) {
            inputStream.transferTo(outputStream);

        } catch (IOException ex) {
            throw new MinioStorageException("Can not download resource " + path, ex);
        }
    }

    private boolean objectExists(String storagePath) {
        try {
            objectStorage.getObjectInfo(storagePath);
            return true;

        } catch (ResourceNotFoundException ex) {
            return false;
        }
    }

    private void uploadFile(MultipartFile file, String storagePath) {

        try (InputStream inputStream = file.getInputStream()) {
            objectStorage.putObject(storagePath, inputStream, file.getSize());

        } catch (IOException ex) {
            throw new MinioStorageException("Can not read uploaded file " + storagePath, ex);
        }
    }

    private void validateParentDirectoryExists(Integer userId, String path) {

        String parentPath = resourcePathHelper.extractParentPath(path);

        if (parentPath.isEmpty()) {
            return;
        }

        String parentStoragePath = resourcePathHelper.buildStoragePath(userId, parentPath);

        directoryStorage.getDirectoryInfo(parentStoragePath);
    }

    private void validateUploadDirectoryExists(Integer userId, String directoryPath) {

        if (directoryPath.isEmpty()) {
            return;
        }

        String directoryStoragePath = resourcePathHelper.buildStoragePath(userId, directoryPath);

        directoryStorage.getDirectoryInfo(directoryStoragePath);
    }
}