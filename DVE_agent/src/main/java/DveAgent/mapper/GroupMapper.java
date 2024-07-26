package DveAgent.mapper;


import DveAgent.entity.Group;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GroupMapper extends BaseMapper<Group> {

    List<Group> getList(@Param("agentId") Long agentId,
                        @Param("centerId") Long centerId,
                        @Param("applicationId") Long applicationId);
}
