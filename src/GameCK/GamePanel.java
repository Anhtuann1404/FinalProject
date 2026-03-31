package GameCK;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    // Kích thước chuẩn
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

    // NGƯỠNG ÂM THANH
    private final double WALK_VOL = 5.0;  // Nói khẽ để đi bộ
    private final double JUMP_VOL = 18.0; // Hét vừa để nhảy

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this); 

        try { 
            bgImage = ImageIO.read(new File("background.png")); 
        } catch (Exception e) {}

        resetGame();
        
        audioSensor = new AudioSensor();
        new Thread(audioSensor).start();
        
        gameTimer = new Timer(16, this);
        gameTimer.start();
    }

    private void resetGame() {
        score = 0;
        isGameOver = false;
        platforms.clear();
        
        // BỤC ĐẦU TIÊN: Đã rút ngắn xuống 400px để game vào nhịp nhanh hơn
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
        
        // CHIA TỶ LỆ VẬT CẢN (Chỉ xuất hiện trên bục đủ rộng)
        if (nextWidth >= 250) {
            int rand = random.nextInt(100);
            if (rand < 25) {
                addMouse = true; // 25% tỷ lệ có Chuột
            } else if (rand < 50) {
                addSaw = true;   // 25% tỷ lệ có Cưa
            } 
            // 50% còn lại là bục trống an toàn
        }

        platforms.add(new Platform(nextX, nextY, nextWidth, 300, addMouse, addSaw));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isGameOver) { 
            repaint(); 
            return; 
        }

        // 1. CẬP NHẬT TRỌNG LỰC CHO NHÂN VẬT
        player.update(platforms);

        // 2. KIỂM TRA VA CHẠM VỚI VẬT CẢN
        Rectangle playerHitbox = new Rectangle(150 + 15, player.getY() + 10, 60 - 30, 75 - 10);
        
        for (Platform p : platforms) {
            Rectangle mouseHitbox = p.getMouseHitbox();
            Rectangle sawHitbox = p.getSawHitbox();
            
            // Nếu đụng chuột HOẶC đụng cưa -> Game Over
            if ((mouseHitbox != null && playerHitbox.intersects(mouseHitbox)) || 
                (sawHitbox != null && playerHitbox.intersects(sawHitbox))) {
                isGameOver = true; 
                break;
            }
        }

        // 3. XỬ LÝ ÂM THANH VÀ DI CHUYỂN
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

        // 4. DI CHUYỂN BỤC ĐẤT VÀ VẬT CẢN
        Iterator<Platform> it = platforms.iterator();
        while (it.hasNext()) {
            Platform p = it.next();
            p.update(speed);
            if (p.x + p.width < -100) {
                it.remove(); 
            }
        }

        if (platforms.size() < 10) {
            generateNextPlatform();
        }
        
        if (player.getY() > HEIGHT) {
            isGameOver = true; // Rớt vực
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Vẽ nền
        if (bgImage != null) g2d.drawImage(bgImage, 0, 0, WIDTH, HEIGHT, null);
        
        // Vẽ bục (bao gồm cả cưa và chuột bên trong)
        for (Platform p : platforms) p.draw(g2d);
        
        // Vẽ nhân vật
        player.draw(g2d);

        // Vẽ giao diện (UI)
        drawUI(g2d);
    }

    private void drawUI(Graphics2D g2d) {
        // Điểm số
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.drawString("ĐIỂM: " + (score/10), 30, 40);
        
        // Thanh Mic
        int v = (int)audioSensor.getCurrentVolume();
        g2d.drawString("MIC:", 30, 80);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(80, 62, 200, 20); 
        
        if (v > JUMP_VOL) g2d.setColor(Color.RED); 
        else if (v > WALK_VOL) g2d.setColor(Color.ORANGE); 
        else g2d.setColor(Color.GREEN);
        
        g2d.fillRect(80, 62, Math.min(200, v * 4), 20);

        // Màn hình đang đo tiếng ồn
        if (!audioSensor.isCalibrated()) {
            g2d.setColor(new Color(0, 0, 0, 160)); 
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
            g2d.setColor(Color.WHITE); 
            g2d.setFont(new Font("Arial", Font.BOLD, 26));
            g2d.drawString("ĐANG ĐO TIẾNG ỒN MÔI TRƯỜNG...", WIDTH/2 - 220, HEIGHT/2 - 20);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20)); 
            g2d.drawString("Vui lòng giữ im lặng trong 2 giây", WIDTH/2 - 150, HEIGHT/2 + 20);
        }

        // Màn hình Game Over
        if (isGameOver) {
            g2d.setColor(new Color(0, 0, 0, 180)); 
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
            g2d.setColor(Color.RED); 
            g2d.setFont(new Font("Arial", Font.BOLD, 65));
            g2d.drawString("THẤT BẠI!", WIDTH/2 - 160, HEIGHT/2 - 20);
            g2d.setColor(Color.WHITE); 
            g2d.setFont(new Font("Arial", Font.PLAIN, 26));
            g2d.drawString("NHẤN SPACE ĐỂ CHƠI LẠI", WIDTH/2 - 170, HEIGHT/2 + 50);
        }
    }

    @Override public void keyPressed(KeyEvent e) { 
        if (isGameOver && e.getKeyCode() == KeyEvent.VK_SPACE) resetGame(); 
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}