// Opt-in capability: only senders with a template engine implement this.
public interface TemplateSender extends NotificationSender {
    void updateTemplate(String templateId, String content);
    String previewTemplate(String templateId);
}
