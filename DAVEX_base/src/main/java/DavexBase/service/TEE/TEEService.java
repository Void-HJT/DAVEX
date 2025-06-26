package DavexBase.service.TEE;

import DavexBase.service.auth.AgentWebClientService;
import DavexBase.service.auth.CenterWebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;


@Service
public class TEEService {

    private final String centerPath = "DAVEX_center/src/main/resources/TEE/";
    private final String agentPath = "DAVEX_agent/src/main/resources/TEE/";

    @Autowired
    private TEEDataOwnerService dataOwnerService;

    @Autowired
    private TEEMachineService teeMachineService;

    @Autowired
    private TempHolder tempHolder;

    @Autowired
    private AgentWebClientService agentWebClientService;

    @Autowired
    private CenterWebClientService centerWebClientService;


    /**
     * 执行加密数据并上传密钥的操作
     *
     * @param cwd             当前工作目录
     * @param certPemsFile    证书文件路径
     * @param privateKeyFile  私钥文件路径
     * @param resourceUri     资源 URI
     * @param fileName        要加密的文件名
     * @param subject         主体，center/agent
     * @return TEEEncryptAndUploadResponse 返回密钥和上传状态
     */
    public TEEEncryptAndUploadResponse encryptAndUploadDataKey(String cwd, String certPemsFile, String privateKeyFile, String resourceUri,
                                                               String fileName, String subject) {
        TEEEncryptAndUploadResponse response = new TEEEncryptAndUploadResponse();
        String templateYamlPath;
        try {
            if (subject.equalsIgnoreCase("center")||subject.equalsIgnoreCase("agent")){
                templateYamlPath = centerPath + "cli-template.yaml";
            }else{
                throw new IllegalArgumentException("Invalid subject. Must be 'center' or 'agent'.");
            }

            String newYamlPath = cwd + "/"+subject +".yaml";
            Files.copy(Path.of(templateYamlPath),Path.of(newYamlPath), StandardCopyOption.REPLACE_EXISTING);

            // 1. 生成 dataKey
            String dataKey = dataOwnerService.generateDataKey().trim();
            response.setDataKey(dataKey); // 设置返回的密钥

            // 2. 生成 PartyId
            String partyId = dataOwnerService.generatePartyId(cwd+"/"+certPemsFile).trim();
            response.setPartyId(partyId);

            tempHolder.setPartyId(partyId, subject);

            // 3. 执行数据加密
            dataOwnerService.encryptData(cwd+"/"+fileName, dataKey);  // 加密文件
            tempHolder.setEncPath(cwd+"/"+fileName+".enc", subject);

            // 4. 上传数据密钥
            dataOwnerService.uploadDataKey(cwd, subject+".yaml", partyId, certPemsFile, privateKeyFile, resourceUri, dataKey);

            // 5. 设置上传成功标志
            response.setUploadSuccess(true);
        } catch (Exception e) {
            // 处理异常
            response.setUploadSuccess(false);
        }

        return response;
    }

    public void registerDataPolicy(String cwd, String subject, String scope, String dataUuid, String ruleId,
                                   List<String> columns, String otherId) throws Exception {
        List<String> granteePartyIds = new ArrayList<>();
        if (subject.equalsIgnoreCase("center")){
            String agentPartyId = getAgentPartyId(otherId);
            tempHolder.setAgentPartyId(agentPartyId);

            String agentEncPath = getAgentEncFilePath(otherId);
            tempHolder.setAgentEncPath(agentEncPath);
        }else if (subject.equalsIgnoreCase("agent")){
            String centerPartyId = getCenterPartyId(otherId);
            tempHolder.setCenterPartyId(centerPartyId);
        }

        granteePartyIds.add(tempHolder.getCenterPartyId());
        dataOwnerService.registerDataPolicy(cwd,subject+".yaml", scope, dataUuid, ruleId, granteePartyIds, columns);
    }

    public void runApp(String cwd, String subject, String taskName, String signedTaskName, String localTaskJsonPath,
                       String scope, String testPath){

        String agentEncPath = tempHolder.getAgentEncPath();
        String centerEncPath = tempHolder.getCenterEncPath();

        try {
            teeMachineService.uploadEncryptedData(agentEncPath, centerEncPath, testPath);
        } catch (Exception e) {
            throw new RuntimeException("上传加密数据失败！"+e.getMessage(), e);
        }

        try {
            teeMachineService.uploadJsonKeyCert(localTaskJsonPath, cwd+"/"+subject+".key", cwd+"/"+subject+".crt",testPath);
        } catch (Exception e) {
            throw new RuntimeException("上传任务文件和证书私钥失败！"+e.getMessage(), e);
        }

        try {
            teeMachineService.signTask(subject+".crt", subject+".key", taskName, scope, signedTaskName, testPath);
        } catch (Exception e) {
            throw new RuntimeException("任务文件签名失败！"+e.getMessage(), e);
        }

        try {
            teeMachineService.runTrustedApp(signedTaskName, testPath);
        } catch (Exception e) {
            throw new RuntimeException("运行可信APP失败！"+e.getMessage(), e);
        }
    }

    public void getEncAndVoteRequest(String resultPath, String cwd, String type, Integer approvedThreshold,
                                     String approvedAction, List<String> certChainFile, String privateKeyFile){
        try {
            dataOwnerService.getResult(resultPath,cwd);
        } catch (Exception e) {
            throw new RuntimeException("获取结果失败！"+e.getMessage(),e);
        }

        String templateYamlPath = centerPath + "vote-request.yaml";
        String newYamlPath = cwd + "/vote-request.yaml";
        try {
            Files.copy(Path.of(templateYamlPath),Path.of(newYamlPath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("复制模板文件出错！"+e.getMessage(),e);
        }

        try {
            String voteRequestSignature =  dataOwnerService.voteRequest(cwd,"vote-request.yaml",type,approvedThreshold,
                    approvedAction, certChainFile, privateKeyFile, "signed-vote-request.yaml");
            tempHolder.setVoteRequestSignature(voteRequestSignature);
        } catch (Exception e) {
            throw new RuntimeException("投票请求失败！"+e.getMessage(),e);
        }

    }

    public void toVote(String cwd, String action,List<String> certChainFile, String privateKeyFile, String subject, String otherId){
        String templateYamlPath = agentPath + "voter.yaml";
        String newYamlPath = cwd + "/voter.yaml";
        String voteRequestSignature;
        try {
            Files.copy(Path.of(templateYamlPath),Path.of(newYamlPath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("复制模板文件出错！"+e.getMessage(),e);
        }

        try {
            if (subject.equalsIgnoreCase("center")){
                voteRequestSignature = centerGetVoteRequestSignature(otherId);
            }else {
                voteRequestSignature = agentGetVoteRequestSignature(otherId);
            }

        } catch (Exception e) {
            throw new RuntimeException("获取voteRequestSignature出错！"+e.getMessage(),e);
        }

        try {
            dataOwnerService.toVote(cwd,"voter.yaml", voteRequestSignature, action, certChainFile, privateKeyFile, "signed-voter.yaml");
            tempHolder.setVoterFilePath(cwd);
        } catch (Exception e) {
            throw new RuntimeException("投票过程出错！"+e.getMessage(),e);
        }
    }

    public void voteResultAndDecryptFile(String cwd, String subject, String resourceUri, String sourceFile, String destFile, String otherId){
        String voterFilePath;
        String exportDataKey;
        try {
            if (subject.equalsIgnoreCase("agent")){
                voterFilePath = agentGetVoterFilePath(otherId);
            }else{
                voterFilePath = centerGetVoterFilePath(otherId);
            }
                   } catch (Exception e) {
            throw new RuntimeException("获取voterCwd出错！"+e.getMessage(),e);
        }

        try {
            dataOwnerService.voteResult(cwd, "signed-vote-request.yaml", "signed-voter.yaml", voterFilePath, "vote-result.json");
        } catch (Exception e) {
            throw new RuntimeException("生成投票结果时出错！"+e.getMessage(),e);
        }

        try {
            exportDataKey = dataOwnerService.getExportDataKey(cwd, subject+".yaml", tempHolder.getPartyId(subject),resourceUri,"vote-result.json");
        } catch (Exception e) {
            throw new RuntimeException("获取结果密钥失败！"+e.getMessage(),e);
        }

        try {
            dataOwnerService.decryptFile(cwd, exportDataKey, sourceFile, destFile);
        } catch (Exception e) {
            throw new RuntimeException("解密失败！"+e.getMessage(),e);
        }

    }


    private String getCenterPartyId(String centerId) throws Exception {
        WebClient webClient = agentWebClientService.agent2CenterWebClient(centerId);
        return webClient.get().uri("/TEE/centerPartyId")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String getAgentPartyId(String agentId) throws Exception {
        WebClient webClient = centerWebClientService.center2AgentWebClient(agentId);
        return webClient.get()
                .uri("/TEE/agentPartyId")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String getAgentEncFilePath(String agentId) throws Exception {
        WebClient webClient = centerWebClientService.center2AgentWebClient(agentId);
        return webClient.get()
                .uri("/TEE/agentEncFilePath")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String centerGetVoteRequestSignature(String agentId) throws Exception {
        WebClient webClient =  centerWebClientService.center2AgentWebClient(agentId);
        return webClient.get().uri("/TEE/voteRequestSignature")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String agentGetVoteRequestSignature(String centerId) throws Exception {
        WebClient webClient = agentWebClientService.agent2CenterWebClient(centerId);
        return webClient.get().uri("/TEE/voteRequestSignature")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String centerGetVoterFilePath(String agentId) throws Exception {
        WebClient webClient = centerWebClientService.center2AgentWebClient(agentId);
        return webClient.get()
                .uri("/TEE/voterFilePath")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String agentGetVoterFilePath(String centerId) throws Exception {
        WebClient webClient = agentWebClientService.agent2CenterWebClient(centerId);
        return webClient.get()
                .uri("/TEE/voterFilePath")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
