package DavexBase.common.docker;

import java.util.List;

import com.github.dockerjava.api.command.InspectExecResponse;
import org.springframework.stereotype.Component;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback.Adapter;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;

@Component
public class DockerExecutor {

    private final DockerClient dockerClient;

    public DockerExecutor() {
        this.dockerClient = DockerClientBuilder.getInstance().build();
    }

    /**
     * 在容器内部执行一条命令
     *
     * @param containerId 容器ID
     * @param cwd         容器内工作文件夹
     * @param commands    执行的命令
     * @return 命令结果
     * @throws Exception 执行出错或命令返回错误
     */
    public String exec(String containerId, String cwd, List<String> commands) throws Exception {
        var cmd = dockerClient.execCreateCmd(containerId)
                .withWorkingDir(cwd)
                .withCmd(commands.toArray(new String[0]))
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .exec();
        var callback = new ExecResultCallback();
        dockerClient.execStartCmd(cmd.getId())
                .exec(callback).awaitCompletion();
//        if (callback.getStderr().length() > 0) {
//            throw new Exception(callback.getStderr());
//        } else {
//            return callback.getStdout();
//        }
        // 获取命令的退出状态
        InspectExecResponse execResponse = dockerClient.inspectExecCmd(cmd.getId()).exec();
        int exitCode = execResponse.getExitCode();
        // 检查退出状态是否为成功
        if (exitCode != 0) {
            throw new Exception("命令执行失败，退出码: " + exitCode + "，错误信息: " + callback.getStderr());
        }
        return callback.getStdout();
    }

    /**
     * 检查容器状态
     *
     * @param containerId 容器ID
     * @return 容器状态
     * @throws Exception 容器不存在
     */
    public InspectContainerResponse get_info(String containerId) throws Exception {
        try {
            return dockerClient.inspectContainerCmd(containerId).exec();
        } catch (Exception e) {
            throw new Exception("Container not found");
        }

    }

    private static class ExecResultCallback extends Adapter<Frame> {
        private final StringBuilder stdout = new StringBuilder();
        private final StringBuilder stderr = new StringBuilder();

        @Override
        public void onNext(Frame frame) {
            if (frame != null) {
                switch (frame.getStreamType()) {
                    case STDOUT:
                        stdout.append(new String(frame.getPayload()));
                        break;
                    case STDERR:
                        stderr.append(new String(frame.getPayload()));
                        break;
                    default:
                        // 忽略其他流
                }
            }
        }

        public String getStdout() {
            return stdout.toString();
        }

        public String getStderr() {
            return stderr.toString();
        }
    }
}
