package DavexBase.info;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

@Data
public class DirectoryInfo {
    private Long uid;
    private Long agentId;
    private Long parentId;
    private String name;
    private String type; // "file" or "folder"
    private java.sql.Timestamp createDate;
    private java.sql.Timestamp lastUpdate;
    private List<DirectoryInfo> children = new ArrayList<>();

    // File specific fields
    private JSONObject attribute;
    private Long size;
    private String description;
    private String hash;
    private String example;
    private java.sql.Timestamp expiredTime;
    // 规则
    private List<String> ruleList = new ArrayList<>();
}
