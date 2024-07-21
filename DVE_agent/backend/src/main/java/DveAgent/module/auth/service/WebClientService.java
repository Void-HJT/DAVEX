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
import DveAgent.entity.Center;
import DveAgent.mapper.CenterMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.netty.http.client.HttpClient;

@Service
public class WebClientService {
    @Autowired
    private CenterMapper centerMapper;
    @Autowired
    private SslContextBuilder sslBuilder;

    @Autowired
    My my;

    public WebClient agent2CenterWebClient(int center_id) throws Exception {
        LambdaQueryWrapper<Center> queryWrapper = Wrappers.<Center>lambdaQuery().eq(Center::getUid, center_id);
        Center center = centerMapper.selectOne(queryWrapper);
        Certificate certificate = Utlis.bytesToCertificate(center.getCrt().getBytes());
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
                .baseUrl("https:\\\\" + center.getIp() + ":" + center.getPort()).build();
    }
}
