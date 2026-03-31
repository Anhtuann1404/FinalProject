package GameCK;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import javax.imageio.ImageIO;

public class Platform {
    public int x, y, width, height;
    private static Image imgL, imgC, imgR;

    // --- VẬT CẢN TRÊN BỤC ---
    public Mouse mouse; 
    public Saw saw;

    public int obstacleWidth = 45; 
    public int obstacleHeight = 45;

    public Platform(int x, int y, int width, int height, boolean hasMouse, boolean hasSaw) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // Tải ảnh gạch
        try {
            if (imgL == null) imgL = ImageIO.read(new File("brick_left.png"));
            if (imgC == null) imgC = ImageIO.read(new File("brick_center.png"));
            if (imgR == null) imgR = ImageIO.read(new File("brick_right.png"));
        } catch (Exception e) {}

        // Tính toán vị trí chính giữa bục cho vật cản
        int obsX = x + (width / 2) - (obstacleWidth / 2);
        
        // Khởi tạo vật cản tương ứng (Chỉ 1 trong 2)
        if (hasMouse) {
            // Kích thước chuột 40x30, đặt trên mặt đất
            this.mouse = new Mouse(obsX, y - 30 + 5, 40, 30); 
        } else if (hasSaw) {
            // Kích thước cưa 45x45, hơi cắm xuống đất
            this.saw = new Saw(obsX, y - 45 + 10, 45, 45);
        }
    }

    public void update(int scrollSpeed) {
        // Bục đất trôi đi
        this.x -= scrollSpeed; 
        
        // Cập nhật vị trí vật cản trôi theo bục
        if (mouse != null) {
            mouse.update(scrollSpeed, this.x, this.width); 
        }
        if (saw != null) {
            saw.update(scrollSpeed); 
        }
    }

    public void draw(Graphics g) {
        // 1. Vẽ bục đất (3 phần: Trái, Giữa, Phải)
        if (imgL != null && imgC != null && imgR != null) {
            int side = 40;
            g.drawImage(imgL, x, y, side, height, null);
            g.drawImage(imgC, x + side, y, width - (side * 2), height, null);
            g.drawImage(imgR, x + width - side, y, side, height, null);
        } else {
            g.setColor(new java.awt.Color(139, 69, 19));
            g.fillRect(x, y, width, height);
        }

        // 2. Vẽ vật cản lên trên bục
        if (mouse != null) {
            mouse.draw(g); 
        }
        if (saw != null) {
            saw.draw(g); 
        }
    }

    // --- HÀM LẤY HỘP VA CHẠM CHO GAMEPANEL ---
    public Rectangle getMouseHitbox() { 
        if (mouse == null) return null;
        return mouse.getMouseHitbox(); 
    }
    
    public Rectangle getSawHitbox() { 
        if (saw == null) return null;
        return saw.getHitbox(); 
    }
}