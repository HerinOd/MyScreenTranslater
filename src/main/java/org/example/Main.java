package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main implements NativeKeyListener {

    private static boolean isSnipping = false;

    public static void main(String[] args) {

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("Lỗi Hook bàn phím: " + ex.getMessage());
            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(new Main());

        SwingUtilities.invokeLater(Main::setupTray);

        System.out.println("=== APP READY: Bấm [ALT + Q] hoặc dùng Icon khay hệ thống ===");
    }

    private static void setupTray() {
        if (!SystemTray.isSupported()) return;

        SystemTray tray = SystemTray.getSystemTray();
        PopupMenu popup = new PopupMenu();

        MenuItem itemSnip = new MenuItem("Dịch màn hình (Alt+Q)");
        itemSnip.addActionListener(e -> startSnipping());

        MenuItem itemExit = new MenuItem("Thoát");
        itemExit.addActionListener(e -> {
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException ex) {
                ex.printStackTrace();
            }
            System.exit(0);
        });

        popup.add(itemSnip);
        popup.addSeparator();
        popup.add(itemExit);

        Image image;
        URL imageUrl = Main.class.getResource("/icon.jpg");

        if (imageUrl != null) {
            image = Toolkit.getDefaultToolkit().getImage(imageUrl);
        } else {
            image = Toolkit.getDefaultToolkit().getImage("icon.jpg");
        }

        TrayIcon trayIcon = new TrayIcon(image, "MyScreenTranslator\n         *HieuDi*", popup);
        trayIcon.setImageAutoSize(true);

        trayIcon.addActionListener(e -> startSnipping());

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Không thể thêm icon vào khay hệ thống.");
            e.printStackTrace();
        }
    }

    private static void startSnipping() {
        if (isSnipping) return;

        isSnipping = true;
        SwingUtilities.invokeLater(() -> {
            SnippingTool tool = new SnippingTool();
            tool.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    isSnipping = false;
                }
            });
            tool.setVisible(true);
        });
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_Q && (e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) {
            startSnipping();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {}

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}
}
