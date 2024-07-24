package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class FolderVisibility {

  @TableId
  private Long uid;
  private long agentId;
  private long folderId;
  private long groupId;

}
