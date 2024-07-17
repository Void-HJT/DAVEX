package DveAgent.module.task.controller;

import DveAgent.entity.MpcTask;
import DveAgent.module.task.service.MpcTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mpcTask")
public class MpcTaskController {
    @Autowired
    private MpcTaskService mpcTaskService;

    @GetMapping
    public List<MpcTask> getAllMpcTasks() {
        return mpcTaskService.findAll();
    }

    @GetMapping("/{id}")
    public MpcTask getMpcTaskById(@PathVariable Long id) {
        return mpcTaskService.findById(id);
    }

    @PostMapping
    public MpcTask createMpcTask(@RequestBody MpcTask mpcTask) {
        return mpcTaskService.save(mpcTask);
    }

    @PutMapping("/{id}")
    public MpcTask updateMpcTask(@PathVariable Long id, @RequestBody MpcTask mpcTask) {
        mpcTask.setId(id);
        return mpcTaskService.save(mpcTask);
    }

    @DeleteMapping("/{id}")
    public void deleteMpcTask(@PathVariable Long id) {
        mpcTaskService.deleteById(id);
    }
}
