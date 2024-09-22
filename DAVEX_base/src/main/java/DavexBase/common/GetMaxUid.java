package DavexBase.common;

import DavexBase.common.HandleUid;
import DavexBase.entity.Application;
import DavexBase.entity.File;
import DavexBase.entity.Folder;
import DavexBase.mapper.ApplicationMapper;
import DavexBase.mapper.FileMapper;
import DavexBase.mapper.FolderMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class GetMaxUid {

    public Integer getFolderMaxUid(String agentId,FolderMapper folderMapper) {
        //
        List<Object> uidList = folderMapper.selectObjs(new QueryWrapper<Folder>().select("uid")
                .eq("agent_id", agentId));
        // 遍历uidList中的每个uid
        return getMaxTailNumber(uidList);
    }

    public Integer getApplicationMaxUid(String centerId,ApplicationMapper applicationMapper){
        //
        List<Object> uidList = applicationMapper.selectObjs(new QueryWrapper<Application>().select("uid")
                .eq("center_id",centerId));

        return getMaxTailNumber(uidList);
    }

    public Integer getFileMaxUid(String agentId, FileMapper fileMapper){
        //
        List<Object> uidList = fileMapper.selectObjs(new QueryWrapper<File>().select("uid")
                .eq("agent_id",agentId));

        return getMaxTailNumber(uidList);
    }


    public Integer getMaxTailNumber(List<Object> uidList){
        int maxTailNumber = 0; // 初始化最大尾部数字
        for (Object obj : uidList) {
            if (obj instanceof String) {
                String uid = (String) obj;
                // 利用UidParser解析uid并获取数字
                HandleUid parser = new HandleUid(uid);
                int[] numbers = parser.getNumbers();
                // 获取最后一个数字（尾部数字）
                if (numbers.length > 0) {
                    int tailNumber = numbers[numbers.length - 1];  // 尾部的数字
                    // 比较更新最大尾部数字
                    if (tailNumber > maxTailNumber) {
                        maxTailNumber = tailNumber;
                    }
                }
            }
        }
        return maxTailNumber;
    }
}
