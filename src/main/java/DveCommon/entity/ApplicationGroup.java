package DveCommon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ApplicationGroup {

  @TableId(type = IdType.AUTO)
  private Long uid;
  private Long agentId;
  private Long centerId;
  private Long applicationId;
  private Long groupId;

}
