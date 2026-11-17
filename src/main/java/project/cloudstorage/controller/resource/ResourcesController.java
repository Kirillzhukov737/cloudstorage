package project.cloudstorage.controller.resource;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import project.cloudstorage.apidocumentation.ResourcesControllerApiDocumentation;
import project.cloudstorage.dto.resource.ResourceResponseDto;
import project.cloudstorage.service.storage.ResourcesService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
public class ResourcesController implements ResourcesControllerApiDocumentation {

    private final ResourcesService resourcesService;

    public ResourcesController(ResourcesService resourcesService) {
        this.resourcesService = resourcesService;
    }

    @Override
    @GetMapping
    public ResponseEntity<ResourceResponseDto> getResource(@RequestParam("path") String path,
                                                           Authentication authentication) {
        ResourceResponseDto resource = resourcesService.getResources(authentication.getName(), path);

        return ResponseEntity.status(HttpStatus.OK).body(resource);
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> removeResource(@RequestParam("path") String path, Authentication authentication) {
        resourcesService.removeResource(authentication.getName(), path);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PostMapping("/move")
    public ResponseEntity<ResourceResponseDto> moveResource(@RequestParam("from") String sourcePath,
                                                            @RequestParam("to") String targetPath,
                                                            Authentication authentication) {

        ResourceResponseDto movedResource = resourcesService.moveResources(authentication.getName(), sourcePath, targetPath);

        return ResponseEntity.status(HttpStatus.OK).body(movedResource);
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<List<ResourceResponseDto>> searchResources(@RequestParam("query") String query,
                                                                     Authentication authentication) {

        List<ResourceResponseDto> resources = resourcesService.searchResources(authentication.getName(), query);

        return ResponseEntity.status(HttpStatus.OK).body(resources);
    }

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ResourceResponseDto>> uploadResources(@RequestParam("path") String path,
                                                                     @RequestPart("object") List<MultipartFile> files,
                                                                     Authentication authentication) {
        List<ResourceResponseDto> uploadedResources = resourcesService.uploadResources(authentication.getName(), path, files);

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedResources);
    }

    @Override
    @GetMapping("/download")
    public void downloadResource(@RequestParam("path") String path, Authentication authentication,
                                 HttpServletResponse response) throws IOException {

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        resourcesService.downloadResource(authentication.getName(), path, response.getOutputStream());
    }
}