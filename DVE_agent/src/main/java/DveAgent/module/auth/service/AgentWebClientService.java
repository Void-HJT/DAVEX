package DveAgent.module.auth.service;

import java.security.KeyStore;
import java.security.cert.Certificate;

import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveAgent.common.My;
import DveAgent.common.Utlis;
import DveAgent.entity.Agent;
import DveAgent.entity.Center;
import DveAgent.mapper.AgentMapper;
import DveAgent.mapper.CenterMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.netty.http.client.HttpClient;

@Service
public class AgentWebClientService {
        @Autowired
        private CenterMapper centerMapper;
        @Autowired
        private AgentMapper agentMapper;
        @Autowired
        private SslContextBuilder sslBuilder;

        @Autowired
        My my;

        public WebClient agent2CenterWebClient(int center_id) throws Exception {
                LambdaQueryWrapper<Center> queryWrapper = Wrappers.<Center>lambdaQuery().eq(Center::getUid, center_id);
                Center center = centerMapper.selectOne(queryWrapper);
                Certificate certificate = Utlis.bytesToCertificate(center.getCrt());
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);

                keyStore.setCertificateEntry(center.getName(), certificate);
                TrustManagerFactory trustManagerFactory = TrustManagerFactory
                                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                SslContext sslContext = sslBuilder.trustManager(trustManagerFactory).build();
                HttpClient httpClient = HttpClient.create()
                                .secure(sslSpec -> sslSpec.sslContext(sslContext));
                return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                                .baseUrl("https://" + center.getIp() + ":" + center.getPort()).build();
        }

        public WebClient agent2AgentWebClient(int agent_id) throws Exception {
                LambdaQueryWrapper<Agent> queryWrapper = Wrappers.<Agent>lambdaQuery().eq(Agent::getUid, agent_id);
                Agent agent = agentMapper.selectOne(queryWrapper);
                Certificate certificate = Utlis.bytesToCertificate(agent.getCrt());
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);

                keyStore.setCertificateEntry(agent.getName(), certificate);
                TrustManagerFactory trustManagerFactory = TrustManagerFactory
                                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                SslContext sslContext = sslBuilder.trustManager(trustManagerFactory).build();
                HttpClient httpClient = HttpClient.create()
                                .secure(sslSpec -> sslSpec.sslContext(sslContext));
                return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                                .baseUrl("https://" + agent.getIp() + ":" + agent.getPort()).build();
        }
}
