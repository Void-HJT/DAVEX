package DveAgent.module.comparison;

import DveBase.common.My;
import DveBase.common.R;
import DveBase.entity.File;
import DveBase.info.TableHeader;
import DveBase.mapper.FileMapper;
import DveBase.service.directory.FileFolderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.opencsv.CSVReader;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ComparisonService {

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private My my;

    @Autowired
    private FileFolderService fileFolderService;

    public R<TableHeader> getCsvHeader(Integer fileId, Integer folderId, Integer agentId) {

        // 查找文件
        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId)
                .eq(File::getFolderId, folderId)
                .eq(File::getAgentId, agentId);
        File queryFile = fileMapper.selectOne(queryWrapper);
        if (queryFile == null) {
            return R.error(String.format("找不到该文件，文件id: %d，文件夹id: %d", fileId, folderId));
        }
        String filePath = fileFolderService.getFilePath(queryFile, my.getBase_path());

        // 解析CSV文件的表头和第一条记录
        try (CSVReader csvReader = new CSVReader(new FileReader(filePath))) {
            // 读取表头
            String[] header = csvReader.readNext();
            if (header == null) {
                return R.error("CSV文件为空或者表头信息无法读取");
            }

            // 读取第一条记录
            String[] firstRecord = csvReader.readNext();
            if (firstRecord == null) {
                return R.error("CSV文件没有包含记录信息");
            }

            // 创建TableHeader对象并设置表头和第一条记录
            TableHeader tableHeader = new TableHeader();
            tableHeader.setName(Arrays.asList(header));  // 将表头转换为List<String>
            tableHeader.setExample(Arrays.asList(firstRecord));  // 将第一条记录转换为List<String>

            return R.success(tableHeader, "解析成功");
        } catch (Exception e) {
            return R.error("解析CSV文件时出错: " + e.getMessage());
        }
    }

    public R<List<String>> getHash(Integer fileId, Integer folderId, Integer agentId,
                                         List<String> attributes) {

        // 查找文件
        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId)
                .eq(File::getFolderId, folderId)
                .eq(File::getAgentId, agentId);
        File queryFile = fileMapper.selectOne(queryWrapper);
        if (queryFile == null) {
            return R.error(String.format("找不到该文件，文件id: %d，文件夹id: %d", fileId, folderId));
        }
        String filePath = fileFolderService.getFilePath(queryFile, "");

        List<String> hashResults = new ArrayList<>();
        String delimiter = "|";  // 分隔符

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 读取CSV文件的表头
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return R.error("CSV文件内容为空");
            }

            // 分割表头以获取每列的名称
            List<String> headers = Arrays.asList(headerLine.split(","));

            // 确定要处理的列的索引
            List<Integer> attributeIndices = new ArrayList<>();
            for (String attribute : attributes) {
                int index = headers.indexOf(attribute);
                if (index == -1) {
                    return R.error("找不到属性: " + attribute);
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
            return R.error("读取CSV文件出错: " + e.getMessage());
        }

        return R.success(hashResults, "获取哈希成功");
    }
}
