package DavexCenter.module.TEE;

import DavexBase.common.CertificateGenerator;
import DavexBase.service.TEE.TEEDataOwnerService;

import DavexBase.service.TEE.TEEMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@RestController
@RequestMapping("/TEETask")
public class TEETaskController {

    @Autowired
    private CertificateGenerator certificateGenerator;

    @Autowired
    private TEEDataOwnerService teeDataOwnerService;

    @Autowired
    private TEEMachineService teeMachineService;

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

    @PostMapping("/uploadEncryptedData")
    public String uploadEncryptedData(@RequestParam String localAgentEnc, @RequestParam String localCenterEnc, @RequestParam String testPath){
        try {
            teeMachineService.uploadEncryptedData(localAgentEnc,localCenterEnc,testPath);
            return "加密数据文件成功上传";
        } catch (Exception e) {
            return "上传失败";
        }
    }

    @PostMapping("/uploadJsonKeyCert")
    public String uploadJsonKeyCert(@RequestParam("file") MultipartFile file,
                                    @RequestParam String privateKeyPath,
                                    @RequestParam String certFilePath,
                                    @RequestParam String testPath){
         try {
            // 将 MultipartFile 转换为临时文件
            File tempFile = convertMultiPartToFile(file);
            teeMachineService.uploadJsonKeyCert(tempFile.getAbsolutePath(), privateKeyPath, certFilePath,testPath);
            tempFile.delete(); // 删除临时文件
            return "上传成功 ";
        } catch (Exception e) {
            return "上传失败: " + e.getMessage();
        }               

    }

    @PostMapping("/signTask")
    public String signTask(@RequestParam String certPath, 
                           @RequestParam String prikeyPath, 
                           @RequestParam String taskConfigPath, 
                           @RequestParam String scope, 
                           @RequestParam String teeTaskConfigPath,
                           @RequestParam String testPath) throws Exception {
        teeMachineService.signTask(certPath, prikeyPath, taskConfigPath, scope, teeTaskConfigPath,testPath);
        return "签名成功";
    }

    @PostMapping("/runTrustedApp")
    public String runTrustedApp(@RequestParam String teeTaskConfigPath,
                                @RequestParam String testPath) throws Exception {
        teeMachineService.runTrustedApp(teeTaskConfigPath, testPath);

        return "执行成功";
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
        return voteRequestSignature;
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
        return "数据密钥为:"+ dataKey;
    }

    @PostMapping("/decryptFile")
    public String decryptFile(@RequestParam String cwd,
                              @RequestParam String exportDataKey,
                              @RequestParam String sourceFile,
                              @RequestParam String destFile) throws Exception {
        teeDataOwnerService.decryptFile(cwd,exportDataKey,sourceFile,destFile);
        return "已成功解密"+destFile;
    }

    private File convertMultiPartToFile(MultipartFile file) throws Exception {
        File convFile = File.createTempFile("upload_", "_" + file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }


}
