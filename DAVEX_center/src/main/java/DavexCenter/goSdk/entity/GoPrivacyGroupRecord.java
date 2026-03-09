package DavexCenter.goSdk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("go_privacy_group_record")
public class GoPrivacyGroupRecord {
    @TableId(type = IdType.AUTO)
    private Long uid;
    private String groupId;
    private String groupName;
    private String issuerDid;
    private String credentialType;
    private String attributePolicy;
    private Integer minRingSize;
    private Integer memberCount;
    private String memberPublicKeysJson;
    private String txId;
    private Long blockHeight;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
