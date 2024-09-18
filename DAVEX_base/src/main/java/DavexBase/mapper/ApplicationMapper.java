package DavexBase.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import DavexBase.entity.Application;
import DavexBase.info.ApplicationInfo;

@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {
    List<ApplicationInfo> getApplicationInfo(@Param("agentId") String agentId,@Param("centerId") String centerId);
}
