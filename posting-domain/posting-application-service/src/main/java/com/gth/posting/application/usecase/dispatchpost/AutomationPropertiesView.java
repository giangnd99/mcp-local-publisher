package com.gth.posting.application.usecase.dispatchpost;

/**
 * Thong tin cau hinh ma use case can biet, khong phu thuoc lop cau hinh
 * cua module automation. Container se cung cap bean nay.
 */
public record AutomationPropertiesView(String pageName) {

    public AutomationPropertiesView {
        pageName = pageName == null ? "" : pageName;
    }
}
