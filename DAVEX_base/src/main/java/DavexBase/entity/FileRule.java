package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class FileRule {

  @TableId(type = IdType.AUTO)
  private String uid;
  private String fileId;
  private String ruleId;

}
