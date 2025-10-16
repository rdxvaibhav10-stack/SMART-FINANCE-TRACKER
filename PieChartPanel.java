package frontend.ui.charts;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class PieChartPanel extends JPanel {
    private Map<String, Double> data;
    public void setData(Map<String, Double> data) { this.data = data; repaint(); }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) { g.drawString("No data", 10, 20); return; }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        int size = Math.min(getWidth(), getHeight()) - 60;
        int x = (getWidth()-size)/2, y = (getHeight()-size)/2;
        int start = 0, i = 0;
        Color[] colors = {new Color(0x4CAF50), new Color(0x2196F3), new Color(0xFF9800),
                new Color(0x9C27B0), new Color(0xF44336), new Color(0x009688), new Color(0x795548)};
        for (var e : data.entrySet()) {
            double pct = e.getValue() / total;
            int arc = (int) Math.round(pct * 360);
            g2.setColor(colors[i % colors.length]);
            g2.fillArc(x, y, size, size, start, arc);
            start += arc; i++;
        }
        int lx = x + size + 20, ly = y; i = 0;
        for (var e : data.entrySet()) {
            g2.setColor(colors[i % colors.length]); g2.fillRect(lx, ly + i*18, 12, 12);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(e.getKey() + " (" + String.format("%.0f%%", 100*(e.getValue()/total)) + ")", lx + 18, ly + 10 + i*18);
            i++;
        }
    }
}
