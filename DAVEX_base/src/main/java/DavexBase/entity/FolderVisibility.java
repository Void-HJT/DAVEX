package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class FolderVisibility {

  @TableId(type = IdType.AUTO)
  private Long uid;

  private String agentId;
  private String folderId;
  private Long groupId;

}
