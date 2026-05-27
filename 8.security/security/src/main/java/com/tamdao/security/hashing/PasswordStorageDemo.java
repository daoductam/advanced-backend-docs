package com.tamdao.security.hashing;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PasswordStorageDemo implements CommandLineRunner {

    // ANSI Escape codes for beautiful console formatting
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n" + BOLD + PURPLE + "==========================================================================" + RESET);
        System.out.println(BOLD + CYAN + "     DEMO CASE STUDY 1: LƯU TRỮ MẬT KHẨU AN TOÀN (PASSWORD STORAGE)" + RESET);
        System.out.println(BOLD + PURPLE + "==========================================================================" + RESET);

        String samplePassword = "SuperSecurePassword123!";

        // 1. DEMO: KỸ THUẬT MUỐI DỮ LIỆU (SALT)
        demoSaltEffect(samplePassword);

        // 2. DEMO: ĐÁNH GIÁ HIỆU NĂNG & ĐỘ AN TOÀN (BENCHMARK)
        demoBenchmark(samplePassword);

        // 3. DEMO: XÁC THỰC MẬT KHẨU (VERIFICATION)
        demoVerification(samplePassword);

        System.out.println(BOLD + PURPLE + "==========================================================================" + RESET + "\n");
    }

    private void demoSaltEffect(String password) {
        System.out.println("\n" + BOLD + YELLOW + "1. Khác biệt giữa Hashing không có Salt (MD5) và Hashing có Salt tự động (BCrypt/Argon2)" + RESET);
        System.out.println("Mật khẩu thử nghiệm: " + BOLD + CYAN + password + RESET);

        PasswordHasher md5 = new MD5Hasher();
        PasswordHasher bcrypt = new BCryptHasher(10);
        PasswordHasher argon2 = new Argon2Hasher();

        System.out.println("\n--- Băm bằng MD5 (Không dùng Salt) ---");
        String md5Hash1 = md5.hash(password);
        String md5Hash2 = md5.hash(password);
        System.out.println("Lần 1: " + RED + md5Hash1 + RESET);
        System.out.println("Lần 2: " + RED + md5Hash2 + RESET);
        System.out.println(md5Hash1.equals(md5Hash2) 
            ? RED + "-> KẾT QUẢ GIỐNG HỆT NHAU! Dễ bị tấn công Rainbow Table." + RESET 
            : GREEN + "-> Khác nhau." + RESET);

        System.out.println("\n--- Băm bằng BCrypt (Tự động sinh Salt) ---");
        String bcryptHash1 = bcrypt.hash(password);
        String bcryptHash2 = bcrypt.hash(password);
        System.out.println("Lần 1: " + GREEN + bcryptHash1 + RESET);
        System.out.println("Lần 2: " + GREEN + bcryptHash2 + RESET);
        System.out.println(!bcryptHash1.equals(bcryptHash2) 
            ? GREEN + "-> KẾT QUẢ KHÁC NHAU HOÀN TOÀN! Rainbow Table hoàn toàn vô dụng." + RESET 
            : RED + "-> Giống nhau." + RESET);

        System.out.println("\n--- Băm bằng Argon2 (Tự động sinh Salt) ---");
        String argon2Hash1 = argon2.hash(password);
        String argon2Hash2 = argon2.hash(password);
        System.out.println("Lần 1: " + GREEN + argon2Hash1 + RESET);
        System.out.println("Lần 2: " + GREEN + argon2Hash2 + RESET);
        System.out.println(!argon2Hash1.equals(argon2Hash2) 
            ? GREEN + "-> KẾT QUẢ KHÁC NHAU HOÀN TOÀN! Đánh bại Rainbow Table." + RESET 
            : RED + "-> Giống nhau." + RESET);
    }

    private void demoBenchmark(String password) {
        System.out.println("\n" + BOLD + YELLOW + "2. Đánh giá tốc độ băm mật khẩu (Mục tiêu: Làm chậm kẻ tấn công Brute-force)" + RESET);
        System.out.println("Băm thử nghiệm mật khẩu: \"" + password + "\"");

        List<PasswordHasher> hashers = new ArrayList<>();
        hashers.add(new MD5Hasher());
        hashers.add(new BCryptHasher(6));   // BCrypt rất nhanh với strength nhỏ
        hashers.add(new BCryptHasher(10));  // Standard default
        hashers.add(new BCryptHasher(13));  // Khá chậm và an toàn hơn
        hashers.add(new Argon2Hasher());    // Argon2id Default (16MB memory, 2 iterations)

        System.out.println(String.format("\n%-40s | %-18s | %-12s", "Thuật toán / Cấu hình", "Thời gian băm (ms)", "Mức độ an toàn"));
        System.out.println("-----------------------------------------+--------------------+-------------");

        for (PasswordHasher hasher : hashers) {
            // Warm up
            hasher.hash(password);

            // Đo thời gian
            long start = System.nanoTime();
            hasher.hash(password);
            long end = System.nanoTime();
            double ms = (end - start) / 1_000_000.0;

            String safety;
            String safetyColor;
            if (hasher instanceof MD5Hasher) {
                safety = "NGUY HIỂM";
                safetyColor = RED;
            } else if (hasher instanceof BCryptHasher) {
                int str = ((BCryptHasher) hasher).hash("test").contains("$2a$06$") ? 6 : 
                          (((BCryptHasher) hasher).hash("test").contains("$2a$10$") ? 10 : 13);
                if (str < 10) {
                    safety = "YẾU";
                    safetyColor = YELLOW;
                } else if (str < 12) {
                    safety = "AN TOÀN";
                    safetyColor = GREEN;
                } else {
                    safety = "RẤT AN TOÀN";
                    safetyColor = BOLD + GREEN;
                }
            } else {
                safety = "RẤT AN TOÀN";
                safetyColor = BOLD + GREEN;
            }

            System.out.println(String.format("%-40s | %-18.2f | %s%-12s%s", 
                hasher.getAlgorithmName(), 
                ms, 
                safetyColor, safety, RESET));
        }

        System.out.println("\n" + BOLD + BLUE + "Nhận xét:" + RESET);
        System.out.println("* MD5 chạy quá nhanh (< 0.1ms). Hacker có thể thử hàng tỷ mật khẩu mỗi giây bằng GPU.");
        System.out.println("* BCrypt nâng cao độ khó bằng cách tăng tham số Strength (mỗi lần tăng 1 đơn vị, thời gian băm tăng gấp đôi).");
        System.out.println("* Argon2id vừa tốn thời gian vừa khóa cứng dung lượng RAM cần thiết (16MB), ngăn cản triệt để card đồ họa GPU.");
    }

    private void demoVerification(String password) {
        System.out.println("\n" + BOLD + YELLOW + "3. Kiểm tra cơ chế xác thực mật khẩu (Verify)" + RESET);

        PasswordHasher bcrypt = new BCryptHasher(10);
        String storedHash = bcrypt.hash(password);

        System.out.println("Chuỗi băm lưu trong CSDL: " + BLUE + storedHash + RESET);

        String correctInput = "SuperSecurePassword123!";
        String wrongInput = "supersecurepassword123!"; // Sai hoa/thường

        boolean correctMatches = bcrypt.verify(correctInput, storedHash);
        boolean wrongMatches = bcrypt.verify(wrongInput, storedHash);

        System.out.println("Nhập đúng mật khẩu: \"" + correctInput + "\" -> Khớp? " 
            + (correctMatches ? GREEN + "ĐÚNG (SUCCESS)" : RED + "SAI (FAIL)") + RESET);

        System.out.println("Nhập sai mật khẩu:  \"" + wrongInput + "\" -> Khớp? " 
            + (wrongMatches ? GREEN + "ĐÚNG (SUCCESS)" : RED + "SAI (FAIL)") + RESET);
    }
}
