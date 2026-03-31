package com.game.view;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// Import các class từ package model
import com.game.model.*; 

public class GamePanel extends JPanel implements ActionListener, KeyListener {
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
    private int difficultyLevel = 1;

    // --- KHU VỰC CHỈNH ĐỘ NHẠY MIC (Sửa ở đây cho máy bạn của bạn) ---
    // Hạ thấp số này xuống nếu Mic yếu (Ví dụ: WALK = 2.0, JUMP = 8.0)
    private final double WALK_VOL = 5.0;  
    private final double JUMP_VOL = 18.0; 
    // Hệ số vẽ thanh Mic (tăng lên 10 nếu muốn thanh màu nhảy cao hơn)
    private final int MIC_VISUAL_MULTIPLIER = 4; 

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this); 

        try { 
            File f = new File("background.png");
            if(f.exists()) bgImage = ImageIO.read(f);
        } catch (Exception e) {}

        resetGame();
        
        try {
            audioSensor = new AudioSensor();
            new Thread(audioSensor).start();
        } catch (Exception e) {}
        
        gameTimer = new Timer(16, this);
        gameTimer.start();
    }

    private void resetGame() {
        score = 0;
        difficultyLevel = 1;
        isGameOver = false;
        platforms.clear();
        platforms.add(new Platform(0, 480, 400, 250, false, false, false));
        player = new Player(150, 100); 
        for(int i = 0; i < 6; i++) generateNextPlatform();
    }

    private void generateNextPlatform() {
        if (platforms.isEmpty()) return;
        Platform last = platforms.get(platforms.size() - 1);
        
        int maxGap = Math.min(320, 160 + (difficultyLevel * 15)); 
        int gap = 150 + random.nextInt(maxGap);
        
        int nextX = last.x + last.width + gap;
        int nextY = Math.max(250, Math.min(500, last.y + (random.nextInt(180) - 90)));
        int nextWidth = Math.max(120, 250 - (difficultyLevel * 10)) + random.nextInt(150);
        
        boolean addMouse = false;
        boolean addSaw = false;
        boolean isMoving = false;

        int rand = random.nextInt(100);
        if (difficultyLevel >= 2 && rand < 20) {
            isMoving = true;
        } else {
            int obstacleChance = Math.min(50, 20 + (difficultyLevel * 5));
            int obsRand = random.nextInt(100);
            if (obsRand < obstacleChance / 2) addMouse = true;
            else if (obsRand < obstacleChance) addSaw = true;
        }

        platforms.add(new Platform(nextX, nextY, nextWidth, 300, addMouse, addSaw, isMoving));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isGameOver) { repaint(); return; }

        int speed = 0;
        if (audioSensor != null && audioSensor.isCalibrated()) {
            double vol = audioSensor.getCurrentVolume();
            int speedBonus = difficultyLevel - 1; 
            
            // Sử dụng các biến hằng số thay vì gõ số trực tiếp
            if (vol > JUMP_VOL) { 
                player.jump(vol); 
                speed = 8 + speedBonus; 
                score += 2;
            } else if (vol > WALK_VOL) {
                speed = 4 + speedBonus; 
                score += 1;
            }
        }

        difficultyLevel = (score / 1000) + 1; 

        Iterator<Platform> it = platforms.iterator();
        while (it.hasNext()) {
            Platform p = it.next();
            p.update(speed);
            if (p.x + p.width < -100) it.remove(); 
        }

        if (player != null) player.update(platforms);

        Rectangle playerHitbox = new Rectangle(150 + 15, player.getY() + 10, 60 - 30, 75 - 10);
        for (Platform p : platforms) {
            Rectangle mH = p.getMouseHitbox();
            Rectangle sH = p.getSawHitbox();
            if ((mH != null && playerHitbox.intersects(mH)) || (sH != null && playerHitbox.intersects(sH))) {
                isGameOver = true; 
                break;
            }
        }

        if (platforms.size() < 10) generateNextPlatform();
        if (player.getY() > HEIGHT) isGameOver = true;

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (bgImage != null) g2d.drawImage(bgImage, 0, 0, WIDTH, HEIGHT, null);
        for (Platform p : platforms) p.draw(g2d);
        if (player != null) player.draw(g2d);
        drawUI(g2d);
    }

    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.drawString("ĐIỂM: " + (score/10), 30, 40);
        g2d.setColor(new Color(255, 69, 0));
        g2d.drawString("LEVEL: " + difficultyLevel, WIDTH - 150, 40);
        
        if (audioSensor != null) {
            int v = (int)audioSensor.getCurrentVolume();
            g2d.setColor(Color.BLACK);
            g2d.drawString("MIC:", 30, 80);
            g2d.setColor(Color.WHITE);
            g2d.drawRect(80, 62, 200, 20); 
            
            // Màu sắc thanh Mic cũng dựa trên biến hằng số
            if (v > JUMP_VOL) g2d.setColor(Color.RED); 
            else if (v > WALK_VOL) g2d.setColor(Color.ORANGE); 
            else g2d.setColor(Color.GREEN);
            
            // Sử dụng MIC_VISUAL_MULTIPLIER để thanh mic nhảy cao hơn
            g2d.fillRect(80, 62, Math.min(200, v * MIC_VISUAL_MULTIPLIER), 20);
            
            if (!audioSensor.isCalibrated()) {
                g2d.setColor(new Color(0, 0, 0, 160)); g2d.fillRect(0, 0, WIDTH, HEIGHT);
                g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.BOLD, 26));
                g2d.drawString("ĐANG ĐO TIẾNG ỒN MÔI TRƯỜNG...", WIDTH/2 - 220, HEIGHT/2 - 20);
            }
        }

        if (isGameOver) {
            g2d.setColor(new Color(0, 0, 0, 180)); g2d.fillRect(0, 0, WIDTH, HEIGHT);
            g2d.setColor(Color.RED); g2d.setFont(new Font("Arial", Font.BOLD, 65));
            g2d.drawString("THẤT BẠI!", WIDTH/2 - 160, HEIGHT/2 - 20);
            g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.PLAIN, 26));
            g2d.drawString("NHẤN SPACE ĐỂ CHƠI LẠI", WIDTH/2 - 170, HEIGHT/2 + 50);
        }
    }

    @Override public void keyPressed(KeyEvent e) { if (isGameOver && e.getKeyCode() == KeyEvent.VK_SPACE) resetGame(); }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}