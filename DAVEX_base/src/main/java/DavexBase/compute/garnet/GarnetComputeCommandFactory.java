package DavexBase.compute.garnet;

import DavexBase.compute.adapter.DockerComputeCommand;
import DavexBase.entity.Mpc;
import DavexBase.entity.MpcTask;
import DavexBase.info.Parameter;
import DavexBase.properties.GarnetProperties;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 将Garnet业务任务转换成Docker执行命令。
 * 本类不访问数据库、不启动容器，也不修改任务状态。
 */
@Component
public class GarnetComputeCommandFactory {

    private static final String GARNET_DIRECTORY = "/usr/src/Garnet";
    private static final String INPUT_DIRECTORY = "/usr/src/Garnet/Inputs";
    private static final String OUTPUT_DIRECTORY = "/usr/src/Garnet/Outputs";

    private static final Duration COMPILE_TIMEOUT = Duration.ofMinutes(10);
    private static final Duration RUN_TIMEOUT = Duration.ofHours(24);

    private static final Set<String> USE_U_PROTOCOLS = Set.of("replicated-ring-party");

    private final GarnetProperties properties;

    public GarnetComputeCommandFactory(GarnetProperties properties) {
        this.properties = properties;
    }

    /**
     * 生成Garnet编译命令，并计算运行阶段使用的mpcName。
     */
    public GarnetPreparedCommand compile(MpcTask task, Mpc mpc) {
        if (task == null || mpc == null) {
            throw new IllegalArgumentException("任务和MPC程序不能为空");
        }
        if (mpc.getPath() == null || mpc.getPath().isBlank()) {
            throw new IllegalArgumentException("MPC程序路径不能为空");
        }

        String fileName = Path.of(mpc.getPath())
                .getFileName()
                .toString();
        String baseName = removeExtension(fileName);

        Map<Integer, String> positionalArguments = new TreeMap<>();
        List<String> flags = new ArrayList<>();

        List<Parameter> parameters = mpc.getCompileParameters();
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                addCompileParameter(
                        task,
                        parameter,
                        positionalArguments,
                        flags);
            }
        }

        List<String> arguments = new ArrayList<>();
        arguments.add("python3");
        arguments.add("compile.py");
        arguments.add("Programs/DAVEX/" + fileName);

        StringBuilder mpcName = new StringBuilder(baseName);

        for (String value : positionalArguments.values()) {
            arguments.add(value);
            mpcName.append("-").append(value);
        }

        arguments.addAll(flags);

        DockerComputeCommand command = new DockerComputeCommand(
                task.getUid(),
                COMPILE_TIMEOUT,
                properties.getContainerID(),
                GARNET_DIRECTORY,
                arguments);

        return new GarnetPreparedCommand(
                command,
                mpcName.toString());
    }

    /**
     * 生成Garnet运行命令。
     */
    public DockerComputeCommand run(MpcTask task) {
        if (task == null) {
            throw new IllegalArgumentException("任务不能为空");
        }
        if (task.getRuntimeParameters() == null) {
            throw new IllegalArgumentException("运行参数不能为空");
        }

        String protocol = task.getRuntimeParameters().getString("protocol");

        requireText(protocol, "protocol");
        requireText(task.getMpcName(), "mpcName");
        requireText(task.getHost(), "host");

        if (task.getPort() == null || task.getPart() == null) {
            throw new IllegalArgumentException(
                    "port和part不能为空");
        }

        List<String> arguments = new ArrayList<>(List.of(
                "./" + protocol + ".x",
                "-IF", INPUT_DIRECTORY + "/" + task.getUid(),
                "-OF", OUTPUT_DIRECTORY + "/" + task.getUid(),
                "-h", task.getHost(),
                "-pn", task.getPort().toString(),
                "-p", task.getPart().toString()));

        if (USE_U_PROTOCOLS.contains(protocol)) {
            arguments.add("-u");
        }

        arguments.add(task.getMpcName());

        return new DockerComputeCommand(
                task.getUid(),
                RUN_TIMEOUT,
                properties.getContainerID(),
                GARNET_DIRECTORY,
                arguments);
    }

    /**
     * 解析一个编译参数，并保持旧Garnet命令的参数格式。
     */
    private void addCompileParameter(
            MpcTask task,
            Parameter parameter,
            Map<Integer, String> positionalArguments,
            List<String> flags) {

        if (parameter.getParameterType() == Parameter.ArgumentsType.HYPER) {
            return;
        }

        String value = task.getCompileParameters() == null
                ? null
                : task.getCompileParameters()
                        .getString(parameter.getName());

        if (value == null && Boolean.TRUE.equals(
                parameter.getRequired())) {
            throw new IllegalArgumentException(
                    "缺少参数: " + parameter.getName());
        }

        if (value == null) {
            value = parameter.getDefaultValue();
        }

        if (value == null) {
            return;
        }

        switch (parameter.getParameterType()) {
            case POS -> positionalArguments.put(
                    (Integer) parameter.getPosORflag(),
                    value);
            case FLAG -> flags.add(
                    parameter.getPosORflag() + " " + value);
            default -> {
                // HYPER参数不属于编译命令行。
            }
        }
    }

    private String removeExtension(String fileName) {
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex > 0
                ? fileName.substring(0, extensionIndex)
                : fileName;
    }

    private void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    name + " 不能为空");
        }
    }
}