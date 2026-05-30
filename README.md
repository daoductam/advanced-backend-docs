# 🚀 Advanced Backend Development Reference Repository

Chào mừng bạn đến với **Advanced Backend Docs** — Kho lưu trữ tài liệu tham khảo, hướng dẫn thực hành và ghi chú ôn tập chuyên sâu về phát triển hệ thống Backend (Advanced Backend Development).

Hệ thống này được xây dựng như một cơ sở tri thức (Knowledge Base) vững chắc, giúp hệ thống hóa các kiến thức cốt lõi từ thiết kế hệ thống, cơ sở dữ liệu, tối ưu hóa hiệu năng cho đến bảo mật và tích hợp hệ thống.

---

## 📂 Cấu Trúc Kho Tài Liệu (Repository Structure)

Các bài học và tài liệu được phân chia khoa học thành các phân vùng kiến thức sau:

| Thư mục | Nội dung & Phân vùng kiến thức |
| :--- | :--- |
| **`1.restful-api-design/`** | Tiêu chuẩn, thực tế tốt nhất (Best Practices) trong thiết kế RESTful API, quản lý phiên bản, thiết kế payload. |
| **`2.caching-design/`** | Các chiến lược Caching (Cache Aside, Write Through,...), cơ chế Invalidation và thiết kế bộ đệm tối ưu. |
| **`3.index/` & `10.index_01/`** | Cơ chế hoạt động của Database Index (B-Tree, B+Tree, Hash Index, Covering Index) và tối ưu hóa câu lệnh SQL. |
| **`4.codebase/`** | Cấu trúc tổ chức mã nguồn ứng dụng lớn và các mẫu thiết kế (Design Patterns) phổ biến. |
| **`5.computer-networking/`** | Nền tảng mạng máy tính: Mô hình OSI, TCP/IP, DNS, TLS Handshake, cơ chế NAT/PAT. |
| **`6.redis/`** | Cấu trúc dữ liệu Redis, cơ chế lưu trữ bộ nhớ ngoài (Persistence), Redis Cluster và Distributed Lock. |
| **`7.data-modeling/`** | Thiết kế và mô hình hóa dữ liệu (Normalization, Denormalization, thực thể mối quan hệ). |
| **`8.security/`** | Lỗ hổng bảo mật phổ biến (OWASP Top 10), mã hóa, xác thực (Authentication) và phân quyền (Authorization). |
| **`11.Regex/`** | Hướng dẫn sử dụng Biểu thức chính quy (Regular Expressions) hiệu quả trong lập trình. |
| **`12.transaction/`** | Quản lý Transaction trong CSDL, thuộc tính ACID, các cấp độ cô lập (Transaction Isolation Levels) và MVCC. |
| **`13.clean-code/`** | Nguyên tắc SOLID, kỹ thuật Refactoring, phát hiện và khử Code Smells. |
| **`14.locking/`** | Cơ chế khóa (Optimistic vs Pessimistic Locking), khóa phân tán (Distributed Lock) và DB Lock Levels. |
| **`15.operating-system/`** | Kiến thức Hệ điều hành cốt lõi: Process vs Thread, Lập lịch CPU, Bộ nhớ ảo (Virtual Memory). |
| **`16.integration/`** | Tích hợp dịch vụ bên thứ ba (Third-party Integration) bảo mật, chịu lỗi. |
| **`17.datetime-pooling/`** | Quản lý múi giờ, định dạng ngày tháng (ISO 8601) và tối ưu kích thước Connection Pool & Thread Pool. |

---

## 🎨 Quy Ước Tài Liệu (Document Conventions)

Để đảm bảo tính nhất quán và chất lượng tri thức, mọi đóng góp tài liệu cần tuân thủ các quy ước sau:

1.  **Ngôn ngữ (Language):**
    *   Tài liệu chủ yếu viết bằng **Tiếng Việt**.
    *   Giữ nguyên các thuật ngữ chuyên ngành bằng **Tiếng Anh** (ví dụ: *Connection Pool*, *Distributed Lock*, *Bookmark Lookup*) để đảm bảo tính chính xác và dễ tra cứu.
2.  **Trực quan hóa (Visuals):**
    *   Sử dụng **Mermaid.js** cho tất cả sơ đồ luồng dữ liệu (Data Flows), sơ đồ tuần tự (Sequence Diagrams), sơ đồ trạng thái (State Diagrams) và hạ tầng.
3.  **Lịch sử thay đổi (Commit Messages):**
    *   Tuân thủ quy chuẩn **Conventional Commits** khi cập nhật tài liệu hoặc chỉnh sửa mã nguồn:
        *   `docs: ...` (cho tài liệu cập nhật)
        *   `feat: ...` (cho bài thực hành/code mới)
        *   `fix: ...` (cho sửa lỗi nội dung hoặc cú pháp sơ đồ)

---

## 🛠️ Hướng Dẫn Sử Dụng & Học Tập

*   **Đọc Lý Thuyết:** Hãy bắt đầu đọc file `README.md` tại mỗi thư mục chuyên đề để nắm vững nền tảng lý thuyết và kiến trúc thiết kế.
*   **Thực Hành:** Mỗi chuyên đề đều đi kèm các hướng dẫn thực hành chi tiết (ví dụ: `index_practice_guide.md`, `practice_guide.md`) giúp bạn chạy thử nghiệm thực tế trên máy cá nhân để đo lường hiệu năng (Response Time, Explain Plan).
*   **Tham Khảo Sơ Đồ:** Sử dụng các sơ đồ trực quan được vẽ bằng Mermaid để hình dung cách hệ thống vận hành và tương tác.

*Chúc bạn có hành trình khám phá kiến thức Backend đầy thú vị!*
