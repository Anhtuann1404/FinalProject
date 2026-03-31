package com.game.model;

import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Platform {
    public int x, y, width, height;
    private static Image imgL, imgC, imgR;
    public boolean isMoving;
    private int startY, moveDir = 1, moveSpeed = 2;

    public Mouse mouse; 
    public Saw saw;
    public List<Coin> coins = new ArrayList<>();

    public Platform(int x, int y, int width, int height, boolean hasMouse, boolean hasSaw, boolean isMoving) {
        this.x = x;
        this.y = y;
        this.startY = y;
        this.width = width;
        this.height = height;
        this.isMoving = isMoving;

        try {
            if (imgL == null) imgL = ImageIO.read(new File("brick_left.png"));
            if (imgC == null) imgC = ImageIO.read(new File("brick_center.png"));
            if (imgR == null) imgR = ImageIO.read(new File("brick_right.png"));
        } catch (Exception e) {}

        // Đặt vật cản ở giữa bục
        int obsX = x + (width / 2) - 22;
        if (hasMouse) this.mouse = new Mouse(obsX, y - 25, 40, 30);
        else if (hasSaw) this.saw = new Saw(obsX, y - 35, 45, 45);

        // LOGIC SINH VÀNG: Chỉ sinh vàng nếu bục đủ rộng và KHÔNG có vật cản
        Random rand = new Random();
        if (!hasMouse && !hasSaw && rand.nextInt(100) < 50) { 
            int numCoins = 1 + rand.nextInt(3); 
            for (int i = 0; i < numCoins; i++) coins.add(new Coin(x + 50 + (i * 40), y - 40));
        }
        
    }

    public void update(int scrollSpeed) {
        this.x -= scrollSpeed;
        if (isMoving) {
            this.y += (moveDir * moveSpeed);
            if (Math.abs(this.y - startY) > 80) moveDir *= -1;
        }
        
        if (mouse != null) {
            mouse.y = this.y - mouse.height + 5;
            mouse.update(scrollSpeed, this.x, this.width);
        }
        if (saw != null) {
            saw.y = this.y - saw.height + 10;
            saw.update(scrollSpeed);
        }
        for (Coin c : coins) c.update(scrollSpeed, this.y);
    }

    public void draw(Graphics g) {
        if (imgL != null) {
            int side = 40;
            g.drawImage(imgL, x, y, side, height, null);
            g.drawImage(imgC, x + side, y, width - (side * 2), height, null);
            g.drawImage(imgR, x + width - side, y, side, height, null);
        } else {
            g.setColor(new Color(139, 69, 19));
            g.fillRect(x, y, width, height);
        }
        if (mouse != null) mouse.draw(g);
        if (saw != null) saw.draw(g);
        for (Coin c : coins) c.draw(g);
    }

    public Rectangle getMouseHitbox() { return mouse != null ? mouse.getMouseHitbox() : null; }
    public Rectangle getSawHitbox() { return saw != null ? saw.getHitbox() : null; }
}