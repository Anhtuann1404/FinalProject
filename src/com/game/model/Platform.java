package com.game.model;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import javax.imageio.ImageIO;

public class Platform {
    public int x, y, width, height;
    private static Image imgL, imgC, imgR;

    // --- LOGIC DI CHUYỂN ---
    public boolean isMoving;
    private int startY;
    private int moveRange = 100; // Khoảng cách di chuyển lên xuống
    private int moveDir = 1;    // 1: Xuống, -1: Lên
    private int moveSpeed = 2;

    public Mouse mouse; 
    public Saw saw;
    public int obstacleWidth = 45; 
    public int obstacleHeight = 45;

    public Platform(int x, int y, int width, int height, boolean hasMouse, boolean hasSaw, boolean isMoving) {
        this.x = x;
        this.y = y;
        this.startY = y; // Lưu vị trí gốc để giới hạn tầm di chuyển
        this.width = width;
        this.height = height;
        this.isMoving = isMoving;

        try {
            if (imgL == null) imgL = ImageIO.read(new File("brick_left.png"));
            if (imgC == null) imgC = ImageIO.read(new File("brick_center.png"));
            if (imgR == null) imgR = ImageIO.read(new File("brick_right.png"));
        } catch (Exception e) {}

        int obsX = x + (width / 2) - (obstacleWidth / 2);
        if (hasMouse) {
            this.mouse = new Mouse(obsX, y - 30 + 5, 40, 30); 
        } else if (hasSaw) {
            this.saw = new Saw(obsX, y - 45 + 10, 45, 45);
        }
    }

    public void update(int scrollSpeed) {
        this.x -= scrollSpeed; 

        // Nếu là bục di động, cập nhật tọa độ Y
        if (isMoving) {
            this.y += (moveDir * moveSpeed);
            // Nếu vượt quá phạm vi thì đổi chiều
            if (Math.abs(this.y - startY) > moveRange) {
                moveDir *= -1;
            }
        }
        
        // Cập nhật vị trí vật cản đi theo bục (cả X và Y)
        if (mouse != null) {
            mouse.y = this.y - mouse.height + 5; // Cập nhật Y cho chuột
            mouse.update(scrollSpeed, this.x, this.width); 
        }
        if (saw != null) {
            saw.y = this.y - saw.height + 10; // Cập nhật Y cho cưa
            saw.update(scrollSpeed); 
        }
    }

    public void draw(Graphics g) {
        if (imgL != null && imgC != null && imgR != null) {
            int side = 40;
            g.drawImage(imgL, x, y, side, height, null);
            g.drawImage(imgC, x + side, y, width - (side * 2), height, null);
            g.drawImage(imgR, x + width - side, y, side, height, null);
        } else {
            g.setColor(new java.awt.Color(139, 69, 19));
            g.fillRect(x, y, width, height);
        }

        if (mouse != null) mouse.draw(g); 
        if (saw != null) saw.draw(g); 
    }

    public Rectangle getMouseHitbox() { 
        if (mouse == null) return null;
        return mouse.getMouseHitbox(); 
    }
    
    public Rectangle getSawHitbox() { 
        if (saw == null) return null;
        return saw.getHitbox(); 
    }
}