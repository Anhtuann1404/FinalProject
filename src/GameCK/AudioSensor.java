package GameCK;
import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioSensor implements Runnable {
    private TargetDataLine line;
    private double currentVolume = 0;
    private boolean isCalibrated = false;
    private double backgroundNoiseLevel = 0;
    private final int CALIBRATION_FRAMES = 100; // Đo 100 khung hình đầu tiên để lấy mẫu tiếng ồn

    public AudioSensor() {
        setupAudio();
    }

    private void setupAudio() {
        try {
            // Thiết lập định dạng âm thanh: 44.1kHz, 16-bit, Mono
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("🚨 Microphone không hỗ trợ định dạng này!");
                return;
            }

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        } catch (LineUnavailableException e) {
            System.err.println("🚨 Không thể truy cập Microphone: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[2048];
        int framesCount = 0;
        double noiseSum = 0;

        while (true) {
            int bytesRead = line.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                // Chuyển đổi Byte thành Short (16-bit PCM)
                short[] samples = new short[bytesRead / 2];
                ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);

                // Tính toán giá trị RMS (Root Mean Square) - Đây là công thức tính biên độ âm thanh
                double sum = 0;
                for (short s : samples) {
                    sum += s * s;
                }
                double rms = Math.sqrt(sum / samples.length);

                // Chuyển đổi sang thang đo đơn giản hơn (0 - 100)
                double volume = Math.min(100, rms / 500);

                // Giai đoạn Calibration: Tính toán tiếng ồn nền
                if (!isCalibrated) {
                    noiseSum += volume;
                    framesCount++;
                    if (framesCount >= CALIBRATION_FRAMES) {
                        backgroundNoiseLevel = noiseSum / CALIBRATION_FRAMES;
                        isCalibrated = true;
                        System.out.println("✅ Đã đo xong tiếng ồn nền: " + backgroundNoiseLevel);
                    }
                } else {
                    // Trừ đi tiếng ồn nền để game chính xác hơn
                    currentVolume = Math.max(0, volume - backgroundNoiseLevel);
                }
            }

            try {
                Thread.sleep(10); // Nghỉ một chút để giảm tải CPU
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public double getCurrentVolume() {
        return currentVolume;
    }

    public boolean isCalibrated() {
        return isCalibrated;
    }
}