package DveCommon.mapper;

import DveAgent.entity.Application;
import DveAgent.info.ApplicationInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {
    List<ApplicationInfo> getApplicationInfo(@Param("agentId") Long agentId,@Param("centerId") Long centerId);
}
