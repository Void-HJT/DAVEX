package DavexBase.info;

import lombok.Data;

@Data
public class InferenceInfo {
    private String agentId;
    private String fileId;
    private String applicationId;

    public InferenceInfo(InferenceInfo other) {
        this.agentId = other.agentId;
        this.fileId = other.fileId;
    }

    public InferenceInfo() {

    }
}
