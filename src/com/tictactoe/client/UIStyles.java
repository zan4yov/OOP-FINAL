package com.tictactoe.client;

import javax.swing.*;
import java.awt.*;

public class UIStyles {

    public static final Color PRIMARY = new Color(103, 58, 183);
    public static final Color ACCENT = new Color(124, 77, 255);
    public static final Color CARD = new Color(34, 32, 48);
    public static final Color TEXT = Color.WHITE;

    public static void install() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("TextField.background", new Color(50, 50, 70));
        UIManager.put("TextArea.background", new Color(50, 50, 70));
    }

    // A simple rounded button with gradient background
    public static class RoundedButton extends JButton {
        private static final int ARC = 24;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(TEXT);
            setFont(getFont().deriveFont(Font.BOLD, 14f));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color c1 = PRIMARY;
            Color c2 = ACCENT;

            if (!isEnabled()) {
                c1 = c1.darker().darker();
                c2 = c2.darker().darker();
            } else if (getModel().isRollover()) {
                c1 = c1.brighter();
                c2 = c2.brighter();
            }

            GradientPaint gp = new GradientPaint(0, 0, c1, 0, h, c2);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, ARC, ARC);

            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    // A rounded panel with optional gradient background
    public static class RoundedPanel extends JPanel {
        private static final int ARC = 20;

        public RoundedPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            GradientPaint gp = new GradientPaint(0, 0, new Color(48, 42, 66), 0, h, new Color(30, 27, 44));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, ARC, ARC);

            super.paintComponent(g);
            g2.dispose();
        }
    }
}
