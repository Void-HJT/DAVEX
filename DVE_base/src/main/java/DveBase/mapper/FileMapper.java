package DveBase.mapper;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import DveBase.entity.File;
import DveBase.info.FileInfo;

@Mapper
public interface FileMapper extends BaseMapper<File> {
    FileInfo getFileInfo(@Param("fileId") Long uid, @Param("agentId") Long agentId, @Param("folderId") Long folderId);

    List<File> selectByFolderId(Long uid);
}
