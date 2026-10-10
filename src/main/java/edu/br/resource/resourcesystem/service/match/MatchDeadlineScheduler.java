package edu.br.resource.resourcesystem.service.match;

import edu.br.resource.resourcesystem.observability.FailureDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import edu.br.resource.resourcesystem.repository.MatchDeadlineRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchDeadlineScheduler {
    private final MatchDeadlineRepository deadlines;
    private final MatchWorkflowService workflow;

    @Scheduled(fixedDelayString = "${resource.match.expiry-delay-ms:30000}", initialDelay = 5000)
    public void cancelExpired() {
        List<String> protocols = deadlines.findExpiredProtocols();
        if (!protocols.isEmpty()) log.debug("event=expiry_batch candidates={}", protocols.size());
        for (String protocol : protocols)
            try {
                workflow.expire(protocol);
            } catch (RuntimeException failure) {
                log.error(
                        "event=expiry_failed protocol={} failure={}",
                        protocol,
                        FailureDetails.describe(failure));
            }
    }
}
