package c8y.dynamicScheduling;

import lombok.Data;


@Data
public class TaskDefinition {

    private String cronExpression;
    private String actionType;
    private String data;
    private String uuid;
}