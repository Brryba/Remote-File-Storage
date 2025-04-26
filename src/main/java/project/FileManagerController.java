package project;

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

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleException(ResponseStatusException ex) {
        Map<String, String> body = Map.of(
                "Error code", ex.getStatusCode().toString(),
                "Message", ex.getReason() != null ? ex.getReason() : "Bad request");
        return new ResponseEntity<>(body, ex.getStatusCode());
    }
}
