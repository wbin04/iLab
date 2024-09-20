package Chat;

import java.util.ArrayList;
import java.util.List;

public class ChatMessages {
    private List<String> messages;

    // Constructor
    public ChatMessages() {
        messages = new ArrayList<>();
    }

    // Thêm tin nhắn mới vào danh sách
    public synchronized void addMessage(String message) {
        messages.add(message);
    }

    // Lấy đoạn hội thoại dưới dạng chuỗi đã được định dạng
    public synchronized String getFormattedMessages() {
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append(message).append("\n");
        }
        return sb.toString();
    }
}
