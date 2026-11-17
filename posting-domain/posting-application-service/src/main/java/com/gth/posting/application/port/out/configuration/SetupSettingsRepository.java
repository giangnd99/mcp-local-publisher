package com.gth.posting.application.port.out.configuration;

import com.gth.posting.application.dto.SetupSettings;

/** Cổng lưu và đọc cấu hình cài đặt do người vận hành quản lý. */
public interface SetupSettingsRepository {

    /** Đọc cấu hình đã lưu; trả về giá trị mặc định nếu chưa có tệp cấu hình. */
    SetupSettings load();

    /** Lưu cấu hình để ứng dụng nạp lại ở lần khởi động tiếp theo. */
    void save(SetupSettings settings);
}
