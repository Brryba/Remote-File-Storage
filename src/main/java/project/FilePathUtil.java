package project;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;

@Component
public class FilePathUtil {
    public final String BASE_PATH = "D:\\bsuir\\KSIS\\lab5\\storage";

    private boolean fileExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    private String parseFullPath(String relativePath) {
        try {
            return new File(BASE_PATH + File.separator + relativePath).getCanonicalPath();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());//500
        }
    }

    private boolean isPathAllowed(String path) {
        return path.contains(BASE_PATH);
    }

    public String parseAndValidateExistingFilePath(String relativePath) {
        String fullPath = parseAndValidatePath(relativePath);
        if (!fileExists(fullPath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File with this path does not exist");//404
        }
        return fullPath;
    }

    public String parseAndValidatePath(String relativePath) {
        String fullPath = parseFullPath(relativePath);
        if (!isPathAllowed(fullPath)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can not access files outside storage directory");//403
        }
        return fullPath;
    }

    public boolean isDirectory(String path) {
        File file = new File(path);
        return file.isDirectory();
    }
}
