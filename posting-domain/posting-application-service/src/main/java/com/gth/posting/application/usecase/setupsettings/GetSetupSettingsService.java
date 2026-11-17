package com.gth.posting.application.usecase.setupsettings;

import com.gth.posting.application.dto.SetupSettings;
import com.gth.posting.application.port.out.configuration.SetupSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cài đặt truy vấn cấu hình qua cổng lưu trữ, không phụ thuộc giao diện web. */
@Service
@Transactional(readOnly = true)
public class GetSetupSettingsService implements GetSetupSettingsUseCase {

    private final SetupSettingsRepository settingsRepository;

    /** Khởi tạo dịch vụ với cổng cấu hình do tầng hạ tầng cung cấp. */
    public GetSetupSettingsService(SetupSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    /** Đọc cấu hình hiện tại từ cổng lưu trữ. */
    @Override
    public SetupSettings handle() {
        return settingsRepository.load();
    }
}
