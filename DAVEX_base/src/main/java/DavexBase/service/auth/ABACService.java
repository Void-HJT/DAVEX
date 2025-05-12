package DavexBase.service.auth;

import java.util.ArrayList;
import java.util.List;

import DavexBase.entity.*;
import DavexBase.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DavexBase.common.Body;
import DavexBase.common.GetMaxUid;
import DavexBase.common.My;

@Service
public class ABACService {
    private final ExpressionParser parser = new SpelExpressionParser();
    private static final Logger logger = LoggerFactory.getLogger(ABACService.class);
    @Autowired
    My my;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private FileRuleMapper fileRuleMapper;
    @Autowired
    private CenterMapper centerMapper;
    @Autowired
    private RuleMapper ruleMapper;
    @Autowired
    private FolderMapper folderMapper;
    @Autowired
    private VisibilityMapper visibilityMapper;
    @Autowired
    private FolderVisibilityMapper folderVisibilityMapper;
    @Autowired
    private AgentWebClientService agentWebClientService;

    // 使用 Jackson ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ABACService() {
        this.objectMapper.registerModule(new JavaTimeModule());
    }

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

    private List<String> getFileRules(String fid) {
        return fileRuleMapper.getRuleExpressions(fid);
    }

    private List<String> getFolderRules(Folder folder) {
        return folderVisibilityMapper.getRuleExpressions(folder.getUid());
    }

    public boolean getAccess(Application app, File file, String action) {
        if (action.equals("pass")) {
            return true;
        }
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
        return check(getFileRules(file.getUid()), context);

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
        for (String rule : getFileRules(file.getUid())) {
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

    // #region 规则管理
    public Body<Rule> getRule(String uid) {
        Rule rule = ruleMapper.selectById(uid);
        if (rule == null) {
            return Body.error("规则不存在");
        }
        return Body.success(rule, "成功");
    }

    public Body<List<Rule>> getRules() {
        List<Rule> rules = ruleMapper.selectList(null);
        return Body.success(rules, "成功");
    }

    public Body<String> createRule(String expression, String description) {
        Rule rule = new Rule();
        rule.setExpression(expression);
        rule.setDescription(description);
        rule.setUid(my.getId() + "-RD" + GetMaxUid.getRuleMaxUid(ruleMapper));
        try {
            ruleMapper.insert(rule);
        } catch (Exception e) {
            return Body.error("创建规则失败：{}", e.getMessage());
        }

        // 通信
        // String exchange = "RuleExchange";
        // String message = null;
        // String operation = "Create";
        // String entity = "rule";
        // message = messageService.getMessage(operation, entity, objectMapper, rule);
        // //
        // List<Object> uidList = rabbitmqConnectionMapper
        // .selectObjs(new QueryWrapper<RabbitmqConnection>().select("uid"));
        // for (Object obj : uidList) {
        // if (obj instanceof String) {
        // String targetId = (String) obj;
        // publishService.sendMessageToFanoutExchange(targetId, exchange, message);
        // }
        // }
        List<Center> centerList = centerMapper.selectList(new LambdaQueryWrapper<>());
        for (Center center : centerList) {
            try {
                // 准备 target 参数，可以根据实际情况选择 add, delete 或 update
                String target = "add";
                if (center.getUid().equals(my.getId())) {// 跳过自己
                    continue;
                }
                // 构建 WebClient 请求并发送 POST 请求
                agentWebClientService.agent2CenterWebClient(center.getUid())
                        .post()
                        .uri(UriBuilder -> UriBuilder.path("/abac/syncRule").queryParam("target", target).build())// 请求
                                                                                                                  // URL
                        .bodyValue(rule)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing center with UID: " + center.getUid());
                e.printStackTrace();
            }
        }

        return Body.success("创建规则成功");
    }

    public Body<String> deleteRule(String uid) {
        Rule rule = ruleMapper.selectById(uid);
        if (rule == null) {
            return Body.error("规则不存在");
        }
        try {
            ruleMapper.deleteById(uid);
        } catch (Exception e) {
            return Body.error("删除规则失败：{}", e.getMessage());
        }
        // 通信
        // String exchange = "RuleExchange";
        // String message = null;
        // String operation = "Delete";
        // String entity = "rule";
        // message = messageService.getMessage(operation, entity, objectMapper, rule);
        // //
        // List<Object> uidList = rabbitmqConnectionMapper
        // .selectObjs(new QueryWrapper<RabbitmqConnection>().select("uid"));
        // for (Object obj : uidList) {
        // if (obj instanceof String) {
        // String targetId = (String) obj;
        // publishService.sendMessageToFanoutExchange(targetId, exchange, message);
        // }
        // }
        List<Center> centerList = centerMapper.selectList(new LambdaQueryWrapper<>());
        for (Center center : centerList) {
            try {
                // 准备 target 参数，可以根据实际情况选择 add, delete 或 update
                String target = "delete";
                if (center.getUid().equals(my.getId())) {// 跳过自己
                    continue;
                }
                // 构建 WebClient 请求并发送 POST 请求
                agentWebClientService.agent2CenterWebClient(center.getUid())
                        .post()
                        .uri(UriBuilder -> UriBuilder.path("/abac/syncRule").queryParam("target", target).build())// 请求
                                                                                                                  // URL
                        .bodyValue(rule)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing center with UID: " + center.getUid());
                e.printStackTrace();
            }
        }
        return Body.success("删除规则成功");
    }

    public Body<String> updateRule(String uid, String expression, String description) {
        Rule rule = ruleMapper.selectById(uid);
        if (rule == null) {
            return Body.error("规则不存在");
        }
        rule.setExpression(expression);
        rule.setDescription(description);
        try {
            ruleMapper.updateById(rule);
        } catch (Exception e) {
            return Body.error("更新规则失败：{}", e.getMessage());
        }
        // 通信
        // String exchange = "RuleExchange";
        // String message = null;
        // String operation = "Update";
        // String entity = "rule";
        // message = messageService.getMessage(operation, entity, objectMapper, rule);
        // //
        // List<Object> uidList = rabbitmqConnectionMapper
        // .selectObjs(new QueryWrapper<RabbitmqConnection>().select("uid"));
        // for (Object obj : uidList) {
        // if (obj instanceof String) {
        // String targetId = (String) obj;
        // publishService.sendMessageToFanoutExchange(targetId, exchange, message);
        // }
        // }
        List<Center> centerList = centerMapper.selectList(new LambdaQueryWrapper<>());
        for (Center center : centerList) {
            try {
                // 准备 target 参数，可以根据实际情况选择 add, delete 或 update
                String target = "update";
                if (center.getUid().equals(my.getId())) {// 跳过自己
                    continue;
                }
                // 构建 WebClient 请求并发送 POST 请求
                agentWebClientService.agent2CenterWebClient(center.getUid())
                        .post()
                        .uri(UriBuilder -> UriBuilder.path("/abac/syncRule").queryParam("target", target).build())// 请求
                                                                                                                  // URL
                        .bodyValue(rule)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing center with UID: " + center.getUid());
                e.printStackTrace();
            }
        }
        return Body.success("更新规则成功");
    }

    public Body<List<Rule>> getFileRule(String fileId) {
        List<Rule> rules = fileRuleMapper.getRules(fileId);
        return Body.success(rules, "成功");
    }

    public Body<String> createFileRule(String fileId, String RuleId) {
        if (fileMapper.selectById(fileId) == null) {
            return Body.error("文件不存在");
        }
        if (ruleMapper.selectById(RuleId) == null) {
            return Body.error("规则不存在");
        }
        if (fileRuleMapper.selectCount(
                Wrappers.<FileRule>lambdaQuery().eq(FileRule::getFileId, fileId).eq(FileRule::getRuleId, RuleId)) > 0) {
            return Body.error("文件规则已存在");
        }
        FileRule fileRule = new FileRule();
        fileRule.setFileId(fileId);
        fileRule.setRuleId(RuleId);
        fileRule.setUid(my.getId() + "-XD" + GetMaxUid.getFileRuleMaxUid(fileRuleMapper));
        try {
            fileRuleMapper.insert(fileRule);
        } catch (Exception e) {
            return Body.error("创建文件规则失败：{}", e.getMessage());
        }
        // 通信
        // String exchange = "RuleExchange";
        // String message = null;
        // String operation = "Create";
        // String entity = "fileRule";
        // message = messageService.getMessage(operation, entity, objectMapper,
        // fileRule);
        // //
        // List<Object> uidList = rabbitmqConnectionMapper
        // .selectObjs(new QueryWrapper<RabbitmqConnection>().select("uid"));
        // for (Object obj : uidList) {
        // if (obj instanceof String) {
        // String targetId = (String) obj;
        // publishService.sendMessageToFanoutExchange(targetId, exchange, message);
        // }
        // }
        List<Center> centerList = centerMapper.selectList(new LambdaQueryWrapper<>());
        for (Center center : centerList) {
            try {
                // 准备 target 参数，可以根据实际情况选择 add, delete 或 update
                String target = "add";
                if (center.getUid().equals(my.getId())) {// 跳过自己
                    continue;
                }
                // 构建 WebClient 请求并发送 POST 请求
                agentWebClientService.agent2CenterWebClient(center.getUid())
                        .post()
                        .uri(UriBuilder -> UriBuilder.path("/abac/syncFileRule").queryParam("target", target).build())// 请求
                                                                                                                      // URL
                        .bodyValue(fileRule)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing center with UID: " + center.getUid());
                e.printStackTrace();
            }
        }

        return Body.success("创建文件规则成功");
    }

    public Body<String> deleteFileRule(String fileRuleId) {
        FileRule fileRule = fileRuleMapper.selectById(fileRuleId);
        if (fileRule == null) {
            return Body.error("文件规则不存在");
        }
        try {
            fileRuleMapper.deleteById(fileRuleId);
        } catch (Exception e) {
            return Body.error("删除文件规则失败：{}", e.getMessage());
        }
        // 通信
        // String exchange = "RuleExchange";
        // String message = null;
        // String operation = "Delete";
        // String entity = "fileRule";
        // message = messageService.getMessage(operation, entity, objectMapper,
        // fileRule);
        // //
        // List<Object> uidList = rabbitmqConnectionMapper
        // .selectObjs(new QueryWrapper<RabbitmqConnection>().select("uid"));
        // for (Object obj : uidList) {
        // if (obj instanceof String) {
        // String targetId = (String) obj;
        // publishService.sendMessageToFanoutExchange(targetId, exchange, message);
        // }
        // }
        List<Center> centerList = centerMapper.selectList(new LambdaQueryWrapper<>());
        for (Center center : centerList) {
            try {
                // 准备 target 参数，可以根据实际情况选择 add, delete 或 update
                String target = "delete";
                if (center.getUid().equals(my.getId())) {// 跳过自己
                    continue;
                }
                // 构建 WebClient 请求并发送 POST 请求
                agentWebClientService.agent2CenterWebClient(center.getUid())
                        .post()
                        .uri(UriBuilder -> UriBuilder.path("/abac/syncFileRule").queryParam("target", target).build())// 请求
                                                                                                                      // URL
                        .bodyValue(fileRule)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing center with UID: " + center.getUid());
                e.printStackTrace();
            }
        }
        return Body.success("删除文件规则成功");
    }

    public Body<String> updateFileRule(String fileRuleId, String RuleId, String fileId) {
        FileRule fileRule = fileRuleMapper.selectById(fileRuleId);
        if (fileRule == null) {
            return Body.error("文件规则不存在");
        }
        if (fileMapper.selectById(fileId) == null) {
            return Body.error("文件不存在");
        }
        if (ruleMapper.selectById(RuleId) == null) {
            return Body.error("规则不存在");
        }
        fileRule.setFileId(fileId);
        fileRule.setRuleId(RuleId);
        try {
            fileRuleMapper.updateById(fileRule);
        } catch (Exception e) {
            return Body.error("更新文件规则失败：{}", e.getMessage());
        }
        // 通信
        // String exchange = "RuleExchange";
        // String message = null;
        // String operation = "Update";
        // String entity = "fileRule";
        // message = messageService.getMessage(operation, entity, objectMapper,
        // fileRule);
        // //
        // List<Object> uidList = rabbitmqConnectionMapper
        // .selectObjs(new QueryWrapper<RabbitmqConnection>().select("uid"));
        // for (Object obj : uidList) {
        // if (obj instanceof String) {
        // String targetId = (String) obj;
        // publishService.sendMessageToFanoutExchange(targetId, exchange, message);
        // }
        // }
        List<Center> centerList = centerMapper.selectList(new LambdaQueryWrapper<>());
        for (Center center : centerList) {
            try {
                // 准备 target 参数，可以根据实际情况选择 add, delete 或 update
                String target = "update";
                if (center.getUid().equals(my.getId())) {// 跳过自己
                    continue;
                }
                // 构建 WebClient 请求并发送 POST 请求
                agentWebClientService.agent2CenterWebClient(center.getUid())
                        .post()
                        .uri(UriBuilder -> UriBuilder.path("/abac/syncFileRule").queryParam("target", target).build())// 请求
                                                                                                                      // URL
                        .bodyValue(fileRule)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing center with UID: " + center.getUid());
                e.printStackTrace();
            }
        }
        return Body.success("更新文件规则成功");
    }

    // #endregion

    // #region 文件夹可见性管理
    public Body<Visibility> getVisibility(String uid) {
        Visibility visibility = visibilityMapper.selectById(uid);
        if (visibility == null) {
            return Body.error("可见性不存在");
        }
        return Body.success(visibility, "成功");
    }

    public Body<List<Visibility>> getVisibilities() {
        List<Visibility> visibilities = visibilityMapper.selectList(null);
        return Body.success(visibilities, "成功");
    }

    public Body<String> createVisibility(String expression, String description) {
        Visibility visibility = new Visibility();
        visibility.setExpression(expression);
        visibility.setDescription(description);
        visibility.setUid(my.getId() + "-RD" + GetMaxUid.getVisibilityMaxUid(visibilityMapper));
        try {
            visibilityMapper.insert(visibility);
        } catch (Exception e) {
            return Body.error("创建可见性失败：{}", e.getMessage());
        }
        return Body.success("创建可见性成功");
    }

    public Body<String> deleteVisibility(String uid) {
        Visibility visibility = visibilityMapper.selectById(uid);
        if (visibility == null) {
            return Body.error("可见性不存在");
        }
        try {
            visibilityMapper.deleteById(uid);
        } catch (Exception e) {
            return Body.error("删除可见性失败：{}", e.getMessage());
        }
        return Body.success("删除可见性成功");
    }

    public Body<String> updateVisibility(String uid, String expression, String description) {
        Visibility visibility = visibilityMapper.selectById(uid);
        if (visibility == null) {
            return Body.error("可见性不存在");
        }
        visibility.setExpression(expression);
        visibility.setDescription(description);
        try {
            visibilityMapper.updateById(visibility);
        } catch (Exception e) {
            return Body.error("更新可见性失败：{}", e.getMessage());
        }
        return Body.success("更新可见性成功");
    }

    public Body<List<Visibility>> getFolderVisibility(String folderId) {
        List<Visibility> visibilities = folderVisibilityMapper.getVisibilities(folderId);
        return Body.success(visibilities, "成功");
    }

    public Body<String> createFolderVisibility(String folderId, String visibilityId) {
        if (folderMapper.selectById(folderId) == null) {
            return Body.error("文件夹不存在");
        }
        if (visibilityMapper.selectById(visibilityId) == null) {
            return Body.error("可见性不存在");
        }
        if (folderVisibilityMapper.selectCount(
                Wrappers.<FolderVisibility>lambdaQuery().eq(FolderVisibility::getFolderId, folderId)
                        .eq(FolderVisibility::getVisibilityId, visibilityId)) > 0) {
            return Body.error("文件夹可见性已存在");
        }
        FolderVisibility folderVisibility = new FolderVisibility();
        folderVisibility.setFolderId(folderId);
        folderVisibility.setVisibilityId(visibilityId);
        folderVisibility.setUid(my.getId() + "-XD" + GetMaxUid.getFolderVisibilityMaxUid(folderVisibilityMapper));
        try {
            folderVisibilityMapper.insert(folderVisibility);
        } catch (Exception e) {
            return Body.error("创建文件夹可见性失败：{}", e.getMessage());
        }
        return Body.success("创建文件夹可见性成功");
    }

    public Body<String> deleteFolderVisibility(String folderVisibilityId) {
        FolderVisibility folderVisibility = folderVisibilityMapper.selectById(folderVisibilityId);
        if (folderVisibility == null) {
            return Body.error("文件夹可见性不存在");
        }
        try {
            folderVisibilityMapper.deleteById(folderVisibilityId);
        } catch (Exception e) {
            return Body.error("删除文件夹可见性失败：{}", e.getMessage());
        }
        return Body.success("删除文件夹可见性成功");
    }

    public Body<String> updateFolderVisibility(String folderVisibilityId, String visibilityId, String folderId) {
        FolderVisibility folderVisibility = folderVisibilityMapper.selectById(folderVisibilityId);
        if (folderVisibility == null) {
            return Body.error("文件夹可见性不存在");
        }
        if (folderMapper.selectById(folderId) == null) {
            return Body.error("文件夹不存在");
        }
        if (visibilityMapper.selectById(visibilityId) == null) {
            return Body.error("可见性不存在");
        }
        folderVisibility.setFolderId(folderId);
        folderVisibility.setVisibilityId(visibilityId);
        try {
            folderVisibilityMapper.updateById(folderVisibility);
        } catch (Exception e) {
            return Body.error("更新文件夹可见性失败：{}", e.getMessage());
        }
        return Body.success("更新文件夹可见性成功");
    }

    // #endregion
    //
    public Body<String> syncRule(Rule rule, String target) {

        // 检查输入参数
        if (rule == null || target == null || target.isEmpty()) {
            return Body.error("Invalid input: rule or target is null or empty.");
        }

        LambdaQueryWrapper<Rule> queryRuleWrapper = Wrappers.<Rule>lambdaQuery()
                .eq(Rule::getUid, rule.getUid());
        // 查看是否存在
        Rule existingRule = ruleMapper.selectOne(queryRuleWrapper);
        // 根据target不同进行不同操作 add delete update
        try {
            switch (target.toLowerCase()) {
                case "add":
                    if (existingRule != null) {
                        return Body.error("Rule with the same UID already exists.");
                    }
                    ruleMapper.insert(rule); // 插入新的
                    return Body.success("Rule added successfully.");

                case "delete":
                    if (existingRule == null) {
                        return Body.error("Rule not found. Cannot delete.");
                    }
                    ruleMapper.delete(queryRuleWrapper); // 删除目标
                    return Body.success("Rule deleted successfully.");

                case "update":
                    if (existingRule == null) {
                        return Body.error("Rule not found. Cannot update.");
                    }
                    ruleMapper.update(rule, queryRuleWrapper); // 更新
                    return Body.success("Rule updated successfully.");

                default:
                    return Body.error("Invalid target action: " + target);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error("An error occurred: " + e.getMessage());
        }
    }

    //
    public Body<String> syncFileRule(FileRule fileRule, String target) {

        // 检查输入参数
        if (fileRule == null || target == null || target.isEmpty()) {
            return Body.error("Invalid input: fileRuler or target is null or empty.");
        }

        LambdaQueryWrapper<FileRule> queryFileRuleWrapper = Wrappers.<FileRule>lambdaQuery()
                .eq(FileRule::getUid, fileRule.getUid());
        // 查看是否存在
        FileRule existingFileRule = fileRuleMapper.selectOne(queryFileRuleWrapper);
        // 根据target不同进行不同操作 add delete update
        try {
            switch (target.toLowerCase()) {
                case "add":
                    if (existingFileRule != null) {
                        return Body.error("FileRule with the same UID already exists.");
                    }
                    fileRuleMapper.insert(fileRule); // 插入新的
                    return Body.success("FileRule added successfully.");

                case "delete":
                    if (existingFileRule == null) {
                        return Body.error("FileRule not found. Cannot delete.");
                    }
                    fileRuleMapper.delete(queryFileRuleWrapper); // 删除目标
                    return Body.success("FileRule deleted successfully.");

                case "update":
                    if (existingFileRule == null) {
                        return Body.error("FileRule not found. Cannot update.");
                    }
                    fileRuleMapper.update(fileRule, queryFileRuleWrapper); // 更新
                    return Body.success("FileRule updated successfully.");

                default:
                    return Body.error("Invalid target action: " + target);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error("An error occurred: " + e.getMessage());
        }
    }
}
