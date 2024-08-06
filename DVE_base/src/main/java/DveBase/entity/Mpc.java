package DveBase.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;

import DveBase.info.Parameter;
import DveBase.serializer.ParameterListTypeHandler;
import lombok.Data;

@Data
@TableName(autoResultMap = true)
public class Mpc {

  @TableId(type = IdType.ASSIGN_UUID)
  private String uid;
  private String name;
  @TableField(typeHandler = ParameterListTypeHandler.class)
  private List<Parameter> compileParameters;
  @TableField(typeHandler = ParameterListTypeHandler.class)
  private List<Parameter> runtimeParameters;
  private Long centerId;
  @JsonIgnore
  private String path;

  public void setCompileParameters(List<Parameter> parameters) throws Exception {
    Set<Integer> s = new HashSet<>();
    for (Parameter parameter : parameters) {
      if (parameter.getParameterType() != Parameter.ArgumentsType.POS) {
        continue;
      }
      if (s.contains((Integer) parameter.getPosORflag())) {
        throw new Exception("参数重复");
      }
      s.add((Integer) parameter.getPosORflag());
    }
    for (int i = 0; i < s.size(); i++) {
      if (!s.contains(i)) {
        throw new Exception("参数缺失");
      }
    }
    this.compileParameters = parameters;
  }

}
