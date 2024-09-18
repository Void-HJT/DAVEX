package DavexBase.mapper;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import DavexBase.entity.File;
import DavexBase.info.FileInfo;

@Mapper
public interface FileMapper extends BaseMapper<File> {
    FileInfo getFileInfo(@Param("fileId") String uid, @Param("agentId") String agentId, @Param("folderId") String folderId);

    List<File> selectByFolderId(String uid);
}
