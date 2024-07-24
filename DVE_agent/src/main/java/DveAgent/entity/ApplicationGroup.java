package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ApplicationGroup {

  @TableId
  private Long uid;
  private long agentId;
  private long centerId;
  private long applicationId;
  private long groupId;

}
