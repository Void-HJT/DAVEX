package DveAgent.module.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import DveAgent.entity.MpcTask;
import DveAgent.module.task.service.MpcTaskService;
import java.util.Optional;

@RestController
@RequestMapping("/api/mpcTask") // 添加基路径，使 API 更加清晰
public class MpcTaskController {

    @Autowired
    private MpcTaskService mpcTaskService;

    @PostMapping("/add")
    public ResponseEntity<String> addMpcTask(@RequestBody MpcTask mpcTask) {
        try {
            Long mpcTaskId = mpcTask.getUid();

            if (mpcTaskId != null) {
                Optional<MpcTask> existingMpcTask = mpcTaskService.getMpcTaskById(mpcTaskId);
                if (existingMpcTask.isPresent()) {
                    boolean result = mpcTaskService.saveMpcTask(mpcTask);
                    if (result) {
                        return ResponseEntity.ok("Task successfully updated.");
                    } else {
                        return ResponseEntity.status(500).body("Failed to update task.");
                    }
                }
            } 
            
            boolean result = mpcTaskService.saveMpcTask(mpcTask);
            if (result) {
                return ResponseEntity.ok("Task successfully added.");
            } else {
                return ResponseEntity.status(500).body("Failed to add task.");
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }
}