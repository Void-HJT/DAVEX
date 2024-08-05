package DveBase.entity;

import org.springframework.boot.autoconfigure.domain.EntityScan;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;

import lombok.Data;

@Data
@EntityScan
@TableName(value = "mpcTask", autoResultMap = true)
public class MpcTask {

  public enum TaskType {
    GARNET_MPC, GARNET_PSI
  }

  public enum Status {
    INIT, COMPILING, READY, RUNNING, FINISHED, FAILED
  }

  @TableId(type = IdType.ASSIGN_UUID)
  private String uid;
  private Long applicationId;
  private Long centerId;
  private String mpcId;
  @TableField(typeHandler = FastjsonTypeHandler.class)
  private JSONObject compileParameters;
  @TableField(typeHandler = FastjsonTypeHandler.class)
  private JSONObject runtimeParameters;
  private Integer N;
  private Integer part;
  private String host;
  private Integer port;
  private Long dataId;
  private String mpcName;
  private TaskType taskType;
  private Status status;
}
