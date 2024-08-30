package DavexBase.info;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import DavexBase.entity.MpcTask;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadAgentTaskInfo extends MpcTask {
    public static class PartInfo {
        private Long agentID;
        private Long part;
        private Long fileID;

        public PartInfo() {
        }

        public PartInfo(Long agentID, Long part, Long fileID) {
            this.agentID = agentID;
            this.part = part;
            this.fileID = fileID;
        }

        public Long getAgentID() {
            return agentID;
        }

        public void setAgentID(Long agent_id) {
            this.agentID = agent_id;
        }

        public Long getPart() {
            return part;
        }

        public void setPart(Long part) {
            this.part = part;
        }

        public Long getFileID() {
            return fileID;
        }

        public void setFileID(Long file_id) {
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
}