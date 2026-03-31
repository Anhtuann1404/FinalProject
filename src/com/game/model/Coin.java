package com.game.model;

import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

public class Coin {
    public int x, y;
    public final int width = 30;
    public final int height = 30;
    private static Image imgA, imgB;
    private int frameCount = 0;
    private boolean isFrameA = true;
    public boolean isCollected = false;

    public Coin(int x, int y) {
        this.x = x;
        this.y = y;
        try {
            if (imgA == null) imgA = ImageIO.read(new File("coin_a.png"));
            if (imgB == null) imgB = ImageIO.read(new File("coin_b.png"));
        } catch (Exception e) {}
    }

    public void update(int scrollSpeed, int platformY) {
        this.x -= scrollSpeed;
        // Giữ đồng xu luôn nổi phía trên bục một chút
        this.y = platformY - height - 15;

        // Hiệu ứng xoay xoay
        frameCount++;
        if (frameCount >= 12) {
            isFrameA = !isFrameA;
            frameCount = 0;
        }
    }

    public void draw(Graphics g) {
        if (isCollected) return;
        Image current = (isFrameA && imgA != null) ? imgA : imgB;
        if (current != null) {
            g.drawImage(current, x, y, width, height, null);
        } else {
            g.setColor(Color.YELLOW);
            g.fillOval(x, y, width, height);
        }
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, width, height);
    }
}