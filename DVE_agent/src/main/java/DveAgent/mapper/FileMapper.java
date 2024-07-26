package DveAgent.mapper;


import DveAgent.common.Body;
import DveAgent.entity.File;
import DveAgent.info.FileInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileMapper extends BaseMapper<File> {
    FileInfo getFileInfo(@Param("fileId") Long uid, @Param("agentId") Long agentId, @Param("folderId") Long folderId);

    List<File> selectByFolderId(Long uid);
}
