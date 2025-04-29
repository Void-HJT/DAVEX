package DavexBase.service.TEE;

import DavexBase.service.TEE.models.*;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class YamlService {

    /**
     * 更新 YAML 文件的 `common` 部分和 `register_data_keys`
     * @param yamlFilePath 需要修改的 YAML 文件路径（agent.yaml 或 center.yaml）
     * @param partyId 更新的 Party ID
     * @param certPemsFile 证书文件路径
     * @param privateKeyFile 私钥文件路径
     * @param resourceUri 资源 URI
     * @param dataKeyB64 数据密钥 (Base64)
     * @throws IOException 发生 IO 异常
     */
    public void updateYamlRegister(String yamlFilePath, String partyId, String certPemsFile,
                                   String privateKeyFile, String resourceUri, String dataKeyB64) throws IOException {
        File yamlFile = new File(yamlFilePath);
        if (!yamlFile.exists()) {
            throw new FileNotFoundException("YAML 文件不存在: " + yamlFilePath);
        }

        // 解析 YAML
        Yaml yaml = new Yaml(new Constructor(YamlConfig.class));
        InputStream inputStream = new FileInputStream(yamlFile);
        YamlConfig yamlConfig = yaml.load(inputStream);
        inputStream.close();

        // 修改 `common` 部分
        if (yamlConfig.getCommon() == null) {
            yamlConfig.setCommon(new CommonConfig());
        }
        yamlConfig.getCommon().setParty_id(partyId);
        yamlConfig.getCommon().setCert_pems_file(Arrays.asList(certPemsFile));
        yamlConfig.getCommon().setPrivate_key_file(privateKeyFile);

        // 修改 `register_data_keys` 部分
        if (yamlConfig.getRegister_data_keys() == null) {
            yamlConfig.setRegister_data_keys(new RegisterDataKeys());
        }
        if (yamlConfig.getRegister_data_keys().getData_keys() == null) {
            yamlConfig.getRegister_data_keys().setData_keys(new ArrayList<>());
        }


         //添加新的 DataKey
        yamlConfig.getRegister_data_keys().getData_keys().add(new DataKey(resourceUri, dataKeyB64));

        // 写回 YAML 文件
        try (FileWriter writer = new FileWriter(yamlFile)) {
            yaml.dump(yamlConfig, writer);
        }

        System.out.println("YAML 密钥注册更新成功：" + yamlFilePath);
    }

    public void updateYamlDataPolicy(String yamlFilePath, String scope, String dataUuid, String ruleId,
                                     List<String> granteePartyIds, List<String> columns) throws IOException {
        File yamlFile = new File(yamlFilePath);
        if (!yamlFile.exists()) {
            throw new FileNotFoundException("YAML 文件不存在: " + yamlFilePath);
        }

        // 解析 YAML
        Yaml yaml = new Yaml(new Constructor(YamlConfig.class));
        InputStream inputStream = new FileInputStream(yamlFile);
        YamlConfig yamlConfig = yaml.load(inputStream);
        inputStream.close();

        // 确保 register_data_policy 存在
        if (yamlConfig.getRegister_data_policy() == null) {
            yamlConfig.setRegister_data_policy(new RegisterDataPolicy());
        }
        RegisterDataPolicy registerDataPolicy = yamlConfig.getRegister_data_policy();

        registerDataPolicy.setScope(scope);
        registerDataPolicy.setData_uuid(dataUuid);

        // 确保 rules 存在
        if (registerDataPolicy.getRules() == null) {
            registerDataPolicy.setRules(new ArrayList<>());
        }

        // 创建 op_constraints
        OpConstraint opConstraint = new OpConstraint();
        opConstraint.setOp_name("psi"); // 设置 op_name 为 "psi"
        opConstraint.setConstraints(new ArrayList<>());


        // 创建新的 PolicyRule
        PolicyRule policyRule = new PolicyRule();
        policyRule.setRule_id(ruleId);
        policyRule.setGrantee_party_ids(granteePartyIds);
        policyRule.setColumns(columns);
        policyRule.setGlobal_constraints(new ArrayList<>());
        policyRule.setOp_constraints(Collections.singletonList(opConstraint)); // 确保 op_constraints 存在 psi


        // 添加到 rules 列表
        registerDataPolicy.getRules().add(policyRule);

        // 写回 YAML
        try (FileWriter writer = new FileWriter(yamlFile)) {
            yaml.dump(yamlConfig, writer);
        }

        System.out.println("YAML 数据授权更新成功：" + yamlFilePath);
    }

    public void updateYamlExportDataKey(String yamlFilePath, String partyId, String resourceUri,
                                        String dataExportCertificateFile) throws IOException {
        File yamlFile = new File(yamlFilePath);
        if (!yamlFile.exists()) {
            throw new FileNotFoundException("YAML 文件不存在: " + yamlFilePath);
        }

        // 解析 YAML
        Yaml yaml = new Yaml(new Constructor(YamlConfig.class));
        InputStream inputStream = new FileInputStream(yamlFile);
        YamlConfig yamlConfig = yaml.load(inputStream);
        inputStream.close();

        // 确保 get_export_data_key_b64 存在
        if (yamlConfig.getGet_export_data_key_b64() == null) {
            yamlConfig.setGet_export_data_key_b64(new GetExportDataKeyB64());
        }
        GetExportDataKeyB64 getExportDataKeyB64 = yamlConfig.getGet_export_data_key_b64();

        getExportDataKeyB64.setParty_id(partyId);
        getExportDataKeyB64.setResource_uri(resourceUri);
        getExportDataKeyB64.setData_export_certificate_file(dataExportCertificateFile);

        // 写回 YAML
        try (FileWriter writer = new FileWriter(yamlFile)) {
            yaml.dump(yamlConfig, writer);
        }

        System.out.println("YAML 结果数据密钥更新成功：" + yamlFilePath);
    }



    public void updateVoteRequestYaml(String yamlFilePath, String type, Integer approvedThreshold,
                                      String approvedAction, List<String> certChainFile, String privateKeyFile) throws IOException {
        File yamlFile = new File(yamlFilePath);
        if (!yamlFile.exists()) {
            throw new FileNotFoundException("投票请求文件不存在: " + yamlFilePath);
        }

        // 解析 YAML
        Yaml yaml = new Yaml(new Constructor(VoteRequestConfig.class));
        InputStream inputStream = new FileInputStream(yamlFile);
        VoteRequestConfig voteRequestConfig = yaml.load(inputStream);
        inputStream.close();

        if (voteRequestConfig.getVote_request() == null) {
            voteRequestConfig.setVote_request(new VoteRequest());
        }
        voteRequestConfig.getVote_request().setType(type);
        voteRequestConfig.getVote_request().setApproved_threshold(approvedThreshold);
        voteRequestConfig.getVote_request().setApproved_action(approvedAction);
        voteRequestConfig.getVote_request().setCert_chain_file(certChainFile);
        voteRequestConfig.getVote_request().setPrivate_key_file(privateKeyFile);

        // 写回 YAML 文件
        try (FileWriter writer = new FileWriter(yamlFile)) {
            yaml.dump(voteRequestConfig, writer);
        }

        System.out.println("投票请求文件更新成功：" + yamlFilePath);
    }

    public void updateVoteYaml(String yamlFilePath, String voteRequestSignature, String action,
                               List<String> certChainFile, String privateKeyFile) throws IOException {
        File yamlFile = new File(yamlFilePath);
        if (!yamlFile.exists()) {
            throw new FileNotFoundException("投票文件不存在: " + yamlFilePath);
        }

        // 解析 YAML
        Yaml yaml = new Yaml(new Constructor(VoterConfig.class));
        InputStream inputStream = new FileInputStream(yamlFile);
        VoterConfig voterConfig = yaml.load(inputStream);
        inputStream.close();

        voterConfig.setVote_request_signature(voteRequestSignature);
        voterConfig.setAction(action);
        voterConfig.setCert_chain_file(certChainFile);
        voterConfig.setPrivate_key_file(privateKeyFile);

        // 写回 YAML 文件
        try (FileWriter writer = new FileWriter(yamlFile)) {
            yaml.dump(voterConfig, writer);
        }

        System.out.println("投票文件更新成功：" + yamlFilePath);
    }


}
