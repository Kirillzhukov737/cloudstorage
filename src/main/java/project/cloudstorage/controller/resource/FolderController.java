package project.cloudstorage.controller.resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.cloudstorage.apidocumentation.FolderControllerApiDocumentation;
import project.cloudstorage.dto.resource.ResourceResponseDto;
import project.cloudstorage.service.storage.FolderService;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
public class FolderController implements FolderControllerApiDocumentation {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ResourceResponseDto>> getFolderContent(@RequestParam("path") String path,
                                                                      Authentication authentication) {
        List<ResourceResponseDto> content = folderService.getFolderContent(authentication.getName(), path);

        return ResponseEntity.status(HttpStatus.OK).body(content);
    }

    @Override
    @PostMapping
    public ResponseEntity<ResourceResponseDto> createFolder(@RequestParam("path") String path,
                                                            Authentication authentication) {
        ResourceResponseDto createdFolder = folderService.createFolder(authentication.getName(), path);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdFolder);
    }
}