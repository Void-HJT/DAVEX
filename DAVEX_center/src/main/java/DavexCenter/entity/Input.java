package DavexCenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class Input {

    @TableId(type = IdType.AUTO)
    private Long uid;
    private Long applicationId;
    private String path;
}
