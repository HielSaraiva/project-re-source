package edu.br.resource.resourcesystem.observability;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class TransactionLog {
    private TransactionLog() {
    }

    public static void afterCommit(Logger logger, String message, Object... arguments) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive())
            throw new IllegalStateException("Transaction logging requires an active transaction.");
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                Map<String, String> previous = MDC.getCopyOfContextMap();
                try {
                    if (context == null)
                        MDC.clear();
                    else
                        MDC.setContextMap(context);
                    logger.info(message, arguments);
                } finally {
                    if (previous == null)
                        MDC.clear();
                    else
                        MDC.setContextMap(previous);
                }
            }
        });
    }
}
