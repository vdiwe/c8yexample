package c8y.dynamicScheduling;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@RestController
@RequestMapping(path = "/schedule")
@AllArgsConstructor
public class JobSchedulingController {

    private TaskSchedulingService taskSchedulingService;

    @PostMapping(path="", consumes = "application/json", produces="application/json")
    public void scheduleATask(@RequestBody TaskDefinition taskDefinition) {
        log.info("Requested Schedule Definition: {}", taskDefinition);
       taskSchedulingService.createScheduleTask(taskDefinition);
    }

    @PutMapping(path="", consumes = "application/json", produces="application/json")
    public void updateScheduleTask(@RequestBody TaskDefinition taskDefinition) {
        log.info("Requested Schedule Definition: {}", taskDefinition);
       taskSchedulingService.updateScheduledTask(taskDefinition);
    }

    @DeleteMapping(path="/{jobId}")
    public void removeJob(@PathVariable String jobId) {
        taskSchedulingService.removeScheduledTask(jobId);
    }

    @GetMapping(path="")
    public Map<String, ScheduledFuture<?>> getJob() {
        return taskSchedulingService.getScheduledTasks();
    }
}

