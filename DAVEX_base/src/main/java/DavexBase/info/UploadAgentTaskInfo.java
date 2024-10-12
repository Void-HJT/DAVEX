package DavexBase.info;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import DavexBase.entity.Mpc;
import DavexBase.entity.MpcTask;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadAgentTaskInfo extends MpcTask {
    public static class PartInfo {
        private String agentID;
        private Long part;
        private String fileID;

        public PartInfo() {
        }

        public PartInfo(String agentID, Long part, String fileID) {
            this.agentID = agentID;
            this.part = part;
            this.fileID = fileID;
        }

        public PartInfo(PartInfo other) {
            this.agentID = other.agentID;
            this.part = other.part;
            this.fileID = other.fileID;
        }

        public String getAgentID() {
            return agentID;
        }

        public void setAgentID(String agent_id) {
            this.agentID = agent_id;
        }

        public Long getPart() {
            return part;
        }

        public void setPart(Long part) {
            this.part = part;
        }

        public String getFileID() {
            return fileID;
        }

        public void setFileID(String file_id) {
            this.fileID = file_id;
        }
    }

    private List<PartInfo> partInfo;

    public List<PartInfo> getPartInfo() {
        return partInfo;
    }

    public void setPartInfo(List<PartInfo> partInfo) {
        this.partInfo = partInfo;
    }

    public void maskFileID() {
        for (PartInfo partInfo : this.partInfo) {
            partInfo.setFileID(null);
        }
    }

    public UploadAgentTaskInfo(UploadAgentTaskInfo other) {
        super(other);
        this.partInfo = new ArrayList<>();
        for (PartInfo partInfo : other.partInfo) {
            this.partInfo.add(new PartInfo(partInfo));
        }
    }

    public UploadAgentTaskInfo() {
    }

    public void useDefault(Mpc mpc) throws Exception {
        if (mpc == null) {
            throw new Exception("Mpc is null");
        }
        List<Parameter> compileParameters = mpc.getCompileParameters();
        List<Parameter> runtimeParameters = mpc.getRuntimeParameters();
        JSONObject compileParametersJson = new JSONObject();
        JSONObject runtimeParametersJson = new JSONObject();
        for (Parameter parameter : compileParameters) {
            compileParametersJson.put(parameter.getName(), parameter.getDefaultValue());
        }
        for (Parameter parameter : runtimeParameters) {
            runtimeParametersJson.put(parameter.getName(), parameter.getDefaultValue());
        }
        this.setCompileParameters(compileParametersJson);
        this.setRuntimeParameters(runtimeParametersJson);
    }
}