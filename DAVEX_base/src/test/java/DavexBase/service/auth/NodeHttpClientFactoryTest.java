package DavexBase.service.auth;

import io.netty.channel.ChannelOption;
import org.junit.jupiter.api.Test;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 验证所有节点通信客户端都具有统一且有限的等待时间。 */
class NodeHttpClientFactoryTest {

    @Test
    void configuresConnectAndResponseTimeouts() {
        HttpClient client = NodeHttpClientFactory.create();

        assertEquals(
                5_000,
                client.configuration().options().get(
                        ChannelOption.CONNECT_TIMEOUT_MILLIS));
        assertEquals(
                Duration.ofSeconds(30),
                client.configuration().responseTimeout());
    }
}
