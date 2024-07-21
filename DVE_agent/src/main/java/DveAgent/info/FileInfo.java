package DveAgent.info;

import DveAgent.entity.File;
import DveAgent.entity.Rule;
import lombok.Data;

import java.util.List;

@Data
public class FileInfo extends File {
    private List<Rule> ruleList;

}
