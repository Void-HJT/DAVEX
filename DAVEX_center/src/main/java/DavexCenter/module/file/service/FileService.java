package DavexCenter.module.file.service;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.*;

//import java.io.File; 命名冲突，使用全限定名
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import DavexBase.common.My;
import DavexBase.common.ContractResponse;
import DavexBase.service.auth.CenterWebClientService;
import DavexBase.service.blockchain.UpChainService;
import DavexBase.service.notification.NotificationService;
import DavexCenter.common.CustomMultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import DavexBase.common.Body;
import DavexBase.entity.File;
import DavexCenter.entity.DownloadTask;
import DavexCenter.entity.Output;
import DavexCenter.mapper.DownloadTaskMapper;
import DavexCenter.mapper.OutputMapper;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import static DavexBase.common.UUIDGenerator.generateUUID;

import com.hankcs.hanlp.HanLP;

@Service
public class FileService {

    @Autowired
    private OutputMapper outputMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DownloadTaskMapper downloadTaskMapper;
    @Autowired
    private My my;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CenterWebClientService centerWebClientService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UpChainService upChainService;

    public Body<String> saveFile(MultipartFile file, File fileInfo, String applicationId,
            java.sql.Timestamp expiredTime) throws IOException {

        // 校验sha256
        String fileHash = getSha256(file);
        if (!fileHash.equals(fileInfo.getHash())) {
            return Body.error(String.format("哈希校验失败，文件id: %s，代理id: %s",
                    fileInfo.getUid(), fileInfo.getAgentId()));
        }

        // 文件信息加入结果表
        // 若已存在，则进行覆盖
        LambdaQueryWrapper<Output> queryWrapper1 = Wrappers.<Output>lambdaQuery()
                .eq(Output::getFileId, fileInfo.getUid())
                .eq(Output::getAgentId, fileInfo.getAgentId())
                .eq(Output::getApplicationId, applicationId)
                .eq(Output::getHash, fileHash);
        Output queryOutput = outputMapper.selectOne(queryWrapper1);
        if (queryOutput != null) {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            outputMapper.deleteById(queryOutput.getUid());
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }

        Output newOutput = new Output();
        String fileName = fileInfo.getName();
        newOutput.setName(fileName);
        newOutput.setType(fileInfo.getType());
        newOutput.setUploadDate(Timestamp.valueOf(LocalDateTime.now()));
        newOutput.setAttribute(fileInfo.getAttribute());
        newOutput.setSize(fileInfo.getSize());
        newOutput.setDescription(fileInfo.getDescription());
        newOutput.setPath(Paths.get(my.getBase_path()).resolve("result").resolve("common")
                .resolve(fileHash + "_appid_" + applicationId).toString());
        newOutput.setExpiredTime(expiredTime);
        newOutput.setHash(fileHash);
        newOutput.setFileId(fileInfo.getUid());
        newOutput.setAgentId(fileInfo.getAgentId());
        newOutput.setApplicationId(applicationId);
        outputMapper.insert(newOutput);
        String filePath = newOutput.getPath();

        // 存储文件到结果管理区
        try {
            saveFileToPath(file, filePath);
            // 提取高频词
            String fileContent = readFileContent(filePath, fileName).getData();
            Map<String, Integer> wordFrequency = extractWordFrequency(fileContent);
            String highFrequencyWords = String.join(",", wordFrequency.keySet());
            newOutput.setTag(highFrequencyWords);
            outputMapper.updateById(newOutput);
        } catch (IOException e) {
            e.printStackTrace();
            return Body.error(String.format("保存失败: 文件id %s，代理id: %s，文件名: %s，错误信息: %s",
                    fileInfo.getUid(), fileInfo.getAgentId(), fileName, e.getMessage()));
        }
        return Body.success(String.format("保存成功，文件id: %s，代理id: %s，文件名: %s",
                fileInfo.getUid(), fileInfo.getAgentId(), fileName));
    }

//    public Body<List<String>> saveFiles(List<MultipartFile> files, List<File> fileInfos, String applicationId,
//            List<java.sql.Timestamp> expiredTimes) {
//        List<String> results = new ArrayList<>();
//
//        if (files.size() != fileInfos.size() || files.size() != expiredTimes.size()) {
//            return Body.error("文件数量、文件信息数量和失效时间数量不匹配");
//        }
//
//        boolean flag = true;
//        for (int i = 0; i < files.size(); i++) {
//            MultipartFile file = files.get(i);
//            File fileInfo = fileInfos.get(i);
//            java.sql.Timestamp expiredTime = expiredTimes.get(i);
//
//            // 校验md5
//            String fileHash = getSha256(file);
//            if (!fileHash.equals(fileInfo.getHash())) {
//                results.add(String.format("哈希校验失败，文件id: %s，代理id: %s", fileInfo.getUid(), fileInfo.getAgentId()));
//                flag = false;
//                continue;
//            }
//
//            // 文件信息加入结果表
//            // 若已存在，则进行覆盖
//            LambdaQueryWrapper<Output> queryWrapper1 = Wrappers.<Output>lambdaQuery()
//                    .eq(Output::getFileId, fileInfo.getUid())
//                    .eq(Output::getAgentId, fileInfo.getAgentId())
//                    .eq(Output::getApplicationId, applicationId)
//                    .eq(Output::getHash, fileHash);
//            Output queryOutput = outputMapper.selectOne(queryWrapper1);
//            if (queryOutput != null) {
//                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
//                outputMapper.deleteById(queryOutput.getUid());
//                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
//            }
//
//            Output newOutput = new Output();
//            String fileName = fileInfo.getName();
//            newOutput.setName(fileInfo.getName());
//            newOutput.setType(fileInfo.getType());
//            newOutput.setUploadDate(Timestamp.valueOf(LocalDateTime.now()));
//            newOutput.setAttribute(fileInfo.getAttribute());
//            newOutput.setSize(fileInfo.getSize());
//            newOutput.setDescription(fileInfo.getDescription());
//            newOutput.setPath(
//                    Paths.get(my.getBase_path()).resolve("result").resolve("common").resolve(fileHash + "_appid_" + applicationId).toString());
//            newOutput.setExpiredTime(expiredTime);
//            newOutput.setHash(fileHash);
//            newOutput.setFileId(fileInfo.getUid());
//            newOutput.setAgentId(fileInfo.getAgentId());
//            newOutput.setApplicationId(applicationId);
//            outputMapper.insert(newOutput);
//            String filePath = newOutput.getPath();
//
//            // 存储文件到结果管理区
//            try {
//                saveFileToPath(file, filePath);
//                results.add(String.format("保存成功，文件id: %s，代理id: %s，文件名: %s",
//                        fileInfo.getUid(), fileInfo.getAgentId(), fileName));
//            } catch (IOException e) {
//                e.printStackTrace();
//                results.add(String.format("保存失败: 文件id %s，代理id: %s，文件名: %s，错误信息: %s",
//                        fileInfo.getUid(), fileInfo.getAgentId(), fileName, e.getMessage()));
//                flag = false;
//            }
//        }
//
//        if (flag == false) {
//            return Body.error(results, "部分文件保存失败");
//        }
//        return Body.success(results, "文件保存处理完成");
//    }

    public Body<String> fetchFileByHttp(Long outputId, String applicationId, HttpServletResponse response) {

        // 根据结果id查找结果表
        LambdaQueryWrapper<Output> queryWrapper = Wrappers.<Output>lambdaQuery()
                .eq(Output::getUid, outputId)
                .eq(Output::getApplicationId, applicationId);
        Output queryOutput = outputMapper.selectOne(queryWrapper);
        if (queryOutput == null) {
            return Body.error(String.format("找不到该文件，结果id: %d", outputId));
        }
        // 判断文件是否过期
        Timestamp expiredTime = queryOutput.getExpiredTime();
        if (expiredTime != null && LocalDateTime.now().isAfter(expiredTime.toLocalDateTime())) {
            return Body.error(String.format("该文件已过期，结果id: %d，文件名: %s，失效时间: %s", outputId, queryOutput.getName(),
                    queryOutput.getExpiredTime()));
        }

        // 添加下载任务记录到任务表
        DownloadTask newDownloadTask = new DownloadTask();
        newDownloadTask.setApplicationId(applicationId);
        newDownloadTask.setOutputId(queryOutput.getUid());
        newDownloadTask.setDownloadTime(Timestamp.valueOf(LocalDateTime.now()));
        newDownloadTask.setType("common");
        downloadTaskMapper.insert(newDownloadTask);

        // 新建文件流，从磁盘读取文件流
        String filePath = queryOutput.getPath();
        String fileName = queryOutput.getName();
        try (FileInputStream fis = new FileInputStream(filePath);
                BufferedInputStream bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream()) { // OutputStream 是文件写出流，将文件下载到浏览器客户端
            // 新建字节数组，长度是文件的大小，比如文件 6kb, bis.available() = 1024 * 6
            byte[] bytes = new byte[bis.available()];
            // 从文件流读取字节到字节数组中
            bis.read(bytes);
            // 重置 response
            response.reset();
            // 设置 response 的下载响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8")); // 这里要设置文件名的编码，否则中文的文件名下载后不显示
            // 写出字节数组到输出流
            os.write(bytes);
            // 刷新输出流
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error(String.format("获取失败: 结果id %d，文件名: %s，错误信息: %s", outputId, fileName, e.getMessage()));
        }
        return Body.success(String.format("获取成功，结果id: %d，文件名: %s", outputId, fileName));
    }

    public Body<String> fetchFile(Long outputId, String applicationId) {

        // 根据结果id查找结果表
        LambdaQueryWrapper<Output> queryWrapper = Wrappers.<Output>lambdaQuery()
                .eq(Output::getUid, outputId)
                .eq(Output::getApplicationId, applicationId);
        Output queryOutput = outputMapper.selectOne(queryWrapper);
        if (queryOutput == null) {
            return Body.error(String.format("找不到该文件，结果id: %d", outputId));
        }
        // 判断文件是否过期
        Timestamp expiredTime = queryOutput.getExpiredTime();
        if (expiredTime != null && LocalDateTime.now().isAfter(expiredTime.toLocalDateTime())) {
            return Body.error(String.format("该文件已过期，结果id: %d，文件名: %s，失效时间: %s", outputId, queryOutput.getName(),
                    queryOutput.getExpiredTime()));
        }

        // 添加下载任务记录到任务表
        String filePath = queryOutput.getPath();
        String fileName = queryOutput.getName();
        Path downloadPath = Paths.get(my.getBase_path()).resolve("download").resolve(applicationId).resolve("common");
        DownloadTask newDownloadTask = new DownloadTask();
        newDownloadTask.setApplicationId(applicationId);
        newDownloadTask.setOutputId(queryOutput.getUid());
        newDownloadTask.setDownloadTime(Timestamp.valueOf(LocalDateTime.now()));
        newDownloadTask.setType("common");
        newDownloadTask.setPath(downloadPath.resolve(fileName).toString());
        downloadTaskMapper.insert(newDownloadTask);

        // 直接通过路径访问文件
        try {
//            copyFile(filePath, fileName, downloadPath.toString());
            Files.createDirectories(downloadPath);
            Files.copy(Paths.get(filePath), downloadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error(String.format("获取失败: 结果id %d，文件名: %s，错误信息: %s", outputId, fileName, e.getMessage()));
        }
        return Body.success(String.format("获取成功，结果id: %d，文件名: %s，保存路径: %s", outputId, fileName, newDownloadTask.getPath()));
    }

    public Body<List<Output>> queryFile(String applicationId) {

        LambdaQueryWrapper<Output> queryWrapper = Wrappers.<Output>lambdaQuery()
                .eq(Output::getApplicationId, applicationId)
                .orderByDesc(Output::getUploadDate);
        List<Output> outputs = outputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }

    public Body<List<Output>> queryFileByIds(String applicationId, List<Long> outputIds) {

        LambdaQueryWrapper<Output> queryWrapper = Wrappers.<Output>lambdaQuery()
                .eq(Output::getApplicationId, applicationId)
                .in(Output::getUid, outputIds)
                .orderByDesc(Output::getUploadDate);

        List<Output> outputs = outputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }

    public Body<String> deleteFile(String applicationId, Long outputId) {

        // 根据文件id查找结果表
        LambdaQueryWrapper<Output> queryWrapper = Wrappers.<Output>lambdaQuery()
                .eq(Output::getApplicationId, applicationId)
                .eq(Output::getUid, outputId);
        Output queryOutput = outputMapper.selectOne(queryWrapper);
        if (queryOutput == null) {
            return Body.error(String.format("找不到该文件，结果id: %d", outputId));
        }
        String filePath = queryOutput.getPath();
        String fileName = queryOutput.getName();

        // 禁用外键检查
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // 删除文件及结果表
        try {
            outputMapper.deleteById(outputId);
            deleteFileFromPath(filePath);
            // 启用外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (Exception e) {
            // 确保在异常情况下重新启用外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            e.printStackTrace();
            return Body.error(String.format("删除失败: 结果id %d，文件名: %s，错误信息: %s", outputId, fileName, e.getMessage()));
        }

        return Body.success(String.format("删除成功，结果id: %d，文件名: %s", outputId, fileName));
    }

    public String getSha256(MultipartFile file) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(file.getBytes());
            byte[] digest = md.digest();
            String mySha256 = DatatypeConverter
                    .printHexBinary(digest).toLowerCase();

            return mySha256;
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveFileToPath(MultipartFile file, String filePath) throws IOException {
        // 新建一个文件路径
        java.io.File uploadFile = new java.io.File(filePath);
        // 当父级目录不存在时，自动创建
        if (!uploadFile.getParentFile().exists()) {
            uploadFile.getParentFile().mkdirs();
        }
        // 存储文件到电脑磁盘
        // file.transferTo(uploadFile);
        FileUtils.copyInputStreamToFile(file.getInputStream(), uploadFile);
    }

    public void csvToJson(String filePath) throws IOException {
        java.io.File csvFile = new java.io.File(filePath);

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
        MappingIterator<Map<String, String>> it = csvMapper.readerFor(Map.class).with(csvSchema).readValues(csvFile);

        List<Map<String, String>> data = it.readAll();
        ObjectMapper jsonMapper = new ObjectMapper();
        String json = jsonMapper.writeValueAsString(data);

        try (FileWriter fileWriter = new FileWriter(csvFile)) {
            fileWriter.write(json);
        }
    }

    public void copyFile(String filePath, String fileName, String downloadPath) throws Exception {
        var source = new java.io.File(filePath);
        var dest = new java.io.File(Paths.get(downloadPath).resolve(fileName).toString());
        Files.createDirectories(dest.getParentFile().toPath());
        try (var fis = new FileInputStream(source);
                var fos = new FileOutputStream(dest)) {

            byte[] buffer = new byte[1024];
            int length;

            while ((length = fis.read(buffer)) > 0) {

                fos.write(buffer, 0, length);
            }
        }
    }

    public void deleteFileFromPath(String filePath) throws Exception {
        java.io.File file = new java.io.File(filePath);
        // 路径是个文件且不为空时删除文件
        if (file.isFile() && file.exists()) {
            file.delete();
        }
    }

    public Body<String> readFile(String applicationId, Long outputId) throws IOException {
        LambdaQueryWrapper<Output> queryWrapper = Wrappers.<Output>lambdaQuery()
                .eq(Output::getApplicationId, applicationId)
                .eq(Output::getUid, outputId);
        Output queryOutput = outputMapper.selectOne(queryWrapper);
        String filePath = queryOutput.getPath();
        String fileName = queryOutput.getName();

        return readFileContent(filePath, fileName);
    }

    public Body<String> readFileContent(String filePath, String fileName) throws IOException {

        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            return Body.error(String.format("文件不存在，文件路径: %s", filePath));
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return switch (extension.toLowerCase()) {
            case "txt" -> Body.success(readTxtFile(file), "读取成功");
            case "csv" -> Body.success(readCsvFile(file), "读取成功");
            case "pdf" -> Body.success(readPdfFile(file), "读取成功");
            case "json" -> Body.success(readJsonFile(file), "读取成功");
//            default -> Body.error(String.format("不支持的文件类型: %s", extension));
            default -> Body.success(readTxtFile(file), "读取成功");
        };
    }

    private String readTxtFile(java.io.File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private String readCsvFile(java.io.File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             CSVParser parser = CSVFormat.DEFAULT
                     .withDelimiter(',')  // 指定分隔符，例如：制表符
                     .withFirstRecordAsHeader() // 明确第一行为表头
                     .parse(reader)) {

            // 表头信息
            content.append("表头:\n");
            if (parser.getHeaderNames() != null) {
                content.append(String.join("\t", parser.getHeaderNames()));
                content.append("\n");
            }
            content.append("\n"); // 空行分隔

            // 内容信息
            content.append("内容:\n");
            for (CSVRecord record : parser) {
                // 添加记录分隔线
                content.append("------------\n");
                for (String field : record) {
                    content.append(field).append("\t");
                }
                content.append("\n");
            }
        }
        return content.toString();
    }

    private String readPdfFile(java.io.File file) throws IOException {
        PDDocument document = PDDocument.load(file);
        PDFTextStripper stripper = new PDFTextStripper();
        String content = stripper.getText(document);
        document.close();
        // 清理多余的空白字符和换行符
        content = content.replaceAll("\\s+", " ").trim();
        // 保留中英文标点符号，去除其他特殊字符
        content = content.replaceAll("[^\\p{Punct}\\p{IsAlphabetic}\\p{IsDigit}\\u4E00-\\u9FFF\\s]", "");
        return content;
    }

    private String readJsonFile(java.io.File file) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(file);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    public static Map<String, Integer> extractWordFrequency(String content) {
        // 停用词集合
        Set<String> stopWords = new HashSet<>(Arrays.asList("的", "了", "和", "在", "是", "有", "被", "于", "与", "及", "其", "中", "对"));

        // 使用 HanLP 分词
        List<String> words = HanLP.segment(content).stream()
                .map(term -> term.word) // 获取分词结果
                .filter(word -> word.length() > 1) // 去掉单字
                .filter(word -> !stopWords.contains(word)) // 过滤停用词
                .collect(Collectors.toList());

        // 统计词频
        Map<String, Integer> wordCountMap = new HashMap<>();
        for (String word : words) {
            wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
        }

        // 根据词频排序，返回前 N 个高频词
        Map<String, Integer> res = wordCountMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10) // 限制返回高频词数量
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
        System.out.println(res);
        return res;
    }

    public CompletableFuture<Body<String>> getFile(String fileId, String agentId, String folderId, String applicationId) throws Exception {

        //========================上链模块-请求文件信息上链========================
//        String centerId = my.getId();
//        String requestId = generateUUID("request", "fileTransfer", centerId);
//        String requestMsg = "{" +
//                "\"fileId\": \"" + fileId + "\", " +
//                "\"agentId\": \"" + agentId + "\"" +
//                "\"folderId\": \"" + folderId + "\"" +
//                "\"application\": \"" + applicationId + "\", " +
//                "\"center\": \"" + centerId + "\", " +
//                "}";
//        String fileDescription = centerId + "向" + agentId + "请求文件信息" + fileId;
//
//        ContractResponse respectResponse = upChainService.requestUpChain(requestId,fileDescription,requestMsg,centerId,"center");
//        Map<String, Object> resultMap = (Map<String, Object>) respectResponse.getData();
//        String requestHash = resultMap.get("sharing_setting_hash").toString();
        //========================上链模块结束===================================

        WebClient webclient = centerWebClientService.center2AgentWebClient(agentId);
        File fileInfo = webclient.post()
                .uri(uriBuilder -> uriBuilder.path("/directory/fileFolder/getFile")
                        .queryParam("fileId", fileId)
                        .queryParam("agentId", agentId)
                        //========================上链时需传递的参数========================
//                        .queryParam("chainMaker", true)
//                        .queryParam("requestHash", requestHash)
//                        .queryParam("requestId", requestId)
                        //========================上链参数结束=============================
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Body<File>>() {
                }).block().getData();

        //========================上链模块-请求下载文件上链========================
//        String requestId1 = generateUUID("request", "fileTransfer", centerId);
//        String fileDescription1 = centerId + "向" + agentId + "请求下载文件" + fileId;
//        ContractResponse respectResponse1 = upChainService.requestUpChain(requestId1,fileDescription1,requestMsg,centerId,"center");
//        Map<String, Object> resultMap1 = (Map<String, Object>) respectResponse1.getData();
//        String requestHash1 = resultMap1.get("sharing_setting_hash").toString();
        //========================上链模块结束===================================

        Flux<byte[]> fileFlux = webclient.post().uri(uriBuilder -> uriBuilder.path("/directory/fileFolder/sendFile")
                        .queryParam("fileId", fileId)
                        .queryParam("agentId", agentId)
                        .queryParam("folderId", folderId)
                        //========================上链时需传递的参数========================
//                        .queryParam("chainMaker", true)
//                        .queryParam("requestHash", requestHash1)
//                        .queryParam("requestId", requestId1)
                        //========================上链参数结束=============================
                        .build()).accept(MediaType.APPLICATION_OCTET_STREAM).retrieve()
                .bodyToFlux(byte[].class);

        // 这里创建一个 CompletableFuture 对象来处理异步结果
        CompletableFuture<Body<String>> future = new CompletableFuture<>();

        fileFlux.collectList().subscribe(bytesList -> {
            try {
                // 将字节数组列表合并为一个完整的字节数组
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                for (byte[] bytes : bytesList) {
                    byteArrayOutputStream.write(bytes);
                }
                byte[] fileBytes = byteArrayOutputStream.toByteArray();

                // 创建 CustomMultipartFile，这里文件名随意
                CustomMultipartFile multipartFile = new CustomMultipartFile(fileBytes, "test1.txt");

                java.sql.Timestamp expiredTime = java.sql.Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));

                // 调用 save 方法
                Body<String> result = saveFile(multipartFile, fileInfo, applicationId,expiredTime);
                String content;
                if (result.getCode() == 1) {
                    content = String.format("文件传输任务完成\n代理: %s\n文件名: %s",
                            agentId, fileInfo.getName());
                } else {
                    content = String.format("文件传输任务失败\n代理: %s\n文件名: %s\n错误信息: %s",
                            agentId, fileInfo.getName(), result.getMessage());
                }
                notificationService.setMessage(applicationId, "文件传输任务结束", content, null, result.getCode(), "fileTransfer", true);
                future.complete(result);
            } catch (IOException e) {
                future.completeExceptionally(e);
                e.printStackTrace();
            }
        });

        return future;
    }
}
