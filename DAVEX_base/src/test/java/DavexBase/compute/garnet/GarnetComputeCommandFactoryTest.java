package DavexBase.compute.garnet;

import DavexBase.compute.adapter.DockerComputeCommand;
import DavexBase.entity.Mpc;
import DavexBase.entity.MpcTask;
import DavexBase.info.Parameter;
import DavexBase.properties.GarnetProperties;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 阶段 6.4.2 测试：锁定Garnet编译和运行参数到Docker命令的转换，
 * 命令生成过程不访问数据库、不启动容器，也不修改任务状态。
 */
class GarnetComputeCommandFactoryTest {

    private GarnetComputeCommandFactory factory;

    @BeforeEach
    void setUp() {
        GarnetProperties properties = new GarnetProperties();
        properties.setContainerID("garnet-container");
        factory = new GarnetComputeCommandFactory(properties);
    }

    @Test
    void createsCompileCommandAndDerivedMpcName() throws Exception {
        Mpc mpc = new Mpc();
        mpc.setPath("programs/demo.mpc");
        mpc.setCompileParameters(List.of(
                parameter("prime", Parameter.ArgumentsType.POS, 0, true),
                parameter("mode", Parameter.ArgumentsType.FLAG, "--mode", false)));

        MpcTask task = task();
        JSONObject parameters = new JSONObject();
        parameters.put("prime", "128");
        parameters.put("mode", "fast");
        task.setCompileParameters(parameters);

        GarnetPreparedCommand prepared = factory.compile(task, mpc);
        DockerComputeCommand command = prepared.command();

        assertEquals("demo-128", prepared.mpcName());
        assertEquals("TASK-1", command.taskId());
        assertEquals("garnet-container", command.containerId());
        assertEquals("/usr/src/Garnet", command.workingDirectory());
        assertEquals(List.of(
                "python3", "compile.py", "Programs/DAVEX/demo.mpc",
                "128", "--mode fast"), command.arguments());
    }

    @Test
    void rejectsMissingRequiredCompileParameter() throws Exception {
        Mpc mpc = new Mpc();
        mpc.setPath("programs/demo.mpc");
        mpc.setCompileParameters(List.of(
                parameter("prime", Parameter.ArgumentsType.POS, 0, true)));

        MpcTask task = task();
        task.setCompileParameters(new JSONObject());

        assertThrows(IllegalArgumentException.class,
                () -> factory.compile(task, mpc));
    }

    @Test
    void createsRunCommandForReplicatedProtocol() {
        MpcTask task = task();
        task.setMpcName("demo-128");
        task.setHost("10.0.0.1");
        task.setPort(5000);
        task.setPart(1L);
        JSONObject runtime = new JSONObject();
        runtime.put("protocol", "replicated-ring-party");
        task.setRuntimeParameters(runtime);

        DockerComputeCommand command = factory.run(task);

        assertEquals(List.of(
                "./replicated-ring-party.x",
                "-IF", "/usr/src/Garnet/Inputs/TASK-1",
                "-OF", "/usr/src/Garnet/Outputs/TASK-1",
                "-h", "10.0.0.1",
                "-pn", "5000",
                "-p", "1",
                "-u", "demo-128"), command.arguments());
    }

    private MpcTask task() {
        MpcTask task = new MpcTask();
        task.setUid("TASK-1");
        return task;
    }

    private Parameter parameter(
            String name,
            Parameter.ArgumentsType type,
            Object positionOrFlag,
            boolean required) {
        return new Parameter(
                name,
                type,
                Parameter.LimitType.STRING,
                positionOrFlag,
                new Parameter.STRINGLimit("default"),
                name,
                required);
    }
}
