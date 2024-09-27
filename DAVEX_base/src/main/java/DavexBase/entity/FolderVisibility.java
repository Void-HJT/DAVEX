package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class FolderVisibility {

    @TableId
    private String uid;
    private String folderId;
    private String visibilityId;

}
