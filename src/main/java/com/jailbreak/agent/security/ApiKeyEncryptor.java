package com.jailbreak.agent.security;

/**
 * API Key 加密器 —— AES-256 对称加密。
 * <p>
 * 调用方:
 * <ul>
 *   <li>{@code TaskController.createTask()} — 落库前加密</li>
 *   <li>{@code AttackTaskService.start()} — 调用目标 API 前解密</li>
 * </ul>
 * <p>
 * 约束: 解密后的明文 API Key 仅在内存中短暂使用，调用完成后立即清零。
 */
public interface ApiKeyEncryptor {

    /**
     * AES-256 加密。
     *
     * @param plainText 明文 API Key
     * @return Base64 编码的密文
     */
    String encrypt(String plainText);

    /**
     * AES-256 解密。
     * 调用方应尽快使用解密结果并在用完后清除引用。
     *
     * @param cipherText Base64 编码的密文
     * @return 明文 API Key
     */
    String decrypt(String cipherText);
}
