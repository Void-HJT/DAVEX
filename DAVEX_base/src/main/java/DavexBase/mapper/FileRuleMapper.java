package DavexBase.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import DavexBase.entity.FileRule;
import DavexBase.entity.Rule;

@Mapper
public interface FileRuleMapper extends BaseMapper<FileRule> {

    @Select("SELECT r.expression " +
            "FROM file_rule fr " +
            "JOIN rule r ON fr.rule_id = r.uid " +
            "WHERE fr.file_id = #{fileId}")
    List<String> getRuleExpressions(@Param("fileId") String fileId);

    @Select("SELECT r.* " +
            "FROM file_rule fr " +
            "JOIN rule r ON fr.rule_id = r.uid " +
            "WHERE fr.file_id = #{fileId}")
    List<Rule> getRules(@Param("fileId") String fileId);
}
