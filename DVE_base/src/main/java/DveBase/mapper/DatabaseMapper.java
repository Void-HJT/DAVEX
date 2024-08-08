package DveBase.mapper;


import DveBase.entity.OutsideDatabase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DatabaseMapper extends BaseMapper<OutsideDatabase> {
}
