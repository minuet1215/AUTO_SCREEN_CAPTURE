
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenCaptureGUI implements NativeKeyListener {
    private JFrame frame;
    private JLabel imageLabel;

    public ScreenCaptureGUI() {
        frame = new JFrame("Screen Capture");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        frame.add(imageLabel, BorderLayout.CENTER);

        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            ex.printStackTrace();
        }

        frame.setVisible(true);
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_C && e.getModifiers() == NativeKeyEvent.CTRL_L_MASK) {
            captureScreen();
        }
    }

    public void nativeKeyTyped(NativeKeyEvent e) {}

    public void nativeKeyReleased(NativeKeyEvent e) {}

    private void captureScreen() {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

            BufferedImage capture = new Robot().createScreenCapture(screenRect);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = "screenshot_" + dateFormat.format(new Date()) + ".png";

            File output = new File(fileName);
            ImageIO.write(capture, "png", output);

            ImageIcon icon = new ImageIcon(capture);
            imageLabel.setIcon(icon);

            System.out.println("Screen captured successfully: " + fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ScreenCaptureGUI();
    }
}
