package com.game.model;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import javax.imageio.ImageIO;

public class Saw {
    public int x, y, width, height;
    
    // --- 2 BỨC ẢNH CƯA XOAY ---
    private static Image imgSawA; 
    private static Image imgSawB; 

    private int frameCount = 0;      
    private boolean isSawA = true;  

    public Saw(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        try {
            if (imgSawA == null) imgSawA = ImageIO.read(new File("saw_a.png"));
            if (imgSawB == null) imgSawB = ImageIO.read(new File("saw_b.png"));
        } catch (Exception e) {}
    }

    public void update(int scrollSpeed) {
        // Cưa đứng yên trên bục, chỉ trôi theo màn hình
        this.x -= scrollSpeed; 

        // --- HOẠT ẢNH CƯA XOAY NHANH ---
        frameCount++;
        if (frameCount >= 4) { // 4 frame đổi ảnh 1 lần -> Cưa xoay tít
            isSawA = !isSawA; 
            frameCount = 0;     
        }
    }

    public void draw(Graphics g) {
        Image currentImg = isSawA ? imgSawA : imgSawB;

        if (currentImg != null) {
            g.drawImage(currentImg, x, y, width, height, null);
        } else {
            g.setColor(java.awt.Color.GRAY);
            g.fillOval(x, y, width, height); // Vẽ hình tròn xám nếu thiếu ảnh
        }
    }

    // HITBOX CỦA CƯA (Bóp vào 10px để người chơi sượt qua không chết oan)
    public Rectangle getHitbox() {
        return new Rectangle(x + 10, y + 10, width - 20, height - 20);
    }
}