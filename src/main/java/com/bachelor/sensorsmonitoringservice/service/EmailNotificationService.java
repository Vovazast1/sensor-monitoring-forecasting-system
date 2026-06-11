package com.bachelor.sensorsmonitoringservice.service;

import com.bachelor.sensorsmonitoringservice.events.SensorStatusChangedEvent;
import com.bachelor.sensorsmonitoringservice.model.entity.Action;
import com.bachelor.sensorsmonitoringservice.model.enums.SensorStatus;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.mail.username", matchIfMissing = false)
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy  HH:mm:ss").withZone(ZoneId.systemDefault());

    public void sendNotification(Action action, SensorStatusChangedEvent event) {
        log.debug("[EmailService] sendNotification called — action={} sensor={} status={}",
                action.getId(), event.getSensor().getSensorKey(), event.getNewStatus());

        String to = (String) action.getConfig().get("to");
        log.debug("[EmailService] Resolved 'to' address: '{}'", to);
        if (to == null || to.isBlank()) {
            log.warn("[EmailService] Email action {} has no 'to' address configured", action.getId());
            return;
        }

        boolean isCritical = event.getNewStatus().isCritical();
        String subject = String.format("[%s] %s — %s",
                event.getNewStatus().getDescription(),
                event.getSensor().getName(),
                event.getSensor().getSensorKey());

        String html = buildHtml(event, isCritical);

        log.debug("[EmailService] Sending email from={} to={} subject='{}'", from, to, subject);
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("[EmailService] Email sent successfully to {} for sensor {}", to, event.getSensor().getSensorKey());
        } catch (Exception e) {
            log.error("[EmailService] Failed to send email to={} from={} subject='{}': {}", to, from, subject, e.getMessage(), e);
        }
    }

    private String buildHtml(SensorStatusChangedEvent event, boolean isCritical) {
        String accentColor = isCritical ? "#d32f2f" : "#f57c00";
        String badgeBg     = isCritical ? "#ffebee" : "#fff3e0";
        String statusLabel = event.getNewStatus().getDescription().toUpperCase();
        String sensorName  = event.getSensor().getName();
        String sensorKey   = event.getSensor().getSensorKey();
        String deviceName  = event.getDeviceName();
        String prevStatus  = event.getPreviousStatus().getDescription();
        String newStatus   = event.getNewStatus().getDescription();
        double value       = event.getSensor().getLastValue() != null ? event.getSensor().getLastValue() : 0.0;
        String unit        = event.getSensor().getUnit();
        String time = event.getEventTimestamp() != null ? TIME_FMT.format(event.getEventTimestamp()) : "—";

        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:32px 0;">
                <tr><td align="center">
                  <table width="520" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                    <!-- Header -->
                    <tr>
                      <td style="background:%s;padding:24px 32px;">
                        <p style="margin:0;font-size:12px;color:rgba(255,255,255,0.8);letter-spacing:1px;text-transform:uppercase;">Sensor Alert</p>
                        <h1 style="margin:6px 0 0;font-size:22px;color:#ffffff;">%s</h1>
                        <p style="margin:4px 0 0;font-size:13px;color:rgba(255,255,255,0.85);">%s &nbsp;·&nbsp; %s</p>
                      </td>
                    </tr>

                    <!-- Status badge -->
                    <tr>
                      <td style="padding:24px 32px 0;">
                        <span style="display:inline-block;background:%s;color:%s;font-size:13px;font-weight:700;padding:6px 14px;border-radius:20px;letter-spacing:0.5px;">
                          %s
                        </span>
                      </td>
                    </tr>

                    <!-- Details table -->
                    <tr>
                      <td style="padding:20px 32px 28px;">
                        <table width="100%%" cellpadding="0" cellspacing="0" style="border-collapse:collapse;">
                          <tr>
                            <td style="padding:10px 0;border-bottom:1px solid #f0f0f0;color:#888;font-size:13px;width:120px;">Sensor</td>
                            <td style="padding:10px 0;border-bottom:1px solid #f0f0f0;color:#212121;font-size:13px;font-weight:600;">%s <span style="color:#999;font-weight:400;">(%s)</span></td>
                          </tr>
                          <tr>
                            <td style="padding:10px 0;border-bottom:1px solid #f0f0f0;color:#888;font-size:13px;">Device</td>
                            <td style="padding:10px 0;border-bottom:1px solid #f0f0f0;color:#212121;font-size:13px;">%s</td>
                          </tr>
                          <tr>
                            <td style="padding:10px 0;border-bottom:1px solid #f0f0f0;color:#888;font-size:13px;">Status change</td>
                            <td style="padding:10px 0;border-bottom:1px solid #f0f0f0;color:#212121;font-size:13px;">%s &nbsp;→&nbsp; <strong style="color:%s;">%s</strong></td>
                          </tr>
                          <tr>
                            <td style="padding:10px 0;border-bottom:1px solid #f0f0f0;color:#888;font-size:13px;">Value</td>
                            <td style="padding:10px 0;border-bottom:1px solid #f0f0f0;font-size:20px;font-weight:700;color:%s;">%.2f <span style="font-size:13px;font-weight:400;color:#666;">%s</span></td>
                          </tr>
                          <tr>
                            <td style="padding:10px 0;color:#888;font-size:13px;">Time</td>
                            <td style="padding:10px 0;color:#212121;font-size:13px;">%s</td>
                          </tr>
                        </table>
                      </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                      <td style="background:#fafafa;padding:14px 32px;border-top:1px solid #eeeeee;">
                        <p style="margin:0;font-size:11px;color:#bbb;">Sensor Monitoring System &nbsp;·&nbsp; Automated alert</p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(
                accentColor,
                sensorName, sensorKey, deviceName,
                badgeBg, accentColor, statusLabel,
                sensorName, sensorKey,
                deviceName,
                prevStatus, accentColor, newStatus,
                accentColor, value, unit,
                time
        );
    }
}
