package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class FileRule {

  private long uid;
  private long agentId;

  private long fileId;
  private long ruleId;


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


  public long getFileId() {
    return fileId;
  }

  public void setFileId(long fileId) {
    this.fileId = fileId;
  }


  public long getRuleId() {
    return ruleId;
  }

  public void setRuleId(long ruleId) {
    this.ruleId = ruleId;
  }

}
