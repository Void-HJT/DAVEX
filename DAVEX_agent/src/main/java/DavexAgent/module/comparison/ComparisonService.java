package DavexAgent.module.comparison;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import DavexBase.info.FileInfo;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.opencsv.CSVReader;

import DavexBase.common.Body;
import DavexBase.common.My;
import DavexBase.entity.File;
import DavexBase.info.TableHeader;
import DavexBase.mapper.FileMapper;
import DavexBase.service.directory.FileFolderService;

@Service
public class ComparisonService {

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private My my;

    @Autowired
    private FileFolderService fileFolderService;

    public Body<TableHeader> getCsvHeader(String fileId, String folderId, String agentId) {

        // 查找文件
        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId)
                .eq(File::getFolderId, folderId)
                .eq(File::getAgentId, agentId);
        File queryFile = fileMapper.selectOne(queryWrapper);
        if (queryFile == null) {
            return Body.error(String.format("找不到该文件，文件id: %d，文件夹id: %d", fileId, folderId));
        }
        String filePath = fileFolderService.getFilePath(queryFile, my.getBase_path());

        // 解析CSV文件的表头和第一条记录
        try (CSVReader csvReader = new CSVReader(new FileReader(filePath))) {
            // 读取表头
            String[] header = csvReader.readNext();
            if (header == null) {
                return Body.error("CSV文件为空或者表头信息无法读取");
            }

            // 读取第一条记录
            String[] firstRecord = csvReader.readNext();
            if (firstRecord == null) {
                return Body.error("CSV文件没有包含记录信息");
            }

            // 创建TableHeader对象并设置表头和第一条记录
            TableHeader tableHeader = new TableHeader();
            tableHeader.setName(Arrays.asList(header));  // 将表头转换为List<String>
            tableHeader.setExample(Arrays.asList(firstRecord));  // 将第一条记录转换为List<String>

            return Body.success(tableHeader, "解析成功");
        } catch (Exception e) {
            return Body.error("解析CSV文件时出错: " + e.getMessage());
        }
    }

    public Body<List<String>> getTXTExample(String fileId, String folderId, String agentId){
        // 查找文件
        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId)
                .eq(File::getFolderId, folderId)
                .eq(File::getAgentId, agentId);
        File queryFile = fileMapper.selectOne(queryWrapper);
        if (queryFile == null) {
            return Body.error(String.format("找不到该文件，文件id: %d，文件夹id: %d", fileId, folderId));
        }
        String filePath = fileFolderService.getFilePath(queryFile, my.getBase_path());

        List<String> firstLine = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // 读取第一行
            String line = br.readLine();

            if (line != null) {
                // 使用空格分隔第一行的内容，并将其添加到 firstLine 中
                String[] dataArray = line.split("\\s+");  // "\\s+" 匹配一个或多个空格

                // 将分割后的数据放入 List
                for (String data : dataArray) {
                    firstLine.add(data);
                }
            }
            return Body.success(firstLine, "解析成功");
        } catch (IOException e) {
            return Body.error("解析txt文件时出错: " + e.getMessage());
        }
    }

    public Body<List<String>> getHash(String fileId, String folderId, String agentId,
                                         List<String> attributes) {

        // 查找文件
        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId)
                .eq(File::getFolderId, folderId)
                .eq(File::getAgentId, agentId);
        File queryFile = fileMapper.selectOne(queryWrapper);
        if (queryFile == null) {
            return Body.error(String.format("找不到该文件，文件id: %d，文件夹id: %d", fileId, folderId));
        }
        String filePath = fileFolderService.getFilePath(queryFile, my.getBase_path());

        List<String> hashResults = new ArrayList<>();
        String delimiter = "|";  // 分隔符

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 读取CSV文件的表头
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return Body.error("CSV文件内容为空");
            }

            // 分割表头以获取每列的名称
            List<String> headers = Arrays.asList(headerLine.split(","));
            // 确定要处理的列的索引
            List<Integer> attributeIndices = new ArrayList<>();
            for (String attribute : attributes) {
                int index = headers.indexOf(attribute);
                if (index == -1) {
                    return Body.error("找不到属性: " + attribute);
                }
                attributeIndices.add(index);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                StringBuilder sb = new StringBuilder();

                // 提取出指定列的值，并用分隔符拼接成一个字符串
                for (int index : attributeIndices) {
                    if (sb.length() > 0) {
                        sb.append(delimiter);  // 追加分隔符
                    }
                    sb.append(values[index]);
                }

                // 计算拼接字符串的SHA-256哈希值
                String hash = DigestUtils.sha256Hex(sb.toString());
                hashResults.add(hash);
            }
        } catch (IOException e) {
            return Body.error("读取CSV文件出错: " + e.getMessage());
        }
        return Body.success(hashResults, "获取哈希成功");
    }

    public Body<List<String>> getTXTHash(String fileId, String folderId, String agentId) {

        // 查找文件
        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId)
                .eq(File::getFolderId, folderId)
                .eq(File::getAgentId, agentId);
        File queryFile = fileMapper.selectOne(queryWrapper);
        if (queryFile == null) {
            return Body.error(String.format("找不到该文件，文件id: %d，文件夹id: %d", fileId, folderId));
        }
        String filePath = fileFolderService.getFilePath(queryFile, my.getBase_path());

        List<String> hashResults = new ArrayList<>();
        String delimiter = "|";  // 分隔符

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(" ");
                StringBuilder sb = new StringBuilder();

                // 提取出每行的各个值，并用分隔符拼接成一个字符串
                for (String value : values) {
                    if (sb.length() > 0) {
                        sb.append(delimiter);  // 追加分隔符
                    }
                    sb.append(value);
                }

                // 计算拼接字符串的SHA-256哈希值
                String hash = DigestUtils.sha256Hex(sb.toString());
                hashResults.add(hash);
            }
        } catch (IOException e) {
            return Body.error("读取txt文件出错: " + e.getMessage());
        }
        return Body.success(hashResults, "获取哈希成功");
    }

    public Body<String> getFileName(String fileId, String folderId, String agentId) {
        FileInfo fileInfo = fileFolderService.getFileInfo(fileId, agentId, folderId).getData();
        return Body.success(fileInfo.getName(), "获取成功");
    }
}
