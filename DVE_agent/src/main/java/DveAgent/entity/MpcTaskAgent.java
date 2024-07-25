package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("mpcTask_agent")
public class MpcTaskAgent {

  @TableId()
  private Long uid;
  @TableField("mpcTask_id")
  private long mpcTaskId;
  private long agentId;
  private long part;
  private long fileId;

}



