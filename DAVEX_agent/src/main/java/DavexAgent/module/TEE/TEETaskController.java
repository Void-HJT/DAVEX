package DavexAgent.module.TEE;

import DavexBase.common.CertificateGenerator;
import DavexBase.service.TEE.TEEDataOwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/TEETask")
public class TEETaskController {

    @Autowired
    private CertificateGenerator certificateGenerator;

    @Autowired
    private TEEDataOwnerService teeDataOwnerService;

    @PostMapping("/keycer")
    public String generateKeyAndCertificate(@RequestParam String keyFilePath, @RequestParam String certFilePath, @RequestParam String subject) throws Exception {
        certificateGenerator.generateKeyAndCertificate(keyFilePath, certFilePath, subject);
        return "OK";
    }

    @GetMapping("/dataKey")
    public String generateKey() throws Exception {
        return teeDataOwnerService.generateDataKey();
    }

    @PostMapping("/encryptData")
    public String encryptData(@RequestParam String filePath,@RequestParam String dataKey) throws Exception {
        teeDataOwnerService.encryptData(filePath, dataKey);
        return "OK";
    }

    @PostMapping("/generatePartyId")
    public String generatePartyId(@RequestParam String certFilePath) throws Exception {
        return teeDataOwnerService.generatePartyId(certFilePath);
    }

    @PostMapping("/uploadDataKey")
    public String uploadDataKey(@RequestParam String cwd,
                                @RequestParam String yamlFilePath,
                                @RequestParam String partyId,
                                @RequestParam String certPemsFile,
                                @RequestParam String privateKeyFile,
                                @RequestParam String resourceUri,
                                @RequestParam String dataKey) throws Exception {

        teeDataOwnerService.uploadDataKey(cwd, yamlFilePath, partyId, certPemsFile, privateKeyFile, resourceUri, dataKey);
        return "OK";
    }

    @PostMapping("/registerDataPolicy")
    public String registerDataPolicy(@RequestParam String cwd,
                                     @RequestParam String yamlFilePath,
                                     @RequestParam String scope,
                                     @RequestParam String dataUuid,
                                     @RequestParam String ruleId,
                                     @RequestParam List<String> granteePartyIds,
                                     @RequestParam List<String> columns) throws Exception {
        teeDataOwnerService.registerDataPolicy(cwd, yamlFilePath, scope, dataUuid, ruleId, granteePartyIds, columns);
        return "数据授权成功";
    }

    @PostMapping("/getResult")
    public String getResult(@RequestParam String resultPath, @RequestParam String savePath) throws Exception {
        teeDataOwnerService.getResult(resultPath,savePath);
        return "获取加密结果成功";
    }

    @PostMapping("/voteRequest")
    public String voteRequest(@RequestParam String cwd,
                              @RequestParam String requestYamlFilePath,
                              @RequestParam String type,
                              @RequestParam Integer approvedThreshold,
                              @RequestParam String approvedAction,
                              @RequestParam List<String> certChainFile,
                              @RequestParam String privateKeyFile,
                              @RequestParam String signedFilePath) throws Exception {
        String voteRequestSignature = teeDataOwnerService.voteRequest(cwd,requestYamlFilePath,type,approvedThreshold,approvedAction,certChainFile,privateKeyFile,signedFilePath);
        return "执行完毕, vote_request_signature为: "+voteRequestSignature;
    }

    @PostMapping("/toVote")
    public String toVote(@RequestParam String cwd,
                         @RequestParam String voteYamlFilePath,
                         @RequestParam String voteRequestSignature,
                         @RequestParam String action,
                         @RequestParam List<String> certChainFile,
                         @RequestParam String privateKeyFile,
                         @RequestParam String signedFilePath) throws Exception {
        String signedFile = teeDataOwnerService.toVote(cwd,voteYamlFilePath,voteRequestSignature,action,certChainFile,privateKeyFile,signedFilePath);
        return "执行完毕, 签名后的投票结果路径为: "+ signedFile;
    }

    @PostMapping("/voteResult")
    public String voteResult(@RequestParam String cwd,
                             @RequestParam String signedVoteRequestYaml,
                             @RequestParam String signedVoterYaml,
                             @RequestParam String voterCwd,
                             @RequestParam String voteResultFile) throws Exception {
        teeDataOwnerService.voteResult(cwd, signedVoteRequestYaml, signedVoterYaml, voterCwd, voteResultFile);
        return "已生成对应的投票结果文件"+voteResultFile;
    }

    @PostMapping("/getExportDataKey")
    public String getExportDataKey(@RequestParam String cwd,                         
                                   @RequestParam String yamlFilePath,                         
                                   @RequestParam String partyId,                         
                                   @RequestParam String resourceUri,                         
                                   @RequestParam String dataExportCertificateFile) throws Exception {
        String dataKey = teeDataOwnerService.getExportDataKey(cwd, yamlFilePath, partyId, resourceUri, dataExportCertificateFile);
        return "数据密钥为: "+ dataKey;
    }

}
