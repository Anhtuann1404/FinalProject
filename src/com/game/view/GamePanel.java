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

// QUAN TRỌNG: Import tất cả từ package model
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

    // Ngưỡng âm thanh
    private final double WALK_VOL = 5.0;  
    private final double JUMP_VOL = 18.0; 

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this); 
        
        try { 
            File f = new File("background.png");
            if(f.exists()) bgImage = ImageIO.read(f);
        } catch (Exception e) {
            System.err.println("Không load được background!");
        }

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
        // Bục đầu tiên: Không có chuột, không cưa, không di chuyển
        platforms.add(new Platform(0, 480, 400, 250, false, false, false));
        player = new Player(150, 100); 
        for(int i = 0; i < 6; i++) generateNextPlatform();
    }

    private void generateNextPlatform() {
        if (platforms.isEmpty()) return;
        Platform last = platforms.get(platforms.size() - 1);
        int gap = 160 + random.nextInt(Math.min(300, 150 + (difficultyLevel * 10)));
        int nextX = last.x + last.width + gap;
        int nextY = Math.max(250, Math.min(520, last.y + (random.nextInt(160) - 80)));
        int nextWidth = Math.max(150, 250 - (difficultyLevel * 5)) + random.nextInt(150);
        
        boolean hasM = false, hasS = false, isMov = false;
        int r = random.nextInt(100);
        if (difficultyLevel >= 2 && r < 20) isMov = true;
        else {
            int chance = Math.min(50, 20 + (difficultyLevel * 5));
            int r2 = random.nextInt(100);
            if (r2 < chance / 2) hasM = true; else if (r2 < chance) hasS = true;
        }
        platforms.add(new Platform(nextX, nextY, nextWidth, 300, hasM, hasS, isMov));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isGameOver) { repaint(); return; }

        int speed = 0;
        if (audioSensor != null && audioSensor.isCalibrated()) {
            double vol = audioSensor.getCurrentVolume();
            if (vol > JUMP_VOL) { 
                player.jump(vol); 
                speed = 8 + (difficultyLevel - 1); 
                score += 2;
            } else if (vol > WALK_VOL) { 
                speed = 4 + (difficultyLevel - 1); 
                score += 1;
            }
        }

        difficultyLevel = (score / 1500) + 1;

        // Cập nhật bục
        for (Platform p : platforms) p.update(speed);
        
        // Cập nhật nhân vật
        if (player != null) player.update(platforms);

        // XỬ LÝ VA CHẠM (VẬT CẢN VÀ VÀNG)
        Rectangle pHit = new Rectangle(150 + 15, player.getY() + 10, 30, 65);
        for (Platform p : platforms) {
            // Đụng vật cản -> Chết
            if ((p.getMouseHitbox() != null && pHit.intersects(p.getMouseHitbox())) || 
                (p.getSawHitbox() != null && pHit.intersects(p.getSawHitbox()))) {
                isGameOver = true; break;
            }
            // Ăn vàng -> Cộng điểm lớn
            for (Coin c : p.coins) {
                if (!c.isCollected && pHit.intersects(c.getHitbox())) {
                    c.isCollected = true;
                    score += 1000; // Mỗi xu được 100 điểm hiển thị (1000/10)
                }
            }
        }

        // Xóa bục đã trôi xa
        Iterator<Platform> it = platforms.iterator();
        while (it.hasNext()) { if (it.next().x < -500) it.remove(); }

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
        g2d.setColor(Color.BLACK); g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("ĐIỂM: " + (score/10), 30, 45);
        g2d.setColor(new Color(255, 69, 0));
        g2d.drawString("LEVEL: " + difficultyLevel, WIDTH - 160, 45);
        
        if (audioSensor != null) {
            int v = (int)audioSensor.getCurrentVolume();
            g2d.setColor(Color.BLACK); g2d.drawString("MIC:", 30, 85);
            g2d.setColor(Color.WHITE); g2d.drawRect(90, 67, 200, 20); 
            if (v > JUMP_VOL) g2d.setColor(Color.RED); 
            else if (v > WALK_VOL) g2d.setColor(Color.ORANGE); 
            else g2d.setColor(Color.GREEN);
            g2d.fillRect(90, 67, Math.min(200, v * 4), 20);
            
            if (!audioSensor.isCalibrated()) {
                g2d.setColor(new Color(0, 0, 0, 160)); g2d.fillRect(0, 0, WIDTH, HEIGHT);
                g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.BOLD, 26));
                g2d.drawString("ĐANG ĐO TIẾNG ỒN...", WIDTH/2 - 130, HEIGHT/2);
            }
        }
        if (isGameOver) {
            g2d.setColor(new Color(0, 0, 0, 180)); g2d.fillRect(0, 0, WIDTH, HEIGHT);
            g2d.setColor(Color.RED); g2d.setFont(new Font("Arial", Font.BOLD, 60));
            g2d.drawString("THẤT BẠI!", WIDTH/2 - 150, HEIGHT/2 - 20);
            g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            g2d.drawString("NHẤN SPACE ĐỂ CHƠI LẠI", WIDTH/2 - 160, HEIGHT/2 + 40);
        }
    }

    @Override public void keyPressed(KeyEvent e) { if (isGameOver && e.getKeyCode() == KeyEvent.VK_SPACE) resetGame(); }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}