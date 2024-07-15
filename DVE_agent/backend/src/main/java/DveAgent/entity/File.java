package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class File {

  private long uid;
  private long agentId;

  private long folderId;
  private String name;
  private java.sql.Timestamp createDate;
  private java.sql.Timestamp lastUpdate;
  private String tag;
  private long size;
  private String path;
  private String description;
  private java.sql.Timestamp expiredTime;
  private String hash;
  private String example;
  private String type;


  public long getUid() {
    return uid;
  }

  public void setUid(long uid) {
    this.uid = uid;
  }


  public long getAgentId() {
    return agentId;
  }

  public void setAgentId(long agentId) {
    this.agentId = agentId;
  }


  public long getFolderId() {
    return folderId;
  }

  public void setFolderId(long folderId) {
    this.folderId = folderId;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public java.sql.Timestamp getCreateDate() {
    return createDate;
  }

  public void setCreateDate(java.sql.Timestamp createDate) {
    this.createDate = createDate;
  }


  public java.sql.Timestamp getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(java.sql.Timestamp lastUpdate) {
    this.lastUpdate = lastUpdate;
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


  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }


  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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


  public String getExample() {
    return example;
  }

  public void setExample(String example) {
    this.example = example;
  }


  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

}
