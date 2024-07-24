package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Folder {

  @TableId
  private long uid;
  private long agentId;
  private long parentId;
  private String name;
  private java.sql.Timestamp createDate;
  private java.sql.Timestamp lastUpdate;
}
