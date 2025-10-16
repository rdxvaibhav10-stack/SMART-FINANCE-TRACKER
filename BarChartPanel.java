package frontend.ui.charts;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class BarChartPanel extends JPanel {
    private Map<String, Double> seriesA = new LinkedHashMap<>();
    private Map<String, Double> seriesB = new LinkedHashMap<>();
    private String aLabel = "Expenses";
    private String bLabel = "Income";

    public void setData(Map<String, Double> a, Map<String, Double> b) {
        this.seriesA = a != null ? a : new LinkedHashMap<>();
        this.seriesB = b != null ? b : new LinkedHashMap<>();
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (seriesA.isEmpty() && seriesB.isEmpty()) { g.drawString("No data", 10, 20); return; }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String[] labels = seriesA.isEmpty() ? seriesB.keySet().toArray(new String[0])
                : seriesA.keySet().toArray(new String[0]);

        double max = 0;
        for (String k : labels) {
            max = Math.max(max, seriesA.getOrDefault(k, 0.0));
            max = Math.max(max, seriesB.getOrDefault(k, 0.0));
        }
        int w = getWidth(), h = getHeight(), pad = 40;
        int chartW = w - pad*2, chartH = h - pad*2;
        int barWidth = Math.max(10, chartW / Math.max(1, labels.length*3));

        g2.setColor(Color.GRAY);
        g2.drawLine(pad, h-pad, w-pad, h-pad);
        g2.drawLine(pad, pad, pad, h-pad);

        for (int i=0;i<labels.length;i++) {
            int groupX = pad + i*(barWidth*3);
            double aVal = seriesA.getOrDefault(labels[i], 0.0);
            double bVal = seriesB.getOrDefault(labels[i], 0.0);
            int aH = (int) ((aVal / (max == 0 ? 1 : max)) * chartH);
            int bH = (int) ((bVal / (max == 0 ? 1 : max)) * chartH);

            g2.setColor(new Color(0xF44336));
            g2.fillRect(groupX, h-pad-aH, barWidth, aH);

            g2.setColor(new Color(0x4CAF50));
            g2.fillRect(groupX+barWidth+4, h-pad-bH, barWidth, bH);

            g2.setColor(Color.BLACK);
            g2.setFont(g2.getFont().deriveFont(10f));
            g2.drawString(labels[i], groupX, h-pad+12);
        }

        g2.setColor(new Color(0xF44336)); g2.fillRect(w-pad-120, pad, 10, 10);
        g2.setColor(Color.BLACK); g2.drawString(aLabel, w-pad-105, pad+10);
        g2.setColor(new Color(0x4CAF50)); g2.fillRect(w-pad-120, pad+16, 10, 10);
        g2.setColor(Color.BLACK); g2.drawString(bLabel, w-pad-105, pad+26);
    }
}
