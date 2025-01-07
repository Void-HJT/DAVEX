package DavexBase.common;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import DavexBase.entity.Application;
import DavexBase.entity.File;
import DavexBase.entity.FileRule;
import DavexBase.entity.Folder;
import DavexBase.entity.FolderVisibility;
import DavexBase.entity.Rule;
import DavexBase.entity.Visibility;
import DavexBase.mapper.ApplicationMapper;
import DavexBase.mapper.FileMapper;
import DavexBase.mapper.FileRuleMapper;
import DavexBase.mapper.FolderMapper;
import DavexBase.mapper.FolderVisibilityMapper;
import DavexBase.mapper.RuleMapper;
import DavexBase.mapper.VisibilityMapper;

public class GetMaxUid {

    public static Integer getRuleMaxUid(RuleMapper ruleMapper) {
        List<Object> uidList = ruleMapper.selectObjs(new QueryWrapper<Rule>().select("uid"));
        return getMaxTailNumber(uidList);
    }

    public static Integer getFileRuleMaxUid(FileRuleMapper fileRuleMapper) {
        List<Object> uidList = fileRuleMapper.selectObjs(new QueryWrapper<FileRule>().select("uid"));
        return getMaxTailNumber(uidList);
    }

    public static Integer getVisibilityMaxUid(VisibilityMapper visibilityMapper) {
        List<Object> uidList = visibilityMapper.selectObjs(new QueryWrapper<Visibility>().select("uid"));
        return getMaxTailNumber(uidList);
    }

    public static Integer getFolderVisibilityMaxUid(FolderVisibilityMapper folderVisibilityMapper) {
        List<Object> uidList = folderVisibilityMapper.selectObjs(new QueryWrapper<FolderVisibility>().select("uid"));
        return getMaxTailNumber(uidList);
    }

    public Integer getFolderMaxUid(String agentId, FolderMapper folderMapper) {
        //
        List<Object> uidList = folderMapper.selectObjs(new QueryWrapper<Folder>().select("uid")
                .eq("agent_id", agentId));
        // 遍历uidList中的每个uid
        return getMaxTailNumber(uidList);
    }

    public Integer getApplicationMaxUid(String centerId, ApplicationMapper applicationMapper) {
        //
        List<Object> uidList = applicationMapper.selectObjs(new QueryWrapper<Application>().select("uid")
                .eq("center_id", centerId));

        return getMaxTailNumber(uidList);
    }

    public Integer getFileMaxUid(String agentId, FileMapper fileMapper) {
        //
        List<Object> uidList = fileMapper.selectObjs(new QueryWrapper<File>().select("uid")
                .eq("agent_id", agentId));

        return getMaxTailNumber(uidList);
    }

    public static Integer getMaxTailNumber(List<Object> uidList) {
        int maxTailNumber = 0; // 初始化最大尾部数字
        for (Object obj : uidList) {
            if (obj instanceof String) {
                String uid = (String) obj;
                // 利用UidParser解析uid并获取数字
                HandleUid parser = new HandleUid(uid);
                int[] numbers = parser.getNumbers();
                // 获取最后一个数字（尾部数字）
                if (numbers.length > 0) {
                    int tailNumber = numbers[numbers.length - 1]; // 尾部的数字
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
