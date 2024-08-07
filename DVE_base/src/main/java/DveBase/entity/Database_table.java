package DveBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Database_table {
    @TableId(type = IdType.AUTO)
    private Long uid;
    private Long database_id;
    private String name;
    private String description;
    private String schema;
    private String example;
}
