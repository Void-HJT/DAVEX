package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Rule {

  @TableId
  private Long uid;
  private long agentId;
  private long groupId;
  private String allowedMethod;

}
