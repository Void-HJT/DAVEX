package DveAgent.module.comparison;

import DveBase.common.R;
import DveBase.entity.File;
import DveBase.info.TableHeader;
import DveBase.mapper.FileMapper;
import DveBase.service.directory.FileFolderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ComparisonService {

    @Autowired
    private FileMapper fileMapper;

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
        String filePath = fileFolderService.getFilePath(queryFile, "");

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
}
