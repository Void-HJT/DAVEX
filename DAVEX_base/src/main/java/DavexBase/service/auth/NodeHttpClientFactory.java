package DavexBase.service.auth;

import java.time.Duration;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

/**
 * 统一创建节点通信使用的 HttpClient，避免节点失联时无限等待。
 */
final class NodeHttpClientFactory {

    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;
    private static final Duration RESPONSE_TIMEOUT =
            Duration.ofSeconds(30);

    private NodeHttpClientFactory() {
    }

    /**
     * 为 Center 与 Agent 的节点请求提供一致的连接和响应超时。
     */
    static HttpClient create() {
        return HttpClient.create()
                .option(
                        ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        CONNECT_TIMEOUT_MILLIS)
                .responseTimeout(RESPONSE_TIMEOUT);
    }
}