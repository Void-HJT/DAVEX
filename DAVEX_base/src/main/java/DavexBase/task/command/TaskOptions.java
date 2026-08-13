package DavexBase.task.command;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MPC 任务的编译和运行参数。
 */
public record TaskOptions(
        Map<String, Object> compileParameters,
        Map<String, Object> runtimeParameters) {

    public TaskOptions {
        compileParameters = immutableCopy(compileParameters);
        runtimeParameters = immutableCopy(runtimeParameters);
    }

    private static Map<String, Object> immutableCopy(
            Map<String, Object> parameters) {

        if (parameters == null) {
            return null;
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(parameters));
    }
}
