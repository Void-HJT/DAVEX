package DavexBase.info;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

@Data
public class DirectoryInfo {
    private String uid;
    private String agentId;
    private String parentId;
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
    private String fileType;
    // 规则
    private List<String> ruleList = new ArrayList<>();
}
