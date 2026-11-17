package com.gth.posting.application.port.out.publisher;

/**
 * Cong ra ngoai de dang bai. Hien thuc P1: MCP client goi server chrome/computer-use.
 * Hien thuc P2 co the la Playwright Java attach CDP. Domain khong biet cai nao.
 */
public interface PostPublisher {

    PublishResult publish(PublishRequest request);
}
