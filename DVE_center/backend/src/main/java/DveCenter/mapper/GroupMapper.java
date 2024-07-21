package DveCenter.mapper;


import DveCenter.entity.Group;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GroupMapper extends BaseMapper<Group> {

    List<Group> getList(@Param("agentId") long agentId,
                        @Param("centerId") long centerId,
                        @Param("applicationId") long applicationId);
}
