package self.exercise.jobapplication.ledger.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpAddressResolverTests {
    private final ClientIpAddressResolver resolver = new ClientIpAddressResolver();

    @Test
    void resolvesRemoteAddressFromServletRequest() {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.20");

        assertThat(resolver.resolve(request)).isEqualTo("192.0.2.20");
    }
}
