package DveCenter.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class MpcTaskAgent {

  @TableId
  private long uid;
  private long mpcTaskId;
  private long agentId;
  private long part;


  public long getUid() {
    return uid;
  }

  public void setUid(long uid) {
    this.uid = uid;
  }


  public long getMpcTaskId() {
    return mpcTaskId;
  }

  public void setMpcTaskId(long mpcTaskId) {
    this.mpcTaskId = mpcTaskId;
  }


  public long getAgentId() {
    return agentId;
  }

  public void setAgentId(long agentId) {
    this.agentId = agentId;
  }


  public long getPart() {
    return part;
  }

  public void setPart(long part) {
    this.part = part;
  }

}
