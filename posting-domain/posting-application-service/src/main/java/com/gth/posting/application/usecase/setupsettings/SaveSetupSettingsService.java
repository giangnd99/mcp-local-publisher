package com.gth.posting.application.usecase.setupsettings;

import com.gth.posting.application.dto.SetupSettings;
import com.gth.posting.application.port.out.configuration.SetupSettingsRepository;
import com.gth.posting.application.port.out.configuration.SetupRuntimeValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Thực hiện kiểm tra nghiệp vụ trước khi chuyển cấu hình cho tầng lưu trữ. */
@Service
@Transactional
public class SaveSetupSettingsService implements SaveSetupSettingsUseCase {

    private final SetupSettingsRepository settingsRepository;
    private final SetupRuntimeValidator runtimeValidator;

    /** Khởi tạo dịch vụ với cổng lưu cấu hình do tầng hạ tầng cung cấp. */
    public SaveSetupSettingsService(SetupSettingsRepository settingsRepository,
                                    SetupRuntimeValidator runtimeValidator) {
        this.settingsRepository = settingsRepository;
        this.runtimeValidator = runtimeValidator;
    }

    /** Kiểm tra các giá trị người dùng nhập rồi lưu cấu hình hợp lệ. */
    @Override
    public void handle(SetupSettings settings) {
        validate(settings);
        runtimeValidator.validate(settings);
        settingsRepository.save(settings);
    }

    /** Bảo đảm cấu hình cơ sở dữ liệu, MCP và nhịp đăng bài có giá trị hợp lệ. */
    private void validate(SetupSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Vui lòng nhập thông tin cấu hình.");
        }
        if (isBlank(settings.databaseUrl()) || !settings.databaseUrl().startsWith("jdbc:postgresql://")) {
            throw new IllegalArgumentException("Địa chỉ cơ sở dữ liệu phải có dạng jdbc:postgresql://...");
        }
        if (isBlank(settings.databaseUser())) {
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập cơ sở dữ liệu.");
        }
        if (settings.mcpEnabled() && (isBlank(settings.chromeCommand()) || isBlank(settings.chromeScript()))) {
            throw new IllegalArgumentException("Để bật điều khiển Chrome, cần cấu hình Node.js và MCP bridge.");
        }
        if (settings.aiEnabled()
                && isBlank(settings.openAiApiKey())
                && !settingsRepository.load().openAiApiKeyConfigured()) {
            throw new IllegalArgumentException("Để bật viết nội dung bằng AI, vui lòng nhập API key hoặc để AI tắt.");
        }
        if (settings.globalGapMinutes() < 1 || settings.globalGapMinutes() > 1440) {
            throw new IllegalArgumentException("Thời gian nghỉ toàn hệ thống phải từ 1 đến 1440 phút.");
        }
        if (settings.groupGapHours() < 1 || settings.groupGapHours() > 720) {
            throw new IllegalArgumentException("Thời gian nghỉ theo nhóm phải từ 1 đến 720 giờ.");
        }
    }

    /** Kiểm tra chuỗi không có nội dung sau khi bỏ khoảng trắng. */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
