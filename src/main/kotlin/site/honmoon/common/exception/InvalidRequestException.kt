package site.honmoon.common.exception

import site.honmoon.common.ErrorCode

/**
 * 잘못된 요청(검증 실패, 지원하지 않는 동작 등) 상황을 나타내는 예외
 */
class InvalidRequestException : CommonException {

    constructor(errorCode: ErrorCode, message: String) : super(errorCode, message)

    constructor(errorCode: ErrorCode, vararg args: Any?) : super(errorCode, *args)

    constructor(errorCode: ErrorCode, message: String, cause: Throwable) : super(errorCode, message, cause)

    constructor(errorCode: ErrorCode, any: Any? = null, cause: Throwable) : super(errorCode, any, cause)
}


