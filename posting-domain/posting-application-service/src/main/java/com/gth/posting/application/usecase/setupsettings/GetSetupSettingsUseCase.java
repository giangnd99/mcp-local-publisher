package com.gth.posting.application.usecase.setupsettings;

import com.gth.posting.application.dto.SetupSettings;

/** Truy vấn cấu hình hiện tại để hiển thị trên màn hình thiết lập. */
public interface GetSetupSettingsUseCase {

    /** Trả về các giá trị cấu hình hiện tại, bao gồm trạng thái bật/tắt. */
    SetupSettings handle();
}
