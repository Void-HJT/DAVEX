package DveAgent.module.task.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import DveAgent.entity.MpcTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import java.util.Optional;

@Mapper
public interface MpcTaskMapper extends BaseMapper<MpcTask> {
    // 在这里添加自定义的数据库操作方法
    // @Select("SELECT * FROM mpcTask WHERE id = #{id}")
    // Optional<MpcTask> getMpcTaskById(@Param("id") Long id);

    @Update("UPDATE mpcTask SET name = #{name}, description = #{description} WHERE id = #{id}")
    boolean updateMpcTask(MpcTask mpcTask);

    @Insert("INSERT INTO mpcTask(name, description) VALUES(#{name}, #{description})")
    boolean insertMpcTask(MpcTask mpcTask);

    @Select("SELECT COUNT(*) FROM mpcTask WHERE uid = #{mpcTaskId}")
    int countByMpcTaskId(@Param("mpcTaskId") Long uid);


}