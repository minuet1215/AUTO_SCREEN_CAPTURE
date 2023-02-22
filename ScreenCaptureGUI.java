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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ScreenCaptureGUI implements NativeKeyListener {
    private JFrame frame;
    private JLabel imageLabel;
    private boolean isCapturing;
    private Timer timer;
    private BlockingQueue<BufferedImage> imageQueue;

    Robot robot = new Robot();

    public ScreenCaptureGUI() throws AWTException {
        frame = new JFrame("Screen Capture");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLayout(new BorderLayout());

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        frame.add(imageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JButton explainButton1 = new JButton("press 'C' = Capture");
        JButton explainButton2 = new JButton("press 'L_control+S' = Auto Capture Start");
        JButton explainButton3 = new JButton("press 'L_control+Q' = Stop Capturing");
        buttonPanel.add(explainButton1);
        buttonPanel.add(explainButton2);
        buttonPanel.add(explainButton3);
        frame.add(buttonPanel, BorderLayout.WEST);

        // Create the blocking queue
        imageQueue = new LinkedBlockingQueue<>();

        // Start the writer thread
        Thread writerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        BufferedImage image = imageQueue.take(); // retrieve the image from the blocking queue

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                        String fileName = "screenshot_" + dateFormat.format(new Date()) + ".jpeg";
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
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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

    private void keyFunction1(NativeKeyEvent e) {
        captureScreen();
        robot.keyPress(KeyEvent.VK_RIGHT);
        robot.keyRelease(KeyEvent.VK_RIGHT);
    }
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_C) {
            keyFunction1(e);
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_S && e.getModifiers() == NativeKeyEvent.CTRL_L_MASK) {
            isCapturing = true;
            timer = new Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    keyFunction1(e);
                }
            });
            timer.start();
        } else if (e.getKeyCode() == NativeKeyEvent.VC_Q && e.getModifiers() == NativeKeyEvent.CTRL_L_MASK) {
            isCapturing = false;
            timer.stop();
            System.out.println("Stop Screen Capture");
        }
    }

    private void captureScreen() {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage capture = new Robot().createScreenCapture(screenRect);

            imageQueue.put(capture); // add the image to the blocking queue

            System.out.println("Screen captured successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) throws AWTException {
        new ScreenCaptureGUI();
    }
}
