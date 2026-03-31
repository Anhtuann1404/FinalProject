package com.game.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.game.model.*;
import com.game.controller.SoundManager;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 950, HEIGHT = 600;
    
    // HỆ THỐNG TRẠNG THÁI GAME
    private enum State { MENU, PLAYING, GAMEOVER }
    private State currentState = State.MENU; 
    
    private Player player;
    private AudioSensor audioSensor;
    private Timer gameTimer;
    
    // QUẢN LÝ ẢNH
    private Image bgImage; 
    private Image flagImgA;
    private Image flagImgB;
    
    private int flagAnimTimer = 0;
    private boolean isFlagA = true;
    
    // DANH SÁCH THỰC THỂ TRONG GAME
    private List<Platform> platforms = new ArrayList<>();
    private List<Bee> bees = new ArrayList<>(); 
    private List<FallingObject> fallingObjects = new ArrayList<>(); 
    private List<Particle> particles = new ArrayList<>(); 
    
    // THÔNG SỐ GAME
    private int score = 0;
    private int highScore = 0; 
    private int difficultyLevel = 1;
    private Random random = new Random();
    
    // TIMERS VÀ DELAYS
    private int meteorSpawnTimer = 0; 
    private int walkSoundTimer = 0;
    private int startDelay = 0; 

    // ĐỘ NHẠY MIC
    private double walkThreshold = 1.5;  
    private double jumpThreshold = 7.0; 

    // TỌA ĐỘ HUD
    private final int HUD_Y = 15;
    private final int HUD_HEIGHT = 50;

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this); 
        
        SoundManager.loadAllSounds();
        loadHighScore(); 
        
        // NẠP ẢNH TỪ THƯ MỤC GỐC
        try { 
            File fBg = new File("background.png");
            if(fBg.exists()) bgImage = ImageIO.read(fBg);
            
            File fFlagA = new File("flag_blue_a.png");
            if(fFlagA.exists()) flagImgA = ImageIO.read(fFlagA);
            
            File fFlagB = new File("flag_blue_b.png");
            if(fFlagB.exists()) flagImgB = ImageIO.read(fFlagB);
        } catch (Exception e) {
            System.err.println("Lỗi nạp ảnh: Kiểm tra background.png, flag_blue_a.png, flag_blue_b.png");
        }

        resetGame();
        currentState = State.MENU; 
        
        try {
            audioSensor = new AudioSensor();
            new Thread(audioSensor).start();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo AudioSensor: " + e.getMessage());
        }
        
        gameTimer = new Timer(16, this); // ~60 FPS
        gameTimer.start();
    }

    private void loadHighScore() {
        try (BufferedReader br = new BufferedReader(new FileReader("highscore.txt"))) {
            String line = br.readLine();
            if (line != null) highScore = Integer.parseInt(line);
        } catch (Exception e) { 
            highScore = 0; 
        }
    }

    private void saveHighScore() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("highscore.txt"))) {
            bw.write(String.valueOf(highScore));
        } catch (Exception e) {
            System.err.println("Không thể lưu kỷ lục.");
        }
    }

    private void resetGame() {
        score = 0; 
        difficultyLevel = 1; 
        meteorSpawnTimer = 0; 
        walkSoundTimer = 0; 
        // Đặt startDelay = 40 để tránh việc âm thanh mic kích hoạt nhảy ngay lập tức khi vừa vào game
        startDelay = 40; 
        
        platforms.clear(); 
        bees.clear(); 
        fallingObjects.clear(); 
        particles.clear(); 
        
        // Tạo bục xuất phát đầu tiên
        Platform startPlatform = new Platform(0, 480, 400, 250, false, false, false);
        // FIX LỖI: Xóa sạch toàn bộ xu ở bục đầu tiên, tránh việc nhân vật vừa xuất hiện đã nhảy ăn xu
        startPlatform.coins.clear(); 
        platforms.add(startPlatform);

        player = new Player(150, 400); 
        
        for(int i = 0; i < 6; i++) {
            generateNextPlatform();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == State.MENU) {
            if (audioSensor != null && audioSensor.isCalibrated()) {
                if (audioSensor.getCurrentVolume() >= jumpThreshold) {
                    currentState = State.PLAYING;
                    startDelay = 40; 
                    SoundManager.playSound("jump.wav");
                }
            }
            repaint(); 
            return;
        }

        if (currentState == State.GAMEOVER) { 
            updateParticles(); 
            repaint(); 
            return; 
        }

        int speed = 0;
        if (startDelay > 0) startDelay--;

        if (startDelay == 0 && audioSensor != null && audioSensor.isCalibrated()) {
            double vol = audioSensor.getCurrentVolume();
            if (vol >= jumpThreshold) { 
                if (player.isGrounded()) { 
                    SoundManager.playSound("jump.wav");
                    player.jump(vol); 
                }
                speed = 9 + (difficultyLevel / 2);
                score += 2;
            } else if (vol >= walkThreshold) { 
                speed = 5 + (difficultyLevel / 2); 
                score += 1;
                walkSoundTimer++;
                if (walkSoundTimer >= 14 && player.isGrounded()) {
                    SoundManager.playSound("walk.wav");
                    walkSoundTimer = 0;
                }
            } else {
                walkSoundTimer = 14; 
            }
        }

        // CẬP NHẬT ANIMATION LÁ CỜ
        flagAnimTimer++;
        if (flagAnimTimer >= 10) { 
            isFlagA = !isFlagA;
            flagAnimTimer = 0;
        }

        difficultyLevel = (score / 1500) + 1;
        updateMeteorTimer();

        // CẬP NHẬT TRẠNG THÁI CÁC THỰC THỂ
        for (Platform p : platforms) p.update(speed);
        for (Bee b : bees) b.update(speed);
        for (FallingObject fo : fallingObjects) fo.update(speed);
        if (player != null) player.update(platforms);
        
        updateParticles();
        checkCollisions();

        // DỌN DẸP OBJECT KHUẤT MÀN HÌNH
        platforms.removeIf(p -> p.x < -600);
        bees.removeIf(b -> b.x < -200);
        fallingObjects.removeIf(fo -> fo.y > HEIGHT + 100 || fo.x < -200);
        
        // SINH ĐỊA HÌNH MỚI
        if (platforms.size() < 10) generateNextPlatform();
        
        // KIỂM TRA RƠI XUỐNG VỰC
        if (player.getY() > HEIGHT) handleGameOver();
        
        repaint();
    }

    private void handleGameOver() {
        SoundManager.playSound("die.wav");
        int currentFinalScore = score / 10;
        if (currentFinalScore > highScore) {
            highScore = currentFinalScore;
            saveHighScore(); 
        }
        currentState = State.GAMEOVER;
    }

    private void checkCollisions() {
        Rectangle pHit = player.getHitbox();
        
        for (Platform p : platforms) {
            if ((p.getMouseHitbox() != null && pHit.intersects(p.getMouseHitbox())) || 
                (p.getSawHitbox() != null && pHit.intersects(p.getSawHitbox()))) {
                spawnExplosion(player.getX() + 30, player.getY() + 40, Color.RED);
                handleGameOver(); 
                return;
            }
            // Ăn xu
            for (Coin c : p.coins) {
                if (!c.isCollected && pHit.intersects(c.getHitbox())) { 
                    c.isCollected = true; 
                    score += 1000; 
                }
            }
        }
        
        for (Bee b : bees) {
            if (pHit.intersects(b.getHitbox())) { 
                spawnExplosion(player.getX() + 30, player.getY() + 40, Color.YELLOW); 
                handleGameOver(); 
                return; 
            }
        }
        
        for (FallingObject fo : fallingObjects) {
            if (pHit.intersects(fo.getHitbox())) { 
                spawnExplosion(player.getX() + 30, player.getY() + 40, new Color(178, 34, 34)); 
                handleGameOver(); 
                return; 
            }
        }
    }

    private void generateNextPlatform() {
        if (platforms.isEmpty()) return;
        
        Platform last = platforms.get(platforms.size() - 1);
        int gap = 160 + random.nextInt(Math.min(300, 150 + (difficultyLevel * 10)));
        int nextX = last.x + last.width + gap;
        int nextY = Math.max(250, Math.min(520, last.y + (random.nextInt(160) - 80)));
        int nextWidth = Math.max(150, 250 - (difficultyLevel * 5)) + random.nextInt(150);
        
        // KIỂM TRA ĐIỀU KIỆN ĐIỂM > 50 
        int currentDisplayScore = score / 10;
        boolean canSpawnObstaclesAndCoins = currentDisplayScore > 50;

        boolean hasObstacle1 = canSpawnObstaclesAndCoins && random.nextBoolean();
        boolean hasObstacle2 = canSpawnObstaclesAndCoins && random.nextBoolean();
        boolean hasAdvancedObstacle = canSpawnObstaclesAndCoins && (difficultyLevel >= 3);

        Platform newPlatform = new Platform(nextX, nextY, nextWidth, 300, hasObstacle1, hasObstacle2, hasAdvancedObstacle);
        
        // --- XÓA XU NẾU ĐIỂM CHƯA ĐẠT 50 ---
        if (!canSpawnObstaclesAndCoins) {
            newPlatform.coins.clear(); 
        }
        
        platforms.add(newPlatform);
        
        // Sinh ong và thiên thạch nếu qua 50 điểm
        if (canSpawnObstaclesAndCoins) {
            if (gap > 220 && random.nextInt(100) < 60) {
                fallingObjects.add(new FallingObject(last.x + last.width + (gap / 2) - 20, -100, 4 + random.nextInt(3)));
            }
            if (random.nextInt(100) < 35) {
                bees.add(new Bee(nextX + random.nextInt(nextWidth), 100 + random.nextInt(150), 120)); 
            }
        }
    }

    private void updateMeteorTimer() {
        int currentDisplayScore = score / 10;
        if (currentDisplayScore <= 50) return;

        meteorSpawnTimer++;
        int interval = Math.max(35, 110 - (difficultyLevel * 8));
        if (meteorSpawnTimer >= interval) {
            fallingObjects.add(new FallingObject(250 + random.nextInt(650), -50, 4 + random.nextInt(4)));
            meteorSpawnTimer = 0; 
        }
    }

    private void updateParticles() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) { 
            Particle p = it.next(); 
            p.update(); 
            if (p.isDead()) it.remove(); 
        }
    }

    private void spawnExplosion(int x, int y, Color color) {
        for (int i = 0; i < 30; i++) {
            particles.add(new Particle(x, y, color));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // VẼ NỀN VÀ THẾ GIỚI GAME TRONG MỌI TRẠNG THÁI (TRỪ MENU)
        if (currentState != State.MENU) {
            if (bgImage != null) {
                g2d.drawImage(bgImage, 0, 0, WIDTH, HEIGHT, null);
            } else { 
                g2d.setColor(new Color(135, 206, 235)); 
                g2d.fillRect(0, 0, WIDTH, HEIGHT); 
            }

            for (Platform p : platforms) p.draw(g2d); 
            for (Bee b : bees) b.draw(g2d);
            for (FallingObject fo : fallingObjects) fo.draw(g2d);
            
            // VẼ LÁ CỜ KỶ LỤC CŨ (Khi đang chơi)
            if (currentState == State.PLAYING && highScore > 0) {
                int flagX = player.getX() + ((highScore * 10) - score) * 5;
                if (flagX > -100 && flagX < WIDTH + 100) {
                    Image currentFlag = isFlagA ? flagImgA : flagImgB;
                    int flagY = 280; 

                    g2d.setColor(new Color(0, 255, 255, 60));
                    g2d.fillOval(flagX + 5, flagY + 50, 50, 15);

                    if (currentFlag != null) {
                        g2d.drawImage(currentFlag, flagX, flagY, 60, 60, null);
                    } else { 
                        g2d.setColor(Color.CYAN); 
                        g2d.fillRect(flagX, flagY, 10, 60); 
                    }
                    
                    g2d.setColor(Color.BLACK); 
                    g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
                    g2d.drawString("KỶ LỤC CŨ", flagX - 12, flagY - 8); 
                    g2d.setColor(Color.YELLOW); 
                    g2d.drawString("KỶ LỤC CŨ", flagX - 14, flagY - 10);
                }
            }

            if (player != null && currentState != State.GAMEOVER) player.draw(g2d);
            for (Particle p : particles) p.draw(g2d);
        }

        drawUI(g2d);
    }

    private void drawUI(Graphics2D g2d) {
        long time = System.currentTimeMillis();

        if (currentState == State.MENU) {
            g2d.setColor(Color.BLACK); 
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
            
            int floatY = (int)(Math.sin(time / 250.0) * 15); 
            g2d.setFont(new Font("Monospaced", Font.BOLD, 75));
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString("SCREAM RUNNER", WIDTH/2 - 286, HEIGHT/2 - 106 + floatY); 
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawString("SCREAM RUNNER", WIDTH/2 - 290, HEIGHT/2 - 110 + floatY); 
            
            g2d.setFont(new Font("Monospaced", Font.BOLD, 30));
            g2d.setColor(Color.CYAN);
            g2d.drawString("🏆 KỶ LỤC: " + highScore, WIDTH/2 - 130, HEIGHT/2 - 20);

            if (audioSensor != null) {
                int v = (int) audioSensor.getCurrentVolume();
                int barWidth = Math.min(400, (int)(v * 15)); 
                int targetWidth = Math.min(400, (int)(jumpThreshold * 15)); 

                g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
                g2d.setColor(Color.WHITE);
                g2d.drawString("THỬ MIC TRƯỚC KHI VÀO:", WIDTH/2 - 140, HEIGHT/2 + 35);
                
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRect(WIDTH/2 - 200, HEIGHT/2 + 50, 400, 20);
                
                if (v >= jumpThreshold) g2d.setColor(Color.RED);
                else if (v >= walkThreshold) g2d.setColor(Color.ORANGE);
                else g2d.setColor(Color.GREEN);
                g2d.fillRect(WIDTH/2 - 200, HEIGHT/2 + 50, barWidth, 20);
                
                g2d.setColor(Color.YELLOW);
                g2d.fillRect(WIDTH/2 - 200 + targetWidth, HEIGHT/2 + 45, 4, 30);
            }

            g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
            g2d.setColor(Color.WHITE);
            if (time % 1000 < 600) {
                g2d.drawString(">>> HÉT VƯỢT VẠCH VÀNG ĐỂ BẮT ĐẦU <<<", WIDTH/2 - 240, HEIGHT/2 + 120);
            }
            
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("Phím [Lên/Xuống] để chỉnh ngưỡng vạch vàng", WIDTH/2 - 210, HEIGHT - 30);
            return;
        }

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(20, HUD_Y, WIDTH - 40, HUD_HEIGHT, 20, 20); 

        g2d.setFont(new Font("Monospaced", Font.BOLD, 22));

        String sT = "💰 ĐIỂM: " + (score / 10);
        g2d.setColor(Color.BLACK); g2d.drawString(sT, 42, 47);
        g2d.setColor(new Color(255, 215, 0)); g2d.drawString(sT, 40, 45);

        String hSText = "🏆 " + highScore;
        g2d.setColor(Color.BLACK); g2d.drawString(hSText, 222, 47);
        g2d.setColor(Color.CYAN); g2d.drawString(hSText, 220, 45);

        String lT = "🔥 LVL: " + difficultyLevel;
        g2d.setColor(Color.BLACK); g2d.drawString(lT, WIDTH - 148, 47);
        g2d.setColor(new Color(255, 69, 0)); g2d.drawString(lT, WIDTH - 150, 45);

        if (audioSensor != null && currentState == State.PLAYING) {
            g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
            String micText = "🎤 MIC:";
            g2d.setColor(Color.BLACK); g2d.drawString(micText, WIDTH/2 - 168, 47);
            g2d.setColor(Color.WHITE); g2d.drawString(micText, WIDTH/2 - 170, 45);

            int v = (int) audioSensor.getCurrentVolume();
            int aB = Math.min(20, v * 2);
            int barStartX = WIDTH/2 - 70; 

            for (int i = 0; i < 20; i++) {
                int cX = barStartX + (i * 12); 
                if (i < aB) {
                    if (i < 8) g2d.setColor(new Color(50, 205, 50));
                    else if (i < 15) g2d.setColor(new Color(255, 140, 0));
                    else g2d.setColor(new Color(220, 20, 60));
                    g2d.fillRect(cX, 25, 8, 24); 
                } else {
                    g2d.setColor(new Color(255, 255, 255, 50));
                    g2d.fillRect(cX, 31, 8, 14); 
                }
            }
        }
        
        if (currentState == State.GAMEOVER) {
            g2d.setColor(new Color(0, 0, 0, 210)); g2d.fillRect(0, 0, WIDTH, HEIGHT);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 80));
            g2d.setColor(new Color(255, 50, 50)); g2d.drawString("THẤT BẠI!", WIDTH/2 - 220, HEIGHT/2 - 60);
            
            g2d.setFont(new Font("Monospaced", Font.BOLD, 35));
            g2d.setColor(Color.WHITE);
            g2d.drawString("ĐIỂM ĐẠT ĐƯỢC: " + (score/10), WIDTH/2 - 190, HEIGHT/2 + 20);
            
            if ((score/10) >= highScore && highScore > 0) {
                g2d.setColor(Color.YELLOW);
                g2d.drawString("🎉 KỶ LỤC MỚI XÁC LẬP! 🎉", WIDTH/2 - 230, HEIGHT/2 + 70);
            } else {
                g2d.setColor(Color.CYAN);
                g2d.drawString("KỶ LỤC CŨ: " + highScore, WIDTH/2 - 150, HEIGHT/2 + 70);
            }
            
            g2d.setFont(new Font("Monospaced", Font.BOLD, 22)); g2d.setColor(Color.WHITE); 
            if (time % 1000 < 700) g2d.drawString("[ NHẤN SPACE ĐỂ PHỤC THÙ ]", WIDTH/2 - 175, HEIGHT/2 + 130);
        }
    }

    @Override public void keyPressed(KeyEvent e) { 
        if (currentState == State.GAMEOVER && e.getKeyCode() == KeyEvent.VK_SPACE) {
            resetGame(); 
            currentState = State.PLAYING;
            startDelay = 40; // Đặt lại delay ở đây một lần nữa cho chắc chắn
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            walkThreshold = Math.max(0.5, walkThreshold - 0.2);
            jumpThreshold = Math.max(walkThreshold + 1, jumpThreshold - 0.5);
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            walkThreshold += 0.2;
            jumpThreshold += 0.5;
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}