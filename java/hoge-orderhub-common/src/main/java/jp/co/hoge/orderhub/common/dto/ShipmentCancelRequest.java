package jp.co.hoge.orderhub.common.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 出荷依頼取消要求。
 *
 * @param cancelReason 取消理由
 * @author Takuya Yamamoto
 */
public record ShipmentCancelRequest(@NotBlank String cancelReason) {}
