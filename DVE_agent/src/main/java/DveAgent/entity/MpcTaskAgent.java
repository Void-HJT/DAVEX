package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("mpcTask_agent")
public class MpcTaskAgent {

  @TableId(value = "uid", type = IdType.AUTO)
  private long uid;
  // 去数据库里修改表名
  @TableField("mpcTask_id")
  private long mpcTaskId;
  private long agentId;
  private long part;

}
