package com.btea.lexiflow.common.cache;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 事务提交后执行
 */
@Component
public class AfterCommitExecutor {

    /**
     * 在当前事务提交成功后执行指定操作，无事务时立即执行
     *
     * @param action 待执行操作
     */
    public void execute(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
