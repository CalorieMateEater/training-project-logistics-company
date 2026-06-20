package jp.co.hoge.orderhub.common.dto;

/**
 * API エラーレスポンス。
 *
 * @param code エラーコード
 * @param message エラーメッセージ
 * @author Takuya Yamamoto
 */
public record ApiErrorResponse(String code, String message) {}
