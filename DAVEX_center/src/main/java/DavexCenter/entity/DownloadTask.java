package DavexCenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class DownloadTask {

    @TableId(type = IdType.AUTO)
    private Long uid;
    private Long applicationId;
    private Long outputId;
    private java.sql.Timestamp downloadTime;
    private String type;
}
