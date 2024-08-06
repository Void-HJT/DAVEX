package DveBase.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import DveBase.entity.Folder;

@Mapper
public interface FolderMapper extends BaseMapper<Folder> {
    List<Folder> selectByParentId(@Param("parentId")Long uid);

    Folder select(@Param("id") Long rootFolderId);
}
