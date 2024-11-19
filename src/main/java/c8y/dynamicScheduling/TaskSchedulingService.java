package c8y.dynamicScheduling;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class TaskSchedulingService {

    private ThreadPoolTaskScheduler taskScheduler;

    private Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    @PostConstruct
    public void init() {
        // Initialize the task scheduler if necessary
        taskScheduler.setPoolSize(2);
        initializeSchedules();
    }

    public void createScheduleTask(TaskDefinition taskDefinition) {
        log.info("Scheduling created task with job id: " + taskDefinition.getUuid() + " and cron expression: " + taskDefinition.getCronExpression());
        ScheduledFuture<?> scheduledTask = taskScheduler.schedule(() -> printParameter(taskDefinition.getUuid(), taskDefinition.getData()), new CronTrigger(taskDefinition.getCronExpression(), TimeZone.getTimeZone(TimeZone.getDefault().getID())));
        scheduledTasks.put(taskDefinition.getUuid(), scheduledTask);
    }

    public void removeScheduledTask(String jobId) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.get(jobId);
        if(scheduledTask != null) {
            scheduledTask.cancel(true);
            scheduledTasks.put(jobId, null);
        }
    }

    public void updateScheduledTask(TaskDefinition taskDefinition) {
        removeScheduledTask(taskDefinition.getUuid());
        createScheduleTask(taskDefinition);
    }

    public Map<String, ScheduledFuture<?>> getScheduledTasks() {
        return scheduledTasks;
    }

    private void printParameter(String taskId, String parameter) {
        log.info("Task ID: " + taskId + " - Parameter: " + parameter);
        //call method to 
    }

    private void initializeSchedules(){
        //call Inventory API list all the schedules based on the type and status
        //loop over
        //call the method -scheduleATask()
    }
    
}