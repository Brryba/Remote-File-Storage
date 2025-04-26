package project;

import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class FileManagerController {
    private final FileManagerService service;

    public FileManagerController(FileManagerService service) {
        this.service = service;
    }

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)//200
    public Map<String, Object> getContent(@RequestParam(name="path") String path) {
        return service.getFileContent(path);
    }

    @RequestMapping(method = RequestMethod.HEAD)
    public ResponseEntity<Void> getFileMetaInfo(@RequestParam(name="path") String path) {
        Map<String, Object> metaInfo = service.getFileMetaInfo(path);
        HttpHeaders headers = new HttpHeaders();
        for (String key : metaInfo.keySet()) {
            headers.add(key, metaInfo.get(key).toString());
        }
        return ResponseEntity.ok().headers(headers).body(null);//200
    }

    @PutMapping("/{*path}")
    @ResponseStatus(HttpStatus.CREATED)//201
    public void putFile(@PathVariable String path, @RequestBody @NonNull Map<String, String> body) {
        if (!body.containsKey("content")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing content in request body");//400
        }
        String fileContent = body.get("content");
        service.writeFileContent(path, fileContent);
    }

    @PutMapping("/dir/{*path}")
    @ResponseStatus(HttpStatus.CREATED)//201
    public void createNewDirectory(@PathVariable String path) {
        service.createDirectory(path);
    }

    @PutMapping("/copy/{*destinationFile}")
    @ResponseStatus(HttpStatus.CREATED)
    public void copyFile(@PathVariable String destinationFile, @RequestHeader("From") @NonNull String sourceFile,
                         @RequestHeader(value = "Delete-Source", required = false, defaultValue = "false") boolean deleteSource) {
        service.copyFile(sourceFile, destinationFile, deleteSource);
    }

    @DeleteMapping("{*path}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFile(@PathVariable String path) {
        service.deleteFile(path);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleException(ResponseStatusException ex) {
        Map<String, String> body = Map.of(
                "Error code", ex.getStatusCode().toString(),
                "Message", ex.getReason() != null ? ex.getReason() : "Bad request");
        return new ResponseEntity<>(body, ex.getStatusCode());
    }
}
