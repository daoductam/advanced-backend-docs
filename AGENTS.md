# AGENTS.md

## Project Overview
**Advanced Backend Docs** là một kho lưu trữ tài liệu tham khảo, ghi chú ôn tập và hướng dẫn chuyên sâu về phát triển hệ thống Backend (Advanced Backend Development). Hệ thống này đóng vai trò là cơ sở tri thức giúp hệ thống hóa kiến thức về thiết kế hệ thống, cơ sở dữ liệu, mạng máy tính, tối ưu hóa hiệu năng, bảo mật.

## Tech Stack & Tools
- **Documentation**: Markdown, Mermaid.js (dùng để mô hình hóa kiến thức, vẽ sơ đồ luồng dữ liệu, sơ đồ mạng và kiến trúc hệ thống).

## Project Structure
Hệ thống tài liệu được tổ chức thành các phân vùng kiến thức cốt lõi:
- `1.restful-api-design/`: Tiêu chuẩn và thực tế tốt nhất trong thiết kế RESTful API.
- `2.caching-design/`: Các chiến lược caching, cơ chế invalidation và thiết kế bộ đệm.
- `3.index/` & `10.index_01/`: Cơ chế hoạt động của Index trong CSDL (B-Tree, Hash, Inverted Index) và tối ưu hóa truy vấn.
- `4.codebase/`: Tổ chức cấu trúc mã nguồn và các mẫu thiết kế (design patterns) phổ biến.
- `5.computer-networking/`: Kiến thức nền tảng mạng máy tính (OSI, TCP/IP, DNS, TLS Handshake, NAT/PAT).
- `6.redis/`: Cấu trúc dữ liệu Redis, cơ chế lưu trữ bộ nhớ ngoài và clustering.
- `7.data-modeling/`: Thiết kế và mô hình hóa dữ liệu (normalization, denormalization, quan hệ thực thể).
- `8.security/`: Các lỗ hổng bảo mật phổ biến (OWASP Top 10), mã hóa, xác thực (Authentication) và phân quyền (Authorization).
- `11.Regex/`: Hướng dẫn sử dụng biểu thức chính quy (Regular Expressions) trong lập trình.
- `12.transaction/`: Quản lý giao dịch trong CSDL, ACID, cô lập giao dịch (Transaction Isolation Levels) và MVCC.
- `13.clean-code/`: Nguyên tắc SOLID, kỹ thuật refactoring và phát hiện code smells.
- `14.locking/`: Cơ chế khóa trong lập trình và CSDL (Optimistic vs Pessimistic locking, Distributed Lock, DB Lock levels).
- `15.operating-system/`: Kiến thức Hệ điều hành (Process vs Thread, lập lịch CPU, bộ nhớ ảo).
- `16.integration/`: Tích hợp các dịch vụ bên thứ ba (Third-party Integration).
- `datetime-pooling/`: Quản lý múi giờ, định dạng ngày tháng và kỹ thuật pooling tài nguyên.
- `.agent/`: Chứa bộ não của AI (workflows, rules, tuyển tập skills hỗ trợ học tập và phát triển).

## Operational Resources (AI Context)
Mọi hành động của AI phải soi chiếu qua các tài nguyên này:
- **Workflows**: Tham khảo các quy trình chạy tại `.agent/workflows/` (ví dụ: `/debug`, `/design`, `/improve`, `/init`).
- **Coding Rules**: Tham khảo `.agent/rules/` để đảm bảo chất lượng code và an toàn thông tin (ví dụ: `code-quality.md`, `security.md`).
- **Special Skills**: Các kỹ năng bổ trợ đã được định nghĩa tại `.agent/skills/`.

## Conventions
- **Language**: Tài liệu chủ yếu viết bằng Tiếng Việt, giữ nguyên các thuật ngữ chuyên ngành Tiếng Anh để đảm bảo tính chính xác và dễ tra cứu.
- **Visuals**: Sử dụng Mermaid diagrams cho tất cả các phần mô tả luồng (flowchart, sequence, state) và sơ đồ hạ tầng.
- **Commit Message**: Tuân thủ Conventional Commits khi cập nhật tài liệu hoặc chỉnh sửa script công cụ (`docs: ...`, `feat: ...`, `fix: ...`, `refactor: ...`).

