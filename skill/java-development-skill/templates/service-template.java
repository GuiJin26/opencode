// Service Template - Business logic layer

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SampleService {

    private final SampleRepository sampleRepository;
    private final SampleMapper sampleMapper;

    @Transactional(readOnly = true)
    public SampleResponse findById(String id) {
        return sampleRepository.findById(id)
            .map(sampleMapper::toResponse)
            .orElseThrow(() -> new SampleNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<SampleResponse> findAll() {
        return sampleRepository.findAll()
            .stream()
            .map(sampleMapper::toResponse)
            .toList();
    }

    @Transactional
    public SampleResponse create(CreateSampleRequest request) {
        validateBusinessRules(request);
        Sample sample = sampleMapper.toEntity(request);
        Sample saved = sampleRepository.save(sample);
        return sampleMapper.toResponse(saved);
    }

    @Transactional
    public SampleResponse update(String id, UpdateSampleRequest request) {
        Sample sample = sampleRepository.findById(id)
            .orElseThrow(() -> new SampleNotFoundException(id));
        updateFields(sample, request);
        return sampleMapper.toResponse(sampleRepository.save(sample));
    }

    @Transactional
    public void delete(String id) {
        if (!sampleRepository.existsById(id)) {
            throw new SampleNotFoundException(id);
        }
        sampleRepository.deleteById(id);
    }

    private void validateBusinessRules(CreateSampleRequest request) {
        // Add business validation logic
    }

    private void updateFields(Sample sample, UpdateSampleRequest request) {
        // Update only non-null fields
        if (request.name() != null) {
            sample.setName(request.name());
        }
    }
}
