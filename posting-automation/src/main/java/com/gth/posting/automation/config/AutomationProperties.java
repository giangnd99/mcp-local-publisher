package com.gth.posting.automation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cau hinh hanh vi trinh duyet. Khong hardcode trong code vi moi may shop
 * co tabId/windowId khac nhau.
 */
@ConfigurationProperties(prefix = "posting.automation")
public class AutomationProperties {

    /** Ten Page/Profile se hien thi ten tac gia bai dang. */
    private String pageName = "Laptop GTH";

    /** Tab Chrome dang mo san phien Facebook. 0 = mo tab moi. */
    private long tabId = 0L;

    private long windowId = 0L;

    private String groupsBaseUrl = "https://www.facebook.com/groups/";

    private long navigateWaitMs = 5500L;

    private long composerWaitMs = 4000L;

    private long uploadWaitMs = 5000L;

    private long postWaitMs = 6000L;

    private int verifyScrollTimes = 3;

    private long verifyScrollStepMs = 1200L;

    private Tools tools = new Tools();

    public static class Tools {

        private String navigate = "chrome_navigate";

        private String javascript = "chrome_javascript";

        private String uploadFile = "chrome_upload_file";

        public String getNavigate() {
            return navigate;
        }

        public void setNavigate(String navigate) {
            this.navigate = navigate;
        }

        public String getJavascript() {
            return javascript;
        }

        public void setJavascript(String javascript) {
            this.javascript = javascript;
        }

        public String getUploadFile() {
            return uploadFile;
        }

        public void setUploadFile(String uploadFile) {
            this.uploadFile = uploadFile;
        }
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public long getTabId() {
        return tabId;
    }

    public void setTabId(long tabId) {
        this.tabId = tabId;
    }

    public long getWindowId() {
        return windowId;
    }

    public void setWindowId(long windowId) {
        this.windowId = windowId;
    }

    public String getGroupsBaseUrl() {
        return groupsBaseUrl;
    }

    public void setGroupsBaseUrl(String groupsBaseUrl) {
        this.groupsBaseUrl = groupsBaseUrl;
    }

    public long getNavigateWaitMs() {
        return navigateWaitMs;
    }

    public void setNavigateWaitMs(long navigateWaitMs) {
        this.navigateWaitMs = navigateWaitMs;
    }

    public long getComposerWaitMs() {
        return composerWaitMs;
    }

    public void setComposerWaitMs(long composerWaitMs) {
        this.composerWaitMs = composerWaitMs;
    }

    public long getUploadWaitMs() {
        return uploadWaitMs;
    }

    public void setUploadWaitMs(long uploadWaitMs) {
        this.uploadWaitMs = uploadWaitMs;
    }

    public long getPostWaitMs() {
        return postWaitMs;
    }

    public void setPostWaitMs(long postWaitMs) {
        this.postWaitMs = postWaitMs;
    }

    public int getVerifyScrollTimes() {
        return verifyScrollTimes;
    }

    public void setVerifyScrollTimes(int verifyScrollTimes) {
        this.verifyScrollTimes = verifyScrollTimes;
    }

    public long getVerifyScrollStepMs() {
        return verifyScrollStepMs;
    }

    public void setVerifyScrollStepMs(long verifyScrollStepMs) {
        this.verifyScrollStepMs = verifyScrollStepMs;
    }

    public Tools getTools() {
        return tools;
    }

    public void setTools(Tools tools) {
        this.tools = tools;
    }
}
