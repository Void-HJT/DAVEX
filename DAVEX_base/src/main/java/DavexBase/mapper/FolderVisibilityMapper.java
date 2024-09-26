package DavexBase.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import DavexBase.entity.FolderVisibility;

@Mapper
public interface FolderVisibilityMapper extends BaseMapper<FolderVisibility> {
    @Select("SELECT v.expression " +
            "FROM folder_visibility fv " +
            "JOIN visibility v ON fv.visibility_id = v.uid " +
            "WHERE fv.folder_id = #{folderId}")
    List<String> getRuleExpressions(@Param("folderId") String folderId);
}
