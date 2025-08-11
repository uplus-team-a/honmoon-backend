package site.honmoon.common


enum class ErrorCode(
    val code: Int,
    val status: ErrorStatus,
    var message: String,
) {

    ILLEGAL_REQUEST(1400, ErrorStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(1401, ErrorStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    FORBIDDEN(1402, ErrorStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(1403, ErrorStatus.NOT_FOUND, "요청한 자원을 찾을 수 없습니다."),
    NO_HANDLER(1404, ErrorStatus.NOT_FOUND, "해당 핸들러를 찾을 수 없습니다."),
    TOO_MANY_REQUESTS(1405, ErrorStatus.TOO_MANY_REQUESTS, "너무 많은 요청입니다."),
    INTERNAL_SERVER_ERROR(1500, ErrorStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    UNKNOWN_ERROR(9999, ErrorStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류입니다."),

    RESOURCE_NOT_FOUND(1406, ErrorStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다: %s"),
    USER_NOT_FOUND(1407, ErrorStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: %s"),
    MISSION_NOT_FOUND(1408, ErrorStatus.NOT_FOUND, "미션을 찾을 수 없습니다: %s"),
    RAFFLE_NOT_FOUND(1409, ErrorStatus.NOT_FOUND, "래플을 찾을 수 없습니다: %s"),
    ACTIVITY_NOT_FOUND(1410, ErrorStatus.NOT_FOUND, "활동 기록을 찾을 수 없습니다: %s"),
    POINT_HISTORY_NOT_FOUND(1411, ErrorStatus.NOT_FOUND, "포인트 기록을 찾을 수 없습니다: %s"),
    PLACE_NOT_FOUND(1412, ErrorStatus.NOT_FOUND, "장소를 찾을 수 없습니다: %s"),
    DUPLICATE_ACTIVITY(1413, ErrorStatus.BAD_REQUEST, "이미 등록된 활동입니다."),

    INVALID_STATE(1414, ErrorStatus.BAD_REQUEST, "요청 상태가 올바르지 않습니다."),
    INVALID_OR_EXPIRED_TOKEN(1415, ErrorStatus.BAD_REQUEST, "토큰이 유효하지 않거나 만료되었습니다."),
    IMAGE_URL_EMPTY(1416, ErrorStatus.BAD_REQUEST, "이미지 URL은 비어 있을 수 없습니다."),
    INVALID_URL_FORMAT(1417, ErrorStatus.BAD_REQUEST, "URL 형식이 올바르지 않습니다: %s"),
    UNSUPPORTED_URL_SCHEME(1418, ErrorStatus.BAD_REQUEST, "허용되지 않은 URL 스킴입니다. HTTP/HTTPS만 허용됩니다."),
    INVALID_URL_HOST(1419, ErrorStatus.BAD_REQUEST, "URL 호스트가 올바르지 않습니다."),
    PATH_TRAVERSAL_DETECTED(1420, ErrorStatus.BAD_REQUEST, "경로 이동이 의심되는 요청입니다."),
    UNSUPPORTED_IMAGE_EXTENSION(1421, ErrorStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있습니다."),
    INSUFFICIENT_POINTS(1422, ErrorStatus.BAD_REQUEST, "포인트가 부족합니다. 필요: %s, 보유: %s"),
    IMAGE_UPLOAD_NOT_SUPPORTED(1423, ErrorStatus.BAD_REQUEST, "해당 미션은 이미지 업로드를 지원하지 않습니다."),
    JSON_NOT_FOUND_IN_RESPONSE(1424, ErrorStatus.BAD_REQUEST, "응답에서 유효한 JSON을 찾지 못했습니다."),
    REQUIRED_PARAMETER_MISSING(1425, ErrorStatus.BAD_REQUEST, "필수 파라미터가 누락되었습니다: %s"),
    INVALID_CHOICE_INDEX(1426, ErrorStatus.BAD_REQUEST, "선택지 인덱스가 유효하지 않습니다."),
    TEXT_ANSWER_EMPTY(1427, ErrorStatus.BAD_REQUEST, "텍스트 답변은 비어 있을 수 없습니다."),
    REQUIRED_FIELD_MISSING(1428, ErrorStatus.BAD_REQUEST, "필수 입력이 누락되었습니다: %s"),
    USER_EMAIL_NOT_FOUND(1429, ErrorStatus.BAD_REQUEST, "해당 사용자에 등록된 이메일이 없습니다."),

}
