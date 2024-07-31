package DveAgent.info;

import DveAgent.entity.Group;
import DveAgent.entity.Rule;
import lombok.Data;

import java.util.List;

@Data
public class GroupInfo extends Group {
    private List<Rule> ruleList;
}
