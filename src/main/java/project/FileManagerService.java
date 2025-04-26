package project;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FileManagerService {
    private final FilePathUtil filePathUtil;

    public FileManagerService(FilePathUtil filePathUtil) {
        this.filePathUtil = filePathUtil;
    }

    private Map<String, Object> getSingleFileContent(String fullPath) {
        StringBuilder fileContent = new StringBuilder();
        File file = new File(fullPath);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                fileContent.append(scanner.nextLine()).append("\n");
            }
            if (!fileContent.toString().isEmpty()) {
                fileContent.deleteCharAt(fileContent.length() - 1);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return Map.of("File content:", fileContent.toString(),
                "Type", "File");
    }

    private Map<String, Object> getDirectoryContent(String fullPath) {
        File file = new File(fullPath);
        List<String> fileNames = Arrays.stream(file.listFiles())
                .map(File::getName)
                .filter(curFile -> !curFile.equals("."))
                .toList();
        return Map.of("Files", fileNames,
                "Type", "Directory");
    }

    public Map<String, Object> getFileContent(String path) {
        String fullPath = filePathUtil.parseAndValidateExistingFilePath(path);
        if (filePathUtil.isDirectory(fullPath)) {
            return getDirectoryContent(fullPath);
        }
        return getSingleFileContent(fullPath);
    }

    public Map<String, Object> getFileMetaInfo(String path) {
        String fullPath = filePathUtil.parseAndValidateExistingFilePath(path);
        File file = new File(fullPath);
        Date lastModifiedTime = new Date(file.lastModified());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long fileSize = file.isFile() ? file.length() : FileUtils.sizeOfDirectory(file);

        return Map.of("File size", fileSize,
                "Last modified", simpleDateFormat.format(lastModifiedTime));
    }
}
