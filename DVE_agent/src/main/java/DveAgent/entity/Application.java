package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Application {

  private long uid;
  private long centerId;
  private String name;
  private byte[] crt;
  private java.sql.Timestamp lastUpdated;
  private String description;


  public long getUid() {
    return uid;
  }

  public void setUid(long uid) {
    this.uid = uid;
  }


  public long getCenterId() {
    return centerId;
  }

  public void setCenterId(long centerId) {
    this.centerId = centerId;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public byte[] getCrt() {
    return crt;
  }

  public void setCrt(byte[] crt) {
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
