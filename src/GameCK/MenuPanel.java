package GameCK;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;

public class MenuPanel extends JPanel {
    private Image menuBg;
    private Main mainFrame; 
    
    private final int WIDTH = 950;
    private final int HEIGHT = 600;

    public MenuPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setLayout(null); 

        // 1. TẢI ẢNH NỀN
        try {
            menuBg = ImageIO.read(new File("menu_bg.png"));
        } catch (Exception e) {
            System.out.println("🚨 Lỗi: Không tìm thấy file menu_bg.png");
        }

        // --- THÔNG SỐ NÚT MỚI (To hơn và sát nhau hơn) ---
        int btnW = 240; // Tăng chiều rộng
        int btnH = 85;  // Tăng chiều cao
        int centerX = (WIDTH - btnW) / 2; 

        // 2. NÚT PLAY
        JButton playBtn = new JButton();
        try {
            Image img = ImageIO.read(new File("btn_play.png"));
            // Ép ảnh đúng kích thước hiển thị
            Image scaled = img.getScaledInstance(btnW, btnH, Image.SCALE_SMOOTH);
            playBtn.setIcon(new ImageIcon(scaled));
            playBtn.setContentAreaFilled(false);
            playBtn.setBorderPainted(false);
            playBtn.setFocusPainted(false);
        } catch (Exception e) {
            playBtn.setText("PLAY");
        }
        // Đặt ở vị trí 400 (hơi lệch xuống dưới một chút)
        playBtn.setBounds(centerX, 400, btnW, btnH); 
        playBtn.addActionListener(e -> mainFrame.startGame()); 

        // 3. NÚT GUIDE
        JButton guideBtn = new JButton();
        try {
            Image img = ImageIO.read(new File("btn_guide.png"));
            Image scaled = img.getScaledInstance(btnW, btnH, Image.SCALE_SMOOTH);
            guideBtn.setIcon(new ImageIcon(scaled));
            guideBtn.setContentAreaFilled(false);
            guideBtn.setBorderPainted(false);
            guideBtn.setFocusPainted(false);
        } catch (Exception e) {
            guideBtn.setText("GUIDE");
        }
        // GIẢI PHÁP: Đặt Y của nút dưới chỉ cách nút trên 65px (trong khi nút cao 85px)
        // Việc này giúp 2 nút "ăn gian" khoảng trống và trông sát nhau hơn
        guideBtn.setBounds(centerX, 448, btnW, btnH); 
        guideBtn.addActionListener(e -> showGuide());

        this.add(playBtn);
        this.add(guideBtn);
    }

    private void showGuide() {
        String msg = "WELCOME TO SCREAM WARRIOR!\n\n" +
                     "- Silence: Nhân vật đứng yên.\n" +
                     "- Speak Softly: Đi bộ.\n" +
                     "- Scream Loud: Nhảy lên!\n\n" +
                     "Hét thật to để vượt vực thẳm nhé!";
        JOptionPane.showMessageDialog(this, msg, "How to Play", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Bật khử mờ cao cấp
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (menuBg != null) {
            g2d.drawImage(menuBg, 0, 0, WIDTH, HEIGHT, null);
        }
    }
}
