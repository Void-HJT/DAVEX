package DavexBase.service.programs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import DavexBase.common.My;
import DavexBase.common.Utils;
import DavexBase.entity.Mpc;
import DavexBase.entity.MpcTask;
import DavexBase.info.Parameter;
import DavexBase.mapper.MpcMapper;
import DavexBase.mapper.MpcTaskMapper;

@Service
public class GarnetService {

    private My my;

    private final File garnet_directory;

    private static final Logger logger = LoggerFactory.getLogger(GarnetService.class);

    @Autowired
    private MpcMapper mpcMapper;

    @Autowired
    private MpcTaskMapper mpcTaskMapper;

    public GarnetService(My my) {
        this.my = my;
        garnet_directory = new File(this.my.getGarnet_path());
    }

    // 这行注释可以不检查garnet
    @EventListener(ApplicationReadyEvent.class)
    @Async("customExecutor")
    public void init() {
        ProcessBuilder makeBuilder = new ProcessBuilder("make").directory(garnet_directory);
        ProcessBuilder pipBuilder = new ProcessBuilder("pip", "install", "-r", "requirements.txt")
                .directory(garnet_directory);
        ProcessBuilder mkdirInputBuilder = new ProcessBuilder("mkdir", "Input").directory(garnet_directory);
        ProcessBuilder mkdirOutputBuilder = new ProcessBuilder("mkdir", "Output").directory(garnet_directory);
        try {
            makeBuilder.start();
            pipBuilder.start();
            mkdirInputBuilder.start();
            mkdirOutputBuilder.start();
            logger.info("Garnet初始化成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Garnet初始化失败:" + e.getMessage());
        }
    }

    public void idExtract(String inputPath, String prefix, Long part) throws Exception {
        Path input = Paths.get(my.getBase_path()).resolve(inputPath);
        String outputFilePath = garnet_directory.getAbsolutePath() + "/Input/" + prefix + "-P" + part + "-0";
        try (BufferedReader reader = Files.newBufferedReader(input);
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int intValue;
                try {
                    intValue = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    intValue = Utils.hashStringToInt(line);
                }
                writer.write(String.valueOf(intValue));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void link(String path, String prefix, Long part) throws Exception {
        List<String> command = new ArrayList<>(Arrays.asList("ln", "-s", path,
                garnet_directory.getAbsolutePath() + "/Input/" + prefix + "-P" + part + "-0"));
        ProcessBuilder processBuilder = new ProcessBuilder(command).directory(garnet_directory);
        try {
            Process process = processBuilder.start();
            Integer exitcode = process.waitFor();
            if (exitcode != 0) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    StringBuilder errorMsg = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorMsg.append(line).append(System.lineSeparator());
                    }
                    if (errorMsg.length() > 0) {
                        logger.error(errorMsg.toString());
                        throw new Exception(errorMsg.toString());
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }

    }

    public void compile(MpcTask mpcTask) throws Exception {
        JSONObject task_parameter = mpcTask.getCompileParameters();
        Mpc mpc = mpcMapper.selectById(mpcTask.getMpcId());
        List<Parameter> mpc_parameters = mpc.getCompileParameters();
        Map<Integer, String> args = new HashMap<>();
        List<String> flags = new ArrayList<>();
        for (Parameter p : mpc_parameters) {
            switch (p.getParameterType()) {
                case POS: {
                    String value = task_parameter.getString(p.getName());
                    if (value == null && p.getRequired()) {
                        throw new IllegalArgumentException("缺少参数: " + p.getName());
                    } else if (value != null) {
                        args.put((Integer) p.getPosORflag(), value);
                    } else if (!p.getRequired()) {
                        args.put((Integer) p.getPosORflag(), p.getDefaultValue());
                    }
                    break;
                }
                case FLAG: {
                    String value = task_parameter.getString(p.getName());
                    if (value == null && p.getRequired()) {
                        throw new IllegalArgumentException("缺少参数: " + p.getName());
                    } else if (value != null) {
                        flags.add((String) p.getPosORflag() + " " + value);
                    } else if (!p.getRequired()) {
                        flags.add((String) p.getPosORflag() + " " + p.getDefaultValue());
                    }
                    break;
                }
                case HYPER:
                default:
                    break;
            }
        }
        Path mpc_path = Paths.get(my.getBase_path()).resolve(mpc.getPath());
        String mpc_name = mpc_path.getFileName().toString().split("\\.")[0];
        List<String> command = new ArrayList<>(Arrays.asList("python3", "compile.py", mpc_path.toString()));

        for (int i = 0; i < args.size(); i++) {
            command.add(args.get(i));
            mpc_name += "-" + args.get(i);
        }
        command.addAll(flags);
        ProcessBuilder processBuilder = new ProcessBuilder(command).directory(garnet_directory);
        logger.info("运行命令：" + command.toString());
        try {
            mpcTask.setStatus(MpcTask.Status.COMPILING);
            logger.info(mpcTask.getUid() + ":开始编译");
            mpcTaskMapper.updateById(mpcTask);
            Process process = processBuilder.start();
            int exitcode = process.waitFor();
            if (exitcode != 0) {
                mpcTask.setStatus(MpcTask.Status.FAILED);
                mpcTaskMapper.updateById(mpcTask);
                logger.error(mpcTask.getUid() + ":编译失败");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    StringBuilder errorMsg = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorMsg.append(line).append(System.lineSeparator());
                    }
                    if (errorMsg.length() > 0) {

                        logger.error(errorMsg.toString());
                        throw new Exception(errorMsg.toString());
                    }
                }
            }
            mpcTask.setStatus(MpcTask.Status.READY);
            mpcTask.setMpcName(mpc_name);
            mpcTaskMapper.updateById(mpcTask);
            logger.info(mpcTask.getUid() + ":编译成功");
        } catch (Exception e) {
            mpcTask.setStatus(MpcTask.Status.FAILED);
            mpcTaskMapper.updateById(mpcTask);
            logger.error(mpcTask.getUid() + ":编译失败");
            e.printStackTrace();
        }
    }

    public void run(MpcTask mpcTask) throws Exception {
        String inputPrefix = garnet_directory.getAbsolutePath() + "/Input/" + mpcTask.getUid();
        String outputPrefix = garnet_directory.getAbsolutePath() + "/Output/" + mpcTask.getUid();
        String protocol = mpcTask.getRuntimeParameters().getString("protocol");
        String mpc_name = mpcTask.getMpcName();
        Long part = mpcTask.getPart();
        List<String> command = new ArrayList<>(Arrays.asList(
                "./" + protocol + ".x",
                "-IF", inputPrefix,
                "-OF", outputPrefix,
                // "-N", mpcTask.getN().toString(),
                "-h", mpcTask.getHost(),
                "-pn", mpcTask.getPort().toString(),
                "-p", part.toString(),
//                "-u",
                mpc_name));
        ProcessBuilder processBuilder = new ProcessBuilder(command).directory(garnet_directory);
        logger.info("运行命令：" + command.toString());
        try {
            mpcTask.setStatus(MpcTask.Status.RUNNING);
            logger.info(mpcTask.getUid() + ":开始运行");
            mpcTaskMapper.updateById(mpcTask);
            Process process = processBuilder.start();
            int exitcode = process.waitFor();
            if (exitcode != 0) {
                mpcTask.setStatus(MpcTask.Status.FAILED);
                mpcTaskMapper.updateById(mpcTask);
                logger.error(mpcTask.getUid() + ":运行失败");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    StringBuilder errorMsg = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorMsg.append(line).append(System.lineSeparator());
                    }
                    if (errorMsg.length() > 0) {
                        logger.error(errorMsg.toString());
                        throw new Exception(errorMsg.toString());
                    }
                }
            }
            mpcTask.setStatus(MpcTask.Status.FINISHED);
            mpcTaskMapper.updateById(mpcTask);
            logger.info(mpcTask.getUid() + ":运行成功");
        } catch (Exception e) {
            mpcTask.setStatus(MpcTask.Status.FAILED);
            mpcTaskMapper.updateById(mpcTask);
            logger.error(mpcTask.getUid() + ":运行失败");
            e.printStackTrace();
        }
    }

    public void csvExtract(String inputCsvPath, String fieldName, String prefix, Long part) {
        String outputFilePath = garnet_directory.getAbsolutePath() + "/Input/" + prefix + "-P" + part + "-0";
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputCsvPath));
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV文件为空");
            }
            String[] headers = headerLine.split(",");
            int fieldIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equals(fieldName)) {
                    fieldIndex = i;
                    break;
                }
            }
            if (fieldIndex == -1) {
                throw new IllegalArgumentException("字段名未找到: " + fieldName);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length > fieldIndex) {
                    String fieldValue = fields[fieldIndex];
                    int intValue;
                    try {
                        intValue = Integer.parseInt(fieldValue);
                    } catch (NumberFormatException e) {
                        intValue = Utils.hashStringToInt(fieldValue);
                    }
                    writer.write(String.valueOf(intValue));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void csvQuery(String inputCsvPath, String prefix, String fieldName, Long part, Path outputCsvPath) {
        Path fieldFilePath = Paths.get(garnet_directory.getAbsolutePath() + "/Output/" + prefix + "-P" + part + "-0");
        try (BufferedReader csvReader = Files.newBufferedReader(Paths.get(inputCsvPath));
                BufferedReader fieldReader = Files.newBufferedReader(fieldFilePath);
                BufferedWriter csvWriter = Files.newBufferedWriter(outputCsvPath)) {

            // 读取字段文件并存储在集合中
            Set<Integer> fieldValues = new HashSet<>();
            String fieldLine;
            while ((fieldLine = fieldReader.readLine()) != null) {
                try {
                    fieldValues.add(Integer.parseInt(fieldLine));
                } catch (NumberFormatException e) {
                    fieldValues.add(Utils.hashStringToInt(fieldLine));
                }
            }

            // 读取CSV文件头部
            String headerLine = csvReader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV文件为空");
            }
            csvWriter.write(headerLine);
            csvWriter.newLine();
            String[] headers = headerLine.split(",");
            int fieldIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equals(fieldName)) {
                    fieldIndex = i;
                    break;
                }
            }
            if (fieldIndex == -1) {
                throw new IllegalArgumentException("字段名未找到: " + fieldName);
            }
            // 逐行读取CSV文件并匹配字段值
            String line;
            while ((line = csvReader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length > fieldIndex) {
                    String fieldValue = fields[fieldIndex];
                    int intValue;
                    try {
                        intValue = Integer.parseInt(fieldValue);
                    } catch (NumberFormatException e) {
                        intValue = Utils.hashStringToInt(fieldValue);
                    }
                    if (fieldValues.contains(intValue)) {
                        csvWriter.write(line);
                        csvWriter.newLine();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Long csvCount(String inputCsvPath) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(my.getBase_path()).resolve(inputCsvPath))) {
            Long count = reader.lines().count();
            return count;
        } catch (IOException e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
