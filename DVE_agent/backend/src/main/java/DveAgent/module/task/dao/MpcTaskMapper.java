package DveAgent.module.task.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import DveAgent.entity.MpcTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import java.util.Optional;

public interface MpcTaskMapper extends BaseMapper<MpcTask> {
    // 在这里添加自定义的数据库操作方法
    @Select("SELECT * FROM mpc_task WHERE id = #{id}")
    Optional<MpcTask> getMpcTaskById(@Param("id") Long id);

    @Update("UPDATE mpc_task SET name = #{name}, description = #{description} WHERE id = #{id}")
    boolean updateMpcTask(MpcTask mpcTask);

    @Insert("INSERT INTO mpc_task(name, description) VALUES(#{name}, #{description})")
    boolean insertMpcTask(MpcTask mpcTask);
}