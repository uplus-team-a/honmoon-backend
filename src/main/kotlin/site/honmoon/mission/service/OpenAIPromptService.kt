package site.honmoon.mission.service

import org.springframework.stereotype.Service
import site.honmoon.mission.entity.MissionDetail

@Service
class OpenAIPromptService {

    fun createImageAnalysisPrompt(): String {
        return """
            Analyze the image and respond with the following JSON format:
            
            {{
                "extractedText": "All text extracted from the image (empty string if no text)",
                "confidence": 0.95,
                "description": "Describe what the image depicts so it is understandable from text alone, in 150 characters or fewer"
            }}
            
            Instructions:
            - If there are multiple texts, join them with spaces into a single string
            - Recognize both handwriting and printed text
            - Extract Korean, English, and numbers
            - description must clearly summarize the image so it can be understood from text alone, and be 150 characters or fewer
            - confidence should be a value between 0.0-1.0 representing text extraction reliability
            - Respond only with JSON, no other text
        """.trimIndent()
    }

    fun createAnswerCheckPrompt(mission: MissionDetail, userAnswer: String): String {
        return """
            Please evaluate the quiz answer very leniently. Accept the answer as correct if it's even partially right.
            
            Question: ${mission.question}
            Correct Answer: ${mission.answer}
            User Answer: $userAnswer
            
            Respond in this JSON format:
            {{
                "isCorrect": true,
                "confidence": 0.95,
                "reasoning": "Explanation for the answer evaluation",
                "hint": "짧은 한국어 힌트 한 문장 (정답이 아닐 경우에만 제공, 15자 이내)"
            }}
            
            Very lenient evaluation criteria (try to mark as correct whenever possible):
            1. Accept if core keywords are included
            2. Accept similar meanings (e.g., "Seoul Metropolitan City", "Seoul", "Seoul City" are all correct)
            3. Accept partial answers (e.g., if correct answer is "South Korea Seoul" but user only wrote "Seoul")
            4. Completely ignore typos, spacing, and grammar errors
            5. Completely ignore case differences
            6. Allow abbreviations, acronyms, and alternative expressions
            7. Allow all number formats (Korean, English, digits)
            8. Accept even if word order is changed but core content is there
            9. Accept if correct content is included even with additional explanations
            10. Accept if intent can be understood
            
            IMPORTANT: Unless the user gave a completely wrong answer, try to mark it as correct!
            The goal is to accept answers unless they are entirely incorrect.
            
            If isCorrect is false, provide a very short Korean hint in "hint" (<= 15 characters) that nudges the user without revealing the full answer. If correct, set hint to an empty string.
            confidence should be 0.0-1.0 (higher scores for lenient evaluations)
            reasoning should positively explain which parts were correct
            
            Respond only with JSON, no other text.
        """.trimIndent()
    }

    fun createImageAnswerCheckPrompt(mission: MissionDetail, extractedText: String): String {
        return """
            Based on text extracted from an image, please evaluate the quiz answer extremely leniently!
            
            Question: ${mission.question}
            Correct Answer: ${mission.answer}
            Extracted Text from Image: $extractedText
            
            Respond in this JSON format:
            {{
                "isCorrect": true,
                "confidence": 0.95,
                "reasoning": "Explanation for answer evaluation",
                "hint": "짧은 한국어 힌트 한 문장 (정답이 아닐 경우에만 제공, 15자 이내)"
            }}
            
            Extremely lenient evaluation criteria (accept as correct whenever possible):
            1. Accept if any core keywords are partially included
            2. Fully account for image text extraction errors (consider OCR errors)
            3. Accept if meaning can be understood despite character recognition errors
            4. Accept if 20% or more of the answer is correct
            5. Completely ignore typos, spacing, and grammar
            6. Ignore case and special characters
            7. Allow word order changes and missing words
            8. Accept similar meanings or related words
            9. Accept if image is blurry and only partially recognized
            10. Be lenient with handwriting or unusual font recognition errors
            
            CRITICAL: Unless the user gave a completely unrelated answer, always mark as correct!
            If there's any similarity or relation, it should be considered correct.
            
            If isCorrect is false, provide a very short Korean hint in "hint" (<= 15 characters) that nudges the user without revealing the full answer. If correct, set hint to an empty string.
            confidence should be high for lenient evaluations (0.8+ recommended)
            reasoning should positively explain which parts were similar enough for acceptance
            
            Respond only with JSON, no other text.
        """.trimIndent()
    }
}
