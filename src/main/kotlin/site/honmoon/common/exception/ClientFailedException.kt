package site.honmoon.common.exception

import site.honmoon.common.ErrorCode


class ClientFailedException : CommonException {

    constructor(errorCode: ErrorCode, message: String) : super(errorCode, message)

    constructor(errorCode: ErrorCode, id: Any? = null) : super(errorCode, id)

    constructor(errorCode: ErrorCode, vararg args: Any?) : super(errorCode, *args)

    constructor(errorCode: ErrorCode, message: String, cause: Throwable) : super(errorCode, message, cause)

    constructor(errorCode: ErrorCode, id: Any? = null, cause: Throwable) : super(errorCode, id, cause)
}
