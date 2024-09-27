package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class FileRule {

    @TableId
    private String uid;
    private String fileId;
    private String ruleId;

}
