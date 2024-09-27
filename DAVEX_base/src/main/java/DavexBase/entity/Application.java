package DavexBase.entity;

import java.time.LocalDateTime;

import org.springframework.boot.autoconfigure.domain.EntityScan;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;

import lombok.Data;

@Data
@EntityScan
@TableName(value = "application", autoResultMap = true)
public class Application {

    @TableId
    private String uid;

    private String centerId;
    private String name;
    private byte[] cert;
    private LocalDateTime lastUpdated;
    private String description;
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject attribute;

}
