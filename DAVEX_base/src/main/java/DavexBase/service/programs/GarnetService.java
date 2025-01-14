package DavexBase.service.programs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.command.InspectContainerResponse.Mount;

import DavexBase.common.My;
import DavexBase.common.Utils;
import DavexBase.common.docker.DockerExecutor;
import DavexBase.entity.Mpc;
import DavexBase.entity.MpcTask;
import DavexBase.info.Parameter;
import DavexBase.mapper.MpcMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.properties.GarnetProperties;

@Service
@ConditionalOnProperty(name = "garnet.enabled", havingValue = "true")
public class GarnetService {

    private final My my;

    private final GarnetProperties garnetProperties;

    private final DockerExecutor dockerExecutor;

    private static final Logger logger = LoggerFactory.getLogger(GarnetService.class);

    public final String Docker_Garnet_Path = "/usr/src/Garnet";
    public final String Docker_Input_Path = "/usr/src/Garnet/Inputs";
    public final String Docker_Output_Path = "/usr/src/Garnet/Outputs";
    public final String Docker_Mpc_Path = "/usr/src/Garnet/Programs/DAVEX";

    @Autowired
    private MpcMapper mpcMapper;

    @Autowired
    private MpcTaskMapper mpcTaskMapper;

    public GarnetService(My my, GarnetProperties garnetProperties, DockerExecutor dockerExecutor) {
        this.my = my;
        this.garnetProperties = garnetProperties;
        this.dockerExecutor = dockerExecutor;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Async("customExecutor")
    public void init() {
        try {
            var containerInfo = dockerExecutor.get_info(garnetProperties.getContainerID());
            if (containerInfo.getState() == null && !containerInfo.getState().getRunning()) {
                logger.error("Garnet容器未启动");
                return;
            }
            if (!containerInfo.getConfig().getImage().equals("garnet")) {
                logger.error("Garnet容器镜像不正确");
                return;
            }

            List<Mount> mounts = containerInfo.getMounts();
            StringBuilder errorMessages = new StringBuilder();
            checkMount(mounts, Docker_Input_Path, garnetProperties.getInputPath(), errorMessages);
            checkMount(mounts, Docker_Output_Path, garnetProperties.getOutputPath(), errorMessages);
            checkMount(mounts, Docker_Mpc_Path, garnetProperties.getMpcPath(), errorMessages);

            if (errorMessages.length() > 0) {
                logger.error(errorMessages.toString());
                logger.error("Mounts: " + containerInfo.getMounts().toString() + "\n");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Container Name: ").append(containerInfo.getName()).append("\n");
            sb.append("Image: ").append(containerInfo.getConfig().getImage()).append("\n");
            sb.append("State: ").append(containerInfo.getState().getStatus()).append("\n");
            sb.append("Mounts: ").append(containerInfo.getMounts().toString()).append("\n");
            logger.info("连接到容器\n{}", sb.toString());
        } catch (Exception e) {
            logger.error("Garnet容器不存在 {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private static void checkMount(List<Mount> mounts, String containerPath, String expectedHostPath,
            StringBuilder errorMessages) {
        boolean found = false;

        for (Mount mount : mounts) {
            if (mount.getDestination().toString().equals(containerPath)) {
                found = true;
                if (!mount.getSource().equals(expectedHostPath)) {
                    errorMessages.append("Error: ").append(containerPath)
                            .append(" is mounted to ").append(mount.getSource())
                            .append(", expected ").append(expectedHostPath).append(".\n");
                }
                break;
            }
        }

        if (!found) {
            errorMessages.append("Error: ").append(containerPath)
                    .append(" is not mounted.\n");
        }
    }

    public void idExtract(String inputPath, String prefix, Long part) throws Exception {
        Path input = Paths.get(my.getBase_path()).resolve(inputPath);
        Path outputFilePath = Paths.get(garnetProperties.getInputPath()).resolve(prefix + "-P" + part + "-0");
        try (BufferedReader reader = Files.newBufferedReader(input);
                BufferedWriter writer = Files.newBufferedWriter(outputFilePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int intValue;
                try {
                    intValue = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    intValue = Utils.hashStringToInt(line);
                }
                writer.write(String.valueOf(intValue));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void link(String path, String prefix, Long part) throws Exception {
        Path originPath = Paths.get(path);
        Path destPath = Paths
                .get(Paths.get(garnetProperties.getInputPath()).resolve(prefix).toString() + "-P" + part + "-0");
        // List<String> command = new ArrayList<>(Arrays.asList("ln", "-s", path,
        // Paths.get(garnetProperties.getInputPath()).resolve(prefix) + "-P" + part +
        // "-0"));
        // ProcessBuilder processBuilder = new ProcessBuilder(command)
        // .directory(Paths.get(garnetProperties.getInputPath()).toFile());
        try {
            // Todo 测试链接和copy哪个行？
            // Files.copy(originPath, destPath);
            Files.createLink(destPath, originPath);
            // Process process = processBuilder.start();
            // Integer exitcode = process.waitFor();
            // if (exitcode != 0) {
            // try (BufferedReader reader = new BufferedReader(new
            // InputStreamReader(process.getErrorStream()))) {
            // StringBuilder errorMsg = new StringBuilder();
            // String line;
            // while ((line = reader.readLine()) != null) {
            // errorMsg.append(line).append(System.lineSeparator());
            // }
            // if (errorMsg.length() > 0) {
            // logger.error(errorMsg.toString());
            // throw new Exception(errorMsg.toString());
            // }
            // }
            // }
        } catch (Exception e) {
            throw e;
        }

    }

    public void compile(MpcTask mpcTask) throws Exception {
        JSONObject task_parameter = mpcTask.getCompileParameters();
        Mpc mpc = mpcMapper.selectById(mpcTask.getMpcId());
        List<Parameter> mpc_parameters = mpc.getCompileParameters();
        Map<Integer, String> args = new HashMap<>();
        List<String> flags = new ArrayList<>();
        for (Parameter p : mpc_parameters) {
            switch (p.getParameterType()) {
                case POS: {
                    String value = task_parameter.getString(p.getName());
                    if (value == null && p.getRequired()) {
                        throw new IllegalArgumentException("缺少参数: " + p.getName());
                    } else if (value != null) {
                        args.put((Integer) p.getPosORflag(), value);
                    } else if (!p.getRequired()) {
                        args.put((Integer) p.getPosORflag(), p.getDefaultValue());
                    }
                    break;
                }
                case FLAG: {
                    String value = task_parameter.getString(p.getName());
                    if (value == null && p.getRequired()) {
                        throw new IllegalArgumentException("缺少参数: " + p.getName());
                    } else if (value != null) {
                        flags.add((String) p.getPosORflag() + " " + value);
                    } else if (!p.getRequired()) {
                        flags.add((String) p.getPosORflag() + " " + p.getDefaultValue());
                    }
                    break;
                }
                case HYPER:
                default:
                    break;
            }
        }
        Path local_mpc_path = Paths.get(my.getBase_path()).resolve(mpc.getPath());
        String mpc_name = local_mpc_path.getFileName().toString().split("\\.")[0];
        Path mount_mpc_path = Paths.get(garnetProperties.getMpcPath()).resolve(local_mpc_path.getFileName());
        Path docker_mpc_path = Paths.get("Programs/DAVEX").resolve(local_mpc_path.getFileName());
        if (!Files.exists(mount_mpc_path)) {
            Files.copy(local_mpc_path, mount_mpc_path);
        }
        List<String> command = new ArrayList<>(Arrays.asList("python3", "compile.py", docker_mpc_path.toString()));

        for (int i = 0; i < args.size(); i++) {
            command.add(args.get(i));
            mpc_name += "-" + args.get(i);
        }
        command.addAll(flags);
        logger.info("容器：" + garnetProperties.getContainerID());
        logger.info("命令：" + command.toString());
        try {
            mpcTask.setStatus(MpcTask.Status.COMPILING);
            logger.info(mpcTask.getUid() + ":开始编译");
            mpcTaskMapper.updateById(mpcTask);
            String output = dockerExecutor.exec(garnetProperties.getContainerID(), Docker_Garnet_Path, command);
            logger.info(mpcTask.getUid() + ":命令输出: " + output);
            mpcTask.setStatus(MpcTask.Status.READY);
            mpcTask.setMpcName(mpc_name);
            mpcTaskMapper.updateById(mpcTask);
            logger.info(mpcTask.getUid() + ":编译成功");
        } catch (Exception e) {
            mpcTask.setStatus(MpcTask.Status.FAILED);
            mpcTaskMapper.updateById(mpcTask);
            logger.error(mpcTask.getUid() + ":编译失败");
            e.printStackTrace();
            throw e;
        }
    }

    public void run(MpcTask mpcTask) throws Exception {
        Path inputPrefix = Paths.get(Docker_Input_Path).resolve(mpcTask.getUid());
        Path outputPrefix = Paths.get(Docker_Output_Path).resolve(mpcTask.getUid());
        String protocol = mpcTask.getRuntimeParameters().getString("protocol");
        String mpc_name = mpcTask.getMpcName();
        Long part = mpcTask.getPart();
        List<String> command = new ArrayList<>(Arrays.asList(
                "./" + protocol + ".x",
                "-IF", inputPrefix.toString(),
                "-OF", outputPrefix.toString(),
                // "-N", mpcTask.getN().toString(),
                "-h", mpcTask.getHost(),
                "-pn", mpcTask.getPort().toString(),
                "-p", part.toString(),
                // "-u",
                mpc_name));
        logger.info("容器：" + garnetProperties.getContainerID());
        logger.info("命令：" + command.toString());
        try {
            mpcTask.setStatus(MpcTask.Status.RUNNING);
            logger.info(mpcTask.getUid() + ":开始运行");
            mpcTaskMapper.updateById(mpcTask);
            String output = dockerExecutor.exec(garnetProperties.getContainerID(), Docker_Garnet_Path, command);
            logger.info(mpcTask.getUid() + ":命令输出: " + output);
            mpcTask.setStatus(MpcTask.Status.FINISHED);
            mpcTaskMapper.updateById(mpcTask);
            logger.info(mpcTask.getUid() + ":运行成功");
        } catch (Exception e) {
            mpcTask.setStatus(MpcTask.Status.FAILED);
            mpcTaskMapper.updateById(mpcTask);
            logger.error(mpcTask.getUid() + ":运行失败");
            e.printStackTrace();
            throw e;
        }
    }

    public void csvExtract(String inputCsvPath, String fieldName, String prefix, Long part) {

        String outputFilePath = Paths.get(garnetProperties.getInputPath()).resolve(prefix).toString() + "-P" + part
                + "-0";
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputCsvPath));
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV文件为空");
            }
            String[] headers = headerLine.split(",");
            int fieldIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equals(fieldName)) {
                    fieldIndex = i;
                    break;
                }
            }
            if (fieldIndex == -1) {
                throw new IllegalArgumentException("字段名未找到: " + fieldName);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length > fieldIndex) {
                    String fieldValue = fields[fieldIndex];
                    int intValue;
                    try {
                        intValue = Integer.parseInt(fieldValue);
                    } catch (NumberFormatException e) {
                        intValue = Utils.hashStringToInt(fieldValue);
                    }
                    writer.write(String.valueOf(intValue));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void csvQuery(String inputCsvPath, String prefix, String fieldName, Long part, Path outputCsvPath) {
        Path fieldFilePath = Paths
                .get(Paths.get(garnetProperties.getOutputPath()).resolve(prefix) + "-P" + part + "-0");
        try (BufferedReader csvReader = Files.newBufferedReader(Paths.get(inputCsvPath));
                BufferedReader fieldReader = Files.newBufferedReader(fieldFilePath);
                BufferedWriter csvWriter = Files.newBufferedWriter(outputCsvPath)) {

            // 读取字段文件并存储在集合中
            Set<Integer> fieldValues = new HashSet<>();
            String fieldLine;
            while ((fieldLine = fieldReader.readLine()) != null) {
                try {
                    fieldValues.add(Integer.parseInt(fieldLine));
                } catch (NumberFormatException e) {
                    fieldValues.add(Utils.hashStringToInt(fieldLine));
                }
            }

            // 读取CSV文件头部
            String headerLine = csvReader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV文件为空");
            }
            csvWriter.write(headerLine);
            csvWriter.newLine();
            String[] headers = headerLine.split(",");
            int fieldIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equals(fieldName)) {
                    fieldIndex = i;
                    break;
                }
            }
            if (fieldIndex == -1) {
                throw new IllegalArgumentException("字段名未找到: " + fieldName);
            }
            // 逐行读取CSV文件并匹配字段值
            String line;
            while ((line = csvReader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length > fieldIndex) {
                    String fieldValue = fields[fieldIndex];
                    int intValue;
                    try {
                        intValue = Integer.parseInt(fieldValue);
                    } catch (NumberFormatException e) {
                        intValue = Utils.hashStringToInt(fieldValue);
                    }
                    if (fieldValues.contains(intValue)) {
                        csvWriter.write(line);
                        csvWriter.newLine();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Long csvCount(String inputCsvPath) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(my.getBase_path()).resolve(inputCsvPath))) {
            Long count = reader.lines().count();
            return count;
        } catch (IOException e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
