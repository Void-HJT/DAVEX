package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("`group`")//防止识别成group语句
public class Group {

  @TableId(type = IdType.AUTO)
  private Long uid;
  private long agentId;
  private long centerId;
  private String name;

}
