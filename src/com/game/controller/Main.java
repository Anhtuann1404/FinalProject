package com.game.controller;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.game.view.GamePanel;
import com.game.view.MenuPanel;

public class Main extends JFrame {
    private MenuPanel menuPanel;
    private GamePanel gamePanel;

    public Main() {
        // 1. Thiết lập tiêu đề và các thuộc tính cơ bản cho cửa sổ
        setTitle("Đồ Án: Audio Jump Game - Scream Warrior");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false); // Khóa kích thước để ảnh không bị vỡ khi kéo giãn

        // 2. Khởi tạo Menu chính
        showMenu();

        // 3. Hiển thị cửa sổ
        pack(); // Tự động đóng gói kích thước dựa trên Panel bên trong (950x600)
        setLocationRelativeTo(null); // Đưa cửa sổ ra chính giữa màn hình máy tính
        setVisible(true);
    }

    // Hàm hiển thị màn hình Menu
    public void showMenu() {
        if (gamePanel != null) {
            remove(gamePanel);
        }
        menuPanel = new MenuPanel(this);
        add(menuPanel);
        revalidate();
        repaint();
    }

    // Hàm bắt đầu vào trò chơi (Được gọi khi nhấn nút PLAY trong MenuPanel)
    public void startGame() {
        // Xóa MenuPanel cũ đi
        remove(menuPanel);
        
        // Khởi tạo GamePanel mới
        gamePanel = new GamePanel();
        add(gamePanel);
        
        // Làm tươi lại giao diện
        revalidate();
        repaint();
        
        // QUAN TRỌNG: Yêu cầu bàn phím tập trung vào GamePanel để nhấn phím Space chơi lại được
        gamePanel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        // Chạy ứng dụng trên luồng sự kiện của Swing để đảm bảo an toàn giao diện
        SwingUtilities.invokeLater(() -> {
            new Main();
        });
    }
}