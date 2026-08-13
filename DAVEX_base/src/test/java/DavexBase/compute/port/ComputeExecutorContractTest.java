package DavexBase.compute.port;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段 6.2 架构测试：锁定外部计算的生命周期端口和最小公共模型，
 * 防止业务 Service 继续直接依赖 Docker、ProcessBuilder 或具体计算运行时。
 */
class ComputeExecutorContractTest {

    @Test
    void computeExecutorExposesCompleteExecutionLifecycle() throws Exception {
        Class<?> command = Class.forName("DavexBase.compute.model.ComputeCommand");
        Class<?> executionId = Class.forName("DavexBase.compute.model.ExecutionId");
        Class<?> executionStatus = Class.forName("DavexBase.compute.model.ExecutionStatus");
        Class<?> executionResult = Class.forName("DavexBase.compute.model.ExecutionResult");
        Class<?> executor = Class.forName("DavexBase.compute.port.ComputeExecutor");

        assertTrue(command.isInterface(), "ComputeCommand 必须是运行时命令的公共接口");
        assertTrue(executor.isInterface(), "ComputeExecutor 必须是外部计算端口接口");
        assertReturnType(command, "taskId", String.class);
        assertReturnType(command, "timeout", Duration.class);
        assertReturnType(executor, "prepare", void.class, command);
        assertReturnType(executor, "start", executionId, command);
        assertReturnType(executor, "status", executionStatus, executionId);
        assertReturnType(executor, "cancel", void.class, executionId);
        assertReturnType(executor, "collect", executionResult, executionId);
    }

    @Test
    void executionModelsDescribeIdentityStatusAndProcessOutput() throws Exception {
        Class<?> executionId = Class.forName("DavexBase.compute.model.ExecutionId");
        Class<?> executionStatus = Class.forName("DavexBase.compute.model.ExecutionStatus");
        Class<?> executionResult = Class.forName("DavexBase.compute.model.ExecutionResult");

        assertRecordComponents(executionId,
                "value:java.lang.String");
        assertRecordComponents(executionResult,
                "executionId:DavexBase.compute.model.ExecutionId",
                "status:DavexBase.compute.model.ExecutionStatus",
                "exitCode:java.lang.Integer",
                "stdout:java.lang.String",
                "stderr:java.lang.String");

        Set<String> statuses = Arrays.stream(executionStatus.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.toSet());
        assertEquals(Set.of("PREPARING", "RUNNING", "SUCCEEDED", "FAILED", "CANCELLED", "TIMED_OUT"),
                statuses);
    }

    private void assertReturnType(
            Class<?> owner, String methodName, Class<?> returnType, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = owner.getMethod(methodName, parameterTypes);
        assertEquals(returnType, method.getReturnType());
    }

    private void assertRecordComponents(Class<?> type, String... expectedComponents) {
        assertTrue(type.isRecord(), type.getSimpleName() + " 必须是不可变 record");
        Set<String> actual = Arrays.stream(type.getRecordComponents())
                .map(this::describe)
                .collect(Collectors.toSet());
        assertEquals(Set.of(expectedComponents), actual);
    }

    private String describe(RecordComponent component) {
        return component.getName() + ":" + component.getType().getName();
    }
}
