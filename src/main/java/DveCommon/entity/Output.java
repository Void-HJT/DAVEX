package DveCommon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Output {

    @TableId(type = IdType.AUTO)
    private long uid;
    private String name;
    private String type;
    private java.sql.Timestamp uploadDate;
    private String tag;
    private long size;
    private String description;
    private String path;
    private java.sql.Timestamp expiredTime;
    private String hash;
    private long fileId;
    private long agentId;
    private long applicationId;


    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public java.sql.Timestamp getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(java.sql.Timestamp uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public java.sql.Timestamp getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(java.sql.Timestamp expiredTime) {
        this.expiredTime = expiredTime;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
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
}
