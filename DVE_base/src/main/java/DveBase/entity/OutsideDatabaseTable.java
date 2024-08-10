package DveBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class OutsideDatabaseTable {
    @TableId(type = IdType.AUTO)
    private Long uid;

    private Long outsideDatabaseId;
    private String name;
    private String description;
    private String schemaExample;
    private String example;
}
