package DveAgent.mapper;

import DveAgent.entity.Application;
import DveAgent.entity.Group;
import DveAgent.info.ApplicationInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.util.List;

@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {
    List<ApplicationInfo> getApplicationInfo(@Param("agentId") long agentId,@Param("centerId") long centerId);
}
