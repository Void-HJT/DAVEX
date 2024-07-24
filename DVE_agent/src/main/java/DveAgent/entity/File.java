package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class File {

  @TableId
  private Long uid;
  private long agentId;
  private long folderId;
  private String name;
  private java.sql.Timestamp createDate;
  private java.sql.Timestamp lastUpdate;
  private String tag;
  private long size;
  private String path;
  private String description;
  private java.sql.Timestamp expiredTime;
  private String hash;
  private String example;
  private String type;

}
