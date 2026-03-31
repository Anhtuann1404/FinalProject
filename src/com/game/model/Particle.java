package com.game.model;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Particle {
    public double x, y;
    private double vx, vy; // Vận tốc ngang và dọc
    private int size;
    private Color color;
    
    private float alpha = 1.0f; // Độ trong suốt (1.0f = rõ nét, 0.0f = trong suốt)
    private float fadeSpeed;   // Tốc độ mờ dần
    private boolean dead = false;

    // Trọng lực tác động lên hạt (làm nó rơi xuống)
    private final double GRAVITY = 0.2;

    public Particle(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;

        // 1. Tạo kích thước ngẫu nhiên (từ 3 đến 7 pixel)
        this.size = 3 + (int)(Math.random() * 5);

        // 2. Tạo vận tốc và hướng ngẫu nhiên (nổ tung ra các phía)
        // Math.random() * 2 * PI tạo ra góc ngẫu nhiên 360 độ
        double angle = Math.random() * Math.PI * 2;
        // Vận tốc ngẫu nhiên (từ 2 đến 6)
        double speed = 2 + Math.random() * 4;
        
        // Chuyển đổi từ hệ cực (angle, speed) sang hệ trục (vx, vy)
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;

        // 3. Tốc độ mờ dần ngẫu nhiên (để các hạt biến mất không cùng lúc)
        this.fadeSpeed = 0.01f + (float)(Math.random() * 0.03f);
    }

    public void update() {
        if (dead) return;

        // Cập nhật vị trí dựa trên vận tốc
        this.x += vx;
        this.y += vy;

        // Tác động của trọng lực làm tăng vận tốc rơi
        this.vy += GRAVITY;

        // Giảm độ trong suốt (mờ dần)
        this.alpha -= fadeSpeed;

        // Nếu mờ hẳn thì đánh dấu là "đã chết"
        if (this.alpha <= 0) {
            this.alpha = 0;
            this.dead = true;
        }
    }

    public void draw(Graphics2D g2d) {
        if (dead) return;

        // THỦ THUẬT QUAN TRỌNG: Thiết lập độ trong suốt trước khi vẽ
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        g2d.setColor(color);
        g2d.fillOval((int)x, (int)y, size, size); // Vẽ hạt hình tròn

        // Reset lại độ trong suốt về mặc định để không ảnh hưởng các đối tượng khác
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public boolean isDead() {
        return dead;
    }
}