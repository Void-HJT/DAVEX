package DavexBase.service.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.URI;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 验证节点通信的超时、不可达和 HTTP 错误具有统一异常语义。 */
class NodeCommunicationFilterTest {

    @Test
    void mapsTimeoutFailure() {
        NodeCommunicationException error = invokeWithFailure(
                new TimeoutException("timed out"));

        assertEquals(NodeCommunicationException.Reason.TIMEOUT,
                error.getReason());
        assertEquals("AGENT-1", error.getNodeId());
        assertEquals("GET /MpcTasks/ready", error.getOperation());
    }

    @Test
    void mapsUnreachableNodeFailure() {
        NodeCommunicationException error = invokeWithFailure(
                new ConnectException("connection refused"));

        assertEquals(NodeCommunicationException.Reason.UNREACHABLE,
                error.getReason());
        assertEquals("AGENT-1", error.getNodeId());
    }

    @Test
    void mapsHttpFailureStatus() {
        ExchangeFilterFunction filter =
                NodeCommunicationFilter.forNode("AGENT-1");
        ClientRequest request = request();
        ExchangeFunction exchange = ignored -> Mono.just(
                ClientResponse.create(HttpStatus.BAD_GATEWAY).build());

        NodeCommunicationException error = assertThrows(
                NodeCommunicationException.class,
                () -> filter.filter(request, exchange).block());

        assertEquals(NodeCommunicationException.Reason.HTTP_ERROR,
                error.getReason());
        assertEquals(502, error.getStatusCode());
        assertEquals("GET /MpcTasks/ready", error.getOperation());
    }

    @Test
    void mapsHttpClientFailureStatus() {
        ExchangeFilterFunction filter =
                NodeCommunicationFilter.forNode("AGENT-1");
        ExchangeFunction exchange = ignored -> Mono.just(
                ClientResponse.create(HttpStatus.BAD_REQUEST).build());

        NodeCommunicationException error = assertThrows(
                NodeCommunicationException.class,
                () -> filter.filter(request(), exchange).block());

        assertEquals(NodeCommunicationException.Reason.HTTP_ERROR,
                error.getReason());
        assertEquals(400, error.getStatusCode());
    }

    private NodeCommunicationException invokeWithFailure(
            Throwable failure) {
        ExchangeFilterFunction filter =
                NodeCommunicationFilter.forNode("AGENT-1");
        ExchangeFunction exchange = ignored -> Mono.error(failure);

        return assertThrows(
                NodeCommunicationException.class,
                () -> filter.filter(request(), exchange).block());
    }

    private ClientRequest request() {
        return ClientRequest.create(
                        HttpMethod.GET,
                        URI.create("http://agent/MpcTasks/ready"))
                .build();
    }
}
