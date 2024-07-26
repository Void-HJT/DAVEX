package DveCenter.info;

import DveAgent.info.UploadAgentTaskInfo;

public class UploadCenterTaskInfo extends UploadAgentTaskInfo {
    private Long inputID;

    public Long getInputID() {
        return inputID;
    }

    public void setInputID(Long inputID) {
        this.inputID = inputID;
    }
}
