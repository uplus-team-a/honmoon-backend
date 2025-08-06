package site.honmoon.mission.type

import site.honmoon.mission.type.MissionType.*

val MissionType.isQuiz: Boolean
    get() = this in listOf(QUIZ_MULTIPLE_CHOICE, QUIZ_TEXT_INPUT, QUIZ_IMAGE_UPLOAD)

val MissionType.isMultipleChoiceQuiz: Boolean
    get() = this == QUIZ_MULTIPLE_CHOICE

val MissionType.isTextInputQuiz: Boolean
    get() = this == QUIZ_TEXT_INPUT

val MissionType.isImageUploadQuiz: Boolean
    get() = this == QUIZ_IMAGE_UPLOAD

val MissionType.requiresChoices: Boolean
    get() = this == QUIZ_MULTIPLE_CHOICE

val MissionType.requiresTextAnswer: Boolean
    get() = this == QUIZ_TEXT_INPUT

val MissionType.requiresImageUpload: Boolean
    get() = this == QUIZ_IMAGE_UPLOAD 