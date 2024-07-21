package DveCenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Task {

    @TableId(type = IdType.AUTO)
    private long uid;
    private long fileId;
    private long agentId;
    private long applicationId;
    private long outputId;
    private java.sql.Timestamp downloadTime;


    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public long getAgentId() {
        return agentId;
    }

    public void setAgentId(long agentId) {
        this.agentId = agentId;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(long applicationId) {
        this.applicationId = applicationId;
    }

    public long getOutputId() {
        return outputId;
    }

    public void setOutputId(long outputId) {
        this.outputId = outputId;
    }

    public java.sql.Timestamp getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(java.sql.Timestamp downloadTime) {
        this.downloadTime = downloadTime;
    }
}
