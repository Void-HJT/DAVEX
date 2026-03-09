package DavexCenter.goSdk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("go_privacy_vp_record")
public class GoPrivacyVpRecord {
    @TableId(type = IdType.AUTO)
    private Long uid;
    private String vpId;
    private String groupId;
    private String holderKeyImage;
    private String credentialType;
    private String issuerDid;
    private String challenge;
    private String vpJson;
    private Integer localVerifyValid;
    private Integer chainVerifyValid;
    private String verifyResultJson;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
