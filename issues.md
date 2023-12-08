# 이슈 관리
## 2023.12.08(금)
### 오류 파악(해결)
> 사용자 지정 예외를 만들고 예외처리를 할 때 예외처리를 해야하는 이유도 로그로 나타게끔 코드를 짜자.
1. 문제점

테스트를 위해 어플리케이션 실행 시 계좌 정보는 저장이 잘 되었지만 잔액확인시 오류가 계속 발생하였다.
로그에서는 예외처리만 했을 뿐 어떤 이유로 예외처리를 했는지 명시되어 있지 않았다.

2. 원인파악

계좌 잔액보다 큰 금액을 결제를 하려다 보니 애플리케이션에서 예외처리를 한 것이었다.
하지만 로그에서 예외처리를 한 이유가 뜨지 않아 오류를 파악하는 데에 시간을 잡아먹었다.

3. 해결방법

테스트 케이스를 로직에 맞게 다시 짰다.

4. 보완점

예외처리시 예외가 발생한 이유를 로그에 뜨게끔 코드를 수정.

~~~java
package com.syk25.account.exception;

import com.syk25.account.type.ErrorCode;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class AccountException extends RuntimeException{
    private ErrorCode errorCode;
    private String errorMessage;

    public AccountException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
        log.info(this.errorMessage); // 예외발생시 에러코드도 같이 로그에 뜨게끔 코드 추가
    }
}
~~~