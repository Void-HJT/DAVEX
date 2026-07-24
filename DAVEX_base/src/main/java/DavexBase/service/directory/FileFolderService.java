package DavexBase.service.directory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import DavexBase.common.My;
import DavexBase.entity.*;
import DavexBase.mapper.*;
import DavexBase.service.auth.AgentWebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import DavexBase.common.Body;
import DavexBase.common.GetMaxUid;
import DavexBase.info.DirectoryInfo;
import DavexBase.info.FileInfo;
import DavexBase.service.auth.CenterWebClientService;

@Service
public class FileFolderService {

    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private CenterMapper centerMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AgentWebClientService agentWebClientService;

    @Autowired
    private CenterWebClientService centerWebClientService;

    @Autowired
    private My my;

    // 使用 Jackson ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FileFolderService() {
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // 创建文件夹
    public Body<String> createFolder(String name, String path, String agent_id, String parent_id) {
        // 1.检查是否父文件夹存在
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, parent_id);
        Folder fatherFolder = folderMapper.selectOne(queryWrapper);
        if (fatherFolder == null) {
            return Body.error("父文件夹不存在");
        }
        queryWrapper.clear();

        // 2.查询数据库相同父文件夹下是否有同名文件夹
        queryWrapper.eq(Folder::getParentId, parent_id).eq(Folder::getName, name);
        List<Folder> folderList = folderMapper.selectList(queryWrapper);
        if (!folderList.isEmpty()) {
            return Body.error("重名文件夹");
        }

        // 3.本地创建新文件夹
        Path create_path = Path.of(getFolderPath(fatherFolder, path) + "/" + name);
        try {
            Files.createDirectories(create_path);
        } catch (IOException e) {
            e.printStackTrace();
            return Body.error("文件夹创建失败: " + e.getMessage());
        }

        // 3.新文件夹插入
        Folder new_folder = new Folder();
        GetMaxUid getMaxUid = new GetMaxUid();
        //
        int maxTailNumber = getMaxUid.getFolderMaxUid(agent_id, folderMapper);
        String uid = agent_id + "-F" + (maxTailNumber + 1);
        new_folder.setUid(uid);
        new_folder.setName(name);
        new_folder.setAgentId(agent_id);
        new_folder.setParentId(parent_id);
        new_folder.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));
        new_folder.setLastUpdate(Timestamp.valueOf(LocalDateTime.now()));
        folderMapper.insert(new_folder);

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
                        .uri(UriBuilder -> UriBuilder.path("/directory/fileFolder/syncFolder")
                                .queryParam("target", target).build())// 请求 URL
                        .bodyValue(new_folder)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing center with UID: " + center.getUid());
                e.printStackTrace();
            }
        }

        return Body.success("插入新文件夹成功");
    }

    // 修改文件夹名
    public Body<String> setFolderName(String agentId, String folderId, String name, String baseDirectory) {
        // 1. 首先查找该文件夹是否存在
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }

        // 2. 得到父文件夹的 ID 列表，并构建本地绝对路径
        List<String> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, parentFolderIds, path);

        // 构建文件夹路径
        Collections.reverse(path); // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        String oldFolderPath = fullPathBuilder.toString();

        // 构建新的文件夹路径
        String newFolderPath = fullPathBuilder.substring(0, fullPathBuilder.lastIndexOf(java.io.File.separator))
                + java.io.File.separator + name;

        // 3. 修改数据库 folder 表
        folder.setName(name);
        folder.setAgentId(agentId);
        folder.setUid(folderId);
        UpdateWrapper<Folder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("agent_id", agentId)
                .eq("uid", folderId);
        folderMapper.update(folder, updateWrapper);

        // 4. 实际修改本地文件夹名
        java.io.File oldFolder = new java.io.File(oldFolderPath);
        java.io.File newFolder = new java.io.File(newFolderPath);
        if (oldFolder.exists() && oldFolder.isDirectory()) {
            boolean renamed = oldFolder.renameTo(newFolder);
            if (!renamed) {
                return Body.error("本地文件夹重命名失败");
            }
        } else {
            return Body.error("本地文件夹不存在或不是一个目录");
        }

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
                        .uri(UriBuilder -> UriBuilder.path("/directory/fileFolder/syncFolder")
                                .queryParam("target", target).build())// 请求 URL
                        .bodyValue(folder)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing center with UID: " + center.getUid());
                e.printStackTrace();
            }
        }

        return Body.success("文件夹名称更新成功");

    }

    // 删除文件夹
    public Body<String> deleteFolder(String agentId, String folderId, String baseDirectory) {
        // 1.查找是否存在或者有子文件夹与文件，如果有则无法删除
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }
        // 检查是否有子文件夹
        List<String> subFolderIds = new ArrayList<>();
        findAllChildFolders(folderId, agentId, subFolderIds);
        if (!subFolderIds.isEmpty()) {
            return Body.error("该文件夹包含子文件夹，无法删除");
        }

        // 检查文件夹是否包含文件
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getFolderId, folderId);
        if (fileMapper.selectCount(queryFileWrapper) > 0) {
            return Body.error("该文件夹包含文件，无法删除");
        }

        // 2.先得到文件夹路径，再从数据库中删除folder
        List<String> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, parentFolderIds, path);
        // 构建文件夹路径
        Collections.reverse(path); // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        String oldFolderPath = fullPathBuilder.toString();

        folderMapper.delete(queryFolderWrapper);
        // 3.本地实际删除folder
        java.io.File oldFolder = new java.io.File(oldFolderPath);
        if (oldFolder.exists() && oldFolder.isDirectory()) {
            boolean deleted = oldFolder.delete();
            if (!deleted) {
                return Body.error("本地文件夹删除失败");
            }
        } else {
            return Body.error("本地文件夹不存在或不是一个目录");
        }

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
                        .uri(UriBuilder -> UriBuilder.path("/directory/fileFolder/syncFolder")
                                .queryParam("target", target).build())// 请求 URL
                        .bodyValue(folder)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing center with UID: " + center.getUid());
                e.printStackTrace();
            }
        }

        return Body.success("文件夹删除成功");
    }

    // 获取文件夹路径
    public Body<String> getFolderPath(String agentId, String folderId, String baseDirectory) {
        // 1.查找是否存在
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }
        String path = getFolderPath(folder, baseDirectory);
        return Body.success(path, "成功");
    }

    // 展示某文件的详细信息
    public Body<FileInfo> getFileInfo(String uid, String agentId, String folderId) {
        return Body.success(fileMapper.getFileInfo(uid, agentId, folderId), "成功");
    }

    // 上传文件
    public Body<String> uploadFile(String agentId, String folderId, MultipartFile file, String baseDirectory, Integer privacy) {
        // 空文件不能生成有效的本地文件和数据库记录。
        if (file == null || file.isEmpty()) {
            return Body.error("上传文件不能为空");
        }
        // 查找是否存在该文件夹
        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId);
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        if (folder == null) {
            return Body.error("该文件夹不存在");
        }
        // 同一 Agent 的同一文件夹中存在同名文件时覆盖原文件。
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getAgentId, agentId)
                .eq(File::getFolderId, folderId)
                .eq(File::getName, file.getOriginalFilename());
        File existingFile = fileMapper.selectOne(queryFileWrapper);
        boolean overwrite = existingFile != null;
        // 构建文件夹路径
        List<String> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, parentFolderIds, path);
        Collections.reverse(path); // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        fullPathBuilder.append(java.io.File.separator).append(file.getOriginalFilename());
        String folderPath = fullPathBuilder.toString();
        // 将文件存储到实际目录中
        java.io.File destFile = new java.io.File(folderPath);
        try {
            org.apache.commons.io.FileUtils.writeByteArrayToFile(destFile, file.getBytes());
        } catch (IOException e) {
            return Body.error("文件上传失败: " + e.getMessage());
        }
        // 将文件元数据添加到数据库中
        File fileRecord = new File();
        try {
            // 获取文件的byte信息
            byte[] uploadBytes = file.getBytes();
            // 拿到一个SHA-256转换器
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha256.digest(uploadBytes);
            // 转换为16进制
            fileRecord.setHash(new BigInteger(1, digest).toString(16));
        } catch (Exception e) {
            return Body.error("文件计算hash失败" + e.getMessage());
        }

        String fileName = file.getOriginalFilename();

        if (overwrite) {
            // 覆盖文件时保留原 UID，避免 Center 产生重复文件记录。
            fileRecord.setUid(existingFile.getUid());
        } else {
            // 新文件才生成新的 UID。
            GetMaxUid getMaxUid = new GetMaxUid();
            int maxTailNumber = getMaxUid.getFileMaxUid(agentId, fileMapper);
            fileRecord.setUid(agentId + "-D" + (maxTailNumber + 1));
        }
        // fileRecord.setType(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")));
        fileRecord.setAgentId(agentId);
        fileRecord.setFolderId(folderId);
        fileRecord.setName(fileName);
        fileRecord.setSize(file.getSize());
        if (privacy == 1) {
            fileRecord.setType("private"); // 传1时置为private
        } else {
            fileRecord.setType("default"); // 默认/传0时置为default
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());

        // 覆盖文件保留原创建时间，只更新最后修改时间。
        fileRecord.setCreateDate(overwrite ? existingFile.getCreateDate() : now);
        fileRecord.setLastUpdate(now);

        // 新增解析逻辑
        if (privacy == 1) {
            try {
                String content = new String(file.getBytes(), StandardCharsets.UTF_8);

                // 判决时间（中文数字日期）
                String judgeTimeStr = null;
                Pattern timePattern = Pattern.compile(
                        "([〇一二三四五六七八九零十]{4})年" + // 匹配4位中文年份（如“二〇二四”）
                                "([〇一二三四五六七八九零十]{1,3})月" +
                                "([〇一二三四五六七八九零十]{1,3})日"
                );
                Matcher timeMatcher = timePattern.matcher(content);

                if (timeMatcher.find()) {
                    try {
                        // 1. 提取中文年、月、日（重点确保年份是4个字符）
                        String yearChinese = timeMatcher.group(1);
                        String monthChinese = timeMatcher.group(2);
                        String dayChinese = timeMatcher.group(3);

                        // 新增：打印原始中文年份，验证是否为4个字符（如“二〇二四”）
                        System.out.println("原始中文年份：" + yearChinese + "（长度：" + yearChinese.length() + "）");

                        // 2. 中文转阿拉伯数字（重点修复年份转换逻辑）
                        String yearArabic = chineseYearToArabic(yearChinese); // 专门处理年份
                        String monthArabic = chineseNumToArabic(monthChinese);
                        String dayArabic = chineseNumToArabic(dayChinese);

                        // 3. 补零（确保月/日为2位，年份已确保4位）
                        monthArabic = String.format("%02d", Integer.parseInt(monthArabic));
                        dayArabic = String.format("%02d", Integer.parseInt(dayArabic));

                        // 4. 构建标准格式（此时年份应为2024，而非4）
                        judgeTimeStr = yearArabic + "-" + monthArabic + "-" + dayArabic;
                        String fullTimeStr = judgeTimeStr + " 00:00:00"; // 完整时分秒
                        System.out.println("待转换的完整时间：" + fullTimeStr);

                        // 5. 转换为Timestamp（此时格式合法）
                        Timestamp judgeTime = Timestamp.valueOf(fullTimeStr);
                        fileRecord.setJudgeTime(judgeTime);
                        System.out.println("成功解析日期：" + judgeTimeStr);

                    } catch (NumberFormatException e) {
                        System.err.println("数字转换错误：" + e.getMessage() + "（当前日期字符串：" + judgeTimeStr + "）");
                    } catch (IllegalArgumentException e) {
                        System.err.println("Timestamp格式错误：" + judgeTimeStr + "（需yyyy-MM-dd HH:mm:ss）");
                    } catch (Exception e) {
                        System.err.println("日期解析异常：" + e.getMessage());
                    }
                } else {
                    System.out.println("未匹配到纯中文日期（格式：XXXX年XX月XX日）");
                }

                String titleArea = "";
                if (content.length() > 200) {
                    titleArea = content.substring(0, 200); // 截取前200字符（覆盖所有判决书标题）
                } else {
                    titleArea = content; // 文本过短时直接用全部内容
                }
                System.out.println("标题区域内容：" + titleArea); // 调试日志，确认标题区域是否正确

                // 判决类型
//            Matcher typeMatcher = Pattern.compile("刑\\s*事\\s*判\\s*决\\s*书").matcher(content);
//            if (typeMatcher.find()) {
//                fileRecord.setJudgeType(typeMatcher.group(0).replaceAll("\\s+", ""));
//            }
                String judgeDistrict = null;
// 1. 优先匹配标题中的“中华人民共和国XX法院”（如最高人民法院）
                Matcher nationalCourtMatcher = Pattern.compile("(中华人民共和国[\\u4e00-\\u9fa5]+人民法院)").matcher(titleArea);
                if (nationalCourtMatcher.find()) {
                    judgeDistrict = nationalCourtMatcher.group(1).replaceAll("\\s+", "");
                } else {
                    // 2. 若无国家级法院，再匹配标题中的“XX省/市+高级/中级/基层人民法院”
                    Matcher localCourtMatcher = Pattern.compile("([\\u4e00-\\u9fa5]+?)(高级|中级|基层)[\\s\\r\\n]*人民法院").matcher(titleArea);
                    if (localCourtMatcher.find()) {
                        judgeDistrict = localCourtMatcher.group(1) + localCourtMatcher.group(2) + "人民法院";
                    }
                }
// 赋值并验证（此时应得到“中华人民共和国最高人民法院”）
                if (judgeDistrict != null) {
                    fileRecord.setJudgeDistrict(judgeDistrict);
                    System.out.println("成功解析标题中的法院：" + judgeDistrict);
                } else {
                    System.out.println("未在标题区域匹配到法院名称");
                }

                // 判决地点
//            Matcher locMatcher = Pattern.compile("(中华人民共和国[\\u4e00-\\u9fa5]+法院)").matcher(content);
//            if (locMatcher.find()) {
//                fileRecord.setJudgeDistrict(locMatcher.group(1).replaceAll("\\s+", "").trim());
//            }
                String judgeType = null;
// 匹配标题中的“XX判决书/裁定书”（支持分行/同行格式）
                Matcher typeMatcher = Pattern.compile("(刑事|民事|行政)[\\s\\r\\n]*(判决|裁定)[\\s\\r\\n]*书").matcher(titleArea);
                if (typeMatcher.find()) {
                    judgeType = typeMatcher.group(1) + typeMatcher.group(2) + "书";
                    fileRecord.setJudgeType(judgeType);
                    System.out.println("成功解析标题中的判决类型：" + judgeType);
                } else {
                    System.out.println("未在标题区域匹配到判决类型");
                }

                // 案由
                String judgeCause = null;
// 1. 找“点击了解更多”这个固定锚点
                int anchorIndex = titleArea.indexOf("点击了解更多");
                if (anchorIndex != -1) {
                    // 2. 截取锚点上方所有内容，再按换行分割成多行
                    String contentBeforeAnchor = titleArea.substring(0, anchorIndex);
                    String[] lines = contentBeforeAnchor.split("[\\r\\n]+"); // 按任意换行符切分

                    // 3. 取上方最后一行（即“案  由	故意杀人”这行）
                    if (lines.length > 0) {
                        String targetLine = lines[lines.length - 1];
                        // 4. 按空白字符分割该行，取最后一个非空片段（就是案由内容）
                        String[] lineParts = targetLine.split("\\s+"); // 所有空白都能分割
                        for (int i = lineParts.length - 1; i >= 0; i--) {
                            String part = lineParts[i].trim();
                            if (!part.isEmpty()) { // 找到最后一个非空白片段
                                judgeCause = part;
                                break;
                            }
                        }
                    }
                }

// 赋值并日志
                if (judgeCause != null) {
                    fileRecord.setJudgeCause(judgeCause);
                    System.out.println("成功解析案由：" + judgeCause);
                } else {
                    System.out.println("未找到有效案由内容");
                }

            } catch (Exception e) {
                System.err.println("解析判决书信息失败：" + e.getMessage());
            }
        }

        // 新文件新增记录，重名文件更新原记录。
        if (overwrite) {
            fileMapper.updateById(fileRecord);
        } else {
            fileMapper.insert(fileRecord);
        }

        List<Center> centerList = centerMapper.selectList(new LambdaQueryWrapper<>());
        for (Center center : centerList) {
            try {
                // 准备 target 参数，可以根据实际情况选择 add, delete 或 update
                String target = overwrite ? "update" : "add";
                if (center.getUid().equals(my.getId())) {// 跳过自己
                    continue;
                }
                // 构建 WebClient 请求并发送 POST 请求
                agentWebClientService.agent2CenterWebClient(center.getUid())
                        .post()
                        .uri(UriBuilder -> UriBuilder.path("/directory/fileFolder/syncFile")
                                .queryParam("target", target).build())// 请求 URL
                        .bodyValue(fileRecord)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing center with UID: " + center.getUid());
                e.printStackTrace();
            }
        }

        // 结果
        return Body.success("文件上传成功");
    }

    // 更新文件
    public Body<String> updateFile(File file) {
        //
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, file.getUid())
                .eq(File::getFolderId, file.getFolderId());
        File new_file = fileMapper.selectOne(queryFileWrapper);
        if (new_file == null) {
            return Body.error("该文件不存在");
        }
        new_file.setName(file.getName());
        new_file.setDescription(file.getDescription());
        new_file.setExpiredTime(file.getExpiredTime());
        new_file.setExample(file.getExample());
        new_file.setLastUpdate(new Timestamp(System.currentTimeMillis()));
        new_file.setType(file.getType());
        new_file.setUid(file.getUid());
        new_file.setAgentId(file.getAgentId());
        new_file.setFolderId(file.getFolderId());
        UpdateWrapper<File> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("folder_id", file.getFolderId())
                .eq("uid", file.getUid());
        fileMapper.update(new_file, updateWrapper);

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
                        .uri(UriBuilder -> UriBuilder.path("/directory/fileFolder/syncFile")
                                .queryParam("target", target).build())// 请求 URL
                        .bodyValue(new_file)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing center with UID: " + center.getUid());
                e.printStackTrace();
            }
        }

        return Body.success("更新成功");
    }

    // 删除文件
    public Body<String> deleteFile(String fileId, String agentId, String folderId, String baseDirectory) {
        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId)
                .eq(File::getFolderId, folderId);
        File file = fileMapper.selectOne(queryFileWrapper);
        if (file == null) {
            return Body.error("该文件不存在");
        }
        // 构建文件夹路径
        List<String> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folderId, parentFolderIds, path);
        Collections.reverse(path); // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        fullPathBuilder.append(java.io.File.separator).append(file.getName());
        String folderPath = fullPathBuilder.toString();

        fileMapper.delete(queryFileWrapper);
        // 3.本地实际删除file
        java.io.File oldFolder = new java.io.File(folderPath);
        if (oldFolder.exists()) {
            boolean deleted = oldFolder.delete();
            if (!deleted) {
                return Body.error("本地文件删除失败");
            }
        } else {
            return Body.error("本地文件不存在或不是一个目录");
        }

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
                        .uri(UriBuilder -> UriBuilder.path("/directory/fileFolder/syncFile")
                                .queryParam("target", target).build())// 请求 URL
                        .bodyValue(file)
                        .retrieve() // 发起请求
                        .bodyToMono(String.class) // 处理返回响应，假设返回的 Body 是 String 类型
                        .block();
            } catch (Exception e) {
                // 处理可能的异常
                System.err.println("Error processing center with UID: " + center.getUid());
                e.printStackTrace();
            }
        }

        return Body.success("文件删除成功");

    }

    public void addCenter(Center center) {
        try {
            centerMapper.insert(center);
        } catch (Exception e) {
            System.err.println("Error processing center with UID: " + center.getUid());
        }
    }

    public void syncFolders2Center(String centerId) {
        try {
            List<Folder> folderList = folderMapper.selectList(null);
            agentWebClientService.agent2CenterWebClient(centerId)
                    .post()
                    .uri(UriBuilder -> UriBuilder.path("/directory/fileFolder/syncFolders")
                            .build())
                    .bodyValue(folderList)
                    .retrieve() // 发起请求
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            System.err.println("Error processing center with UID: " + centerId);
            e.printStackTrace();
        }
    }

    public void syncFiles2Center(String centerId) {
        try {
            List<File> fileList = fileMapper.selectList(null);
            agentWebClientService.agent2CenterWebClient(centerId)
                    .post()
                    .uri(UriBuilder -> UriBuilder.path("/directory/fileFolder/syncFiles")
                            .build())
                    .bodyValue(fileList)
                    .retrieve() // 发起请求
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            System.err.println("Error processing center with UID: " + centerId);
            e.printStackTrace();
        }
    }

    //
    public Body<String> syncFolder(Folder folder, String target) {

        // 检查输入参数
        if (folder == null || target == null || target.isEmpty()) {
            return Body.error("Invalid input: folder or target is null or empty.");
        }

        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folder.getUid());
        // 查看folder是否存在
        Folder existingFolder = folderMapper.selectOne(queryFolderWrapper);
        // 根据target不同进行不同操作 add delete update
        try {
            switch (target.toLowerCase()) {
                case "add":
                    if (existingFolder != null) {
                        return Body.error("Folder with the same UID already exists.");
                    }
                    folderMapper.insert(folder); // 插入新的 folder
                    return Body.success("Folder added successfully.");

                case "delete":
                    if (existingFolder == null) {
                        return Body.error("Folder not found. Cannot delete.");
                    }
                    folderMapper.delete(queryFolderWrapper); // 删除目标 folder
                    return Body.success("Folder deleted successfully.");

                case "update":
                    if (existingFolder == null) {
                        return Body.error("Folder not found. Cannot update.");
                    }
                    folderMapper.update(folder, queryFolderWrapper); // 更新 folder
                    return Body.success("Folder updated successfully.");

                default:
                    return Body.error("Invalid target action: " + target);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error("An error occurred: " + e.getMessage());
        }
    }

    public Body<String> syncFile(File file, String target) {

        // 检查输入参数
        if (file == null || target == null || target.isEmpty()) {
            return Body.error("Invalid input: file or target is null or empty.");
        }

        LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, file.getUid());
        // 查看file是否存在
        File existingFile = fileMapper.selectOne(queryFileWrapper);
        // 根据target不同进行不同操作 add delete update
        try {
            switch (target.toLowerCase()) {
                case "add":
                    if (existingFile != null) {
                        return Body.error("File with the same UID already exists.");
                    }
                    fileMapper.insert(file); // 插入新的 file
                    return Body.success("File added successfully.");

                case "delete":
                    if (existingFile == null) {
                        return Body.error("File not found. Cannot delete.");
                    }
                    fileMapper.delete(queryFileWrapper); // 删除目标 file
                    return Body.success("File deleted successfully.");

                case "update":
                    if (existingFile == null) {
                        return Body.error("File not found. Cannot update.");
                    }
                    fileMapper.update(file, queryFileWrapper); // 更新 file
                    return Body.success("File updated successfully.");

                default:
                    return Body.error("Invalid target action: " + target);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error("An error occurred: " + e.getMessage());
        }
    }

    public Body<String> syncFolders(List<Folder> folderList) {
        try {
            for (Folder folder : folderList) {
                LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                        .eq(Folder::getUid, folder.getUid());
                Folder existingFolder = folderMapper.selectOne(queryFolderWrapper);
                if (existingFolder == null) {
                    folderMapper.insert(folder); // 只插不存在的
                }
            }

            return Body.success("Folders added successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error("An error occurred: " + e.getMessage());
        }
    }

    public Body<String> syncFiles(List<File> fileList) {
        try {
            for (File file : fileList) {
                LambdaQueryWrapper<File> queryFileWrapper = Wrappers.<File>lambdaQuery()
                        .eq(File::getUid, file.getUid());
                File existingFile = fileMapper.selectOne(queryFileWrapper);
                if (existingFile == null) {
                    fileMapper.insert(file); // 只插不存在的
                }
            }
            return Body.success("Files added successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error("An error occurred: " + e.getMessage());
        }
    }

    //
    public void addAgent(Agent agent) {
        agentMapper.insert(agent);
    }

    // center连接新agent
    public Body<String> linkAgent(String agentId, String ip, Integer port) {
        try {
            Agent agent = new Agent();
            agent.setUid(agentId);
            agent.setIp(ip);
            agent.setPort(port);
            agentMapper.insert(agent);
            Center center = new Center();
            center.setUid(my.getId());
            center.setName(my.getName());
            center.setIp(my.getIp());
            center.setPort(my.getPort());
            centerWebClientService.center2AgentWebClient(agent.getUid())
                    .post()
                    .uri(UriBuilder -> UriBuilder.path("/directory/fileFolder/addCenter")
                            .build())
                    .bodyValue(center)
                    .retrieve() // 发起请求
                    .bodyToMono(String.class)
                    .block();

            centerWebClientService.center2AgentWebClient(agent.getUid())
                    .post()
                    .uri(UriBuilder -> UriBuilder.path("/directory/fileFolder/syncFolders2Center")
                            .queryParam("centerId", center.getUid())
                            .build())
                    .retrieve() // 发起请求
                    .bodyToMono(String.class)
                    .block();
            centerWebClientService.center2AgentWebClient(agent.getUid())
                    .post()
                    .uri(UriBuilder -> UriBuilder.path("/directory/fileFolder/syncFiles2Center")
                            .queryParam("centerId", center.getUid())
                            .build())
                    .retrieve() // 发起请求
                    .bodyToMono(String.class)
                    .block();

            return Body.success("Agent linked to center successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error(e.getMessage());
        }
    }

    private void findAllParentFolders(String folderId, List<String> parentFolderIds,
            List<String> path) {
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, folderId);
        Folder folder = folderMapper.selectOne(queryWrapper);
        if (folder != null) {
            path.add(folder.getName());
            if (folder.getParentId() != "-1") {
                parentFolderIds.add(folder.getParentId());
                findAllParentFolders(folder.getParentId(), parentFolderIds, path);
            }
        }
    }

    private void findAllChildFolders(String folderId, String agentId, List<String> childFolderIds) {
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getParentId, folderId);
        List<Folder> childFolders = folderMapper.selectList(queryWrapper);
        for (Folder folder : childFolders) {
            childFolderIds.add(folder.getUid());
            findAllChildFolders(folder.getUid(), agentId, childFolderIds);
        }
    }

    public String getFolderPath(Folder folder, String baseDirectory) {
        List<String> parentFolderIds = new ArrayList<>();
        List<String> path = new ArrayList<>();
        findAllParentFolders(folder.getUid(), parentFolderIds, path);
        // 构建文件夹路径
        Collections.reverse(path); // 反转路径列表，确保路径顺序正确
        StringBuilder fullPathBuilder = new StringBuilder(baseDirectory);
        for (String folderName : path) {
            fullPathBuilder.append(java.io.File.separator).append(folderName);
        }
        String oldFolderPath = fullPathBuilder.toString();
        return oldFolderPath;
    }

    public String getFilePath(File file, String baseDirectory) {

        LambdaQueryWrapper<Folder> queryFolderWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getUid, file.getFolderId());
        Folder folder = folderMapper.selectOne(queryFolderWrapper);
        String folderPath = getFolderPath(folder, baseDirectory);
        String filePath = folderPath + "/" + file.getName();
        return filePath;
    }

    public DirectoryInfo getDirectoryStructure(String rootFolderId) {
        Folder rootFolder = folderMapper.select(rootFolderId);
        DirectoryInfo root = new DirectoryInfo();
        mapFolderToDirectoryNode(rootFolder, root);
        root.setType("folder");
        buildDirectoryTree(root);
        return root;
    }

    private void buildDirectoryTree(DirectoryInfo node) {
        List<Folder> subFolders = folderMapper.selectByParentId(node.getUid());
        for (Folder subFolder : subFolders) {
            DirectoryInfo childNode = new DirectoryInfo();
            mapFolderToDirectoryNode(subFolder, childNode);
            childNode.setType("folder");
            node.getChildren().add(childNode);
            buildDirectoryTree(childNode);
        }

        List<File> files = fileMapper.selectByFolderId(node.getUid());
        for (File file : files) {
            DirectoryInfo childNode = new DirectoryInfo();
            mapFileToDirectoryNode(file, childNode);
            childNode.setType("file");
            node.getChildren().add(childNode);
        }
    }

    //
    private void mapFolderToDirectoryNode(Folder folder, DirectoryInfo node) {
        node.setUid(folder.getUid());
        node.setAgentId(folder.getAgentId());
        node.setParentId(folder.getParentId());
        node.setName(folder.getName());
        node.setCreateDate(folder.getCreateDate());
        node.setLastUpdate(folder.getLastUpdate());
    }

    //
    private void mapFileToDirectoryNode(File file, DirectoryInfo node) {
        node.setUid(file.getUid());
        node.setAgentId(file.getAgentId());
        node.setParentId(file.getFolderId());
        node.setName(file.getName());
        node.setCreateDate(file.getCreateDate());
        node.setLastUpdate(file.getLastUpdate());
        node.setAttribute(file.getAttribute());
        node.setSize(file.getSize());
        node.setDescription(file.getDescription());
        node.setHash(file.getHash());
        node.setExample(file.getExample());
        node.setExpiredTime(file.getExpiredTime());
        node.setFileType(file.getType());
    }

    //
    public ResponseEntity<Resource> sendFile(String fileId, String agentId, String folderId, String baseDirectory,
            Boolean chainMaker, String requestHash, String requestId) throws Exception {

        LambdaQueryWrapper<File> queryFolderWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId)
                .eq(File::getFolderId, folderId);
        File file = fileMapper.selectOne(queryFolderWrapper);
        String filePath = getFilePath(file, baseDirectory);

        // 读取文件路径
        Path path = Paths.get(filePath);
        // 文件名从filePath末尾截断获得
        String fileName = path.getFileName().toString();

        // 对文件名进行UTF-8编码以处理中文
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        // =========================上链模块=========================
        // if(chainMaker){
        // String responseMsgJson = JSON.toJSONString(resource);
        // //生成responseID
        // String responseId = generateUUID("response", "fileTransfer", my.getId());
        // //将响应进行上链操作
        // ContractResponse responseResponse =
        // upChainService.responseUpChain(requestHash,responseMsgJson,responseId,requestId,my.getId(),"agent");
        // }
        // ======================上链模块结束=========================

        // 发送文件
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .body(resource);

    }

    public Body<File> getFile(String fileId, String agentId, Boolean chainMaker, String requestHash, String requestId)
            throws Exception {
        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery().eq(File::getUid, fileId);
        File file = fileMapper.selectOne(queryWrapper);

        // ========================上链模块=====================
        // if(chainMaker){
        // String responseMsgJson = JSON.toJSONString(file);
        // //生成responseID
        // String responseId = generateUUID("response", "fileTransfer", my.getId());
        // //将响应进行上链操作
        // ContractResponse responseResponse =
        // upChainService.responseUpChain(requestHash,responseMsgJson,responseId,requestId,my.getId(),"agent");
        // }
        // =====================上链模块结束=====================

        return Body.success(file, "查询成功");
    }

    public String test() {
        return "1";
    }

    public Body<Long> getRowCount(String fileId, String agentId, String baseDirectory) {
        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId);
        File file = fileMapper.selectOne(queryWrapper);
        if (file == null) {
            return Body.error("找不到文件");
        }
        if (!file.getName().endsWith(".csv")) {
            return Body.error("非csv文件");
        } else {
            String filePath = getFilePath(file, baseDirectory);
            Long rowCount = Long.valueOf(0);
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                while (reader.readLine() != null) {
                    rowCount++;
                }
            } catch (IOException e) {
                return Body.error("读取文件时出错：" + e.getMessage());
            }
            return Body.success(rowCount, "成功获取行数");
        }
    }

    public Body<Folder> getFolder(String folderId, String agentId) {
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery().eq(Folder::getUid, folderId);
        Folder folder = folderMapper.selectOne(queryWrapper);
        return Body.success(folder, "查询成功");
    }

    public Body<DirectoryInfo> getDirectory2Agent(String agentId, String applicationId) {
        try {
            Body<DirectoryInfo> response = centerWebClientService.center2AgentWebClient(agentId).post()
                    .uri(uriBuilder -> uriBuilder.path("/directory/fileFolder/getDirectoryByApplication")
                            .queryParam("rootId", 1)
                            .queryParam("agentId", agentId)
                            .queryParam("applicationId", applicationId)
                            .build())// 将请求体设置为QueryRequest
                    .retrieve() // 准备接收响应
                    .bodyToMono(new ParameterizedTypeReference<Body<DirectoryInfo>>() {
                    }) // 指定返回类型
                    .block(); // 阻塞等待响应并获取结果
            return response;
        } catch (Exception e) {
            return Body.error(e.getMessage());
        }
    }

    public Body<Folder> getRoot() {
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery().eq(Folder::getParentId, -1);
        Folder folder = folderMapper.selectOne(queryWrapper);
        return Body.success(folder, "查询成功");
    }

    public Body<Folder> getRootByAgent(String agentId) {
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getParentId, -1)
                .eq(Folder::getAgentId, agentId);
        Folder folder = folderMapper.selectOne(queryWrapper);
        return Body.success(folder, "查询成功");
    }

    // 专门处理中文年份转换（确保“二〇二四”→2024，而非4）
    public String chineseYearToArabic(String chineseYear) {
        // 中文数字单字映射（每个字符对应1位阿拉伯数字）
        Map<Character, String> yearMap = new HashMap<>();
        yearMap.put('〇', "0");
        yearMap.put('零', "0");
        yearMap.put('一', "1");
        yearMap.put('二', "2");
        yearMap.put('三', "3");
        yearMap.put('四', "4");
        yearMap.put('五', "5");
        yearMap.put('六', "6");
        yearMap.put('七', "7");
        yearMap.put('八', "8");
        yearMap.put('九', "9");
        // 年份中不会出现“十”，若出现直接抛出错误
        if (chineseYear.contains("十")) {
            throw new NumberFormatException("年份中不应包含“十”：" + chineseYear);
        }

        // 拼接4位年份（逐个字符转换，确保完整）
        StringBuilder yearSb = new StringBuilder();
        for (char c : chineseYear.toCharArray()) {
            if (!yearMap.containsKey(c)) {
                throw new NumberFormatException("无效年份字符：" + c);
            }
            yearSb.append(yearMap.get(c));
        }

        // 确保年份是4位（避免“二四”→“24”这类异常，但正则已限制4位，此处兜底）
        if (yearSb.length() != 4) {
            throw new NumberFormatException("年份不是4位：" + chineseYear + "→" + yearSb);
        }
        return yearSb.toString();
    }

    // 处理月份/日期的中文数字转换（如“三”→3，“十二”→12）
    private String chineseNumToArabic(String chineseNum) {
        Map<Character, Integer> numMap = new HashMap<>();
        numMap.put('〇', 0);
        numMap.put('零', 0);
        numMap.put('一', 1);
        numMap.put('二', 2);
        numMap.put('三', 3);
        numMap.put('四', 4);
        numMap.put('五', 5);
        numMap.put('六', 6);
        numMap.put('七', 7);
        numMap.put('八', 8);
        numMap.put('九', 9);
        numMap.put('十', 10);

        int result = 0;
        int tempNum = 0;
        for (char c : chineseNum.toCharArray()) {
            if (!numMap.containsKey(c)) {
                throw new NumberFormatException("无效数字字符：" + c);
            }
            int currentNum = numMap.get(c);

            if (currentNum == 10) {
                tempNum = (tempNum == 0) ? 1 : tempNum;
                result += tempNum * 10;
                tempNum = 0;
            } else {
                tempNum = currentNum;
            }
        }
        result += tempNum;
        return String.valueOf(result);
    }

    /**
     * 根据fileId和agentId读取文件内容
     * @param fileId 文件ID
     * @param agentId 代理ID
     * @param baseDirectory 基础目录路径
     * @return 文件内容
     */
    public Body<String> readFileContent(String fileId, String agentId, String baseDirectory) {
        try {
            // 1. 获取文件信息
            LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery()
                    .eq(File::getUid, fileId);
            File file = fileMapper.selectOne(queryWrapper);
            
            if (file == null) {
                return Body.error("文件不存在，文件ID: " + fileId);
            }

            // 2. 获取文件路径
            String filePath = getFilePath(file, baseDirectory);
            String fileName = file.getName();
            
            // 3. 读取文件内容
            java.io.File fileObj = new java.io.File(filePath);
            if (!fileObj.exists()) {
                return Body.error("文件不存在，文件路径: " + filePath);
            }

            // 根据文件扩展名选择读取方式
            String extension = "";
            if (fileName != null && fileName.contains(".")) {
                extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            }

            String content;
            switch (extension) {
                case "txt":
                case "text":
                    content = readTxtFile(fileObj);
                    break;
                case "csv":
                    content = readCsvFile(fileObj);
                    break;
                case "json":
                    content = readJsonFile(fileObj);
                    break;
                default:
                    // 默认按文本文件读取
                    content = readTxtFile(fileObj);
                    break;
            }

            return Body.success(content, "读取成功");

        } catch (Exception e) {
            return Body.error("读取文件失败: " + e.getMessage());
        }
    }

    /**
     * 读取文本文件
     */
    private String readTxtFile(java.io.File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * 读取CSV文件（简化版本，按文本读取）
     */
    private String readCsvFile(java.io.File file) throws IOException {
        // 简化实现：按文本文件读取
        // 如果需要更复杂的CSV解析，可以添加Apache Commons CSV依赖
        return readTxtFile(file);
    }

    /**
     * 读取JSON文件
     */
    private String readJsonFile(java.io.File file) throws IOException {
        try {
            JsonNode jsonNode = objectMapper.readTree(file);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (Exception e) {
            // 如果JSON解析失败，按文本文件读取
            return readTxtFile(file);
        }
    }
}
