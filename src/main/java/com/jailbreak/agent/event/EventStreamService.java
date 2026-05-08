package com.jailbreak.agent.event;

import com.jailbreak.agent.model.ExecutionEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 事件推送引擎 —— SSE 事件流管理接口。
 * <p>
 * 管理 SseEmitter 的注册/推送/注销，支持:
 * <ul>
 *   <li>多端同时观看同一任务（Set&lt;SseEmitter&gt; 广播）</li>
 *   <li>连接断开后的事件补发（事件缓冲队列重放）</li>
 *   <li>SseEmitter 超时自动重新注册</li>
 * </ul>
 * <p>
 * 调用方: 攻击编排引擎（每个节点完成时 push 事件）。
 * 消费方: 前端 EventSource。
 */
public interface EventStreamService {

    /**
     * 注册一个 SseEmitter 到指定会话/任务。
     * 新连接注册后会立即补发历史事件（从事件缓冲队列重放）。
     *
     * @param sessionOrTaskId 会话 ID 或任务 ID
     * @param emitter         要注册的 SseEmitter 实例
     */
    void register(String sessionOrTaskId, SseEmitter emitter);

    /**
     * 推送事件到指定会话/任务的所有已注册 emitter。
     *
     * @param sessionOrTaskId 会话 ID 或任务 ID
     * @param event           要推送的执行事件
     */
    void push(String sessionOrTaskId, ExecutionEvent event);

    /**
     * 注销指定会话/任务的所有 emitter。
     *
     * @param sessionOrTaskId 会话 ID 或任务 ID
     */
    void unregister(String sessionOrTaskId);

    /**
     * 获取指定会话/任务的事件历史（用于重放）。
     *
     * @param sessionOrTaskId 会话 ID 或任务 ID
     * @return 事件历史列表，按时间升序
     */
    java.util.List<ExecutionEvent> getEventHistory(String sessionOrTaskId);
}
