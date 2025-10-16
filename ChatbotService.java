package backend.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatbotService {
    private final Pattern monthSpend = Pattern.compile("show.*(spend|expense).*month", Pattern.CASE_INSENSITIVE);
    private final Pattern setBudget = Pattern.compile("set (.+) budget to (\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
    private final Pattern predictCat = Pattern.compile("predict.*next month (.+)", Pattern.CASE_INSENSITIVE);
    private final Pattern addExpense = Pattern.compile("add expense (\\d+(?:\\.\\d+)?) for (.+)", Pattern.CASE_INSENSITIVE);
    private final Pattern addExpenseAt = Pattern.compile("add expense (\\d+(?:\\.\\d+)?) (?:at|from|to) (.+)", Pattern.CASE_INSENSITIVE);

    public String respond(String input, DomainFacade domain, String month) {
        Matcher m;
        if (monthSpend.matcher(input).find()) {
            return domain.thisMonthSummary(month);
        } else if ((m = setBudget.matcher(input)).find()) {
            String cat = m.group(1).trim(); double amt = Double.parseDouble(m.group(2));
            String res = domain.setBudget(cat, amt, month);
            return res == null ? "Budget for " + cat + " set to " + amt : res;
        } else if ((m = predictCat.matcher(input)).find()) {
            String cat = m.group(1).trim();
            return domain.predictNextMonth(cat);
        } else if ((m = addExpense.matcher(input)).find()) {
            double amt = Double.parseDouble(m.group(1));
            String cat = m.group(2).trim();
            return domain.quickAddExpense(amt, cat);
        } else if ((m = addExpenseAt.matcher(input)).find()) {
            double amt = Double.parseDouble(m.group(1));
            String merchantText = m.group(2).trim();
            return domain.quickAddExpenseAuto(amt, merchantText);
        }
        return "Try: show my spending this month | set food budget to 200 | predict next month groceries | add expense 12.5 for transport | add expense 12.5 at starbucks";
    }
}
