package site.honmoon.mission.service

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import site.honmoon.mission.entity.MissionDetail
import site.honmoon.mission.type.MissionType
import site.honmoon.mission.vo.MissionAnswerVo

@OptIn(ExperimentalKotest::class)
@Disabled
@SpringBootTest
@ActiveProfiles("local")
class OpenAIServiceTest : DescribeSpec() {

    @Autowired
    private lateinit var openAIService: OpenAIService

    init {
        describe("OpenAIService 통합 테스트 (실제 호출)").config(enabled = false) {
            it("isAvailable 가 true 여야 한다") {
                openAIService.isAvailable().shouldBeTrue()
            }

            it("mission_place 이름 텍스트 정답 검증 (정답 인정)") {
                val places = listOf(
                    "한강공원 여의도",
                    "남산서울타워",
                    "경복궁"
                )

                places.forEach { name ->
                    val mission = MissionDetail(
                        title = "장소 이름 맞히기",
                        points = 100,
                        missionType = MissionType.QUIZ_TEXT_INPUT,
                        question = "다음 장소의 이름은 무엇인가요?",
                        answer = MissionAnswerVo(name)
                    )
                    val result = openAIService.checkTextAnswer(mission, name)
                    println("[OpenAI][Text] name=$name -> ${result}")
                    result.isCorrect.shouldBeTrue()
                }
            }

            it("이미지 분석 호출 (mission_place 이미지 URL)") {
                val imageUrls = listOf(
                    "https://storage.googleapis.com/honmoon-bucket/place/image/hangangpark-yeouido.jpeg",
                    "https://storage.googleapis.com/honmoon-bucket/place/image/namsan-seoultower.jpeg",
                    "https://storage.googleapis.com/honmoon-bucket/place/image/gyeongbokgung-palace.jpeg"
                )

                imageUrls.forEach { url ->
                    val analysis = openAIService.analyzeImage(url)
                    println("[OpenAI][Image] url=$url -> ${analysis}")
                    analysis.shouldNotBeNull()
                    analysis.confidence.shouldBeBetween(0.0, 1.0, 0.000001)
                    analysis.description.shouldNotBeBlank()
                }
            }
        }
    }
}
