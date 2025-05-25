package at.meks.quarkiverse.axon.server.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.meks.quarkiverse.axon.server.runtime.QuarkusAxonServerConfiguration.GrpcMessageSize;

@ExtendWith(MockitoExtension.class)
class AxonServerConfigurerTest {

    @Mock
    private QuarkusAxonServerConfiguration configuration;

    @InjectMocks
    private AxonServerConfigurer axonServerConfigurer;

    @Mock
    private GrpcMessageSize grpcMessageSize;

    @Nested
    class MaxGrpcMessageSize {

        @Test
        void twoMB() {
            given(grpcMessageSize.value()).willReturn(Optional.of(2));
            given(grpcMessageSize.unit()).willReturn(GrpcMessageSize.Unit.MB);

            Optional<Integer> result = whenMaxMessageSize();

            assertThat(result).hasValue(2 * 1024 * 1024);
        }

        private Optional<Integer> whenMaxMessageSize() {
            given(configuration.maxMessageSize()).willReturn(grpcMessageSize);
            return axonServerConfigurer.maxGrpcMessageSize();
        }

        @Test
        void fiveKB() {
            given(grpcMessageSize.value()).willReturn(Optional.of(5));
            given(grpcMessageSize.unit()).willReturn(GrpcMessageSize.Unit.KB);

            Optional<Integer> result = whenMaxMessageSize();

            assertThat(result).hasValue(5 * 1024);
        }

        @Test
        void _768bytes() {
            given(grpcMessageSize.value()).willReturn(Optional.of(768));
            given(grpcMessageSize.unit()).willReturn(GrpcMessageSize.Unit.Bytes);

            Optional<Integer> result = whenMaxMessageSize();

            assertThat(result).hasValue(768);
        }

    }
}
