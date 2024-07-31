package DveCenter.module.task.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.alibaba.fastjson.JSONObject;

import DveAgent.entity.Mpc;
import DveAgent.entity.MpcTask;
import DveAgent.info.CompileParameter;
import DveAgent.mapper.MpcMapper;
import DveAgent.mapper.MpcTaskMapper;
import DveCenter.common.My;

@Async("customExecutor")
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

    @EventListener(ApplicationReadyEvent.class)
    @Async("customExecutor")
    public void init() {
        String[] command = { "make", ";", "pip", "install", "-r", "requirements.txt" };
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(garnet_directory);
        try {
            processBuilder.start();
            logger.info("Garnet初始化成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Garnet初始化失败:" + e.getMessage());
        }
    }

    public void link(String path, String prefix, Integer part) throws Exception {
        List<String> command = new ArrayList<>(Arrays.asList("ln", "-s", path,
                garnet_directory.getAbsolutePath() + "/Input/" + prefix + "-P" + part + "-0"));
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(garnet_directory);
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
        JSONObject parameter = mpcTask.getCompileParameters();
        Mpc mpc = mpcMapper.selectById(mpcTask.getMpcId());
        List<CompileParameter> parameters = mpc.getParameters();
        Map<Integer, String> args = new HashMap<>();
        List<String> flags = new ArrayList<>();
        for (CompileParameter p : parameters) {
            switch (p.getParameterType()) {
                case ARG:
                    args.put((Integer) p.getValue(), parameter.getString(p.getName()));
                    break;
                case FLAG:
                    flags.add(parameter.getString(p.getName()));
                    break;
            }
        }
        File mpc_file = ResourceUtils.getFile("classpath:" + mpc.getPath());
        String mpc_path = mpc_file.getAbsolutePath();
        String mpc_name = mpc_file.getName().split("\\.")[0];
        List<String> command = new ArrayList<>(Arrays.asList("python", "compile.py", mpc_path));

        for (int i = 0; i < args.size(); i++) {
            command.add(args.get(i));
            mpc_name += "-" + args.get(i);
        }
        command.addAll(flags);
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(garnet_directory);
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
        Integer part=mpcTask.getPart();
        List<String> command = new ArrayList<>(Arrays.asList(
                "./" + protocol + ".x",
                "-IF", inputPrefix,
                "-OF", outputPrefix,
                "-N", mpcTask.getN().toString(),
                "-h", mpcTask.getHost(),
                "-pn", mpcTask.getPort().toString(),
                "-p", part.toString(),
                mpc_name));
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(garnet_directory);

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
}
