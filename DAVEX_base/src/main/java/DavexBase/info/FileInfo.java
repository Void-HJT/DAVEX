package DavexBase.info;

import java.util.List;

import DavexBase.entity.File;
import DavexBase.entity.Rule;

public class FileInfo extends File {
    private List<Rule> ruleList;

    public List<Rule> getRuleList() {
        return ruleList;
    }

    public void setRuleList(List<Rule> ruleList) {
        this.ruleList = ruleList;
    }

}
