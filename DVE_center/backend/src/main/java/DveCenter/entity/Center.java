package DveCenter.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Center {

  @TableId
  private long uid;

  private String name;
  private String ip;
  private long port;
  private String crt;
  private java.sql.Timestamp lastUpdated;
  private String description;


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


  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }


  public long getPort() {
    return port;
  }

  public void setPort(long port) {
    this.port = port;
  }


  public String getCrt() {
    return crt;
  }

  public void setCrt(String crt) {
    this.crt = crt;
  }


  public java.sql.Timestamp getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(java.sql.Timestamp lastUpdated) {
    this.lastUpdated = lastUpdated;
  }


  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
