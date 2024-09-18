package DavexAgent.module.auth.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import DavexBase.common.My;
import DavexBase.entity.Application;
import DavexBase.entity.File;

@Service
public class ABACService {
    private final ExpressionParser parser = new SpelExpressionParser();
    private List<String> rules;
    private static final Logger logger = LoggerFactory.getLogger(ABACService.class);

    private My my;

    private void loadRules() throws IOException {
        rules = Files.readAllLines(Paths.get(my.getRules_path()));
    }

    public ABACService(My my) throws IOException {
        this.my = my;
        loadRules();
    }

    public void refreshRules() throws IOException {
        loadRules();
    }

    public boolean getAccess(Application application, File file, String action) {
        logger.info("处理ABAC请求\nsubject:{}\nobject:{}\naction:{}", application, file, action);
        StandardEvaluationContext context = new StandardEvaluationContext();

        context.setVariable("subject", application);
        context.setVariable("object", file);
        context.setVariable("action", action);
        context.setVariable("subjectAttribute", application.getAttribute().getInnerMap());
        context.setVariable("objectAttribute", file.getAttribute().getInnerMap());

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

    public List<Boolean> test(Application application, File file, String action) {
        logger.info("处理ABAC请求\nsubject:{}\nobject:{}\naction:{}", application, file, action);
        List<Boolean> res = new ArrayList<>();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("subject", application);
        context.setVariable("object", file);
        context.setVariable("action", action);
        context.setVariable("subjectAttribute", application.getAttribute().getInnerMap());
        context.setVariable("objectAttribute", file.getAttribute().getInnerMap());

        try {
            refreshRules();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String rule : rules) {
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
