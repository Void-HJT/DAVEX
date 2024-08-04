package DveCommon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Folder {

  @TableId(type = IdType.AUTO)
  private Long uid;
  private Long agentId;
  private Long parentId;
  private String name;
  private java.sql.Timestamp createDate;
  private java.sql.Timestamp lastUpdate;
}
