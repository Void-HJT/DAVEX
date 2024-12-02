package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("keys_storage")
public class KeysStorage {

    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;

    private String ownerName; // 用户唯一标识

    private String publicKey; // 公钥（Base64编码形式）
}
