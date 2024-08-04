package DveCommon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class File {

  @TableId(type = IdType.AUTO)
  private Long uid;

  private Long agentId;
  private Long folderId;

  private String name;
  private java.sql.Timestamp createDate;
  private java.sql.Timestamp lastUpdate;
  private String tag;
  private Long size;
  private String description;
  private java.sql.Timestamp expiredTime;
  private String hash;
  private String example;
  private String type;

}
