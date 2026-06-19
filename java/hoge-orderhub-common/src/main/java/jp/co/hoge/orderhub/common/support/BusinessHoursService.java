package jp.co.hoge.orderhub.common.support;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

/**
 * Bar 社営業時間判定サービス。
 *
 * @author Takuya Yamamoto
 */
@Component
public class BusinessHoursService {
    /** 営業開始時刻。 */
    private static final LocalTime START = LocalTime.of(8, 0);
    /** 営業終了時刻。 */
    private static final LocalTime END = LocalTime.of(18, 0);

    /**
     * 指定日時が Bar 社営業時間内かを判定する。
     *
     * @param timestamp 判定対象日時
     * @return 営業時間内の場合 true
     */
    public boolean isBarBusinessHours(LocalDateTime timestamp) {
        DayOfWeek dayOfWeek = timestamp.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }

        LocalTime time = timestamp.toLocalTime();
        return !time.isBefore(START) && time.isBefore(END);
    }

    /**
     * 次に配送依頼を送れる営業日時を返す。
     *
     * @param timestamp 判定基準日時
     * @return 次回営業日時
     */
    public LocalDateTime nextBarBusinessTime(LocalDateTime timestamp) {
        if (isBarBusinessHours(timestamp)) {
            return timestamp;
        }

        LocalDate candidateDate = timestamp.toLocalDate();
        LocalTime candidateTime = timestamp.toLocalTime();

        if (candidateTime.isBefore(START) && isWeekday(candidateDate)) {
            return LocalDateTime.of(candidateDate, START);
        }

        do {
            candidateDate = candidateDate.plusDays(1);
        } while (!isWeekday(candidateDate));

        return LocalDateTime.of(candidateDate, START);
    }

    private boolean isWeekday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
}
