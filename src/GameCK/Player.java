package GameCK;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.List;

public class Player {
    private int x;
    private double y; 
    
    // KÍCH THƯỚC CHUẨN: 60x75
    private int width = 60;   
    private int height = 75;  
    
    private double velocityY = 0;   
    private final double GRAVITY = 0.6; 
    private final double JUMP_FORCE = -12.5; 
    
    private boolean isGrounded = false; 
    
    // --- KHAI BÁO 3 BỨC ẢNH ANIMATION ---
    private Image imgJump;   // Ảnh lúc nhảy/bay
    private Image imgWalkA;  // Ảnh bước chân A
    private Image imgWalkB;  // Ảnh bước chân B

    // --- BIẾN ĐIỀU KHIỂN ANIMATION ---
    private int frameCount = 0;      
    private boolean isWalkA = true;  

    public Player(int x, int y) {
        this.x = x;
        this.y = (double)y;
        
        // Tải đủ 3 ảnh vào game
        try {
            imgJump = ImageIO.read(new File("character.png"));
            imgWalkA = ImageIO.read(new File("character_walk_a.png"));
            imgWalkB = ImageIO.read(new File("character_walk_b.png"));
        } catch (Exception e) {
            System.out.println("🚨 Lỗi: Thiếu 1 trong 3 file ảnh nhân vật!");
        }
    }

    public void jump(double volume) {
        if (isGrounded) {
            double boost = Math.min(10.0, volume / 6.0);
            velocityY = JUMP_FORCE - boost; 
            isGrounded = false;
            y -= 10; // Nhấc lên để thoát bục an toàn
        }
    }

    public void update(List<Platform> platforms) {
        // Áp dụng trọng lực
        velocityY += GRAVITY;
        y += velocityY;

        isGrounded = false; 
        for (Platform p : platforms) {
            // Xử lý va chạm
            if (velocityY >= 0 && x + width - 15 > p.x && x + 15 < p.x + p.width &&
                y + height >= p.y && y + height <= p.y + velocityY + 10) {
                
                y = p.y - height;
                velocityY = 0;
                isGrounded = true;
                break;
            }
        }

        // --- LOGIC HOẠT ẢNH ĐI BỘ ---
        if (isGrounded) {
            frameCount++;
            // Cứ 8 khung hình thì đổi chân 1 lần (để bước đi mượt mà)
            if (frameCount >= 8) { 
                isWalkA = !isWalkA; 
                frameCount = 0;     
            }
        }
    }

    public void draw(Graphics g) {
        Image currentImg = null;

        // CHỌN ẢNH ĐỂ VẼ DỰA TRÊN TRẠNG THÁI
        if (!isGrounded) {
            currentImg = imgJump; // Đang lơ lửng -> Dáng nhảy
        } else {
            // Đứng trên đất -> Đổi qua lại giữa A và B
            if (isWalkA) {
                currentImg = imgWalkA;
            } else {
                currentImg = imgWalkB;
            }
        }

        // Vẽ ảnh lên màn hình
        if (currentImg != null) {
            g.drawImage(currentImg, x, (int)y, width, height, null);
        } else {
            g.setColor(java.awt.Color.BLUE);
            g.fillRect(x, (int)y, width, height);
        }
    }

    public int getY() { return (int)y; }
}