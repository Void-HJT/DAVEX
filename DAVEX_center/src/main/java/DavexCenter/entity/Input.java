package DavexCenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class Input {

    @TableId(type = IdType.ASSIGN_UUID)
    private String uid;
    private String applicationId;
    private String path;
}
