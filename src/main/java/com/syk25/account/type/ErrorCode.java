package com.syk25.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST("잘못된 요청입니다."),
    USER_NOT_FOUND("사용자가 없습니다."),
    MAX_ACCOUNT_PER_USER_10("계좌 한도 개수를 초과하였습니다!"),
    ACCOUNT_NOT_FOUND("계좌가 없습니다."),
    ACCOUNT_AND_USER_MISMATCHED("사용자 아이디와 계좌 소유주가 다릅니다."),
    ACCOUNT_ALREADY_UNREGISTERED("계좌 이미 해지한 상태입니다."),
    BALANCE_EXISTS("잔액이 존재합니다."),
    AMOUNT_EXCEED_BALANCE("거래 금액이 잔액을 초과합니다."),
    TRANSACTION_NOT_FOUND("해당 거래를 확인할 수 없습니다."),
    TRANSACTION_UNMATCHED("해당 계좌에서 발생한 거래가 아닙니다."),
    FULL_CANCEL_REQUIRED("부분 취소는 허용되지 않습니다."),
    TRANSACTION_CANCEL_AVAILABLE_DATE_EXPIRED("1년이 지난 거래는 취소가 불가능합니다.")
    ;

    private final String description;
}
