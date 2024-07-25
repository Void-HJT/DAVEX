package DveCenter.module.task;

import DveCenter.common.My;
import DveAgent.entity.Mpc;
import DveCenter.module.auth.service.CenterWebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import DveAgent.entity.MpcTask;
import DveAgent.entity.Agent;
import DveAgent.entity.MpcTaskAgent;
import DveCenter.entity.Input;
import DveCenter.module.task.MpcTaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;


@RestController
@RequestMapping("/MpcTasks")
public class TaskController {

    @Autowired
    private MpcTaskService mpcTaskService;

    @Autowired
    CenterWebClientService centerWebClientService;

    @Autowired
    My my;

    @PostMapping("createMpcTask")
    public MpcTask createMpcTask(@RequestBody MpcTask mpcTask , @RequestParam String path ) {
        //先填好Input
        long applicationId = mpcTask.getApplicationId();
        Input input  = new Input();
        input.setApplicationId(applicationId);
        input.setPath(path);
        mpcTaskService.createInput(input);
        //再填好mpcTask表
        mpcTaskService.createMpcTask(mpcTask);
        return mpcTask;
    }

    @PostMapping("/{mpcTaskId}/agents")
    //根据mpcTaskId将agents添加到MpcTaskAgent表中
    //是否需要返回数据后面再考虑吧
    //centerId也要一起加进去 CenterId和MpcTaskId共同确定一个mpcTask
    public void addAgentToMPCTask(@PathVariable Long mpcTaskId, @RequestBody List<MpcTaskAgent> agents) {

        try {
            long centerId = my.getId();
            mpcTaskService.addAgentToMPCTask(mpcTaskId, centerId,agents);
            //如果存成功了 就把这几个表发送给对应的agent 还得发mpcTask表 然后agent接到以后去填自己本地的表项（这一步要怎么做？）
            //将agents转化为需要传输的字符串
            //获取mpcTaskId对应的mpcTask的内容并转化为json
            MpcTask mpcTask = mpcTaskService.getMpcTaskById(mpcTaskId);

            for(MpcTaskAgent agent :agents) {
                WebClient client = centerWebClientService.center2AgentWebClient((int) agent.getAgentId());
                client.post().uri("/MpcTasks/" + mpcTaskId + "/receiveAgentTable").bodyValue(agents).retrieve().bodyToMono(void.class).block();
                client.post().uri("/MpcTasks/" + mpcTaskId + "/receiveMpcTaskTable").bodyValue(mpcTask).retrieve().bodyToMono(MpcTask.class).block();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("{agentId}/{mpcTaskId}/ready")
    public MpcTask checkTaskStatus(@PathVariable Long agentId, @PathVariable Long mpcTaskId){
        long centerId = my.getId();
        //调用通信接口
        long agentIdLong = agentId.longValue();
        //agent那边需要进行查询，并返回值，这是个异步的过程？那边需要一个接口吗 查询某个任务的状态 这个状态存在哪里？ 要么也在mpcTask表中
        try{
            WebClient client = centerWebClientService.center2AgentWebClient((int)agentIdLong);
            //需要传两个参数，一个是centerId 一个是taskId
            MpcTask mpcTask = client.post().uri("/MpcTasks/" + mpcTaskId + "/checkTaskStatus").bodyValue(centerId).retrieve().bodyToMono(MpcTask.class).block();
            System.out.println("这条信息将输出到控制台的标准输出流"+mpcTask.getStatus());
            //获取到了状态信息，做进一步的判断
            return mpcTask;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("{mpcTaskId}/execute")
    public void runTask(@PathVariable Long mpctaskId){
        //根据taskId 和 centerId查task agent表 确认需要发的agent有哪几个
        long centerId = my.getId();
        List<MpcTaskAgent> agents = mpcTaskService.getAgentsByMpcTaskIdAndCenterId(mpctaskId,centerId);

        //给每个agent发check信息
        try{
            for(MpcTaskAgent agent :agents) {
                if(checkTaskStatus(agent.getAgentId(),mpctaskId).getStatus()!="ready"){
                    //如果有任意一方没有准备就绪 结束并返回错误
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        //check无误后执行本地任务
        MpcTask mpcTask = mpcTaskService.getTaskByMpcTaskIdAndCenterId(mpctaskId,centerId);


        //并给agent发请求也执行他们的任务
        try{
            for(MpcTaskAgent agent :agents) {
                    WebClient client = centerWebClientService.center2AgentWebClient((int) agent.getAgentId());
                    //需要mpcTaskId和centerId唯一确认一个执行的任务
                    long mpcTaskIdLong = mpctaskId.longValue();
                    client.post().uri("/MpcTasks/" + mpcTaskIdLong + "/execute").bodyValue(centerId).retrieve().bodyToMono(void.class).block();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}