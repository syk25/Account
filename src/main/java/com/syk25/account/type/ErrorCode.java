package com.syk25.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("사용자가 없습니다."),
    MAX_ACCOUNT_PER_USER_10("계좌 한도 개수를 초과하였습니다!"),
    ACCOUNT_NOT_FOUND("계좌가 없습니다."),
    ACCOUNT_AND_USER_MISMATCHED("사용자 아이디와 계좌 소유주가 다릅니다."),
    ACCOUNT_ALREADY_UNREGISTERED("계좌 이미 해지한 상태입니다."),
    BALANCE_EXISTS("잔액이 존재합니다."),
    AMOUNT_EXCEED_BALANCE("거래 금액이 잔액을 초과합니다.");

    private final String description;
}
