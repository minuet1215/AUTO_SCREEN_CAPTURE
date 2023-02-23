import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ScreenCaptureGUI implements NativeKeyListener {
    private Timer timer;
    private final BlockingQueue<BufferedImage> imageQueue;
    private JTextField timerDelayField;

    Robot robot = new Robot();

    public ScreenCaptureGUI() throws AWTException {
        timerDelayField = new JTextField("500");
        JFrame frame = new JFrame("Screen Capture");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLayout(new BorderLayout());

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        frame.add(imageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JLabel explainButton1 = new JLabel("press 'C' = Capture");
        JLabel explainButton2 = new JLabel("press 'L_control+S' = Auto Capture Start");
        JLabel explainButton3 = new JLabel("press 'L_control+Q' = Stop Capturing");

        buttonPanel.add(explainButton1);
        buttonPanel.add(explainButton2);
        buttonPanel.add(explainButton3);

        JPanel textPanel = new JPanel();
        JLabel timerDelayFieldLabel = new JLabel("시간 입력 (단위: ms)");
        textPanel.add(timerDelayFieldLabel);
        textPanel.add(timerDelayField);

        frame.add(buttonPanel, BorderLayout.WEST);
        frame.add(textPanel, BorderLayout.SOUTH);

        imageQueue = new LinkedBlockingQueue<>();

        Thread writerThread = new Thread(() -> {
            try {
                int index = 1;
                while (true) {
                    BufferedImage image = imageQueue.take();
                    String fileName = "screenshot_" + index + ".jpeg";
                    File output = new File(fileName);

                    Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
                    ImageWriter writer = iter.next();
                    ImageWriteParam iwp = writer.getDefaultWriteParam();
                    iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    iwp.setCompressionQuality(1.0f);

                    FileImageOutputStream outputImage = new FileImageOutputStream(output);
                    writer.setOutput(outputImage);
                    writer.write(null, new IIOImage(image, null, null), iwp);

                    System.out.println("Image saved to file: " + fileName);
                    index++;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        writerThread.start();

        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            ex.printStackTrace();
        }

        frame.setVisible(true);
    }

    private void keyFunction1() {
        captureScreen();
        robot.keyPress(KeyEvent.VK_RIGHT);
        robot.keyRelease(KeyEvent.VK_RIGHT);
    }
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_C) {
            keyFunction1();
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_S && e.getModifiers() == NativeKeyEvent.CTRL_L_MASK) {
            int delay = Integer.parseInt(timerDelayField.getText());
            timer = new Timer(delay, evt -> keyFunction1());
            timer.start();
        } else if (e.getKeyCode() == NativeKeyEvent.VC_Q && e.getModifiers() == NativeKeyEvent.CTRL_L_MASK) {
            timer.stop();
            System.out.println("Stop Screen Capture");
        }
    }

    private void captureScreen() {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage capture = new Robot().createScreenCapture(screenRect);

            imageQueue.put(capture);
            
            System.out.println("Screen captured successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) throws AWTException {
        new ScreenCaptureGUI();
    }
}
