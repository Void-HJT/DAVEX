package DavexBase.service.TEE;

import DavexBase.common.docker.DockerExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TEEMachineService {
    private final DockerExecutor dockerExecutor;

    public TEEMachineService(DockerExecutor dockerExecutor) {
        this.dockerExecutor = dockerExecutor;
    }

    private static final String CONTAINER_NAME = "teeapps-sim";
    private static final String CONTAINER_TEEAPPS_PATH = "/home/teeapp/sim/teeapps";

    public void uploadJsonKeyCert(String localTaskJsonPath, String privateKeyPath, String certFilePath, String testPath) throws Exception {

        //复制convert.py到测试数据文件夹
        List<String> command0 = new ArrayList<>();
        command0.add("cp");
        command0.add("/host/integration_test/convert.py");
        command0.add(testPath);
        dockerExecutor.exec(CONTAINER_NAME,"/host",command0);

        // 复制 psi.json 到容器
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("cp");
        command.add(localTaskJsonPath);
//        command.add(CONTAINER_NAME + ":" + CONTAINER_INTEGRATION_PATH + "/psi.json");
        command.add(CONTAINER_NAME + ":" + testPath + "psi.json");

        List<String> command1 = new ArrayList<>();
        command1.add("docker");
        command1.add("cp");
        command1.add(privateKeyPath);
//        command1.add(CONTAINER_NAME + ":" + CONTAINER_INTEGRATION_PATH+"/");
        command1.add(CONTAINER_NAME + ":" + testPath);

        List<String> command2 = new ArrayList<>();
        command2.add("docker");
        command2.add("cp");
        command2.add(certFilePath);
        command2.add(CONTAINER_NAME + ":" + testPath);

        ProcessBuilder pb = new ProcessBuilder(command);
        ProcessBuilder pb1 = new ProcessBuilder(command1);
        ProcessBuilder pb2 = new ProcessBuilder(command2);

        pb.start().waitFor();
        pb1.start().waitFor();
        pb2.start().waitFor();

    }

    /**
     * 上传加密数据文件 (agent.csv.enc, center.csv.enc) 到 TEE 容器
     */
    public void uploadEncryptedData(String localAgentEnc, String localCenterEnc, String testPath) throws Exception {
        // 确保 testPath 目录存在
        List<String> createDirCommand = new ArrayList<>();
        createDirCommand.add("docker");
        createDirCommand.add("exec");
        createDirCommand.add(CONTAINER_NAME);
        createDirCommand.add("mkdir");
        createDirCommand.add("-p");
        createDirCommand.add(testPath);

        ProcessBuilder createDirProcess = new ProcessBuilder(createDirCommand);
        createDirProcess.start().waitFor();

        // 复制 agent.csv.enc
        List<String> command1 = new ArrayList<>();
        command1.add("docker");
        command1.add("cp");
        command1.add(localAgentEnc);
        command1.add(CONTAINER_NAME + ":" + testPath);

        // 复制 center.csv.enc
        List<String> command2 = new ArrayList<>();
        command2.add("docker");
        command2.add("cp");
        command2.add(localCenterEnc);
        command2.add(CONTAINER_NAME + ":" + testPath);

        ProcessBuilder pb1 = new ProcessBuilder(command1);
        ProcessBuilder pb2 = new ProcessBuilder(command2);

        pb1.start().waitFor();
        pb2.start().waitFor();

    }

    /**
     * 在容器中对任务进行签名
     */
    public void signTask(String certPath, String prikeyPath, String taskConfigPath, String scope, String teeTaskConfigPath, String testPath) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("python");
        command.add("convert.py");
        command.add("--cert_path");
        command.add(certPath);
        command.add("--prikey_path");
        command.add(prikeyPath);
        command.add("--task_config_path");
        command.add(taskConfigPath);
        command.add("--scope");
        command.add(scope);
        command.add("--capsule_manager_endpoint");
        command.add("127.0.0.1:8888");
        command.add("--tee_task_config_path");
        command.add(teeTaskConfigPath);

        dockerExecutor.exec(CONTAINER_NAME,testPath,command);
    }

    /**
     * 在 TEE 容器中执行可信 app
     */
    public void runTrustedApp(String teeTaskConfigPath, String testPath) throws Exception {
        List<String> command = new ArrayList<>(Arrays.asList("./main", "--plat=sim", "--enable_console_logger=true", "--enable_capsule_tls=false", "--entry_task_config_path=" + testPath+teeTaskConfigPath));

        dockerExecutor.exec(CONTAINER_NAME, CONTAINER_TEEAPPS_PATH,command);
    }

}
