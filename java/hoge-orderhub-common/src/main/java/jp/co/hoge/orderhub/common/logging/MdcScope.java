package jp.co.hoge.orderhub.common.logging;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.MDC;

/**
 * MDC 値を一時的に差し替え、クローズ時に元へ戻すスコープ。
 *
 * @author Takuya Yamamoto
 */
public class MdcScope implements AutoCloseable {

    /** 退避した MDC 値。 */
    private final Map<String, String> previousValues = new HashMap<>();

    /**
     * 指定キーの MDC 値を退避し、新しい値へ差し替える。
     *
     * @param values 差し替えるキーと値
     */
    public MdcScope(Map<String, String> values) {
        values.forEach((key, value) -> {
            previousValues.put(key, MDC.get(key));
            if (value == null) {
                MDC.remove(key);
            } else {
                MDC.put(key, value);
            }
        });
    }

    /**
     * 差し替え前の MDC 値へ復元する。
     */
    @Override
    public void close() {
        previousValues.forEach((key, value) -> {
            if (value == null) {
                MDC.remove(key);
            } else {
                MDC.put(key, value);
            }
        });
    }
}
