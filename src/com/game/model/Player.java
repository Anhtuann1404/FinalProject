package com.game.model;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.List;

public class Player {
    private int x;
    private double y; 
    
    // KÍCH THƯỚC CHUẨN: 60x75 (Khớp với code cũ của bạn)
    private int width = 70;   
    private int height = 75;  
    
    private double velocityY = 0;   
    private final double GRAVITY = 0.6; 
    private final double JUMP_FORCE = -12.5; 
    
    private boolean isGrounded = false; 
    
    // --- 3 BỨC ẢNH ANIMATION ---
    private Image imgJump;   // character.png
    private Image imgWalkA;  // character_walk_a.png
    private Image imgWalkB;  // character_walk_b.png

    // --- BIẾN ĐIỀU KHIỂN ANIMATION ---
    private int frameCount = 0;      
    private boolean isWalkA = true;  

    public Player(int x, int y) {
        this.x = x;
        this.y = (double)y;
        
        // Tải đúng tên file bạn yêu cầu
        try {
            imgJump = ImageIO.read(new File("character.png"));
            imgWalkA = ImageIO.read(new File("character_walk_a.png"));
            imgWalkB = ImageIO.read(new File("character_walk_b.png"));
        } catch (Exception e) {
            System.out.println("🚨 Lỗi: Hãy kiểm tra file character.png, character_walk_a.png và character_walk_b.png trong thư mục gốc!");
        }
    }

    public void jump(double volume) {
        if (isGrounded) {
            // Logic nhảy có boost theo âm lượng cực hay của bạn
            double boost = Math.min(10.0, volume / 6.0);
            velocityY = JUMP_FORCE - boost; 
            isGrounded = false;
            y -= 5; // Nhấc nhẹ để tránh dính vào bục
        }
    }

    public void update(List<Platform> platforms) {
        // Áp dụng trọng lực
        velocityY += GRAVITY;
        y += velocityY;

        isGrounded = false; 
        for (Platform p : platforms) {
            // Xử lý va chạm dựa trên code cũ của bạn (chính xác và mượt)
            if (velocityY >= 0 && x + width - 15 > p.x && x + 15 < p.x + p.width &&
                y + height >= p.y && y + height <= p.y + velocityY + 10) {
                
                y = p.y - height;
                velocityY = 0;
                isGrounded = true;
                break;
            }
        }

        // --- LOGIC HOẠT ẢNH ĐI BỘ (Chỉ chạy khi đang ở trên đất) ---
        if (isGrounded) {
            frameCount++;
            if (frameCount >= 8) { // Cứ 8 khung hình đổi chân 1 lần
                isWalkA = !isWalkA; 
                frameCount = 0;     
            }
        }
    }

    public void draw(Graphics g) {
        Image currentImg = null;

        // CHỌN ẢNH DỰA TRÊN TRẠNG THÁI
        if (!isGrounded) {
            currentImg = imgJump; // Đang bay dùng ảnh character.png
        } else {
            // Đứng trên đất thì đổi chân A-B
            currentImg = isWalkA ? imgWalkA : imgWalkB;
        }

        // Vẽ ảnh lên màn hình
        if (currentImg != null) {
            g.drawImage(currentImg, x, (int)y, width, height, null);
        } else {
            // Dự phòng nếu mất ảnh
            g.setColor(java.awt.Color.BLUE);
            g.fillRect(x, (int)y, width, height);
        }
    }

    // --- CÁC HÀM BỔ SUNG ĐỂ TƯƠNG THÍCH VỚI GAMEPANEL MỚI ---

    public boolean isGrounded() {
        return isGrounded;
    }

    public int getY() {
        return (int)y;
    }

    public int getX() {
        return x;
    }

    // Hộp va chạm để tính đụng bẫy, ong, thiên thạch
    public Rectangle getHitbox() {
        // Thu nhỏ hitbox một tí để người chơi đỡ bị "chết oan"
        return new Rectangle(x + 10, (int)y + 5, width - 20, height - 10);
    }
}