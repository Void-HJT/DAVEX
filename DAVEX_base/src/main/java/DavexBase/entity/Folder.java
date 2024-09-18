package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class Folder {

  @TableId
  private String uid;
  private String agentId;
  private String parentId;
  private String name;
  private java.sql.Timestamp createDate;
  private java.sql.Timestamp lastUpdate;
}
