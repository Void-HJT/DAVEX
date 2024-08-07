package DveBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Database {
    @TableId(type = IdType.AUTO)
    private Long uid;
    private String name;
    private String type;
    private String connection;
    private String description;
}
