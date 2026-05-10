package com.jailbreak.agent.controller;

import com.jailbreak.agent.cost.CostGuard;
import com.jailbreak.agent.model.*;
import com.jailbreak.agent.report.ReportService;
import com.jailbreak.agent.session.AttackTaskService;
import com.jailbreak.agent.event.EventStreamService;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final AttackTaskService taskService;
    private final EventStreamService eventStreamService;
    private final ReportService reportService;
    private final CostGuard costGuard;

    public TaskController(AttackTaskService taskService,
                          EventStreamService eventStreamService,
                          ReportService reportService,
                          CostGuard costGuard) {
        this.taskService = taskService;
        this.eventStreamService = eventStreamService;
        this.reportService = reportService;
        this.costGuard = costGuard;
    }

    @PostMapping("/create")
    public Map<String, String> createTask(@Valid @RequestBody CreateTaskRequest request) {
        String taskId = taskService.createTask(request);
        return Map.of("task_id", taskId);
    }

    @PostMapping("/{id}/start")
    public Map<String, String> startTask(@PathVariable String id) {
        taskService.start(id);
        return Map.of("status", "started");
    }

    @PostMapping("/{id}/pause")
    public Map<String, String> pauseTask(@PathVariable String id) {
        taskService.pause(id);
        return Map.of("status", "paused");
    }

    @PostMapping("/{id}/resume")
    public Map<String, String> resumeTask(@PathVariable String id) {
        taskService.resume(id);
        return Map.of("status", "resumed");
    }

    @PostMapping("/{id}/stop")
    public Map<String, String> stopTask(@PathVariable String id) {
        taskService.stop(id);
        return Map.of("status", "stopped");
    }

    @GetMapping("/list")
    public List<TaskSummary> getTaskList() {
        return taskService.getTaskSummaries();
    }

    @GetMapping("/{id}/report")
    public AttackReport getTaskReport(@PathVariable String id) {
        AttackState state = taskService.getTaskState(id);
        if (state == null) {
            throw new IllegalArgumentException("Task not found: " + id);
        }
        return reportService.generateReport(state);
    }

    @PostMapping("/preflight")
    public PreFlightResult preFlightCheck(@Valid @RequestBody CreateTaskRequest request) {
        UserQuota quota = costGuard.getUserQuota("default");
        return costGuard.preFlightCheck(request, quota);
    }

    @GetMapping(value = "/{id}/execute-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter executeStream(@PathVariable String id) {
        SseEmitter emitter = new SseEmitter(300_000L);
        eventStreamService.register(id, emitter);
        return emitter;
    }
}
