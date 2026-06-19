package jp.co.hoge.orderhub.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * HTTP リクエスト単位で MDC を初期化するフィルタ。
 *
 * @author Takuya Yamamoto
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTrackingFilter extends OncePerRequestFilter {

    /** リクエスト ID ヘッダー名。 */
    private static final String HEADER_REQUEST_ID = "X-Request-Id";

    /** トレース ID ヘッダー名。 */
    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    /**
     * 受信リクエストに対する追跡情報を MDC とレスポンスヘッダーへ設定する。
     *
     * @param request HTTP リクエスト
     * @param response HTTP レスポンス
     * @param filterChain 後続フィルタ
     * @throws ServletException サーブレット例外
     * @throws IOException 入出力例外
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = firstNonBlank(request.getHeader(HEADER_REQUEST_ID), MdcUtils.getOrCreateTrackingId());
        String traceId = firstNonBlank(request.getHeader(HEADER_TRACE_ID), requestId);

        try (MdcScope scope = MdcUtils.withEntries(Map.of(
                MdcKeys.TRACKING_ID, MdcUtils.getOrCreateTrackingId(),
                MdcKeys.REQUEST_ID, requestId,
                MdcKeys.TRACE_ID, traceId,
                MdcKeys.REQUEST_PATH, request.getRequestURI(),
                MdcKeys.HTTP_METHOD, request.getMethod()
        ))) {
            response.setHeader(HEADER_REQUEST_ID, requestId);
            response.setHeader(HEADER_TRACE_ID, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MdcKeys.TRACKING_ID);
            MDC.remove(MdcKeys.REQUEST_ID);
            MDC.remove(MdcKeys.TRACE_ID);
            MDC.remove(MdcKeys.REQUEST_PATH);
            MDC.remove(MdcKeys.HTTP_METHOD);
            MDC.remove(MdcKeys.REQUEST_KEY);
            MDC.remove(MdcKeys.ORDER_ID);
            MDC.remove(MdcKeys.EXTERNAL_SYSTEM);
        }
    }

    /**
     * 先頭値が空でなければ先頭値を、空なら代替値を返却する。
     *
     * @param first 候補値
     * @param fallback 代替値
     * @return 採用値
     */
    private String firstNonBlank(String first, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return fallback;
    }
}
