package DveAgent.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import DveAgent.info.CompileParameter;
import DveAgent.serializer.CompileParameterListTypeHandler;
import lombok.Data;

@Data
@TableName(autoResultMap = true)
public class Mpc {

  @TableId(type = IdType.AUTO)
  private Long uid;
  private String name;
  @TableField(typeHandler = CompileParameterListTypeHandler.class)
  private List<CompileParameter> parameters;
  private Long centerId;
  private String path;

  public void setParameters(List<CompileParameter> parameters) throws Exception {
    Set<Integer> s = new HashSet<>();
    for (CompileParameter parameter : parameters) {
      if (parameter.getParameterType() != CompileParameter.CompileParameterType.ARG) {
        continue;
      }
      if (s.contains((Integer) parameter.getValue())) {
        throw new Exception("参数重复");
      }
      s.add((Integer) parameter.getValue());
    }
    for (int i = 0; i < s.size(); i++) {
      if (!s.contains(i)) {
        throw new Exception("参数缺失");
      }
    }
    this.parameters = parameters;
  }

}
