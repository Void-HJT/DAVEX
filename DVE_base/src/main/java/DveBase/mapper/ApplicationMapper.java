package DveBase.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import DveBase.entity.Application;
import DveBase.info.ApplicationInfo;

@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {
    List<ApplicationInfo> getApplicationInfo(@Param("agentId") Long agentId,@Param("centerId") Long centerId);
}
