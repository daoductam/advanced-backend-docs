# System Design & Software Architecture

## 1. Mùi hương của kiến trúc xấu (Bad Architecture Smells / Bad Code)
* **Cứng nhắc (Rigidity):** Phần mềm khó thay đổi vì một thay đổi nhỏ sẽ dẫn đến chuỗi thay đổi khác kéo dài không điểm dừng.
* **Dễ vỡ (Fragility):** Chỉ một thay đổi nhỏ cũng có thể dẫn đến những hành vi (behaviors) không mong muốn và không thể dự đoán được ở các khu vực khác.
* **Lỏng lẻo (Immobility):** Hệ thống dễ dàng để làm sai hơn là làm đúng. Khi muốn thực hiện thay đổi, thay vì tuân theo thiết kế chuẩn, lập trình viên thường đi "đường tắt" (workaround).
* **Trùng lặp (Duplication):** Xảy ra khi các khái niệm trừu tượng (abstraction) cần thiết không được thiết kế/thực hiện, do thiếu thời gian hoặc thiếu kinh nghiệm.
* **Không rõ ràng (Opacity):** Mã nguồn được viết một cách mờ đục, khó hiểu, bắt buộc người đọc phải đi sâu vào từng phương thức (method) mới hiểu được hoạt động chung.
* **Phức tạp không cần thiết (Needless Complexity):** Quá lạm dụng các lớp trừu tượng (abstraction) để dự phòng cho tương lai, dẫn đến việc phức tạp hóa vấn đề. Thiết kế tốt cần nhẹ nhàng, linh hoạt, dễ đọc, dễ hiểu và dễ thay đổi, thay vì cố gắng dự đoán mọi thay đổi trong tương lai.

---

## 2. Kiến trúc & Thiết kế hệ thống (Architecture Concepts)

### Architecture Styles
Là mức độ cao nhất (high level) trong thiết kế hệ thống, định hình "phong cách" và cách thức các mô-đun cấp cao tương tác với nhau.
* **Ví dụ:**
  * Component-based
  * Monolithic application
  * Layered Architecture
  * Event-driven Architecture
  * Publish-subscribe
  * Client-server
  * Service-oriented Architecture (SOA)
* *Ví dụ cụ thể về SOA:* Hệ thống được chia nhỏ thành các dịch vụ độc lập, mỗi dịch vụ được xây dựng và thực thi riêng biệt rồi ghép nối lại với nhau. Nó không quy định chi tiết về việc có dùng MVC hay Singleton hay không, mà chỉ định hình phong cách cấu trúc của hệ thống.

### Architecture Patterns
Là giải pháp cho các vấn đề lặp đi lặp lại liên quan đến Architecture Styles (xác định cấu trúc cụ thể của các lớp, phân tầng dữ liệu).
* **Ví dụ:**
  * Three-tier (Client - Application Server - Database)
  * Model-View-Controller (MVC)
* *Ví dụ cụ thể:* Khi hệ thống theo phong cách Client-Server, ta quyết định dùng mô hình 3-tier: Tier 1 là Client (UI), Tier 2 là Application Server xử lý nghiệp vụ (Business Logic), Tier 3 tối ưu hóa cho lưu trữ (Database).

### Design Patterns
Khác với Architectural Patterns ở phạm vi (scope) áp dụng. Design Patterns giải quyết các vấn đề cục bộ, nhỏ lẻ trong mã nguồn, không làm ảnh hưởng lớn đến toàn bộ codebase.
* **Ví dụ:**
  * Làm thế nào để khởi tạo đối tượng khi chỉ biết loại đối tượng lúc runtime? -> *Factory Pattern*
  * Làm sao khởi tạo duy nhất một đối tượng trong toàn ứng dụng (Logger, ConfigHolder)? -> *Singleton Pattern*
  * Các pattern khác: *Strategy*, *State*, *Observer*, etc.

---

## 3. Khớp nối & Kết dính (Coupling & Cohesion)

### Loose Coupling (Khớp nối lỏng lẻo)
* **Coupling** thể hiện mức độ phụ thuộc lẫn nhau giữa các thành phần (components).
* **Low/Loose coupling:** Các thành phần ít phụ thuộc vào nhau. Thay đổi ở thành phần này ít hoặc không ảnh hưởng đến thành phần khác.
* **High/Tight coupling:** Các thành phần phụ thuộc chặt chẽ. Khi thay đổi một nơi, hàng loạt nơi khác bị ảnh hưởng và cần sửa đổi theo.
* **Mục tiêu:** Luôn hướng tới *Loose Coupling* để hệ thống dễ bảo trì, nâng cấp.
* **Giải pháp:** Áp dụng **Dependency Inversion (DIP)** (ví dụ: trong Java, sử dụng `interface` để Class A phụ thuộc vào interface thay vì implementation cụ thể của Class B).

### High Cohesion (Độ kết dính cao)
* **Cohesion** thể hiện mức độ tập trung nhiệm vụ của từng mô-đun. Nhiệm vụ càng rõ ràng và tách biệt thì độ kết dính càng cao (*High Cohesion*).
* Đạt được thông qua việc tuân thủ nguyên tắc đơn nhiệm (**Single Responsibility Principle - SRP**), mỗi mô-đun chỉ làm tốt một nhiệm vụ duy nhất.

| Cohesion | Coupling |
| :--- | :--- |
| Mối quan hệ bên trong một module. | Sự liên kết giữa các module khác nhau. |
| Thể hiện sức mạnh liên kết giữa các chức năng trong cùng module. | Thể hiện mức độ phụ thuộc vào các module khác. |
| Đánh giá chất lượng tập trung vào một công việc đơn lẻ. | Đánh giá mức độ liên kết/ảnh hưởng lẫn nhau giữa các module. |

---

## 4. Các mô hình kiến trúc phổ biến

### Monolith (Kiến trúc nguyên khối)
* **Khái niệm:** Tất cả các tính năng được đóng gói trong một ứng dụng duy nhất.
* **Hạn chế khi scale:**
  * Hệ thống phình to khi người dùng, tính năng và dữ liệu tăng trưởng.
  * Khó khăn cho lập trình viên để nắm bắt toàn bộ codebase. Việc sửa lỗi hoặc phát triển tính năng mới trở nên chậm chạp và rủi ro.
  * **Độ tin cậy thấp (Single Point of Failure):** Do chạy chung một process, lỗi rò rỉ bộ nhớ (memory leak) hoặc crash ở một module nhỏ có thể làm sập toàn bộ hệ thống.

### Microservices (Kiến trúc vi dịch vụ)
* **Khái niệm:** Ứng dụng được chia nhỏ thành các dịch vụ độc lập giao tiếp với nhau qua API.
* **Đặc điểm:**
  * Mỗi dịch vụ có database riêng biệt, được phát triển, triển khai và scale độc lập.
  * Giúp tăng tốc độ phát triển, quản lý dễ dàng hơn và tối ưu hóa nhân lực.
  * Kết hợp hoàn hảo với Agile, Cloud computing, DevOps, CI/CD và Containerization (Docker, Kubernetes).

### Event-Driven Architecture (EDA)
* **Khái niệm:** Kiến trúc phần mềm xây dựng xung quanh việc tạo ra, phát hiện, tiêu thụ và phản hồi lại các sự kiện (**Events**). Event đóng vai trò là phương tiện giao tiếp chính giúp giảm thiểu tối đa sự phụ thuộc giữa các dịch vụ.
* **Khi nào nên sử dụng:**
  * **Opaque consumer ecosystem:** Producer không cần biết Consumer là ai và hoạt động thế nào.
  * **High fan-out:** Một event được xử lý đồng thời bởi nhiều consumer khác nhau.
  * **Complex pattern matching:** Các event có thể được chuỗi hóa (xâu chuỗi) để tạo ra các event phức tạp hơn.
* **Ưu điểm:**
  * Xử lý bất đồng bộ giúp Producer không bị block bởi tốc độ xử lý của Consumer.
  * Dễ dàng thêm/bớt Consumer/Producer mà không ảnh hưởng hệ thống hiện tại.
  * Dễ dàng xử lý song song và tăng tốc độ xử lý lượng lớn dữ liệu.
* **Nhược điểm:**
  * Luồng tương tác (Event chaining) dễ trở nên phức tạp, khó kiểm soát và debug.
  * Tăng độ phức tạp vận hành vì cần thêm thành phần trung gian (Message Broker) thay vị giao tiếp Client-Server truyền thống.

### Layered Architecture (Kiến trúc phân tầng)
* Thiết kế phân tầng nghiêm ngặt (chỉ gọi tầng ngay bên dưới) hoặc linh hoạt (gọi bất kỳ tầng nào bên dưới).
* **Cấu trúc cơ bản:**
  * **Client (Web browser):** Render giao diện, gửi và nhận request.
  * **Application Server:** Chứa Presentation Layer, Application Layer, Domain Layer, Persistence Layer.
  * **Database Server:** Được tối ưu hóa cho mục đích lưu trữ dữ liệu.
* Di chuyển toàn bộ business logic lên Server giúp giải quyết bài toán scale; client chỉ tập trung vào hiển thị.

---

## 5. Tổ chức Package hiệu quả
* Mỗi package cần có kích thước đủ nhỏ để tránh gây ảnh hưởng diện rộng khi các package khác phụ thuộc vào nó.
* Đảm bảo tính đóng gói tốt nhất: khi có thay đổi xảy ra, số lượng package bị tác động là ít nhất.

---

## 6. Nguyên tắc SOLID
* **SRP (Single Responsibility Principle):** Một class chỉ nên có một lý do duy nhất để thay đổi.
* **OCP (Open/Closed Principle):** Class nên mở rộng để phát triển (open for extension) nhưng đóng đối với việc chỉnh sửa trực tiếp (closed for modification).
* **LSP (Liskov Substitution Principle):** Các lớp con (subtype) phải có khả năng thế chỗ hoàn toàn lớp cha (supertype) mà không làm thay đổi tính đúng đắn của chương trình.
* **ISP (Interface Segregation Principle):** Nên chia nhỏ thành nhiều interface chuyên biệt cho từng mục đích hơn là gộp chung vào một interface lớn đa năng.
* **DIP (Dependency Inversion Principle):** Module tầng trên không được phụ thuộc vào module tầng dưới. Cả hai đều phải phụ thuộc vào các trừu tượng (interfaces/abstractions), không phụ thuộc vào chi tiết thực thể (concrete implementations).

---

## 7. Các tính chất của Hệ thống phân tán (Distributed System)
* **Scalability (Khả năng mở rộng):** Khả năng chịu tải và mở rộng hệ thống khi lượng công việc tăng lên theo thời gian.
* **Reliability (Độ tin cậy):** Khả năng duy trì hoạt động liên tục ngay cả khi một hoặc một vài thành phần phần cứng/phần mềm bị lỗi.
* **Availability (Tính sẵn sàng):** Tỷ lệ phần trăm thời gian hệ thống hoạt động bình thường, liên tục đáp ứng yêu cầu người dùng.
* **Performance (Hiệu suất):** Được đo bằng khả năng chịu tải (high load) và độ trễ thấp (low latency).
* **Maintainability (Khả năng bảo trì):** Tốc độ phát hiện, cô lập nguyên nhân lỗi (root cause) và sửa chữa hệ thống khi có sự cố.

### Cân bằng tải (Load Balancing - LB)
* Phân phối tải đều cho các Server (Application/Database) nhằm tăng tính sẵn sàng (Availability).
* LB liên tục thực hiện **Health checks** để kiểm tra trạng thái hoạt động của các server, tự động loại bỏ server bị lỗi (unhealthy) và khôi phục khi chúng hoạt động bình thường.
* **Các thuật toán phổ biến:** Round Robin, Weighted Round Robin, Least Response Time, Least Connections.

### Caching
* Tiết kiệm tài nguyên và giảm chi phí bằng cách lưu trữ tạm thời các dữ liệu ít thay đổi nhưng được truy cập thường xuyên vào bộ nhớ đệm (Cache).
* **CDN (Content Delivery Network):** Caching tài nguyên tĩnh (images, css, js) tại các máy chủ gần vị trí địa lý của người dùng nhất để tối ưu tốc độ tải trang.

### Sharding & Partitioning dữ liệu
* **Horizontal sharding (Sharding ngang):** Phân chia các dòng dữ liệu của cùng một bảng sang nhiều database khác nhau.
* **Vertical sharding (Sharding dọc):** Chia dữ liệu dựa trên tính năng/nghiệp vụ của hệ thống (ví dụ: tách bảng User và Order sang các DB khác nhau).
* **Hạn chế:** Khó khăn khi thực hiện câu lệnh JOIN trên nhiều server khác nhau; việc tái phân bổ dữ liệu (rebalancing) khi hệ thống scale gây tốn chi phí.

### Định lý CAP
Hệ thống phân tán không thể đồng thời thỏa mãn cả 3 yếu tố:
1. **Consistency (Tính nhất quán):** Tất cả các node đều có dữ liệu đồng nhất tại cùng một thời điểm.
2. **Availability (Tính sẵn sàng):** Mọi request gửi đi đều nhận được phản hồi (thành công hoặc thất bại), ngay cả khi có node bị sập.
3. **Partition Tolerance (Khả năng chịu lỗi phân mảnh mạng):** Hệ thống tiếp tục hoạt động bất chấp đường truyền mạng giữa các node bị ngắt kết nối.
* *Lưu ý:* Thường ta sẽ đánh đổi sự nhất quán tức thời (**Consistency**) lấy **Availability** và **Partition Tolerance**, hướng tới sự nhất quán sau một khoảng thời gian trễ nhất định (**Eventually Consistency**).

---

## 8. Quy trình thiết kế hệ thống (System Design Interview)
1. **Làm rõ yêu cầu:**
   * *Functional Requirements:* Các chức năng người dùng cần (Đăng nhập, tìm kiếm, đặt hàng...).
   * *Non-Functional Requirements:* Khả năng vận hành hệ thống (High availability, Scalability, Low latency, Consistency...).
2. **Ước lượng tài nguyên (Estimation):**
   * Số lượng request/giây (TPS), lưu lượng băng thông, dung lượng lưu trữ cần thiết.
3. **Thiết kế API (System Interface Design):**
   * Định nghĩa các API endpoints và kiểu dữ liệu trao đổi.
4. **Thiết kế Data Model:**
   * Chọn loại Database phù hợp (Relational DB cho dữ liệu có cấu trúc cao, hoặc NoSQL cho dữ liệu phi cấu trúc, lưu trữ dạng tài liệu).
5. **Thiết kế tổng quan (High-level Design):**
   * Sơ đồ kết nối các thành phần chính (LB, Web Server, App Server, DB, Cache).
6. **Thiết kế chi tiết (Detailed Design):**
   * Tối ưu hóa hệ thống: Thiết lập Cache, CDN, phân vùng dữ liệu (Sharding), đồng bộ database (Replication).
7. **Xác định điểm nghẽn (Bottlenecks):**
   * Dự đoán các kịch bản lỗi, thiết lập giám sát (Monitoring) và giải pháp khắc phục sự cố.

---

## 9. Xử lý bất đồng bộ với Message Queue (Asynchronous Processing)
* **Khái niệm:** Cơ chế truyền tin bất đồng bộ cho phép các ứng dụng/dịch vụ giao tiếp thông qua việc gửi tin nhắn (Messages) vào hàng đợi (Queue) giúp tách biệt sự phụ thuộc lẫn nhau (**Decoupling**).
* **Ưu điểm:**
  * Giúp tăng tốc độ phản hồi cho người dùng bằng cách chuyển các tác vụ nặng (gửi email, xử lý ảnh, báo cáo) xuống xử lý ngầm (Background Jobs).
  * **Throttling (Giới hạn tốc độ tải):** Đóng vai trò bộ đệm bảo vệ hệ thống không bị quá tải khi lượng request tăng đột biến.
  * Tăng độ tin cậy: Tin nhắn được lưu trữ an toàn trong Broker, dễ dàng retry khi gặp lỗi.
* **Các thuật ngữ cơ bản:**
  * **Asynchronous Broker:** Trình trung gian định tuyến, lưu trữ và truyền tải message (Kafka, RabbitMQ...).
  * **Producer:** Ứng dụng tạo và gửi message.
  * **Consumer:** Ứng dụng đọc và xử lý message từ Broker.
  * **Queue / Topic:** Thành phần lưu trữ trung gian chứa message.
* **Mô hình truyền tin:**
  * *One-to-One (Point-to-Point):* Mỗi tin nhắn chỉ được tiêu thụ bởi duy nhất một Consumer (Work Queue).
  * *One-to-Many (Broadcast):* Tin nhắn được gửi tới tất cả Consumer đang đăng ký lắng nghe (Pub/Sub).

### RabbitMQ Concepts
* **Exchange:** Thành phần nhận message từ Producer đầu tiên và định tuyến chúng vào các Queue dựa trên luật cấu hình (Routing key).
* **Các loại Exchange:**
  * **Direct Exchange:** Định tuyến message đến Queue có routing key khớp chính xác.
  * **Topic Exchange:** Định tuyến message dựa trên việc so khớp mẫu ký tự của routing key (wildcard matching).
  * **Fanout Exchange:** Gửi message tới tất cả các Queue được liên kết mà không quan tâm đến routing key.
  * **Headers Exchange:** Định tuyến dựa trên các thuộc tính của message header.
  * **Dead Letter Exchange (DLX):** Nơi chứa các message bị lỗi, hết hạn (TTL) hoặc bị từ chối xử lý bởi consumer.
