package DveAgent.mapper;

import DveAgent.entity.Folder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FolderMapper extends BaseMapper<Folder> {
    List<Folder> selectByParentId(@Param("parentId")long uid);

    Folder select(@Param("id") Integer rootFolderId);
}
