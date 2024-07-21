package DveAgent.mapper;

import DveAgent.entity.MpcTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MpcTaskMapper extends BaseMapper<MpcTask> {
    @Update("UPDATE mpcTask SET name = #{name}, description = #{description} WHERE id = #{id}")
    boolean updateMpcTask(MpcTask mpcTask);

    @Insert("INSERT INTO mpcTask(name, description) VALUES(#{name}, #{description})")
    boolean insertMpcTask(MpcTask mpcTask);

    @Select("SELECT COUNT(*) FROM mpcTask WHERE uid = #{mpcTaskId}")
    int countByMpcTaskId(@Param("mpcTaskId") Long uid);
}
