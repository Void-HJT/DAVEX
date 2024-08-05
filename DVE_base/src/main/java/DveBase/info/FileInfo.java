package DveBase.info;

import java.util.List;

import DveBase.entity.File;
import DveBase.entity.Rule;

public class FileInfo extends File {
    private List<Rule> ruleList;

    public List<Rule> getRuleList() {
        return ruleList;
    }

    public void setRuleList(List<Rule> ruleList) {
        this.ruleList = ruleList;
    }

}
