package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class Rule {
    @TableId
    private String uid;
    private String expression;
    private String description;

}
