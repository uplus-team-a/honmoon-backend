package site.honmoon.auth.service

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: SpringTemplateEngine,
) {
    private val replyTo = "honmoon.site@gmail.com"

    /**
     * HTML 템플릿으로 매직 링크 메일 발송
     */
    fun sendMagicLinkHtml(email: String, link: String, purpose: String = "로그인", name: String = "") {
        val mimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")
        helper.setFrom(replyTo, "혼문")
        helper.setTo(email)
        helper.setReplyTo(replyTo)
        val titleName = if (name.isNotBlank()) "[ $name ] " else "회원"
        val subject = "${titleName}님의 혼문 $purpose 확인 링크"
        helper.setSubject(subject)

        val context = Context().apply {
            setVariable("subject", subject)
            setVariable("preheader", "혼문 $purpose 링크 안내드립니다. 링크 유효시간은 15분입니다.")
            setVariable("headline", "$purpose 을(를) 계속하세요")
            setVariable("expiresInMinutes", 15)
            setVariable("link", link)
            setVariable("replyTo", replyTo)
            setVariable("logoUrl", "https://storage.googleapis.com/honmoon-bucket/image/honmmon.png")
        }
        val html = templateEngine.process("email/magic-link", context)
        helper.setText(buildMagicLinkText(link, purpose), html)
        mimeMessage.addHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click")
        mimeMessage.addHeader("X-Auto-Response-Suppress", "All")
        mailSender.send(mimeMessage)
    }

    private fun buildMagicLinkText(link: String, purpose: String): String {
        return """
        혼문 $purpose 링크 안내드립니다.
        
        아래 링크를 클릭해 $purpose 을(를) 완료하세요. 링크 유효시간은 15분입니다.
        $link
        
        버튼이 동작하지 않으면 위 링크를 복사하여 브라우저 주소창에 붙여넣기 해주세요.
        
        문의: $replyTo
        """.trimIndent()
    }
}
