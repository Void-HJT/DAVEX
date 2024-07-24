package DveAgent.info;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DirectoryInfo {
    private long uid;
    private long agentId;
    private long parentId;
    private String name;
    private String type; // "file" or "folder"
    private java.sql.Timestamp createDate;
    private java.sql.Timestamp lastUpdate;
    private List<DirectoryInfo> children = new ArrayList<>();

    // File specific fields
    private String tag;
    private long size;
    private String path;
    private String description;
    private String hash;
    private String example;
    private java.sql.Timestamp expiredTime;
    //规则
    private List<String> ruleList = new ArrayList<>();
}
