package com.jailbreak.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 上下文感知越狱代理 —— 红队安全评估平台入口。
 * <p>
 * 模块结构（共 10 个核心模块）:
 * <ol>
 *   <li>攻击编排引擎 — {@code orchestration}</li>
 *   <li>策略决策引擎 — {@code strategy}</li>
 *   <li>攻击执行引擎 — {@code execution}</li>
 *   <li>评估引擎 — {@code evaluation}</li>
 *   <li>攻击向量库 — {@code vector}</li>
 *   <li>上下文污染引擎 — {@code contamination}</li>
 *   <li>报告生成引擎 — {@code report}</li>
 *   <li>事件推送引擎 — {@code event}</li>
 *   <li>会话/任务管理 — {@code session}</li>
 *   <li>安全合规模块 — {@code security}</li>
 * </ol>
 * 横切关注点: 成本控制引擎 — {@code cost}
 */
@SpringBootApplication
public class ContextAwareJailbreakAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContextAwareJailbreakAgentApplication.class, args);
    }
}
