package DveBase.info;

import java.util.List;

import DveBase.entity.Group;
import DveBase.entity.Rule;

public class GroupInfo extends Group {
    private List<Rule> ruleList;

    public List<Rule> getRuleList() {
        return ruleList;
    }

    public void setRuleList(List<Rule> ruleList) {
        this.ruleList = ruleList;
    }
}
