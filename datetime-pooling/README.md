# 1. Datetime và Timezone
## 1.1. Máy tính lưu trữ thời gian như thế nào?

*   **UNIX timestamp**: số giây đã trôi qua kể từ ngày 01-01-1970 lúc 00:00:00 UTC
*   Ví dụ:
    *   Timestamp tính bằng giây: 1695718262
    *   Timestamp tính bằng mili giây: 1695718262342
*   Sự thật (Facts):
    *   Ở các hệ thống 32bit cũ, số nguyên có dấu sẽ bị **tràn (overflow) vào năm 2038**
    *   Tại sao lại là năm 1970? Hệ điều hành Unix được tạo ra vào cuối những năm 1960 và đầu những năm 1970. Các kỹ sư Unix đã tùy ý chọn năm 1970.

## 1.2. Múi giờ (Timezone)

*   Tại sao chúng ta lại cần các múi giờ?
*   Các múi giờ duy trì trật tự logic và điều chỉnh ngày và đêm trên toàn cầu
*   UTC = GMT = Múi giờ có độ lệch (offset) bằng 0

## 1.3. Định dạng thời gian (Datetime Format)

*   **ISO 8601**: 2023-09-25T16:20:52+07:00, 2023-09-25T09:20:52Z
    *   Z = +00:00
*   Việt Nam: 26/09/2023
*   RFC 2822: Mon, 25 Sep 2023 09:16:58 +0000
*   Javascript: YYYY-MM-DDTHH:mm:ss.sssZ
*   Java: Tue Sep 26 16:05:37 ICT 2023 (+0700)
*   RFC 3339

## 1.4. Bài tập (Exercise)

*   **ISO 8601**
*   Thời gian ở Hà Nội: 2023-09-26T07:15:52+07:00 -> Thời gian ở Tokyo (+09), Paris (+01), California (...)

## 1.5. Khi nào nên sử dụng DateTime, Timestamp?

*   **Timestamp**: để ghi lại một thời điểm cố định (ít nhiều).
    *   Ví dụ: created_at
*   **Datetime**: thời gian có thể được thiết lập và thay đổi tùy ý.
    *   Ví dụ: thời gian lên lịch cho các cuộc hẹn (schedule time for appointments)

## 1.6. Múi giờ trong hệ thống (Timezone in System)

*(Sơ đồ luồng đi của thời gian)*
*   **FE** (tz = +07) gửi timestamp `1724753640234`
*   **BE** (OS: tz = +07) nhận và chuyển thành chuỗi `'2024-08-27T17:13:24+07:00'`
*   **DB** (tz = +00) lưu trữ dạng chuỗi `'2024-08-27T17:13:24'`

*   **Backend, DB sử dụng múi giờ UTC. Khuyến nghị lưu trữ cả Múi giờ (TZ).**
*   Frontend sử dụng datetime cục bộ (hiển thị múi giờ) và gửi kèm Múi giờ (TZ) nếu cần.
*   **Cẩn thận**: Múi giờ của JVM (JVM timezone) != Múi giờ của Hệ điều hành (OS timezone) -> thiết lập múi giờ JVM bằng cách sử dụng biến môi trường (env var).

## 1.7. Độ chính xác (Precision)

*   FE insert: `'2024-08-27T17:13:25.123+07:00'` vào CSDL MySQL
*   BE return: `'2024-08-27T17:13:25.000+07:00'`
*   Tại sao?
*   Bởi vì độ chính xác của kiểu dữ liệu thời gian trong MySQL mặc định là theo giây.
*   Nguyên nhân: Kiểu dữ liệu: Datetime
*   **Sử dụng các chữ số thập phân (fractional digits) -> độ chính xác (accuracy)**
    *   datetime(3)
    *   timestamp(3)
*   Timestamp trong Postgres sử dụng 6 chữ số thập phân theo mặc định.

## 1.8. Làm việc với kiểu dữ liệu Datetime như thế nào? (How to Work with Datetime Data Type?)

*   **Các kiểu dữ liệu datetime của MySQL không lưu trữ thông tin múi giờ -> Khuyến nghị nên lưu trữ thông tin múi giờ.**
*   Ví dụ trong Postgres:
    *   Cột `started_at`: timestamptz(3)
    *   Cột `tz`: smallint
    *   TZ: UTC (+00)

## 1.9. Các thực hành tốt nhất (Best Practices)

*   **ISO 8601**
*   **Backend, DB sử dụng múi giờ UTC**
*   Frontend sử dụng datetime cục bộ (hiển thị múi giờ)
*   DB lưu trữ dưới dạng timestamp nếu có thể
*   Lưu trữ múi giờ (Store time zone)
*   **MySQL: sử dụng các chữ số thập phân (fractional digitals) -> độ chính xác (accuracy)**
*   **Múi giờ của JVM (JVM timezone) != Múi giờ của Hệ điều hành (OS timezone)**

Cẩn thận với (Be careful with):
*   **Múi giờ (Time zone)**
*   **Định dạng thời gian (Datetime format)**
*   **Độ chính xác: Chữ số thập phân (Precision: Fractional digitals)**
*   Một số case ma giáo khác

# Connection Pool