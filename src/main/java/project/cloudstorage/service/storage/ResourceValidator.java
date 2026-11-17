package project.cloudstorage.service.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import project.cloudstorage.exceptions.storage.InvalidResourcePathException;

import java.util.List;

@Component
public class ResourceValidator {

    void validateUserPath(String path) {

        if (path == null || path.startsWith("/") || path.contains("//") || path.contains("\\")) {
            throw new InvalidResourcePathException("Resource path is invalid");
        }

        if (path.isEmpty()) {
            return;
        }

        String pathForValidation = path;

        if (path.endsWith("/")) {
            pathForValidation = path.substring(0, path.length() - 1);
        }

        for (String partPath : pathForValidation.split("/")) {
            if (partPath.isBlank()) {
                throw new InvalidResourcePathException("Path part is blank");
            }

            if (".".equals(partPath) || "..".equals(partPath)) {
                throw new InvalidResourcePathException("Path parts can not be '.' or '..' ");
            }
        }
    }

    void validateFolderPath(String path, boolean canUseRootFolder) {

        if (path == null) {
            throw new InvalidResourcePathException("Folder path is not exist");
        }

        if (path.isEmpty()) {
            if (canUseRootFolder) {
                return;
            }
            throw new InvalidResourcePathException("Folder path is empty");
        }

        if (path.isBlank()) {
            throw new InvalidResourcePathException("Folder path is blank");
        }

        validateUserPath(path);

        if (!path.endsWith("/")) {
            throw new InvalidResourcePathException("Folder path must end with /");
        }
    }

    void validateResourcePath(String path) {
        if (path == null || path.isBlank()) {
            throw new InvalidResourcePathException("Resource path is empty");
        }

        validateUserPath(path);
    }

    void validateOriginalFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new InvalidResourcePathException("Original file name is missing");
        }

        validateUserPath(originalFileName);
    }

    void validateQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new InvalidResourcePathException("Query is blank");
        }
    }

    void validateUploadRequest(String path, List<MultipartFile> files) {

        validateFolderPath(path, true);

        if (files == null || files.isEmpty()) {
            throw new InvalidResourcePathException("Upload files are empty");
        }
    }
}