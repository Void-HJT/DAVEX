package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ApplicationGroup {

  private long uid;
  private long agentId;
  private long centerId;
  private long applicationId;
  private long groupId;


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


  public long getCenterId() {
    return centerId;
  }

  public void setCenterId(long centerId) {
    this.centerId = centerId;
  }


  public long getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(long applicationId) {
    this.applicationId = applicationId;
  }


  public long getGroupId() {
    return groupId;
  }

  public void setGroupId(long groupId) {
    this.groupId = groupId;
  }

}
