package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("`group`")//防止识别成group语句
public class Group {

  @TableId
  private long uid;
  private long agentId;
  private long centerId;
  private String name;

}
