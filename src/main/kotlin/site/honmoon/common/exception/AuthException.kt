package site.honmoon.common.exception

import site.honmoon.common.ErrorCode


class AuthException : CommonException {

    constructor(errorCode: ErrorCode, message: String) : super(errorCode, message)

    constructor(errorCode: ErrorCode, any: Any? = null) : super(errorCode, any)

    constructor(errorCode: ErrorCode, message: String, cause: Throwable) : super(errorCode, message, cause)

    constructor(errorCode: ErrorCode, any: Any? = null, cause: Throwable) : super(errorCode, any, cause)
}
