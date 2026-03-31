package com.game.view;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// Import các lớp từ package khác (Model)
import com.game.model.Player;
import com.game.model.Platform;
import com.game.model.AudioSensor;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    // Kích thước chuẩn - Để private để đảm bảo tính đóng gói (Encapsulation)
    private final int WIDTH = 950;
    private final int HEIGHT = 600;
    
    private Player player;
    private AudioSensor audioSensor;
    private Timer gameTimer;
    private Image bgImage; 
    private List<Platform> platforms = new ArrayList<>();
    private int score = 0;
    private boolean isGameOver = false;
    private Random random = new Random();

    // NGƯỠNG ÂM THANH (Dễ dàng tinh chỉnh cho phần Demo)
    private final double WALK_VOL = 5.0;  
    private final double JUMP_VOL = 18.0; 

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this); 

        // 1. XỬ LÝ NGOẠI LỆ (Đáp ứng tiêu chí Exception Handling)
        try { 
            bgImage = ImageIO.read(new File("background.png")); 
        } catch (IOException e) {
            System.err.println("Lỗi: Không tìm thấy file background.png. Vui lòng kiểm tra thư mục dự án.");
        }

        resetGame();
        
        // Khởi tạo và chạy luồng âm thanh
        audioSensor = new AudioSensor();
        new Thread(audioSensor).start();
        
        // Game Loop (60 FPS)
        gameTimer = new Timer(16, this);
        gameTimer.start();
    }

    private void resetGame() {
        score = 0;
        isGameOver = false;
        if(platforms != null) platforms.clear();
        
        // Bục xuất phát
        platforms.add(new Platform(0, 480, 400, 250, false, false));
        player = new Player(150, 100); 
        
        for(int i = 0; i < 6; i++) {
            generateNextPlatform();
        }
    }

    private void generateNextPlatform() {
        if (platforms.isEmpty()) return;
        Platform last = platforms.get(platforms.size() - 1);
        
        int gap = 160 + random.nextInt(150);
        int nextX = last.x + last.width + gap;
        int nextY = Math.max(280, Math.min(520, last.y + (random.nextInt(160) - 80)));
        int nextWidth = 200 + random.nextInt(200);
        
        boolean addMouse = false;
        boolean addSaw = false;
        
        if (nextWidth >= 250) {
            int rand = random.nextInt(100);
            if (rand < 25) addMouse = true; 
            else if (rand < 50) addSaw = true;   
        }
        platforms.add(new Platform(nextX, nextY, nextWidth, 300, addMouse, addSaw));
    }

    // --- PHẦN CONTROLLER: XỬ LÝ LOGIC ---
    @Override
    public void actionPerformed(ActionEvent e) {
        if (isGameOver) { 
            repaint(); 
            return; 
        }
        updateGameLogic();
        repaint();
    }

    private void updateGameLogic() {
        // 1. Cập nhật vật lý nhân vật
        player.update(platforms);

        // 2. Kiểm tra va chạm (Collision Detection)
        checkCollisions();

        // 3. Xử lý âm thanh điều khiển di chuyển
        handleAudioInput();

        // 4. Kiểm tra rơi vực
        if (player.getY() > HEIGHT) {
            isGameOver = true;
        }
    }

    private void handleAudioInput() {
        double vol = audioSensor.getCurrentVolume();
        int speed = 0;

        if (audioSensor.isCalibrated()) {
            if (vol > JUMP_VOL) {
                player.jump(vol); 
                speed = 8; 
                score += 2;
            } else if (vol > WALK_VOL) {
                speed = 4; 
                score += 1;
            }
        }

        // Di chuyển thế giới game (Side Scrolling)
        moveWorld(speed);
    }

    private void moveWorld(int speed) {
        Iterator<Platform> it = platforms.iterator();
        while (it.hasNext()) {
            Platform p = it.next();
            p.update(speed);
            if (p.x + p.width < -100) it.remove(); 
        }

        if (platforms.size() < 10) generateNextPlatform();
    }

    private void checkCollisions() {
        // Hitbox tinh chỉnh để khớp với hình ảnh nhân vật
        Rectangle playerHitbox = new Rectangle(150 + 15, player.getY() + 10, 60 - 30, 75 - 10);
        for (Platform p : platforms) {
            if ((p.getMouseHitbox() != null && playerHitbox.intersects(p.getMouseHitbox())) || 
                (p.getSawHitbox() != null && playerHitbox.intersects(p.getSawHitbox()))) {
                isGameOver = true; 
                break;
            }
        }
    }

    // --- PHẦN VIEW: VẼ GIAO DIỆN ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Khử răng cưa giúp game trông mượt hơn
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (bgImage != null) g2d.drawImage(bgImage, 0, 0, WIDTH, HEIGHT, null);
        
        for (Platform p : platforms) p.draw(g2d);
        player.draw(g2d);

        drawUI(g2d);
        
        if (isGameOver) drawGameOverScreen(g2d);
    }

    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 20, 40);
        
        // Vẽ thanh đo âm lượng để giảng viên thấy Mic đang hoạt động
        g2d.drawString("Mic Level:", 20, 70);
        g2d.setColor(Color.GREEN);
        g2d.fillRect(120, 55, (int)audioSensor.getCurrentVolume() * 5, 20);
    }

    private void drawGameOverScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        g2d.drawString("GAME OVER", WIDTH/2 - 150, HEIGHT/2);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Press 'R' to Restart", WIDTH/2 - 80, HEIGHT/2 + 50);
    }

    // --- XỬ LÝ SỰ KIỆN PHÍM ---
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_R && isGameOver) {
            resetGame();
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}