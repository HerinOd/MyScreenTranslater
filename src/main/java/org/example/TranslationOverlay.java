package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TranslationOverlay extends JWindow {

    private Point initialClick;

    public TranslationOverlay(Rectangle captureRect, String translatedText) {
        setAlwaysOnTop(true);

        Color bgColor = new Color(32, 33, 36);
        Color textColor = new Color(100, 255, 120);
        Color borderColor = new Color(255, 165, 0);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(bgColor);
        contentPanel.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Consolas", Font.BOLD, 14));
        closeButton.setForeground(Color.RED);
        closeButton.setBackground(bgColor);
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(bgColor);
        header.add(closeButton, BorderLayout.EAST);

        JTextArea textArea = new JTextArea(translatedText);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, getSmartFontSize()));
        textArea.setForeground(textColor);
        textArea.setBackground(bgColor);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        int maxWidth = 300;
        int maxHeight = 310;

        textArea.setSize(new Dimension(maxWidth, Short.MAX_VALUE));
        Dimension preferredSize = textArea.getPreferredSize();

        int finalWidth = Math.min(preferredSize.width + 30, maxWidth);
        int finalHeight = Math.min(preferredSize.height + 40, maxHeight);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(finalWidth, finalHeight));

        contentPanel.add(header, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(contentPanel);
        pack();

        calculateSmartPosition(captureRect);

        MouseAdapter dragHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { initialClick = e.getPoint(); }

            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                setLocation(thisX + xMoved, thisY + yMoved);
            }
        };
        header.addMouseListener(dragHandler);
        header.addMouseMotionListener(dragHandler);
        textArea.addMouseListener(dragHandler);
        textArea.addMouseMotionListener(dragHandler);
    }

    private int getSmartFontSize() {
        int height = Toolkit.getDefaultToolkit().getScreenSize().height;
        int size = height / 40;
        if (size < 14) return 14;
        if (size > 24) return 24;
        return size;
    }

    private void calculateSmartPosition(Rectangle rect) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int gap = 10;
        int w = getWidth();
        int h = getHeight();

        int spaceRight = screenSize.width - (rect.x + rect.width);
        int spaceLeft = rect.x;
        int spaceBottom = screenSize.height - (rect.y + rect.height);
        int spaceTop = rect.y;

        int x, y;

        if (spaceRight >= w + gap) {
            x = rect.x + rect.width + gap;
            y = rect.y;
        } else if (spaceBottom >= h + gap) {
            x = rect.x;
            y = rect.y + rect.height + gap;
        } else if (spaceLeft >= w + gap) {
            x = rect.x - w - gap;
            y = rect.y;
        } else if (spaceTop >= h + gap) {
            x = rect.x;
            y = rect.y - h - gap;
        } else {
            int maxSpace = Math.max(Math.max(spaceRight, spaceLeft), Math.max(spaceBottom, spaceTop));

            if (maxSpace == spaceRight) {
                x = screenSize.width - w;
                y = rect.y;
            } else if (maxSpace == spaceBottom) {
                x = rect.x;
                y = screenSize.height - h;
            } else if (maxSpace == spaceLeft) {
                x = 0;
                y = rect.y;
            } else {
                x = rect.x;
                y = 0;
            }
        }

        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + w > screenSize.width) x = screenSize.width - w;
        if (y + h > screenSize.height) y = screenSize.height - h;

        setLocation(x, y);
    }
}
