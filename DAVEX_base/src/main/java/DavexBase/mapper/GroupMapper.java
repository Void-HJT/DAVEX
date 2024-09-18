package DavexBase.mapper;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import DavexBase.entity.Group;

@Mapper
public interface GroupMapper extends BaseMapper<Group> {

    List<Group> getList(@Param("agentId") String agentId,
                        @Param("centerId") String centerId,
                        @Param("applicationId") String applicationId);
}
