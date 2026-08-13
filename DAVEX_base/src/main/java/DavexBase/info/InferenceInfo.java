package DavexBase.info;

import java.util.Map;

import lombok.Data;

@Data
public class InferenceInfo {
    private String agentId;
    private String fileId;
    private String applicationId;

    // 本次推理任务的参数覆盖值；未提供时由 Center 使用 MPC 注册的默认值。
    private Map<String, Object> compileParameters;
    private Map<String, Object> runtimeParameters;

    public InferenceInfo(InferenceInfo other) {
        this.agentId = other.agentId;
        this.fileId = other.fileId;
        this.applicationId = other.applicationId;
        this.compileParameters = other.compileParameters;
        this.runtimeParameters = other.runtimeParameters;
    }

    public InferenceInfo() {

    }
}
