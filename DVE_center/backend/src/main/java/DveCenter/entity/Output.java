package DveCenter.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Output {

    @TableId
    private long uid;
    private String name;
    private String type;
    private java.sql.Timestamp downloadDate;
    private String tag;
    private long size;
    private String description;
    private String path;
    private java.sql.Timestamp expiredTime;


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

    public java.sql.Timestamp getDownloadDate() {
        return downloadDate;
    }

    public void setDownloadDate(java.sql.Timestamp downloadDate) {
        this.downloadDate = downloadDate;
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
}
