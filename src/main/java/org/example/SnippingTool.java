package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

public class SnippingTool extends JFrame {

    private Point startPoint = null;
    private Point endPoint = null;
    private BufferedImage screenCapture;
    private JPanel canvas;

    public SnippingTool() {
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        try {
            Robot robot = new Robot();
            screenCapture = robot.createScreenCapture(screenRect);
        } catch (AWTException e) {
            e.printStackTrace();
            dispose();
            return;
        }

        canvas = new JPanel() {
            private final Color dimColor = new Color(0, 0, 0, 100);
            private final BasicStroke stroke = new BasicStroke(2);

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                if (screenCapture != null) {
                    g2d.drawImage(screenCapture, 0, 0, getWidth(), getHeight(), this);
                }

                g2d.setColor(dimColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                if (startPoint != null && endPoint != null) {
                    int x = Math.min(startPoint.x, endPoint.x);
                    int y = Math.min(startPoint.y, endPoint.y);
                    int w = Math.abs(startPoint.x - endPoint.x);
                    int h = Math.abs(startPoint.y - endPoint.y);

                    if (w > 0 && h > 0) {
                        g2d.drawImage(screenCapture, x, y, x + w, y + h, x, y, x + w, y + h, this);

                        g2d.setColor(Color.RED);
                        g2d.setStroke(stroke);
                        g2d.drawRect(x, y, w - 1, h - 1);
                    }
                }
            }
        };

        canvas.setOpaque(true);
        canvas.setDoubleBuffered(true);
        setContentPane(canvas);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                endPoint = startPoint;
                canvas.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                canvas.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                endPoint = e.getPoint();
                int x = Math.min(startPoint.x, endPoint.x);
                int y = Math.min(startPoint.y, endPoint.y);
                int w = Math.abs(startPoint.x - endPoint.x);
                int h = Math.abs(startPoint.y - endPoint.y);

                if (w > 0 && h > 0) {
                    setVisible(false);
                    try {
                        BufferedImage crop = screenCapture.getSubimage(x, y, w, h);
                        processImage(crop, x, y, w, h);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        dispose();
                    }
                } else {
                    dispose();
                }
            }
        };
        canvas.addMouseListener(ma);
        canvas.addMouseMotionListener(ma);
    }

    private void processImage(BufferedImage image, int x, int y, int w, int h) {
        new Thread(() -> {
            try {
                ITesseract tesseract = new Tesseract();
                tesseract.setDatapath("tessdata");
                tesseract.setLanguage("eng");

                String text = tesseract.doOCR(image);
                if (text == null || text.trim().isEmpty()) {
                    dispose();
                    return;
                }

                String translated = GeminiTranslator.translate(text.trim());

                SwingUtilities.invokeLater(() -> {
                    TranslationOverlay overlay = new TranslationOverlay(new Rectangle(x, y, w, h), translated);
                    overlay.setVisible(true);
                    dispose();
                    screenCapture = null;
                });
            } catch (Exception e) {
                e.printStackTrace();
                dispose();
            }
        }).start();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            this.toFront();
            this.requestFocus();
            this.setAlwaysOnTop(false);
            this.setAlwaysOnTop(true);
        }
    }
}
