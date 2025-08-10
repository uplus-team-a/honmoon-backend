package site.honmoon.common.exception

import site.honmoon.common.ErrorCode
import site.honmoon.common.ErrorStatus


abstract class CommonException : RuntimeException {
    val code: Int
    val status: ErrorStatus

    constructor(errorCode: ErrorCode, message: String) : super(message) {
        code = errorCode.code
        status = errorCode.status
    }

    constructor(errorCode: ErrorCode, vararg args: Any?) : this(
        errorCode = errorCode,
        args = args.map { it.toString() }.toTypedArray()
    )

    constructor(errorCode: ErrorCode, vararg args: String?) : super(args.let { errorCode.message.format(*it) }) {
        code = errorCode.code
        status = errorCode.status
    }

    constructor(errorCode: ErrorCode, message: String, cause: Throwable) : super(message, cause) {
        code = errorCode.code
        status = errorCode.status
    }

    constructor(errorCode: ErrorCode, args: Any?, cause: Throwable) :
            super(args?.let { errorCode.message.format(it.toString()) }, cause) {
        code = errorCode.code
        status = errorCode.status
    }

    constructor(
        errorCode: ErrorCode,
        cause: Throwable,
        vararg args: String?,
    ) : super(args.let { errorCode.message.format(*it) }, cause) {
        code = errorCode.code
        status = errorCode.status
    }

    open fun getMessageCode(): String = javaClass.simpleName
}
