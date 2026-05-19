// Test Template - Unit Test with Mockito (Java 8)

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SampleServiceTest {

    @Mock
    private SampleRepository sampleRepository;

    @Mock
    private SampleMapper sampleMapper;

    @InjectMocks
    private SampleService sampleService;

    private SampleEntity sampleEntity;
    private SampleResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleEntity = new SampleEntity();
        sampleEntity.setId("test-id");
        sampleEntity.setName("Test Sample");

        sampleResponse = new SampleResponse("test-id", "Test Sample", null);
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return sample when exists")
        void shouldReturnSampleWhenExists() {
            when(sampleRepository.findById("test-id")).thenReturn(Optional.of(sampleEntity));
            when(sampleMapper.toResponse(sampleEntity)).thenReturn(sampleResponse);

            SampleResponse result = sampleService.findById("test-id");

            assertThat(result).isEqualTo(sampleResponse);
            verify(sampleRepository).findById("test-id");
        }

        @Test
        @DisplayName("should throw exception when not found")
        void shouldThrowExceptionWhenNotFound() {
            when(sampleRepository.findById("non-existent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sampleService.findById("non-existent"))
                .isInstanceOf(SampleNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create sample successfully")
        void shouldCreateSampleSuccessfully() {
            CreateSampleRequest request = new CreateSampleRequest();
            request.setName("New Sample");
            
            SampleEntity newEntity = new SampleEntity();
            newEntity.setName("New Sample");

            when(sampleMapper.toEntity(request)).thenReturn(newEntity);
            when(sampleRepository.save(newEntity)).thenReturn(newEntity);
            when(sampleMapper.toResponse(newEntity)).thenReturn(
                new SampleResponse("new-id", "New Sample", null)
            );

            SampleResponse result = sampleService.create(request);

            assertThat(result.getName()).isEqualTo("New Sample");
            verify(sampleRepository).save(newEntity);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete sample when exists")
        void shouldDeleteSampleWhenExists() {
            when(sampleRepository.existsById("test-id")).thenReturn(true);

            sampleService.delete("test-id");

            verify(sampleRepository).deleteById("test-id");
        }

        @Test
        @DisplayName("should throw exception when not found")
        void shouldThrowExceptionWhenNotFound() {
            when(sampleRepository.existsById("non-existent")).thenReturn(false);

            assertThatThrownBy(() -> sampleService.delete("non-existent"))
                .isInstanceOf(SampleNotFoundException.class);

            verify(sampleRepository, never()).deleteById(any());
        }
    }
}
