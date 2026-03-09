package DavexCenter.goSdk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("go_vc_record")
public class GoVcRecord {
    @TableId(type = IdType.AUTO)
    private Long uid;
    private String vcId;
    private String issuerDid;
    private String holderDid;
    private String credentialType;
    private String vcJson;
    private String txId;
    private Long blockHeight;
    private Integer latestVerifyValid;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
