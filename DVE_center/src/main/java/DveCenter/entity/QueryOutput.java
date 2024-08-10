package DveCenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class QueryOutput {
    @TableId(type = IdType.AUTO)
    private Long uid;
    private String hash;
    private String path;
    private java.sql.Timestamp uploadDate;
    private Long applicationId;
    private java.sql.Timestamp expiredTime;
    private String name;


    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public java.sql.Timestamp getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(java.sql.Timestamp uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public java.sql.Timestamp getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(java.sql.Timestamp expiredTime) {
        this.expiredTime = expiredTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
