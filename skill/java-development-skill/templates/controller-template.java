// Controller Template - REST API controller

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/samples")
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;

    @GetMapping
    public ResponseEntity<List<SampleResponse>> getAll() {
        return ResponseEntity.ok(sampleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SampleResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(sampleService.findById(id));
    }

    @PostMapping
    public ResponseEntity<SampleResponse> create(
            @Valid @RequestBody CreateSampleRequest request) {
        SampleResponse created = sampleService.create(request);
        return ResponseEntity
            .created(URI.create("/api/samples/" + created.id()))
            .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SampleResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateSampleRequest request) {
        return ResponseEntity.ok(sampleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        sampleService.delete(id);
    }
}
