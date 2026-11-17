package com.gth.posting.application.port.out.configuration;

import com.gth.posting.application.dto.SetupSettings;

/** Cổng kiểm tra các chương trình và tệp ngoài cần thiết trước khi bật tích hợp. */
public interface SetupRuntimeValidator {

    /** Ném lỗi có nội dung dễ hiểu nếu máy hiện tại chưa sẵn sàng cho cấu hình đã chọn. */
    void validate(SetupSettings settings);
}
