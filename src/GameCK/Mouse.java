package GameCK;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import javax.imageio.ImageIO;

public class Mouse {
    public int x, y, width, height;
    
    private static Image imgWalkA; 
    private static Image imgWalkB; 

    private int frameCount = 0;      
    private boolean isWalkA = true;  

    // --- BIẾN AI DI CHUYỂN ---
    private int speed = 3;       // Tốc độ chạy của chuột
    private int direction = -1;  // -1: Chạy sang trái (tiến về phía người chơi), 1: Chạy sang phải

    public Mouse(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        try {
            if (imgWalkA == null) imgWalkA = ImageIO.read(new File("mouse_walk_a.png"));
            if (imgWalkB == null) imgWalkB = ImageIO.read(new File("mouse_walk_b.png"));
        } catch (Exception e) {}
    }

    // Nâng cấp: Nhận thêm tọa độ của Bục đất để biết điểm dừng
    public void update(int scrollSpeed, int platX, int platWidth) {
        // 1. Trôi theo màn hình
        this.x -= scrollSpeed; 

        // 2. Tự di chuyển độc lập
        this.x += (speed * direction);

        // 3. Logic đi tuần (Chạm mép bục thì quay đầu)
        int leftEdge = platX;
        int rightEdge = platX + platWidth - this.width;

        if (this.x <= leftEdge) {
            this.x = leftEdge;
            direction = 1; // Đụng mép trái -> Quay sang phải
        } else if (this.x >= rightEdge) {
            this.x = rightEdge;
            direction = -1; // Đụng mép phải -> Quay sang trái
        }

        // 4. Logic Hoạt ảnh
        frameCount++;
        if (frameCount >= 8) { 
            isWalkA = !isWalkA; 
            frameCount = 0;     
        }
    }

    public void draw(Graphics g) {
        Image currentImg = isWalkA ? imgWalkA : imgWalkB;

        if (currentImg != null) {
            // LOGIC LẬT ẢNH THÔNG MINH
            if (direction == -1) {
                // Nhìn sang trái (vẽ bình thường)
                g.drawImage(currentImg, x, y, width, height, null);
            } else {
                // Nhìn sang phải (Lật ngược ảnh theo trục X)
                g.drawImage(currentImg, x + width, y, -width, height, null);
            }
        } else {
            g.setColor(java.awt.Color.BLACK);
            g.fillRect(x, y, width, height);
        }
    }

    public Rectangle getMouseHitbox() {
        return new Rectangle(x + 5, y + 5, width - 10, height - 10);
    }
}