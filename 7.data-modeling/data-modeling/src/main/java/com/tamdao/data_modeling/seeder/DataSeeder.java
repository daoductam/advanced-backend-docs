package com.tamdao.data_modeling.seeder;

import com.tamdao.data_modeling.entity.*;
import com.tamdao.data_modeling.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Autowired
    private HomestayRepository homestayRepository;

    @Autowired
    private HomestayAvailabilityRepository availabilityRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private AdClickLogRepository adClickLogRepository;

    @Autowired
    private AdClickSummaryRepository adClickSummaryRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        createDatabaseView();
        seedArticles();
        seedTodoItems();
        seedHomestays();
        seedEvents();
        seedAdReports();
    }

    private void createDatabaseView() {
        System.out.println(">>> Đang khởi tạo Database View v_homestay_availability_report...");
        jdbcTemplate.execute("DROP TABLE IF EXISTS v_homestay_availability_report");
        jdbcTemplate.execute("CREATE OR REPLACE VIEW v_homestay_availability_report AS " +
                "SELECT ha.id AS id, h.id AS homestay_id, h.name AS homestay_name, " +
                "ha.booking_date AS booking_date, ha.price AS price, ha.status AS status " +
                "FROM homestays h JOIN homestay_availabilities ha ON h.id = ha.homestay_id");
        System.out.println(">>> Database View khởi tạo thành công!");
    }



    private void seedArticles() {
        if (articleRepository.count() == 0) {
            System.out.println(">>> Bắt đầu khởi tạo dữ liệu mẫu cho Đa ngôn ngữ (JSON) & Tagging...");

            Article article1 = new Article();
            Map<String, String> translation1 = new HashMap<>();
            translation1.put("vi", "Kỹ Sư Phần Mềm");
            translation1.put("en", "Software Engineer");
            translation1.put("cn", "软件工程师");
            article1.setTitleTranslations(translation1);
            article1.setTags(List.of("Java", "Backend", "Spring Boot"));
            article1.setStatus("ACTIVE");

            Article article2 = new Article();
            Map<String, String> translation2 = new HashMap<>();
            translation2.put("vi", "Quản Lý Sản Phẩm");
            translation2.put("en", "Product Manager");
            translation2.put("cn", "产品经理");
            article2.setTitleTranslations(translation2);
            article2.setTags(List.of("Product", "Management"));
            article2.setStatus("ACTIVE");

            Article article3 = new Article();
            Map<String, String> translation3 = new HashMap<>();
            translation3.put("vi", "Nhà Khoa Học Dữ Liệu");
            translation3.put("en", "Data Scientist");
            translation3.put("cn", "数据科学家");
            article3.setTitleTranslations(translation3);
            article3.setTags(List.of("Data Science", "Python", "Backend"));
            article3.setStatus("DRAFT");

            articleRepository.saveAll(List.of(article1, article2, article3));
            System.out.println(">>> Khởi tạo dữ liệu mẫu Article thành công!");
        } else {
            System.out.println(">>> Đã có dữ liệu trong bảng articles, bỏ qua bước seeding.");
        }
    }


    private void seedTodoItems() {
        if (todoItemRepository.count() == 0) {
            System.out.println(">>> Bắt đầu khởi tạo dữ liệu mẫu cho Ordering (Fractional Indexing)...");

            List<TodoItem> todos = List.of(
                    new TodoItem(null, "Thiết kế Database Schema", 1000.0, "TODO"),
                    new TodoItem(null, "Viết REST API",             2000.0, "TODO"),
                    new TodoItem(null, "Viết Unit Test",            3000.0, "TODO"),
                    new TodoItem(null, "Review Code",               4000.0, "TODO"),
                    new TodoItem(null, "Deploy Production",         5000.0, "TODO")
            );

            todoItemRepository.saveAll(todos);
            System.out.println(">>> Khởi tạo dữ liệu mẫu TodoItem thành công!");
        } else {
            System.out.println(">>> Đã có dữ liệu trong bảng todo_items, bỏ qua bước seeding.");
        }
    }

    private void seedHomestays() {
        if (homestayRepository.count() == 0) {
            System.out.println(">>> Bắt đầu khởi tạo dữ liệu mẫu cho Homestay (Pre-allocation)...");

            Homestay h1 = new Homestay(null, "Da Lat Pine Hill Homestay", "Dalat, Vietnam", BigDecimal.valueOf(500000));
            Homestay h2 = new Homestay(null, "Hanoi Old Quarter Oasis", "Hanoi, Vietnam", BigDecimal.valueOf(800000));

            homestayRepository.saveAll(List.of(h1, h2));

            // Sinh trước 30 ngày cho mỗi homestay để thử nghiệm nhanh
            LocalDate today = LocalDate.now();
            List<HomestayAvailability> availabilities = new ArrayList<>();

            for (Homestay h : List.of(h1, h2)) {
                for (int i = 0; i < 30; i++) {
                    HomestayAvailability av = new HomestayAvailability();
                    av.setHomestay(h);
                    av.setBookingDate(today.plusDays(i));
                    // Đặt giá ngày cuối tuần (Thứ 7, Chủ Nhật) tăng 20%
                    LocalDate date = today.plusDays(i);
                    BigDecimal price = h.getDefaultPrice();
                    if (date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7) {
                        price = price.multiply(BigDecimal.valueOf(1.2)); // Tăng 20% cuối tuần
                    }
                    av.setPrice(price);
                    av.setStatus("AVAILABLE");
                    availabilities.add(av);
                }
            }

            availabilityRepository.saveAll(availabilities);
            System.out.println(">>> Khởi tạo dữ liệu mẫu Homestay & lịch trống 30 ngày thành công!");
        } else {
            System.out.println(">>> Đã có dữ liệu trong bảng homestays, bỏ qua bước seeding.");
        }
    }

    private void seedEvents() {
        if (eventRepository.count() == 0) {
            System.out.println(">>> Bắt đầu khởi tạo dữ liệu mẫu cho Lịch biểu sự kiện lặp lại (Calendar Events)...");

            // 1. Sự kiện đơn lẻ: "Họp Dự án A"
            Event singleEvent = new Event(null, "Họp Dự án A", "Họp thảo luận sprint backlog", "NONE");
            eventRepository.save(singleEvent);

            TimeSlot singleSlot = new TimeSlot();
            singleSlot.setEvent(singleEvent);
            // Diễn ra lúc 10:00 sáng mai
            LocalDateTime tomorrow10Am = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
            singleSlot.setBeginLocalTime(tomorrow10Am);
            singleSlot.setEndLocalTime(tomorrow10Am.plusHours(1));
            singleSlot.setTimezoneId("Asia/Ho_Chi_Minh");
            singleSlot.setStatus("ACTIVE");
            timeSlotRepository.save(singleSlot);

            // 2. Sự kiện lặp lại hàng tuần: "Họp Giao Ban Công Ty"
            Event weeklyEvent = new Event(null, "Họp Giao Ban Công Ty", "Họp giao ban toàn công ty hàng tuần", "WEEKLY");
            eventRepository.save(weeklyEvent);

            // Sinh 10 slots cho 10 tuần tiếp theo bắt đầu từ Thứ 2 tới lúc 09:00 sáng
            LocalDateTime baseMonday = LocalDateTime.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.MONDAY))
                    .withHour(9).withMinute(0).withSecond(0).withNano(0);

            List<TimeSlot> weeklySlots = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                TimeSlot slot = new TimeSlot();
                slot.setEvent(weeklyEvent);
                slot.setBeginLocalTime(baseMonday.plusWeeks(i));
                slot.setEndLocalTime(baseMonday.plusWeeks(i).plusHours(1));
                slot.setTimezoneId("Asia/Ho_Chi_Minh");
                slot.setStatus("ACTIVE");
                weeklySlots.add(slot);
            }
            timeSlotRepository.saveAll(weeklySlots);

            System.out.println(">>> Khởi tạo dữ liệu mẫu Event & TimeSlots thành công!");
        } else {
            System.out.println(">>> Đã có dữ liệu trong bảng events, bỏ qua bước seeding.");
        }
    }

    private void seedAdReports() {
        if (adClickLogRepository.count() == 0) {
            System.out.println(">>> Bắt đầu khởi tạo dữ liệu mẫu cho Thống kê & Báo cáo (Report Queries)...");

            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime startOfYesterday = yesterday.atStartOfDay();

            // Sinh 25 click logs cho Quảng cáo ID 101 vào ngày hôm qua
            List<AdClickLog> logs = new ArrayList<>();
            for (int i = 0; i < 25; i++) {
                AdClickLog log = new AdClickLog();
                log.setAdId(101L);
                // Rải rác click trong ngày hôm qua
                log.setClickedAt(startOfYesterday.plusHours(i % 24).plusMinutes(i * 2));
                log.setVisitorIp("192.168.1." + i);
                logs.add(log);
            }
            adClickLogRepository.saveAll(logs);

            // Chạy tiến trình tổng hợp trước cho ngày hôm qua để lưu vào bảng AdClickSummary
            AdClickSummary summary = new AdClickSummary();
            summary.setAdId(101L);
            summary.setClickDate(yesterday);
            summary.setClickCount(25L);
            adClickSummaryRepository.save(summary);

            System.out.println(">>> Khởi tạo dữ liệu mẫu và tổng hợp trước ngày hôm qua cho Ad clicks thành công!");
        } else {
            System.out.println(">>> Đã có dữ liệu trong bảng ad_click_logs, bỏ qua bước seeding.");
        }
    }
}



