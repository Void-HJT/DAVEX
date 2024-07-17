package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Rule {

  private long uid;
  private long agentId;
  private long groupId;
  private String allowedMethod;


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


  public long getGroupId() {
    return groupId;
  }

  public void setGroupId(long groupId) {
    this.groupId = groupId;
  }


  public String getAllowedMethod() {
    return allowedMethod;
  }

  public void setAllowedMethod(String allowedMethod) {
    this.allowedMethod = allowedMethod;
  }

}
