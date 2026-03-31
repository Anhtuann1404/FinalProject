package com.game.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import javax.imageio.ImageIO;

public class Bee {
    public int x, y;
    public final int width = 45;
    public final int height = 40;
    
    // 2 Biến ảnh để tạo hiệu ứng đập cánh
    private Image imgWingUp, imgWingDown; 
    private boolean movingLeft = true;
    
    private int startX; 
    private int patrolRange; 
    private int moveSpeed = 3;
    private int animationFrame = 0; // Bộ đếm nhịp đập cánh

    public Bee(int x, int y, int patrolRange) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.patrolRange = patrolRange;
        
        // Tải 2 file ảnh: 1 cái cánh nâng lên, 1 cái cánh hạ xuống
        try {
            imgWingUp = ImageIO.read(new File("bee_a.png"));
            imgWingDown = ImageIO.read(new File("bee_b.png"));
        } catch (Exception e) {}
    }

    public void update(int scrollSpeed) {
        // Trôi theo màn hình
        this.startX -= scrollSpeed;
        this.x -= scrollSpeed;

        // Logic bay qua bay lại
        if (movingLeft) {
            this.x -= moveSpeed;
            if (this.x < startX - patrolRange) {
                movingLeft = false; // Chạm biên trái -> Quay đầu sang phải
            }
        } else {
            this.x += moveSpeed;
            if (this.x > startX + patrolRange) {
                movingLeft = true; // Chạm biên phải -> Quay đầu sang trái
            }
        }
        
        // Tăng bộ đếm frame liên tục để làm nhịp thời gian
        animationFrame++; 
    }

    public void draw(Graphics g) {
        // Hiệu ứng nhấp nhô lên xuống (vẫn giữ nguyên cho sinh động)
        int hoverOffset = (int)(Math.sin(animationFrame * 0.2) * 5); 
        int drawY = y + hoverOffset;

        if (imgWingUp != null && imgWingDown != null) {
            
            // --- LOGIC ĐẬP CÁNH ---
            // Cứ 8 frame (khoảng nửa giây) thì đổi ảnh 1 lần
            // Phép chia lấy dư % 2 sẽ giúp nó luân phiên ra kết quả 0 -> 1 -> 0 -> 1...
            Image currentImg = ((animationFrame / 8) % 2 == 0) ? imgWingUp : imgWingDown;

            // --- LOGIC QUAY ĐẦU (Kết hợp đập cánh) ---
            // Giả sử ảnh gốc (bee_1 và bee_2) của bạn là ong đang HƯỚNG SANG TRÁI
            if (movingLeft) {
                // Bay sang trái -> Vẽ bình thường
                g.drawImage(currentImg, x, drawY, width, height, null);
            } else {
                // Bay sang phải -> Lật ngược ảnh
                g.drawImage(currentImg, x + width, drawY, -width, height, null);
            }
            
        } else {
            // Chế độ dự phòng nếu chưa load được ảnh
            g.setColor(Color.YELLOW);
            g.fillOval(x, drawY, width, height);
            
            // Cánh đập chay (bằng khối màu)
            g.setColor(Color.WHITE);
            if ((animationFrame / 8) % 2 == 0) {
                g.fillOval(x + 15, drawY - 10, 15, 15); // Cánh dơ lên
            } else {
                g.fillOval(x + 15, drawY + 5, 15, 5);   // Cánh hạ xuống
            }

            g.setColor(Color.BLACK);
            g.fillRect(x + 10, drawY, 5, height);
            g.fillRect(x + 25, drawY, 5, height);
        }
    }

    public Rectangle getHitbox() {
        // Thu nhỏ hitbox một chút để nhân vật dễ né hơn
        return new Rectangle(x + 5, y + 5, width - 10, height - 10);
    }
}