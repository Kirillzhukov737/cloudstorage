package project.cloudstorage.apidocumentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import project.cloudstorage.dto.resource.ResourceResponseDto;

import java.io.IOException;
import java.util.List;

@Tag(name = "Resource", description = "Operation with resources")
public interface ResourcesControllerApiDocumentation {

    @Operation(summary = "Resource information")
    @ApiResponse(responseCode = "200", description = "Resource information")
    @ApiResponse(responseCode = "400", description = "Invalid or missing path")
    @ApiResponse(responseCode = "401", description = "User is not authorized")
    @ApiResponse(responseCode = "404", description = "Resource not found")
    @ApiResponse(responseCode = "500", description = "Unknown error")
    ResponseEntity<ResourceResponseDto> getResource(String path, Authentication authentication);

    @Operation(summary = "Remove resource")
    @ApiResponse(responseCode = "204", description = "No Content")
    @ApiResponse(responseCode = "400", description = "Invalid or missing path")
    @ApiResponse(responseCode = "401", description = "User is not authorized")
    @ApiResponse(responseCode = "404", description = "Resource not found")
    @ApiResponse(responseCode = "500", description = "Unknown error")
    ResponseEntity<Void> removeResource(String path, Authentication authentication);

    @Operation(summary = "Move resource")
    @ApiResponse(responseCode = "200", description = "The resource was moved")
    @ApiResponse(responseCode = "400", description = "Invalid or missing path")
    @ApiResponse(responseCode = "401", description = "User is not authorized")
    @ApiResponse(responseCode = "404", description = "Resource not found")
    @ApiResponse(responseCode = "409", description = "The resource located along the path to [target] already exists.")
    @ApiResponse(responseCode = "500", description = "Unknown error")
    ResponseEntity<ResourceResponseDto> moveResource(String sourcePath, String targetPath, Authentication authentication);

    @Operation(summary = "Search resource")
    @ApiResponse(responseCode = "200", description = "The resource was found")
    @ApiResponse(responseCode = "400", description = "Invalid or missing search query")
    @ApiResponse(responseCode = "401", description = "User is not authorized")
    @ApiResponse(responseCode = "500", description = "Unknown error")
    ResponseEntity<List<ResourceResponseDto>> searchResources(String query, Authentication authentication);

    @Operation(summary = "Upload resource")
    @ApiResponse(responseCode = "201", description = "The resource was upload")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "401", description = "User is not authorized")
    @ApiResponse(responseCode = "409", description = "The file already exists")
    @ApiResponse(responseCode = "500", description = "Unknown error")
    ResponseEntity<List<ResourceResponseDto>> uploadResources(String path, List<MultipartFile> files, Authentication authentication);

    @Operation(summary = "Download resource")
    @ApiResponse(responseCode = "200", description = "The resource was download")
    @ApiResponse(responseCode = "400", description = "Invalid or missing path")
    @ApiResponse(responseCode = "401", description = "User is not authorized")
    @ApiResponse(responseCode = "404", description = "Resource not found")
    @ApiResponse(responseCode = "500", description = "Unknown error")
    void downloadResource(String path, Authentication authentication, HttpServletResponse response) throws IOException;
}