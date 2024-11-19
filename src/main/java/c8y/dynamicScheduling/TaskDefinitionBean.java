package c8y.dynamicScheduling;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TaskDefinitionBean implements Runnable {

    private TaskDefinition taskDefinition;

    @Override
    public void run() {
        log.info("Running action: " + taskDefinition.getActionType());
        log.info("With Data: " + taskDefinition.getData());
        log.info("With Data: " + taskDefinition.getUuid());
    }

    public TaskDefinition getTaskDefinition() {
        return taskDefinition;
    }
  
    public void setTaskDefinition(TaskDefinition taskDefinition) {
        this.taskDefinition = taskDefinition;
    }
}