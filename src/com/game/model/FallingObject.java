package com.game.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import javax.imageio.ImageIO;

public class FallingObject {
    public int x, y;
    public int width = 50; 
    public int height = 50;
    
    private Image imgA, imgB; 
    private int fallSpeed; 
    private int animationFrame = 0; 

    public FallingObject(int x, int y, int fallSpeed) {
        this.x = x;
        this.y = y;
        this.fallSpeed = fallSpeed;
        
        try {
            imgA = ImageIO.read(new File("meteor_a.png"));
            imgB = ImageIO.read(new File("meteor_b.png"));
        } catch (Exception e) {}
    }

    public void update(int scrollSpeed) {
        this.x -= scrollSpeed; // Trôi ngang theo màn hình
        this.y += fallSpeed;   // Rơi dọc xuống
        animationFrame++;      // Chạy bộ đếm ảnh
    }

    public void draw(Graphics g) {
        // Đổi ảnh liên tục mỗi 5 frame để tạo hiệu ứng cháy/xoay
        Image currentImg = ((animationFrame / 5) % 2 == 0) ? imgA : imgB;

        if (currentImg != null) {
            g.drawImage(currentImg, x, y, width, height, null);
        } else {
            // Chế độ dự phòng nếu thiếu ảnh
            g.setColor(Color.ORANGE);
            g.fillPolygon(new int[]{x + 5, x + width - 5, x + width / 2}, new int[]{y + 20, y + 20, y - 15}, 3);
            g.setColor(((animationFrame / 5) % 2 == 0) ? new Color(178, 34, 34) : Color.RED); 
            g.fillOval(x, y + 10, width, height - 10);
        }
    }

    public Rectangle getHitbox() {
        return new Rectangle(x + 5, y + 15, width - 10, height - 20);
    }
}