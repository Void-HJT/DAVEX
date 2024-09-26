package DavexBase.service.auth;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import DavexBase.entity.Application;
import DavexBase.entity.File;
import DavexBase.entity.Folder;
import DavexBase.mapper.FileRuleMapper;
import DavexBase.mapper.FolderVisibilityMapper;

@Service
public class ABACService {
    private final ExpressionParser parser = new SpelExpressionParser();
    private static final Logger logger = LoggerFactory.getLogger(ABACService.class);
    @Autowired
    private FileRuleMapper fileRuleMapper;
    @Autowired
    private FolderVisibilityMapper folderVisibilityMapper;

    private Boolean check(List<String> rules, StandardEvaluationContext context) {
        boolean hasNonNull = false;
        for (String rule : rules) {
            try {
                logger.info(rule);
                Boolean result = parser.parseExpression(rule).getValue(context, Boolean.class);
                if (result == null) {
                    logger.info("规则{} 不匹配", rule);
                    continue;
                }
                logger.info("规则{} {}", rule, result);
                hasNonNull = true;
                if (result == Boolean.FALSE) {
                    return false;
                }
            } catch (SpelEvaluationException e) {
                logger.warn("规则 {} 解析不匹配：", rule, e.getMessage());
                continue;
            } catch (Exception e) {
                logger.error("规则 {} 错误：", rule, e.getMessage());
                return false;
            }
        }
        return hasNonNull ? true : false;
    }

    private List<String> getFileRules(File file) {
        return fileRuleMapper.getRuleExpressions(file.getUid());
    }

    private List<String> getFolderRules(Folder folder) {
        return folderVisibilityMapper.getRuleExpressions(folder.getUid());
    }

    public boolean getAccess(Application app, File file, String action) {
        logger.info("处理数据访问ABAC请求\nsubject:{}\nobject:{}\naction:{}", app, file, action);
        StandardEvaluationContext context = new StandardEvaluationContext();

        context.setVariable("subject", app);
        context.setVariable("action", action);
        try {
            context.setVariable("subjectAttribute", app.getAttribute().getInnerMap());
        } catch (NullPointerException e) {
            context.setVariable("subjectAttribute", null);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
        // context.setVariable("object", file);
        // context.setVariable("objectAttribute", file.getAttribute().getInnerMap());
        return check(getFileRules(file), context);

    }

    public boolean getVisibility(Application app, Folder folder) {
        logger.info("处理文件夹访问ABAC请求\nsubject:{}\nobject:{}", app, folder);
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("subject", app);
        try {
            context.setVariable("subjectAttribute", app.getAttribute().getInnerMap());
        } catch (NullPointerException e) {
            context.setVariable("subjectAttribute", null);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
        return check(getFolderRules(folder), context);
    }

    public List<Boolean> fileTest(Application app, File file, String action) {
        logger.info("处理ABAC请求\nsubject:{}\nobject:{}\naction:{}", app, file, action);
        List<Boolean> res = new ArrayList<>();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("subject", app);
        context.setVariable("action", action);
        try {
            context.setVariable("subjectAttribute", app.getAttribute().getInnerMap());
        } catch (NullPointerException e) {
            context.setVariable("subjectAttribute", null);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
        for (String rule : getFileRules(file)) {
            try {
                logger.info(rule);
                Boolean b = parser.parseExpression(rule).getValue(context, Boolean.class);
                res.add(b);
                logger.info("{}", b);
            } catch (SpelEvaluationException e) {
                logger.warn(e.getMessage());
                res.add(null);
                continue;
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw e;
            }
        }
        return res;
    }

    public List<Boolean> folderTest(Application application, Folder folder) {
        logger.info("处理ABAC请求\nsubject:{}\nobject:{}", application, folder);
        List<Boolean> res = new ArrayList<>();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("subject", application);
        try {
            context.setVariable("subjectAttribute", application.getAttribute().getInnerMap());
        } catch (NullPointerException e) {
            context.setVariable("subjectAttribute", null);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
        for (String rule : getFolderRules(folder)) {
            try {
                logger.info(rule);
                Boolean b = parser.parseExpression(rule).getValue(context, Boolean.class);
                res.add(b);
                logger.info("{}", b);
            } catch (SpelEvaluationException e) {
                logger.warn(e.getMessage());
                res.add(null);
                continue;
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw e;
            }
        }
        return res;
    }

}
