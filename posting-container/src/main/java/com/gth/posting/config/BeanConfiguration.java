package com.gth.posting.config;

import com.gth.posting.application.port.out.clock.TimeProvider;
import com.gth.posting.application.usecase.enforcegrouppolicy.EnforceGroupPostingPolicyUseCase;
import com.gth.posting.domain.service.BatchSelectionPolicy;
import com.gth.posting.domain.service.ContentPolicy;
import com.gth.posting.domain.service.SchedulingPolicy;
import com.gth.posting.application.usecase.enforcegrouppolicy.GroupPolicyEnforcementResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.Instant;

/**
 * Noi day cac domain service thuan Java thanh bean.
 * Day la ly do domain-core khong can mot dong annotation Spring nao
 * ma van duoc inject binh thuong.
 */
@Configuration
public class BeanConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BeanConfiguration.class);

    @Bean
    public ContentPolicy contentPolicy() {
        return new ContentPolicy();
    }

    @Bean
    public BatchSelectionPolicy batchSelectionPolicy() {
        return new BatchSelectionPolicy();
    }

    /** Khóa nhóm ngoài ngành và dọn các bài chưa gửi trước khi app nhận yêu cầu MCP. */
    @Bean
    public ApplicationRunner enforceGroupPostingPolicy(EnforceGroupPostingPolicyUseCase useCase) {
        return arguments -> {
            GroupPolicyEnforcementResult result = useCase.handle();
            log.info("Áp dụng quy tắc nhóm công nghệ: đã khóa {} nhóm, hủy {} bài chưa gửi.",
                    result.disabledGroups(), result.cancelledJobs());
        };
    }

    @Bean
    public TimeProvider timeProvider() {
        return Instant::now;
    }

    @Bean
    public SchedulingPolicy schedulingPolicy(
            @Value("${posting.pacing.global-gap-minutes:6}") long globalGapMinutes,
            @Value("${posting.pacing.group-gap-hours:12}") long groupGapHours) {
        return new SchedulingPolicy(Duration.ofMinutes(globalGapMinutes), Duration.ofHours(groupGapHours));
    }
}
