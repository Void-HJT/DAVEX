package DavexBase.service.TEE;

import DavexBase.common.docker.DockerExecutor;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Data
@Service
public class TEEDataOwnerService {

    private final DockerExecutor dockerExecutor;
    private final YamlService yamlService;

    public final String Container_Id = "capsule-manager-sdk";
    public final String Docker_capsule_sdk_Path = "/DAVEX";

    public TEEDataOwnerService(DockerExecutor dockerExecutor, YamlService yamlService) {
        this.dockerExecutor = dockerExecutor;
        this.yamlService = yamlService;
    }

    /**
     * 生成密钥
     * @return 生成的密钥
     * @throws Exception 执行出错或命令返回错误
     */
    public String generateDataKey() throws Exception {
        // 生成密钥命令
        //String generateKeyCommand = "cms_util generate-data-key-b64";
        List<String> command = new ArrayList<>(Arrays.asList("/root/miniconda3/bin/cms_util", "generate-data-key-b64"));
        // 执行命令并返回生成的密钥
        return dockerExecutor.exec(Container_Id,Docker_capsule_sdk_Path,command);
    }

    public void encryptData(String filePath, String dataKey) throws Exception {
        // 获取文件的目录和文件名
        String destFilePath = filePath + ".enc"; // 加密后的文件路径，添加 .enc 后缀

        // 构建加密命令
        List<String> command = new ArrayList<>(Arrays.asList("/root/miniconda3/bin/cms_util", "encrypt-file","--source-file",filePath,"--dest-file",destFilePath,"--data-key-b64",dataKey));

        // 执行加密命令
        dockerExecutor.exec(Container_Id, Docker_capsule_sdk_Path, command);
    }

    public String generatePartyId(String certFilePath) throws Exception {
        List<String> command = new ArrayList<>(Arrays.asList("/root/miniconda3/bin/cms_util","generate-party-id","--cert-file",certFilePath));
        return dockerExecutor.exec(Container_Id,Docker_capsule_sdk_Path,command);
    }

    /**
     * 上传数据密钥
     * @param yamlFile    配置文件名，传入指定的 .yaml 文件名
     * @param partyId         party ID，需根据证书生成
     * @param certPemsFile   List[str]，证书文件路径
     * @param privateKeyFile  私钥文件路径
     * @param dataKey      数据密钥
     * @throws Exception 执行命令时的异常
     */
    public void uploadDataKey(String cwd, String yamlFile, String partyId, String certPemsFile,
                              String privateKeyFile, String resourceUri, String dataKey) throws Exception {
        String yamlPath = cwd +"/"+ yamlFile;
        // 先更新 YAML 文件
        yamlService.updateYamlRegister(yamlPath, partyId, certPemsFile, privateKeyFile, resourceUri, dataKey);

        // 准备执行 cms 数据密钥上传命令
        List<String> command = new ArrayList<>();
        command.add("/root/miniconda3/bin/cms");
        command.add("--config-file");
        command.add(yamlFile);
        command.add("register-data-keys");

        // 执行命令上传密钥
        dockerExecutor.exec(Container_Id, Docker_capsule_sdk_Path+"/"+cwd, command);

    }

    /**
     * 处理数据授权，更新 YAML 中的 register_data_policy并上传授权信息
     * @param cwd 仓库内目录
     * @param yamlFilePath YAML 配置文件路径
     * @param scope 作用域 (如 "default")
     * @param dataUuid 数据 UUID
     * @param ruleId 规则 ID
     * @param granteePartyIds 被授权机构 ID 列表（即 TEE 提供方 PartyId）
     * @param columns 授权的数据列
     * @throws Exception 执行命令时的异常
     */
    public void registerDataPolicy(String cwd,String yamlFilePath, String scope, String dataUuid, String ruleId,
                                   List<String> granteePartyIds, List<String> columns) throws Exception {

        String yamlPath = cwd +"/"+ yamlFilePath;
        yamlService.updateYamlDataPolicy(yamlPath, scope, dataUuid, ruleId, granteePartyIds, columns);

        // 准备执行 cms 数据授权命令
        List<String> command = new ArrayList<>();
        command.add("/root/miniconda3/bin/cms");
        command.add("--config-file");
        command.add(yamlFilePath);
        command.add("register-data-policy");

        // 执行命令上传数据授权
        dockerExecutor.exec(Container_Id, Docker_capsule_sdk_Path+"/"+cwd, command);

    }

    public void getResult(String resultPath, String savePath)throws Exception{
        String dokerResPath =  "teeapps-sim:" + resultPath;
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("cp");
        command.add(dokerResPath);
        command.add(savePath);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.start().waitFor();
    }

    public String voteRequest(String cwd, String requestYamlFilePath, String type, Integer approvedThreshold,
                              String approvedAction, List<String> certChainFile, String privateKeyFile, String signedFilePath) throws Exception {

        String yamlPath = cwd +"/"+ requestYamlFilePath;
        yamlService.updateVoteRequestYaml(yamlPath,type,approvedThreshold,approvedAction,certChainFile,privateKeyFile);

        //对投票请求进行签名
        List<String> command = new ArrayList<>();
        command.add("/root/miniconda3/bin/cms_util");
        command.add("sign-vote-request");
        command.add("--vote-request-file");
        command.add(requestYamlFilePath);
        command.add("--signed-vote-request-file");
        command.add(signedFilePath);
        dockerExecutor.exec(Container_Id, Docker_capsule_sdk_Path+"/"+cwd, command);

        // 读取 signed-vote-request.yaml 文件并提取 vote_request_signature
        Yaml yaml = new Yaml();
        Map<String, Object> signedYaml = yaml.load(Files.newBufferedReader(Paths.get(cwd+"/"+signedFilePath)));

        // 提取 vote_request_signature 字段
        String voteRequestSignature;
        voteRequestSignature = (String) signedYaml.get("vote_request_signature");
        return Base64.getEncoder().encodeToString(voteRequestSignature.getBytes());

    }

    public void toVote(String cwd, String voterYamlFilePath, String voteRequestSignature, String action,
                         List<String> certChainFile, String privateKeyFile, String signedFilePath) throws Exception {
        String yamlPath = cwd +"/"+ voterYamlFilePath;
        byte[] decodedBytes = Base64.getDecoder().decode(voteRequestSignature);
        String originalSignature = new String(decodedBytes);
        yamlService.updateVoteYaml(yamlPath,originalSignature,action,certChainFile,privateKeyFile);

        //对投票进行签名
        List<String> command = new ArrayList<>();
        command.add("/root/miniconda3/bin/cms_util");
        command.add("voter-sign");
        command.add("--voter-file");
        command.add(voterYamlFilePath);
        command.add("--signed-voter-file");
        command.add(signedFilePath);
        dockerExecutor.exec(Container_Id, Docker_capsule_sdk_Path+"/"+cwd, command);

    }

    public void voteResult(String cwd, String signedVoteRequestYaml, String signedVoterYaml, String voterFilePath, String voteResultFile) throws Exception{
        List<String> command0 = new ArrayList<>();
        command0.add("cp");
        command0.add(voterFilePath+"/"+signedVoterYaml);
        command0.add(cwd);
        ProcessBuilder pb = new ProcessBuilder(command0);
        pb.start().waitFor();

        //生成投票结果文件
        List<String> command = new ArrayList<>();
        command.add("/root/miniconda3/bin/cms_util");
        command.add("generate-vote-result");
        command.add("--signed-vote-request-file");
        command.add(signedVoteRequestYaml);
        command.add("--signed-voter-files");
        command.add(signedVoterYaml);
        command.add("--vote-result-file");
        command.add(voteResultFile);
        dockerExecutor.exec(Container_Id, Docker_capsule_sdk_Path+"/"+cwd, command);
        
    }

    public String getExportDataKey(String cwd, String yamlFilePath, String partyId, String resourceUri, String dataExportCertificateFile) throws Exception {
        String yamlPath = cwd +"/"+ yamlFilePath;
        // 先更新 YAML 文件
        yamlService.updateYamlExportDataKey(yamlPath, partyId, resourceUri, dataExportCertificateFile);

        // 准备执行获取结果密钥命令
        List<String> command = new ArrayList<>();
        command.add("/root/miniconda3/bin/cms");
        command.add("--config-file");
        command.add(yamlFilePath);
        command.add("get-export-data-key-b64");

        String dataKey = dockerExecutor.exec(Container_Id, Docker_capsule_sdk_Path+"/"+cwd, command);
        return Base64.getEncoder().encodeToString(dataKey.getBytes());
    }

    public void decryptFile(String cwd, String exportDataKey, String sourceFile, String destFile) throws Exception {

        byte[] decodedBytes = Base64.getDecoder().decode(exportDataKey);
        String originalKey = new String(decodedBytes);

        List<String> command = new ArrayList<>();
        command.add("/root/miniconda3/bin/cms_util");
        command.add("decrypt-file");
        command.add("--data-key-b64");
        command.add(originalKey);
        command.add("--source-file");
        command.add(sourceFile);
        command.add("--dest-file");
        command.add(destFile);
        dockerExecutor.exec(Container_Id, Docker_capsule_sdk_Path+"/"+cwd, command);
    }

}

