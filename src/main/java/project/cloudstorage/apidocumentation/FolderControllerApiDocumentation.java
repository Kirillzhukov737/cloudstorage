package project.cloudstorage.apidocumentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import project.cloudstorage.dto.resource.ResourceResponseDto;

import java.util.List;

@Tag(name = "Folder", description = "Folder content and creation")
public interface FolderControllerApiDocumentation {

    @Operation(summary = "List folder content")
    @ApiResponse(responseCode = "200", description = "List of folder content")
    @ApiResponse(responseCode = "400", description = "Invalid or missing path")
    @ApiResponse(responseCode = "401", description = "User is not authorized")
    @ApiResponse(responseCode = "404", description = "The folder does not exist")
    @ApiResponse(responseCode = "500", description = "Unknown error")
    ResponseEntity<List<ResourceResponseDto>> getFolderContent( String path, Authentication authentication);

    @Operation(summary = "Create folder")
    @ApiResponse(responseCode = "200", description = "List of folder content")
    @ApiResponse(responseCode = "400", description = "Invalid or missing path to the new folder")
    @ApiResponse(responseCode = "401", description = "User is not authorized")
    @ApiResponse(responseCode = "404", description = "The parent folder does not exist")
    @ApiResponse(responseCode = "409", description = "The folder already exists")
    @ApiResponse(responseCode = "500", description = "Unknown error")
    ResponseEntity<ResourceResponseDto> createFolder(String path, Authentication authentication);
}
