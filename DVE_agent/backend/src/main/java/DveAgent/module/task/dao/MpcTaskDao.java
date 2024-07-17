package DveAgent.module.task.dao;

import DveAgent.entity.MpcTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MpcTaskDao extends JpaRepository<MpcTask, Long> {
}