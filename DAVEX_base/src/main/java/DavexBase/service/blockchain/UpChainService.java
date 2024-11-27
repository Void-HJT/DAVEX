package DavexBase.service.blockchain;

import DavexBase.common.ContractResponse;
import DavexBase.common.PersistentKeyUtils;
import DavexBase.info.ContractVerificationSubmitRequest;
import DavexBase.info.ContractVerificationSubmitResponseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static DavexBase.info.ContractVerificationSubmitRequest.generateRequest;
import static DavexBase.info.ContractVerificationSubmitResponseRequest.generateResponse;

@Service
public class UpChainService {

    @Autowired
    private PersistentKeyUtils persistentKeyUtils;

    //请求上链
    public ContractResponse requestUpChain(String requestId, String fileDescription,String requestMsg, String ownerName,String identity) throws Exception {

        // 初始化密钥对
        KeyPair keyPair = persistentKeyUtils.initializeKeyPair(ownerName,identity);
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // 公钥信息
        byte[] publicKeyDER = publicKey.getEncoded();
        String publicKeyString = Base64.getEncoder().encodeToString(publicKeyDER);

        ContractVerificationSubmitRequest request = generateRequest(fileDescription, privateKey, requestMsg);
        request.setCertificateID("9247c912-003b-4e39-b781-cfa07050a3ac");
        request.setRequestID(requestId);
        request.setPublicKey(publicKeyString);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(request);
        System.out.println(jsonBody);

        WebClient webClient = WebClient.create("http://localhost:8000");

        ContractResponse response = webClient.post()
                .uri("/api/contract/verification/invoke/submit_request")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .retrieve()
                .bodyToMono(ContractResponse.class)
                .block();  // 使用 block() 来等待请求结果
        System.out.println("Response: " + response);
        return response;
    }

    //响应上链
    public ContractResponse responseUpChain(String requestHash, String responseMsg, String responseId, String requestId,String ownerName,String identity) throws Exception {
        // 初始化密钥对
        KeyPair keyPair = persistentKeyUtils.initializeKeyPair(ownerName,identity);
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // 公钥信息
        byte[] publicKeyDER = publicKey.getEncoded();
        String publicKeyString = Base64.getEncoder().encodeToString(publicKeyDER);


        // 构建请求对象
        ContractVerificationSubmitResponseRequest request = generateResponse(requestHash, privateKey, responseMsg);
        request.setCertificateID("9247c912-003b-4e39-b781-cfa07050a3ac");
        request.setPublicKey(publicKeyString);
        request.setRequestID(requestId);
        request.setResponseID(responseId);

        // 创建 ObjectMapper 实例并将请求对象序列化为 JSON 字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(request);
        System.out.println(jsonBody);

        WebClient webClient = WebClient.create("http://localhost:8000");

        ContractResponse response = webClient.post()
                .uri("/api/contract/verification/invoke/submit_response")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .retrieve()
                .bodyToMono(ContractResponse.class)
                .block();  // 使用 block() 来等待请求结果
        System.out.println("Response: " + response);

        return response;
    }
}
