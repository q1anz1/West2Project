package west2project.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EmailService {
    private JavaMailSender javaMailSender;
    public void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setTo(to);
        helper.setFrom("3525382599@qq.com");
        helper.setSubject(subject);
        helper.setText(text);
        javaMailSender.send(helper.getMimeMessage());
    }

    public void sendEmailCode(String to,String code)throws MessagingException{
        String subject = "west2Project邮箱验证码";
        String text = "验证码为： "+code+" ，15分钟内有效。";
        sendEmail(to,subject,text);
    }
}
