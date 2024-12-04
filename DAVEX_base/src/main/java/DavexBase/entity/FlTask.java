package DavexBase.entity;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@EntityScan
@TableName(value = "flTask", autoResultMap = true)
public class FlTask {
    @TableId(type = IdType.ASSIGN_UUID)
    private String uid;
    private String applicationId;
    private String centerId;
    @JsonProperty("N")  // 映射 JSON 中的 "N" 到 Java 中的 "N"
    private Integer N;
    private Integer part;
    private String host;
    private Integer port;
    private String dataId;
    private String taskName;
    private String resultPath;
    private String status;
    private String message;

    public FlTask(FlTask other){
        this.uid = other.uid;
        this.applicationId = other.applicationId;
        this.centerId = other.centerId;
        this.N = other.N;
        this.part = other.part;
        this.host = other.host;
        this.port = other.port;
        this.dataId = other.dataId;
        this.taskName = other.taskName;
        this.resultPath = other.resultPath;
        this.status = other.status;
        this.message = other.message;
    }
    public FlTask() {
    }
}


