package DveBase.mapper;

import DveBase.entity.OutsideDatabaseTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DatabaseTableMapper extends BaseMapper<OutsideDatabaseTable> {
}
