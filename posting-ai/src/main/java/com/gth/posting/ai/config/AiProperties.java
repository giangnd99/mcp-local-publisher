package com.gth.posting.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "posting.ai")
public class AiProperties {

    /** Bat/tat lop AI. Tat thi he thong van chay bang template co dinh. */
    private boolean enabled = false;

    private String tone = "tu nhien, nhu nguoi ban that, khong hoa my, khong viet hoa toan bo";

    private int maxCharacters = 1800;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public int getMaxCharacters() {
        return maxCharacters;
    }

    public void setMaxCharacters(int maxCharacters) {
        this.maxCharacters = maxCharacters;
    }
}
