package project;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileWriter;
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

    public void writeFileContent(String path, String content) {
        String fullPath = filePathUtil.parseAndValidatePath(path);
        File file = new File(fullPath);
        if (file.exists() && file.isDirectory()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Can not overwrite the existing directory");//409
        }
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());//500
        }
    }

    public void createDirectory(String path) {
        String fullPath = filePathUtil.parseAndValidatePath(path);
        File file = new File(fullPath);
        if (file.exists()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Directory already exists");//409
        }
        file.mkdirs();
    }

    public void copyFile(String sourcePath, String destinationPath, boolean deleteSource) {
        sourcePath = filePathUtil.parseAndValidateExistingFilePath(sourcePath);
        destinationPath = filePathUtil.parseAndValidatePath(destinationPath);

        File destinationFile = new File(destinationPath);
        File sourceFile = new File(sourcePath);

        try {
            if (sourceFile.isFile()) {
                if (destinationFile.exists() && destinationFile.isDirectory()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Can not overwrite the existing directory");
                }
                FileUtils.copyFile(new File(sourcePath), destinationFile);
                if (deleteSource) {
                    FileUtils.delete(sourceFile);
                }
            } else {
                if (destinationFile.exists() && destinationFile.isFile()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Can not overwrite the existing file with a directory, delete it first");
                }
                FileUtils.copyDirectory(sourceFile, destinationFile);
                if (deleteSource) {
                    FileUtils.deleteDirectory(sourceFile);
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());//500
        }
    }

    public void deleteFile(String path) {
        try {
            String fullPath = filePathUtil.parseAndValidateExistingFilePath(path);
            File file = new File(fullPath);
            if (file.isFile()) {
                file.delete();
            } else {
                FileUtils.deleteDirectory(file);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());//500
        }
    }
}
