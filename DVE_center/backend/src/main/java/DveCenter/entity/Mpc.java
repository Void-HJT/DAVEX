package DveCenter.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Mpc {

  @TableId
  private long uid;
  private long centerId;
  private long path;
  private long status;


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


  public long getPath() {
    return path;
  }

  public void setPath(long path) {
    this.path = path;
  }


  public long getStatus() {
    return status;
  }

  public void setStatus(long status) {
    this.status = status;
  }

}
