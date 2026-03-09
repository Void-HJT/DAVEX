package DavexCenter.goSdk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("go_did_record")
public class GoDidRecord {
    @TableId(type = IdType.AUTO)
    private Long uid;
    private String did;
    private String didDocument;
    private String txId;
    private Long blockHeight;
    private String chainResult;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
