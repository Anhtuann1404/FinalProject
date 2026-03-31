package com.game.controller;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    // Lưu sẵn các đoạn âm thanh vào bộ nhớ RAM
    private static Map<String, Clip> soundMap = new HashMap<>();

    // Nạp sẵn tất cả âm thanh khi bắt đầu game
    public static void loadAllSounds() {
        loadSound("walk.wav");
        loadSound("jump.wav");
        loadSound("die.wav");
    }

    private static void loadSound(String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) return;
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            soundMap.put(fileName, clip);
        } catch (Exception e) {
            System.err.println("Lỗi nạp file: " + fileName);
        }
    }

    public static void playSound(String fileName) {
        Clip clip = soundMap.get(fileName);
        if (clip != null) {
            clip.setFramePosition(0); // Quay lại đầu file ngay lập tức
            clip.start();
        }
    }
}