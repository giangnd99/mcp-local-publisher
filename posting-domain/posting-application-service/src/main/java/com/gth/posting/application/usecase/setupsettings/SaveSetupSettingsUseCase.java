package com.gth.posting.application.usecase.setupsettings;

import com.gth.posting.application.dto.SetupSettings;

/** Thay đổi cấu hình ứng dụng từ màn hình thiết lập. */
public interface SaveSetupSettingsUseCase {

    /** Kiểm tra dữ liệu rồi lưu cấu hình để dùng từ lần khởi động kế tiếp. */
    void handle(SetupSettings settings);
}
