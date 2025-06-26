package DavexAgent.module.TEE;

import DavexBase.common.Body;
import DavexBase.common.CertificateGenerator;
import DavexBase.service.TEE.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/TEE")
public class TEEController {

    @Autowired
    private CertificateGenerator certificateGenerator;

    @Autowired
    private TEEService teeService;

    @Autowired
    private TempHolder tempHolder;

    @PostMapping("/keycer")
    public Body<String> generateKeyAndCertificate(
            @RequestParam String keyFilePath,
            @RequestParam String certFilePath,
            @RequestParam String subject) {
        try {
            // 生成密钥和证书
            certificateGenerator.generateKeyAndCertificate(keyFilePath, certFilePath, subject);
            return Body.success("密钥和证书生成成功");
        } catch (Exception e) {
            return Body.error("密钥和证书生成失败：" + e.getLocalizedMessage());
        }
    }

    @PostMapping("/encrypt-upload")
    public Body<TEEEncryptAndUploadResponse> encryptAndUpload(
            @RequestParam String cwd,
            @RequestParam String certPemsFile,
            @RequestParam String privateKeyFile,
            @RequestParam String resourceUri,
            @RequestParam String fileName,
            @RequestParam String subject
    ) {
        TEEEncryptAndUploadResponse response = teeService.encryptAndUploadDataKey(cwd, certPemsFile, privateKeyFile, resourceUri, fileName, subject);

        if (response.isUploadSuccess()) {
            return Body.success(response, "数据密钥上传成功");
        } else {
            return Body.error(response, "数据密钥上传失败");
        }
    }

    @PostMapping("/registerDataPolicy")
    public Body<String> registerDataPolicy(@RequestParam String cwd,
                                           @RequestParam String subject,
                                           @RequestParam String scope,
                                           @RequestParam String dataUuid,
                                           @RequestParam String ruleId,
                                           @RequestParam List<String> columns,
                                           @RequestParam String otherId) {
        try {
            //数据授权
            teeService.registerDataPolicy(cwd, subject, scope, dataUuid, ruleId, columns, otherId);
            return Body.success("数据授权成功");
        } catch (Exception e) {
            return Body.error("数据授权失败：" + e.getLocalizedMessage());
        }
    }

    @PostMapping("/getEncAndVoteRequest")
    public Body<String> getEncAndVoteRequest(@RequestParam String resultPath,
                                             @RequestParam String cwd,
                                             @RequestParam String type,
                                             @RequestParam Integer approvedThreshold,
                                             @RequestParam String approvedAction,
                                             @RequestParam List<String> certChainFile,
                                             @RequestParam String privateKeyFile) {
        try {
            teeService.getEncAndVoteRequest(resultPath, cwd, type, approvedThreshold, approvedAction, certChainFile, privateKeyFile);
            return Body.success("获取加密结果及投票请求成功");
        } catch (Exception e) {
            return Body.error("获取加密结果及投票请求失败：" + e.getLocalizedMessage());
        }
    }

    @PostMapping("/toVote")
    public Body<String> toVote(@RequestParam String cwd,
                               @RequestParam String action,
                               @RequestParam List<String> certChainFile,
                               @RequestParam String privateKeyFile,
                               @RequestParam String subject,
                               @RequestParam String otherId) {
        try {
            teeService.toVote(cwd, action, certChainFile, privateKeyFile, subject, otherId);
            return Body.success("投票成功");
        } catch (Exception e) {
            return Body.error("投票失败：" + e.getLocalizedMessage());
        }
    }

    @PostMapping("/voteResultAndDecryptFile")
    public Body<String> voteResultAndDecryptFile(@RequestParam String cwd,
                                                 @RequestParam String subject,
                                                 @RequestParam String resourceUri,
                                                 @RequestParam String sourceFile,
                                                 @RequestParam String destFile,
                                                 @RequestParam String otherId) {
        try {
            teeService.voteResultAndDecryptFile(cwd, subject, resourceUri,sourceFile, destFile, otherId);
            return Body.success("文件解密成功");
        } catch (Exception e) {
            return Body.error("文件解密失败：" + e.getLocalizedMessage());
        }
    }

    @GetMapping("/agentPartyId")
    public String getAgentPartyId() {
        return tempHolder.getAgentPartyId();
    }

    @GetMapping("/agentEncFilePath")
    public String getAgentEncFilePath(){
        return tempHolder.getAgentEncPath();
    }

    @GetMapping("/centerPartyId")
    public String getCenterPartyId() {
        return tempHolder.getCenterPartyId();
    }

    @GetMapping("/voteRequestSignature")
    public String getVoteRequestSignature(){
        return tempHolder.getVoteRequestSignature();
    }

    @GetMapping("/voterFilePath")
    public String getVoterFilePath(){
        return tempHolder.getVoterFilePath();
    }

}
