package DveAgent.mapper;


import DveAgent.common.Body;
import DveAgent.entity.File;
import DveAgent.info.FileInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileMapper extends BaseMapper<File> {
    FileInfo getFileInfo(@Param("fileId") Integer uid, @Param("agentId") Integer agentId, @Param("folderId") Integer folderId);
}
