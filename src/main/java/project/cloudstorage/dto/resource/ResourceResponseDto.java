package project.cloudstorage.dto.resource;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceResponseDto(String path, String name, Long size, ResourceType type) {
}