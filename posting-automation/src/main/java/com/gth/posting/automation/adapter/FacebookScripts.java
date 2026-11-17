package com.gth.posting.automation.adapter;

import com.gth.posting.application.support.PostVerificationPhrase;

/**
 * Các đoạn JavaScript điều khiển và xác minh giao diện Facebook.
 *
 * Hai quy ước đã được xác nhận qua thử nghiệm:
 *
 * 1. chrome_javascript chạy nội dung như thân của một hàm async nên cần dùng
 *    `return` ở cấp ngoài cùng; bọc IIFE có thể khiến kết quả trả về là undefined.
 *
 * 2. MCP có thể bọc kết quả trong chuỗi JSON và escape dấu ngoặc kép. Vì vậy
 *    các đoạn lệnh trả về mã kết quả dạng văn bản để việc đối chiếu ổn định.
 */
public final class FacebookScripts {

    public static final String COMPOSER_OK = "COMPOSER_OK";
    public static final String INSERT_OK = "INSERT_OK";
    public static final String SUBMIT_OK = "SUBMIT_OK";
    public static final String FEED_FOUND = "FEED_FOUND";

    private FacebookScripts() {
    }

    /** Bấm vào ô bắt đầu soạn bài trên trang nhóm. */
    public static String clickComposer() {
        return """
                var el = Array.prototype.slice.call(document.querySelectorAll('[role="button"]'))
                  .find(function(b){ return /Bạn viết gì đi|Bạn đang nghĩ gì|Write something|What's on your mind/i
                    .test((b.innerText || '') + ' ' + (b.getAttribute('aria-label') || '')); });
                if (!el) { return 'COMPOSER_FAIL: khong co o soan bai | ' + document.title; }
                el.click();
                return 'COMPOSER_OK';
                """;
    }

    /** Xác minh URL và tiêu đề nhóm để ngăn đăng nhầm khi Facebook chuyển hướng. */
    public static String verifyGroup(String groupId, String groupName) {
        return """
                var expectedId = %s;
                var expectedName = %s;
                if (!expectedName) { return 'GROUP_NAME_MISSING'; }
                var path = location.pathname + location.search;
                var idPattern = new RegExp('(?:^|[/=?])' + expectedId + '(?:[/&?]|$)');
                if (!idPattern.test(path)) { return 'GROUP_ID_MISMATCH path=' + path; }
                var normalize = function(value) {
                  return (value || '').toLowerCase().replace(/đ/g, 'd')
                    .normalize('NFD').replace(/[\\u0300-\\u036f]/g, '')
                    .replace(/[^a-z0-9]+/g, ' ').trim();
                };
                var expected = normalize(expectedName);
                var headings = Array.prototype.slice.call(document.querySelectorAll('h1, [role="heading"][aria-level="1"]'))
                  .filter(function(el){ return !!(el.offsetWidth || el.offsetHeight || el.getClientRects().length); })
                  .map(function(el){ return normalize(el.innerText || el.textContent); })
                  .filter(function(value){ return value.length > 0; });
                var nameMatches = headings.some(function(value) {
                  return value === expected || value.indexOf(expected) >= 0 || expected.indexOf(value) >= 0;
                });
                if (!nameMatches) { return 'GROUP_NAME_MISMATCH expected=' + expected + ' headings=' + headings.join('|'); }
                return 'GROUP_VERIFIED id=' + expectedId + ' name=' + expected;
                """.formatted(jsString(groupId), jsString(groupName));
    }

    /**
     * Chèn nội dung vào trình soạn bài và chuẩn bị input ảnh khi chiến dịch có ảnh.
     * Facebook có thể gộp ký tự xuống dòng khi chèn văn bản, vì vậy hàm thử paste,
     * đo lại số dòng rồi mới dùng cách chèn từng dòng. Thiếu dòng thì dừng, không đăng.
     */
    public static String insertText(String text, int expectedPhotoCount) {
        int expectedLines = countLines(text);
        return """
                var eds = Array.prototype.slice.call(document.querySelectorAll('[contenteditable="true"]'))
                  .filter(function(e){ return /viết gì|tạo bài viết|write something|post something/i
                    .test(e.getAttribute('aria-label') || e.getAttribute('placeholder') || ''); });
                var ed = eds[eds.length - 1];
                if (!ed) { return 'INSERT_FAIL: khong co contenteditable'; }
                var wait = function(ms){ return new Promise(function(r){ setTimeout(r, ms); }); };
                var clear = function(){
                  ed.focus();
                  var range = document.createRange();
                  range.selectNodeContents(ed);
                  var sel = window.getSelection();
                  sel.removeAllRanges(); sel.addRange(range);
                  document.execCommand('delete', false, null);
                };
                var lineCount = function(){
                  var t = ed.innerText || '';
                  return t.split('\\n').filter(function(l){ return l.trim().length > 0; }).length;
                };
                clear();
                try {
                  var dt = new DataTransfer();
                  dt.setData('text/plain', %s);
                  var ev = new ClipboardEvent('paste', { bubbles: true, cancelable: true });
                  Object.defineProperty(ev, 'clipboardData', { value: dt });
                  ed.dispatchEvent(ev);
                } catch (e) { /* Nếu paste không được thì dùng cách dự phòng. */ }
                await wait(700);
                if (lineCount() < %d) {
                  clear();
                  var parts = %s.split('\\n');
                  for (var i = 0; i < parts.length; i++) {
                    if (parts[i].length) { document.execCommand('insertText', false, parts[i]); }
                    if (i < parts.length - 1) { document.execCommand('insertParagraph', false, null); }
                  }
                  await wait(500);
                }
                if (%d > 0) {
                  var inputs = Array.prototype.slice.call(document.querySelectorAll('input[type="file"]'))
                    .filter(function(x){ return x.multiple; });
                  inputs.forEach(function(x){ x.removeAttribute('id'); });
                  var target = inputs.filter(function(x){ return x.closest('[role="dialog"]'); })[0] || inputs[0];
                  if (!target) { return 'INSERT_FAIL: khong co input file'; }
                  target.id = 'fb_up';
                }
                var lines = lineCount();
                var len = (ed.innerText || '').trim().length;
                if (lines < %d) { return 'NL_MISMATCH lines=' + lines + ' expect=%d len=' + len; }
                return 'INSERT_OK lines=' + lines + ' expect=%d len=' + len;
                """.formatted(jsString(text), expectedLines, jsString(text), expectedPhotoCount,
                expectedLines, expectedLines, expectedLines);
    }

    /** Đếm số dòng có nội dung để so sánh với phần văn bản Facebook nhận được. */
    public static int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String line : text.split("\n")) {
            if (!line.trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /** Chỉ bấm Đăng khi nội dung, số dòng và số ảnh mong đợi đã xuất hiện đầy đủ. */
    public static String submit(int expectedLines, int expectedPhotoCount) {
        return """
                var blobs = Array.prototype.slice.call(document.querySelectorAll('img'))
                  .filter(function(i){ return (i.src || '').indexOf('blob:') === 0; }).length;
                var eds = Array.prototype.slice.call(document.querySelectorAll('[contenteditable="true"]'))
                  .filter(function(e){ return /viết gì|tạo bài viết|write something|post something/i
                    .test(e.getAttribute('aria-label') || e.getAttribute('placeholder') || ''); });
                var ed = eds[eds.length - 1];
                var body = ((ed || {}).innerText || '');
                var len = body.trim().length;
                var lines = body.split('\\n').filter(function(l){ return l.trim().length > 0; }).length;
                var buttons = Array.prototype.slice.call(document.querySelectorAll('[role="button"]'))
                  .filter(function(b){ return /^(Đăng|Post)$/i.test((b.innerText || '').trim()); });
                if (lines < %d || len === 0) {
                  return 'SUBMIT_ABORT lines=' + lines + ' expect=' + %d + ' len=' + len + ' blobs=' + blobs;
                }
                if (blobs < %d) {
                  return 'SUBMIT_WAIT photos=' + blobs + ' expect=' + %d + ' len=' + len + ' lines=' + lines;
                }
                var button = buttons.filter(function(b){ return !b.getAttribute('aria-disabled')
                    && b.getAttribute('aria-disabled') !== 'true' && !b.disabled; }).slice(-1)[0];
                if (button) {
                  button.click();
                  return 'SUBMIT_OK blobs=' + blobs + ' len=' + len + ' lines=' + lines;
                }
                return 'SUBMIT_WAIT blobs=' + blobs + ' len=' + len + ' lines=' + lines + ' btn=' + buttons.length;
                """.formatted(expectedLines, expectedLines, expectedPhotoCount, expectedPhotoCount);
    }

    /** Tìm nội dung trong từng thẻ bài mới nhất, không kết luận chỉ từ thao tác bấm nút. */
    public static String verifyPublishedPost(String phrase, int attempts, long delayMs) {
        return """
                var expected = %s;
                var normalize = function(value) {
                  return (value || '').toLowerCase().replace(/đ/g, 'd')
                    .normalize('NFD').replace(/[\\u0300-\\u036f]/g, '')
                    .replace(/\\s+/g, ' ').trim();
                };
                var target = normalize(expected);
                if (target.length < 20) { return 'POST_UNVERIFIED phrase_too_short=' + target.length; }
                var wait = function(ms){ return new Promise(function(resolve){ setTimeout(resolve, ms); }); };
                window.scrollTo(0, 0);
                for (var attempt = 0; attempt < %d; attempt++) {
                  var body = document.body.innerText || '';
                  if (/Đang chờ quản trị viên phê duyệt|awaiting admin approval/i.test(body)) {
                    return 'POST_PENDING_APPROVAL attempt=' + (attempt + 1);
                  }
                  var articles = Array.prototype.slice.call(document.querySelectorAll('[role="article"]'));
                  var found = articles.find(function(article){
                    return normalize(article.innerText || article.textContent).indexOf(target) >= 0;
                  });
                  if (found) {
                    if (/đang chờ quản trị viên phê duyệt|pending admin approval|awaiting admin approval/i
                        .test(found.innerText || found.textContent)) {
                      return 'POST_PENDING_APPROVAL attempt=' + (attempt + 1);
                    }
                    var link = found.querySelector('a[href*="/posts/"], a[href*="story_fbid"], a[href*="permalink"]');
                    return 'POST_VERIFIED url=' + (link ? link.href : '') + ' attempt=' + (attempt + 1);
                  }
                  await wait(%d);
                }
                var pendingNotice = Array.prototype.slice.call(document.querySelectorAll('[role="alert"], [role="status"]'))
                  .some(function(item){ return /đang chờ quản trị viên phê duyệt|pending admin approval/i
                    .test(item.innerText || item.textContent); });
                return pendingNotice ? 'POST_PENDING_APPROVAL notice_only' : 'POST_UNVERIFIED phrase=' + expected;
                """.formatted(jsString(phrase), Math.max(1, attempts), Math.max(250L, delayMs));
    }

    /** Tìm đoạn nội dung riêng trong từng bài của feed nhóm để xác minh bài đã đăng. */
    public static String scrollAndFindPhrase(String phrase, int scrollTimes, long stepMs) {
        return """
                for (var i = 0; i < %d; i++) {
                  window.scrollBy(0, 1200);
                  await new Promise(function(r){ setTimeout(r, %d); });
                }
                var normalize = function(value) {
                  return (value || '').toLowerCase().replace(/đ/g, 'd')
                    .normalize('NFD').replace(/[\\u0300-\\u036f]/g, '')
                    .replace(/\\s+/g, ' ').trim();
                };
                var expected = normalize(%s);
                var articles = Array.prototype.slice.call(document.querySelectorAll('[role="article"]'));
                var found = articles.find(function(article){
                  return normalize(article.innerText || article.textContent).indexOf(expected) >= 0;
                });
                if (found) {
                  if (/đang chờ quản trị viên phê duyệt|pending admin approval|awaiting admin approval/i
                      .test(found.innerText || found.textContent)) { return 'PENDING'; }
                  var link = found.querySelector('a[href*="/posts/"], a[href*="story_fbid"], a[href*="permalink"]');
                  return 'FEED_FOUND URL=' + (link ? link.href : '');
                }
                return 'FEED_NOTFOUND articles=' + articles.length;
                """.formatted(scrollTimes, stepMs, jsString(phrase));
    }

    /** Chọn câu đủ riêng biệt để xác minh, ưu tiên dòng dài có từ khóa và số liệu. */
    /** Chuyển chuỗi Java thành literal JavaScript an toàn, tránh lỗi cú pháp và chèn mã. */
    static String jsString(String value) {
        if (value == null) {
            return "''";
        }
        StringBuilder sb = new StringBuilder("'");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '\'' -> sb.append("\\'");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '<' -> sb.append("\\u003c");
                case '>' -> sb.append("\\u003e");
                default -> sb.append(c);
            }
        }
        return sb.append('\'').toString();
    }
}
