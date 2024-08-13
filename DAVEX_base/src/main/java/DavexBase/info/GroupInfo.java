package DavexBase.info;

import java.util.List;

import DavexBase.entity.Group;
import DavexBase.entity.Rule;

public class GroupInfo extends Group {
    private List<Rule> ruleList;

    public List<Rule> getRuleList() {
        return ruleList;
    }

    public void setRuleList(List<Rule> ruleList) {
        this.ruleList = ruleList;
    }
}
