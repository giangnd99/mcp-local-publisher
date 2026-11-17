package com.gth.posting.automation.config;

import com.gth.posting.application.usecase.dispatchpost.AutomationPropertiesView;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AutomationProperties.class)
public class AutomationConfiguration {

    @Bean
    public AutomationPropertiesView automationPropertiesView(AutomationProperties properties) {
        return new AutomationPropertiesView(properties.getPageName());
    }
}
