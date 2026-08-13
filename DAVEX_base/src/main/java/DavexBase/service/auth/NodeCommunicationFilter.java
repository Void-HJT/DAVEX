package DavexBase.service.auth;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

import io.netty.handler.timeout.ReadTimeoutException;
import reactor.core.publisher.Mono;

import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ClientResponse;

/**
 * 将 WebClient 和 Netty 的底层失败转换成统一节点通信异常。
 */
final class NodeCommunicationFilter {

    private NodeCommunicationFilter() {
    }

    /**
     * 为指定节点创建异常转换过滤器。
     */
    @SuppressWarnings("null")
    static ExchangeFilterFunction forNode(String nodeId) {
        return (request, next) -> {
            String operation = request.method()
                    + " " + request.url().getPath();

            return next.exchange(request)
                    .onErrorMap(error -> mapTransportError(
                            nodeId,
                            operation,
                            error))
                    .flatMap(response -> {
                        if (!response.statusCode().isError()) {
                            return Mono.just(response);
                        }

                        int statusCode = response.statusCode().value();

                        return response.createException()
                                .flatMap(error -> Mono.<ClientResponse>error(
                                        NodeCommunicationException.httpError(
                                                nodeId,
                                                operation,
                                                statusCode,
                                                error)));
                    });
        };
    }

    private static NodeCommunicationException mapTransportError(
            String nodeId,
            String operation,
            Throwable error) {

        if (hasCause(error, TimeoutException.class)
                || hasCause(error, ReadTimeoutException.class)) {
            return NodeCommunicationException.timeout(
                    nodeId,
                    operation,
                    error);
        }

        if (hasCause(error, ConnectException.class)) {
            return NodeCommunicationException.unreachable(
                    nodeId,
                    operation,
                    error);
        }

        // 其他底层网络错误也统一归类为节点不可达。
        return NodeCommunicationException.unreachable(
                nodeId,
                operation,
                error);
    }

    private static boolean hasCause(
            Throwable error,
            Class<? extends Throwable> type) {

        Throwable current = error;

        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }

        return false;
    }
}