package frontend.ui;

import backend.service.ChatbotService;
import backend.service.DomainFacade;
import util.DateUtil;

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel {
    private final JTextArea chat = new JTextArea();
    private final JTextField input = new JTextField();
    private final ChatbotService bot;
    private final DomainFacade domain;

    public ChatPanel(DomainFacade domain, ChatbotService bot) {
        this.domain = domain; this.bot = bot;
        setLayout(new BorderLayout(8,8));
        chat.setEditable(false);
        add(new JScrollPane(chat), BorderLayout.CENTER);
        add(input, BorderLayout.SOUTH);

        input.addActionListener(e -> {
            String q = input.getText().trim();
            if (q.isEmpty()) return;
            append("You: " + q);
            String a = bot.respond(q, domain, DateUtil.currentMonth());
            append("Bot: " + a);
            input.setText("");
        });

        append("Bot: Hi! Try 'show my spending this month', 'set food budget to 200', 'predict next month groceries', 'add expense 12.50 at starbucks'.");
    }

    private void append(String s) {
        chat.append(s + "\n");
        chat.setCaretPosition(chat.getDocument().getLength());
    }
}
