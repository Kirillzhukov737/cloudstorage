package project.cloudstorage.mapper;

import org.springframework.stereotype.Component;
import project.cloudstorage.dto.resource.ResourceResponseDto;
import project.cloudstorage.dto.resource.ResourceType;
import project.cloudstorage.repository.storage.StorageResourceInfo;
import project.cloudstorage.service.storage.ResourcePathHelper;

@Component
public class ResourceMapper {

    private final ResourcePathHelper resourcePathHelper;

    public ResourceMapper(ResourcePathHelper resourcePathHelper) {
        this.resourcePathHelper = resourcePathHelper;
    }

    public ResourceResponseDto toDto(Integer userId, StorageResourceInfo resourceInfo) {

        String publicPath = resourcePathHelper.buildUserPath(userId, resourceInfo.storagePath());

        String parentPath = resourcePathHelper.extractParentPath(publicPath);
        String resourceName = resourcePathHelper.extractNameFromPath(publicPath);

        ResourceType resourceType;
        Long size;

        if (resourceInfo.directory()) {
            resourceType = ResourceType.DIRECTORY;
            size = null;

        } else {
            resourceType = ResourceType.FILE;
            size = resourceInfo.size();
        }

        return new ResourceResponseDto(parentPath, resourceName, size, resourceType);
    }
}