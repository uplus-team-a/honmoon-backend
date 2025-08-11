package site.honmoon.mission.service

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import site.honmoon.mission.entity.MissionDetail
import site.honmoon.mission.type.MissionType

@OptIn(ExperimentalKotest::class)
@Disabled
@SpringBootTest
@ActiveProfiles("local")
class FallbackAIServiceTest : DescribeSpec() {

    @Autowired
    private lateinit var fallbackAIService: FallbackAIService

    init {
        describe("FallbackAIService 통합 테스트 (실제 호출)").config(enabled = false) {
            it("isAvailable 가 true 여야 한다") {
                fallbackAIService.isAvailable().shouldBeTrue()
            }

            it("OpenAI 오류 시 Gemini 폴백 작동 (텍스트)") {
                val mission = MissionDetail(
                    title = "장소 이름 맞히기",
                    points = 100,
                    missionType = MissionType.QUIZ_TEXT_INPUT,
                    question = "다음 장소의 이름은 무엇인가요?",
                    answer = "경복궁"
                )
                // 고의로 템플릿 문제를 유발할 가능성이 있으므로 정상 문자열 사용
                val result = fallbackAIService.checkTextAnswer(mission, "경복궁")
                println("[Fallback][Text] -> ${result}")
                result.isCorrect.shouldBeTrue()
            }

            it("이미지 분석 + 이미지 기반 정답 체크: 폴백 포함 전체 흐름") {
                val mission = MissionDetail(
                    title = "이미지로 장소 맞히기",
                    points = 100,
                    missionType = MissionType.QUIZ_IMAGE_UPLOAD,
                    question = "이 이미지는 어떤 장소인가요?",
                    answer = "경복궁"
                )
                val imageUrl = "https://storage.googleapis.com/honmoon-bucket/place/image/gyeongbokgung-palace.jpeg"

                val analysis = fallbackAIService.analyzeImage(imageUrl)
                println("[Fallback][ImageAnalysis] -> ${analysis}")

                val check = fallbackAIService.checkImageAnswer(mission, analysis.extractedText)
                println("[Fallback][ImageCheck] -> ${check}")
                check.isCorrect.shouldBeTrue()
            }
        }
    }
}
