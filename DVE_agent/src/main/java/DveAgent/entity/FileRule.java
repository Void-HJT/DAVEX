package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class FileRule {

  @TableId
  private Long uid;
  private long agentId;
  private long fileId;
  private long ruleId;

}
