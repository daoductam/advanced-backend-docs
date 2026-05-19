# Hướng dẫn về Mã sạch (Clean Code Guide)

> *“Tìm những người mày mang ơn trả.*  
> *Mọi thứ cứ để nhân quả”*  
> — **Mở Mắt (Lil Wuyn, Đen)**

<details open>
<summary><b>Mục lục (Table of Contents)</b></summary>

- [1. Mã sạch (Clean Code)](#1-mã-sạch-clean-code)
  - [1.1. Tại sao cần Clean Code? (Why Clean Code?)](#11-tại-sao-cần-clean-code-why-clean-code)
  - [1.2. Clean Code là gì? (What is Clean Code?)](#12-clean-code-là-gì-what-is-clean-code)
  - [1.3. Mục tiêu & Tư duy (Objectives/Mindset)](#13-mục-tiêu--tư-duy-objectivesmindset)
  - [1.4. Nguyên nhân gây ra "Code Smell" (Causes of Code Smell)](#14-nguyên-nhân-gây-ra-code-smell-causes-of-code-smell)
  - [1.5. Cách viết Clean Code (How to Write Clean Code)](#15-cách-viết-clean-code-how-to-write-clean-code)
  - [1.6. Các nguyên lý cốt lõi (Principles)](#16-các-nguyên-lý-cốt-lõi-principles)
- [2. Thực hành tốt (Practices)](#2-thực-hành-tốt-practices)
  - [2.0. Các bước lập trình (Coding Steps)](#20-các-bước-lập-trình-coding-steps)
  - [2.1. Đặt tên đối tượng (Naming Things)](#21-đặt-tên-đối-tượng-naming-things)
  - [2.2. Hàm & Phương thức (Functions)](#22-hàm--phương-thức-functions)
  - [2.3. Lớp đối tượng (Class)](#23-lớp-đối-tượng-class)
  - [2.4. Trừu tượng hóa (Abstraction)](#24-trừu-tượng-hóa-abstraction)
  - [2.5. Đơn giản hóa luồng điều khiển (Simplify Control Flow)](#25-đơn-giản-hóa-luồng-điều-khiển-simplify-control-flow)
  - [2.6. Xử lý ngoại lệ & Lỗi (Error Handling)](#26-xử-lý-ngoại-lệ--lỗi-error-handling)
  - [2.7. Chú thích & Định dạng (Comments & Formatting)](#27-chú-thích--định-dạng-comments--formatting)
  - [2.8. Kiểm thử (Test)](#28-kiểm-thử-test)
  - [2.9. Ranh giới hệ thống (Boundary)](#29-ranh-giới-hệ-thống-boundary)
  - [2.10. Tái cấu trúc mã nguồn (Refactoring)](#210-tái-cấu-trúc-mã-nguồn-refactoring)
- [3. Đánh giá mã nguồn (Code Review)](#3-đánh-giá-mã-nguồn-code-review)
  - [3.1. Mục tiêu của Code Review (The Goal of Code Review)](#31-mục-tiêu-của-code-review-the-goal-of-code-review)
  - [3.2. Các khía cạnh cần Review (Code Review Points)](#32-các-khía-cạnh-cần-review-code-review-points)
  - [3.3. Quy trình Code Review (Code Review Process)](#33-quy-trình-code-review-code-review-process)
  - [3.4. Thái độ khi Code Review (Attitude)](#34-thái-độ-khi-code-review-attitude)
  - [3.5. Cách viết bình luận Code Review (How to Write Comments)](#35-cách-viết-bình-luận-code-review-how-to-write-comments)
  - [3.6. Thực hành tốt nhất (Best Practices)](#36-thực-hành-tốt-nhất-best-practices)
  - [3.7. Giải quyết xung đột ý kiến (Resolving Conflicts)](#37-giải-quyết-xung-đột-ý-kiến-resolving-conflicts)
- [Tóm tắt & Tài liệu tham khảo (Recap & References)](#tóm-tắt--tài-liệu-tham-khảo-recap--references)
- [Bài tập về nhà (Homework)](#bài-tập-về-nhà-homework)

</details>

---

# 1. Mã sạch (Clean Code)

## 1.1. Tại sao cần Clean Code? (Why Clean Code?)

> *“Nếu bạn nghĩ kiến trúc tốt là đắt đỏ, hãy thử một kiến trúc tồi xem.”*  
> — **Brian Foote và Joseph Yoder**

Các vấn đề phổ biến trong phát triển phần mềm:
*   **Dễ phát sinh lỗi (Buggy):** Code lộn xộn, thiếu kiểm thử làm hệ thống kém ổn định và thường xuyên xảy ra lỗi.
*   **Chậm đưa sản phẩm ra thị trường (Slow to market):** Khó đọc, khó hiểu khiến tốc độ phát triển tính năng mới bị kéo lùi đáng kể qua thời gian.

---

## 1.2. Clean Code là gì? (What is Clean Code?)
*   **Clean Code** đơn giản là mã nguồn **rất dễ hiểu** và **dễ dàng thay đổi/mở rộng**.

---

## 1.3. Mục tiêu & Tư duy (Objectives/Mindset)
*   **Tư duy Bộ tứ "-able" (Fourable):**
    *   **Readable:** Dễ đọc và hiểu mục đích của code.
    *   **Maintainable:** Dễ bảo trì và sửa lỗi.
    *   **Testable:** Dễ dàng viết các ca kiểm thử tự động (Unit Test, Integration Test).
    *   **Scalable:** Dễ mở rộng cấu trúc khi quy mô dự án lớn lên.
*   **Mục tiêu chung (tương tự như Kiến trúc sạch - Clean Architecture):**
    > *"Giảm thiểu tối đa nguồn nhân lực cần thiết để xây dựng và bảo trì hệ thống."*
*   **Sự đánh đổi cần lưu ý (Trade-offs):**
    *   **Clean Code vs Hiệu năng (Performance):** Đôi khi cấu trúc phân tách rõ ràng, tạo nhiều đối tượng/lớp trung gian sẽ đánh đổi một phần rất nhỏ hiệu năng so với viết gộp tối ưu phần cứng.
    *   **Clean Code vs Tốc độ ra mắt (Speed of Market Delivery):** Viết code sạch có thể tốn thời gian hơn ở giai đoạn đầu, nhưng sẽ tiết kiệm rất nhiều thời gian ở các giai đoạn sau.

---

## 1.4. Nguyên nhân gây ra "Code Smell" (Causes of Code Smell)
"Mùi hôi" của code thường xuất phát từ:
*   Áp lực bàn giao sản phẩm quá nhanh khiến lập trình viên phải "đi tắt" (code tạm bợ).
*   Thiếu tính kỷ luật cá nhân hoặc không có sự thống nhất về tiêu chuẩn chung trong nhóm.
*   Lười biếng hoặc không có thói quen tái cấu trúc (refactoring) sau khi chạy thử nghiệm thành công.
*   Chưa nắm vững các nguyên lý thiết kế hướng đối tượng và các mẫu thiết kế cơ bản.

---

## 1.5. Cách viết Clean Code (How to Write Clean Code)

### Phương pháp thực hành (Practice Ways)
*   **Đọc nhiều hơn (Read) $\rightarrow$ Để cảm nhận được vẻ đẹp của code:**
    *   Đọc mã nguồn của người khác (học cách phân biệt code sạch và code "bốc mùi").
    *   Đọc sách chuyên ngành (cả sách Kỹ thuật lẫn Phi kỹ thuật để mở rộng tư duy).
*   **Kỷ luật và Thực tế (Be Disciplined & Be Pragmatic):**
    *   Luôn luôn suy nghĩ về mục tiêu dài hạn, các nguyên lý và quy ước đặt ra trước khi viết dòng code đầu tiên.
*   **Luyện tập (Practices) $\rightarrow$ Học cách đối phó với sự phức tạp:**
    *   Hiểu rõ kiến thức nền tảng và nghiệp vụ/ngữ cảnh hệ thống (Domain).
    *   Không chỉ sửa triệu chứng bên ngoài, hãy giải quyết tận gốc nguyên nhân của lỗi (Fix symptoms & root causes).
    *   Tái cấu trúc (Refactor) thường xuyên bất cứ khi nào chạm vào code.
    *   Lập trình cặp (Pair programming) để cùng học hỏi và kiểm soát chất lượng chéo.

### Phương pháp trừu tượng (Abstract Ways)
*   **Dễ hiểu (Easy to understand):** Đặt tên rõ nghĩa, tuân thủ Quy ước (Conventions), Định dạng nhất quán (Format), Đánh giá mã nguồn (Code Review), Quét lỗi tĩnh (Static Scanning - SonarQube),...
*   **Dễ thay đổi (Easy to change):** Áp dụng các Nguyên lý (Principles), Mẫu thiết kế (Design Patterns), Viết kiểm thử (Unit Test), Tái cấu trúc thường xuyên (Refactoring),...

---

## 1.6. Các nguyên lý cốt lõi (Principles)
*   **Nguyên lý KISS (Keep It Simple, Stupid - Giữ mọi thứ đơn giản):**
    *   Chia nhỏ vấn đề lớn thành các phần nhỏ đủ để hiểu và kiểm soát được.
    *   Không làm phức tạp hóa vấn đề ngay từ đầu (Tránh Over-engineering).
*   **Nguyên lý SOLID (Năm nguyên lý thiết kế hướng đối tượng):**
    1.  **S**ingle Responsibility Principle (Nguyên lý đơn trách nhiệm)
    2.  **O**pen-Closed Principle (Nguyên lý đóng/mở)
    3.  **L**iskov Substitution Principle (Nguyên lý thay thế Liskov)
    4.  **I**nterface Segregation Principle (Nguyên lý phân tách giao diện)
    5.  **D**ependency Inversion Principle (Nguyên lý đảo ngược phụ thuộc)
    
    > *“Nếu Nguyên lý Đóng/Mở (OCP) xác định mục tiêu của kiến trúc hướng đối tượng, thì Nguyên lý Đảo ngược Phụ thuộc (DIP) chính là cơ chế cốt lõi để đạt được mục tiêu đó.”*

---

# 2. Thực hành tốt (Practices)

## 2.0. Các bước lập trình (Coding Steps)
Để tạo ra một đoạn code chất lượng, hãy tuân theo quy trình từng bước sau:
1.  **Trừu tượng hóa các bước chính (Abstract):** Định hình các bước logic lớn trước khi viết code chi tiết.
2.  **Viết mã nguồn theo các bước chính (Write):** Tập trung hiện thực hóa khung xương logic.
3.  **Xem xét và cải tiến sơ bộ (Review & Refactor):** Trước khi chạy thử, hãy review lại code của mình và tái cấu trúc nhanh những phần chưa ổn.
4.  **Tự kiểm thử & Viết Unit Test (Self-test):** Đảm bảo logic chạy đúng và có kiểm thử bao phủ các trường hợp biên.
5.  **Tạo Merge Request nhỏ (MR):** Chuyển sang cho Tester kiểm thử kiểm chứng độc lập.
6.  **Tái cấu trúc nâng cao (Optional Refactor):** 
    *   **a. Clean:** Làm sạch, tối ưu hóa cấu trúc code.
    *   **b. Performance:** Tối ưu hóa hiệu năng nếu cần thiết.

---

## 2.1. Đặt tên đối tượng (Naming Things)

> *“Chỉ có hai điều khó khăn trong Khoa học Máy tính: thu hồi bộ nhớ đệm (cache invalidation) và đặt tên cho các đối tượng.”*  
> — **Phil Karlton**

*   **Tên có ý nghĩa (Meaningful names):**
    *   **Đơn giản:** Sử dụng danh từ thể hiện rõ chức năng/mục tiêu của đối tượng.  
        *Ví dụ:* `DiscountCalculator` thay vì viết tắt mơ hồ.
    *   **Sự phân biệt rõ ràng (Distinction):** Sử dụng các thuộc tính/tiền tố đặc tả cấu trúc thực thi.  
        *Ví dụ:* Interface đặt tên là `Cache`, các lớp triển khai cụ thể sẽ là `LocalCache`, `RemoteCache`.
    *   **Tránh các tên mơ hồ:** Tránh dùng các tên chung chung như `Data`, `Msg1`, `msg2`, `temp`,...
*   **Sử dụng tên có thể phát âm được (Pronounceable names):** Giúp việc thảo luận và trao đổi mã nguồn giữa các thành viên dễ dàng hơn.
*   **Không đặt tiền tố chỉ kiểu dữ liệu/container (No container prefixes):** Ví dụ, không đặt tên là `listUser` mà nên dùng số nhiều như `users`.
*   **Nhất quán trong chính tả (Consistent Spelling):** Sử dụng chung một cách gọi từ ngữ cho cùng một khái niệm trong toàn bộ dự án.

> [!NOTE]
> **Bài tập đặt tên (Exercise):**
> Lựa chọn đặt tên nào tốt hơn cho biến logic check quyền/phạm vi?
> ```java
> boolean scope = checkScope(); 
> // hay
> boolean isScope = checkScope(); // (Tốt hơn vì biến boolean nên đi kèm tiền tố is/has/can)
> ```

---

## 2.2. Hàm & Phương thức (Functions)
*   **Quy tắc Đọc từ trên xuống dưới (Top to Bottom Rule / Tell a Story):** Code nên đọc trôi chảy giống như một cuốn sách từ trên xuống dưới, các hàm phụ trợ nằm bên dưới các hàm gọi chúng.
*   **Chỉ làm một việc duy nhất (Do one thing - SRP):** Giúp hàm dễ bảo trì và tái sử dụng.
    *   Hàm không được có tác dụng phụ ẩn (no side effects).
    *   Áp dụng quy tắc **Tách biệt Lệnh và Truy vấn (Command Query Separation - CQS)**: Một hàm hoặc là thực hiện thay đổi trạng thái (Command), hoặc là trả về kết quả truy vấn (Query), không làm cả hai.
*   **Giữ kích thước hàm nhỏ gọn (Keep it small):** Giúp việc viết kiểm thử đơn giản hơn.
    *   Độ dài tối ưu: $\le 40$ dòng code.
    *   Chỉ nên chứa tối đa 1 khối lệnh `switch-case`.
    *   Số lượng tham số truyền vào: $\le 3$ tham số.
*   **Ưu tiên hàm thuần túy (Pure functions):** Hàm luôn trả về cùng một kết quả cho cùng một tập tham số đầu vào và không gây tác động bên ngoài hệ thống.
*   **Tránh lồng cấu trúc quá sâu (Avoid deep nesting):** Tránh viết các cấu trúc `if-else` lồng nhau quá nhiều tầng (nesting conditions) hoặc lồng callback liên tiếp.
*   **Đừng lặp lại chính mình (Don't Repeat Yourself - DRY):** Triệt tiêu các đoạn mã trùng lặp bằng cách đóng gói chúng thành các hàm dùng chung.

---

## 2.3. Lớp đối tượng (Class)
*   **Giữ lớp nhỏ gọn (Keep it small):** Mỗi lớp chỉ nên đảm nhận một vai trò cụ thể.
*   **Nguyên lý Đơn trách nhiệm (SRP):** Một trong những cách tốt nhất để nhận biết một lớp có đang ôm đồm quá nhiều việc hay không là nhìn vào cách đặt tên của nó. Nếu tên lớp quá chung chung hoặc chứa chữ "And", lớp đó cần được chia nhỏ.
*   **Nguyên lý Đóng/Mở (OCP) - Thiết kế để dễ dàng thay đổi:**
    *   *Kịch bản:* Lớp `A` hiện tại dùng để tạo câu lệnh SQL `SELECT`.
    *   *Yêu cầu mới:* Cần thêm tính năng tạo câu lệnh SQL `UPDATE`.
    *   *Cách giải quyết tốt:* Tránh sửa đổi trực tiếp mã nguồn cốt lõi của lớp `A`. Thay vào đó, hãy thiết kế kế thừa hoặc đa hình (ví dụ: tạo lớp con `B` kế thừa từ `A` hoặc triển khai chung một Interface).

---

## 2.4. Trừu tượng hóa (Abstraction)
Chia nhỏ các tầng trừu tượng để che giấu độ phức tạp chi tiết:
*   **Bước 1:** Chuẩn bị nguyên liệu (`prepare`)
*   **Bước 2:** Nướng bánh (`bake`)
    *   *Chi tiết bước 2:* Làm nóng lò (`heat`) $\rightarrow$ Tiến hành nướng (`bake`)
*   **Bước 3:** Đóng hộp sản phẩm (`box`)

Mã nguồn ở tầng cao chỉ nên gọi các hàm trừu tượng lớn (`prepare()`, `bake()`, `box()`), còn chi tiết triển khai cụ thể sẽ được ẩn ở tầng dưới.

---

## 2.5. Đơn giản hóa luồng điều khiển (Simplify Control Flow)
*   **Trả về sớm (Early returns):** Hãy giải quyết các trường hợp biên (edge/corner cases) hoặc các điều kiện lỗi trước và trả về kết quả ngay lập tức để tránh lồng khối `if-else` vô tận.

---

## 2.6. Xử lý ngoại lệ & Lỗi (Error Handling)
*   Hãy viết cấu trúc `try-catch-finally` trước tiên để định hình phạm vi xử lý lỗi.
*   **Khối lệnh `try` giống như một giao dịch (Transaction):** Các thao tác bên trong khối `try` nên đảm bảo tính toàn vẹn, nếu có lỗi xảy ra thì mọi thay đổi dở dang phải được hủy bỏ.
*   **Khối lệnh `catch` phải để lại hệ thống ở trạng thái nhất quán (Consistent state).**
*   **Cung cấp ngữ cảnh rõ ràng đi kèm ngoại lệ:**
    *   Loại lỗi và mức độ nghiêm trọng (Type of failure, level).
    *   Nơi xảy ra lỗi bằng cú pháp rõ nghĩa: *Chủ ngữ + Động từ + Tân ngữ (Subject + Verb + Object)*.
    *   Lý do xảy ra lỗi (Why it did).
*   **Xử lý lỗi tập trung (Centralized Handling):** Nên gom các logic xử lý lỗi về một nơi tập trung (ví dụ: Global Exception Handler) để tránh lặp lại code try-catch khắp nơi.

### So sánh Checked Exception và Unchecked Exception

| Tiêu chí | Checked Exception (Ngoại lệ kiểm tra) | Unchecked Exception (Ngoại lệ không kiểm tra) |
| :--- | :--- | :--- |
| **Định nghĩa** | - Được kiểm tra tại thời điểm biên dịch (Compile-time).<br>- Đại diện cho các lỗi nằm ngoài tầm kiểm soát của chương trình (như mất kết nối mạng, file không tồn tại).<br>- Bắt buộc phải xử lý bằng `try-catch` hoặc khai báo `throws` ở chữ ký hàm. | - Không được kiểm tra tại thời điểm biên dịch.<br>- Thường phản ánh các lỗi logic nghiệp vụ hoặc lỗi lập trình bên trong hệ thống (như `NullPointerException`, `IndexOutOfBoundsException`).<br>- Không bắt buộc phải khai báo bằng từ khóa `throws`. |
| **Ưu điểm** | - Nhắc nhở lập trình viên bắt buộc phải xử lý kịch bản lỗi.<br>- Giữ vững tính đóng gói. | - Chi phí thấp, giữ code ngắn gọn và dễ đọc. |
| **Nhược điểm** | - Vi phạm Nguyên lý Đóng/Mở (OCP) vì nếu thêm một ngoại lệ mới, toàn bộ chữ ký hàm ở các tầng trên đều phải thay đổi.<br>- Làm hỏng tính đóng gói nếu truyền qua quá nhiều lớp trung gian. | - Dễ bị bỏ quên không xử lý dẫn tới sập ứng dụng đột ngột (crash). |
| **Khi nào dùng**| Sử dụng khi xây dựng các thư viện cốt lõi, quan trọng (Critical Library) cần tính chặt chẽ cao. | Sử dụng khi phát triển các ứng dụng thông thường, cần độ linh hoạt và tối ưu tốc độ code (Robust Apps). |

---

## 2.7. Chú thích & Định dạng (Comments & Formatting)
*   **Chú thích (Comments):**
    *   **Nên dùng (Do):** Giải thích các thuật toán đặc thù phức tạp, viết tài liệu tóm tắt (API docs) trên các interface.
    *   **Không nên dùng (Don't):** Chú thích để giải thích các câu lệnh hoặc phương thức đơn giản. Hãy cố gắng viết mã nguồn tự giải thích (Self-explain code) bằng cách đặt tên hàm/biến rõ ràng.
*   **Định dạng (Formatting):** Thiết lập và sử dụng các công cụ kiểm tra định dạng tự động (Linting) để thống nhất phong cách viết code trong toàn bộ đội ngũ.

---

## 2.8. Kiểm thử (Test)
*   **Chỉ đưa chi tiết liên quan vào kiểm thử:** Tránh làm loãng ca kiểm thử bằng các dữ liệu thiết lập không liên quan.
*   **Hạn chế lạm dụng Mock:** Việc giả lập (mocking) quá đà sẽ làm mất đi tính chân thực của bài test và khiến test dễ bị vỡ khi cấu trúc code thay đổi.

---

## 2.9. Ranh giới hệ thống (Boundary)
*   Ranh giới (Boundary) là nơi mã nguồn của bạn tiếp xúc và làm việc với mã nguồn bên ngoài (thư viện bên thứ ba, API hệ thống khác). Ranh giới là một vùng không ổn định và khó dự đoán.
*   **Tránh truyền các đối tượng bên thứ ba đi khắp nơi trong hệ thống nội bộ của bạn.**
*   Không nên trả về trực tiếp hoặc nhận đối tượng bên thứ ba làm đối số trong các API public của hệ thống.
*   **Sử dụng Adaptor làm vùng đệm:**
    *   Hạn chế tối đa sự phụ thuộc trực tiếp vào phần mềm bên ngoài.
    *   Bảo trì hệ thống trong tương lai dễ dàng hơn vì khi thư viện ngoài thay đổi, bạn chỉ cần sửa đổi lớp Adapter.
*   **Viết các ca kiểm thử tìm hiểu (Learning Tests):** Viết unit test để kiểm chứng và củng cố hiểu biết của bạn về cách hoạt động của thư viện bên thứ ba trước khi tích hợp chính thức.

---

## 2.10. Tái cấu trúc mã nguồn (Refactoring)
*   **Các kỹ thuật chính:**
    *   **Composing Methods:** Gom nhóm và tổ chức lại các phương thức.
    *   **Moving features between objects:** Di chuyển hợp lý các tính năng giữa các đối tượng để tránh việc một lớp bị phình to.
    *   **Organizing Data:** Tổ chức lại cách cấu trúc dữ liệu.
    *   **Dealing with generalization:** Xử lý mối quan hệ kế thừa và khái quát hóa dữ liệu.
*   **Thực hành tốt nhất (Best Practices):** Tách biệt cấu trúc thành các lớp chuyên sâu (khoảng 3 lớp chuyên sâu), chia nhỏ file (khoảng 10 files) và luôn đảm bảo có Unit Tests đi kèm để tránh làm hỏng logic cũ.

---

# 3. Đánh giá mã nguồn (Code Review)

> [!TIP]
> **Bộ câu hỏi đánh giá nhanh trước khi bàn giao code:**
> 1.  Code nhìn có đẹp mắt không? (*Is She Pretty?*)
> 2.  Code nhìn có sạch sẽ, gọn gàng không? (*Is It Clean, Neat?*)
> 3.  Code đã thực sự tối ưu và sạch chưa? (*Is This Clean?*)
> 
> *Nếu tất cả đều đạt $\rightarrow$ Chúc mừng! Bạn có thể viết Clean Code rồi đó!*

---

## 3.1. Mục tiêu của Code Review (The Goal of Code Review)
*   **Kiểm soát chất lượng (Quality Control):**
    *   Nâng cao chất lượng của sản phẩm đầu ra (Products).
    *   Giữ gìn sự trong sạch, nhất quán của kho mã nguồn (Codebase).
*   **Phát triển bản thân (Self Growth):**
    *   Nâng cao kiến thức chuyên môn (Hard Skills).
    *   Rèn luyện các kỹ năng mềm (Soft Skills) như thuyết trình bảo vệ quan điểm (Presentation) và lắng nghe phản biện (Listening).

---

## 3.2. Các khía cạnh cần Review (Code Review Points)
Khi tiến hành review, cần tập trung đánh giá các điểm sau:
*   **Chức năng (Functionality):** Code có chạy đúng yêu cầu nghiệp vụ đề ra không?
*   **Thiết kế (Design):** Kiến trúc code có hợp lý không? Có áp dụng đúng Design Patterns hay SOLID không?
*   **Độ dễ đọc (Readability):** Code có dễ hiểu không? Đặt tên biến/hàm đã rõ nghĩa chưa?
*   **Tính nhất quán (Consistency):** Có tuân thủ đúng Coding Conventions của dự án không?
*   **Kiểm thử (Testing):** Đã viết Unit Test đầy đủ chưa? Các test case có bao phủ hết các trường hợp biên không?
*   **Tài liệu (Documentation):** API docs hoặc các chú thích kỹ thuật cần thiết đã được cập nhật chưa?
*   **Khen ngợi điểm tốt (Good Things):** Đừng quên dành lời khen cho những giải pháp hay hoặc đoạn code thông minh của đồng nghiệp.

---

## 3.3. Quy trình Code Review (Code Review Process)
*   **Các hình thức review:**
    *   *Peer Review (Review cặp/ngang hàng):* Áp dụng cho các thay đổi nhỏ (Minor Changes).
    *   *Group Review (Review nhóm):* Áp dụng cho các thay đổi lớn, ảnh hưởng đến kiến trúc hệ thống (Major Changes).
*   **Quy trình 7 bước chuẩn:**
    1.  **Hoàn thành:** Lập trình viên (Reviewee) hoàn thành tính năng hoặc sửa xong lỗi.
    2.  **Tạo MR:** Tạo Merge Request (MR) có kèm theo mô tả ngữ cảnh, mục đích của thay đổi.
    3.  **Tự kiểm tra:** Đảm bảo mã nguồn đã được biên dịch thành công và vượt qua tất cả các bài test tự động dưới local.
    4.  **Thông báo:** Lập trình viên thông báo cho những người review (Reviewers) để lên lịch review.
    5.  **Lập danh sách:** Người review lập danh sách kiểm tra nhanh (Checklist) dựa trên các yêu cầu.
    6.  **Sửa đổi (Tùy chọn):** Lập trình viên sửa đổi các vấn đề đã được cả hai bên thống nhất.
    7.  **Phê duyệt:** Người review phê duyệt (Approve) Merge Request để tiến hành merge code.

---

## 3.4. Thái độ khi Code Review (Attitude)
*   **Lịch sự (Polite):** Luôn dùng từ ngữ tôn trọng đồng nghiệp.
*   **Mang tính đóng góp (Contribution):** Hướng tới mục tiêu chung là làm cho sản phẩm tốt lên.
*   **Chia sẻ và hướng dẫn thay vì chỉ trích (Education without criticism).**

---

## 3.5. Cách viết bình luận Code Review (How to Write Comments)
*   Giữ thái độ lịch sự.
*   **Giải thích lý do:** Không chỉ nói "sửa chỗ này", hãy giải thích *tại sao* cần sửa.
*   **Gợi ý giải pháp:** Đưa ra các hướng tiếp cận hoặc đoạn code mẫu cụ thể để đồng nghiệp tham khảo.
*   **Sử dụng nhãn phân loại (Labeling comments) để giảm áp lực cho người nhận:**
    *   `[TODO] (Critical):` Điểm nghiêm trọng, bắt buộc phải sửa trước khi merge.
    *   `[NIT] (Nitpick):` Lỗi nhỏ nhặt (như khoảng trắng, định dạng), sửa thì tốt nhưng không bắt buộc.
    *   `[OPTIONAL]:` Gợi ý cải tiến tùy chọn, khuyến khích làm theo.
    *   `[FYI] (For Your Information):` Cung cấp thêm thông tin hữu ích để tham khảo, không cần sửa đổi gì.

---

## 3.6. Thực hành tốt nhất (Best Practices)
*   **Ít hơn là nhiều hơn (Less is more):** Commit nhỏ $\rightarrow$ Review nhanh $\rightarrow$ Code chất lượng hơn.
    *   *Tiêu chuẩn tại Google:* ~250 dòng code cho mỗi lần Code Review.
    *   *Khuyến nghị chung:* Không nên vượt quá 400 dòng code mỗi lần review.
*   Sử dụng Checklist để tránh bỏ sót lỗi.
*   Tận dụng tối đa các công cụ tự động quét lỗi tĩnh (Static Code Analysis) trước khi review thủ công.
*   Đẩy nhanh tốc độ review để tránh làm tắc nghẽn tiến độ phát triển của nhóm.

---

## 3.7. Giải quyết xung đột ý kiến (Resolving Conflicts)
Khi người viết và người review không đạt được sự đồng thuận:
1.  Lắng nghe và thấu hiểu quan điểm của cả hai bên.
2.  Phân tích kỹ lưỡng ưu & nhược điểm của từng giải pháp.
3.  **Xác định ngữ cảnh cụ thể:** Đặt thứ tự ưu tiên cho các đặc tính hệ thống tại thời điểm hiện tại (ví dụ: tốc độ ra mắt quan trọng hơn hay hiệu năng tối ưu quan trọng hơn).
4.  Lựa chọn giải pháp phù hợp với ngữ cảnh:
    *   Làm PoC (Proof of Concept) chạy thử để so sánh số liệu thực tế.
    *   Đưa vấn đề ra thảo luận và lấy ý kiến của:
        *   Toàn bộ đội ngũ (Whole team).
        *   Tech Lead / Solution Architect (SA).
        *   Trưởng phòng kỹ thuật (Head of Engineering).
5.  *Nếu vẫn không tìm được tiếng nói chung?* Hãy tuân thủ quyết định của người có thẩm quyền cao nhất để dự án tiếp tục tiến lên.

---

# Tóm tắt & Tài liệu tham khảo (Recap & References)

### Tóm tắt cốt lõi (Recap)
*   **Bộ tứ vàng (Fourable):** Hãy luôn hướng tới việc viết code **Dễ đọc (Readable)**, **Dễ bảo trì (Maintainable)**, **Dễ kiểm thử (Testable)** và **Dễ mở rộng (Scalable)**.
*   Sử dụng các tầng trừu tượng phù hợp để kiểm soát và làm giảm độ phức tạp của hệ thống.
*   Sự bất nhất quán (Inconsistency) trong phong cách viết code sẽ tạo ra những rủi ro tiềm ẩn khôn lường (Unknown unknowns).
*   **Đọc nhiều, thực hành nhiều và liên tục cải thiện.**
*   Clean Code không phải là đích đến cuối cùng hay mục tiêu duy nhất. Hãy luôn cân nhắc và hài hòa với các yếu tố thực tế khác như: tốc độ đưa sản phẩm ra thị trường, tính cộng tác nhóm và giới hạn tài nguyên.

### Tài liệu tham khảo (References)
*   **2 Hard Things in Computer Science:** [TwoHardThings - Martin Fowler](https://martinfowler.com/bliki/TwoHardThings.html)
*   **Quotes:** Quotes on Design
*   **Nguyên lý SOLID:** [SOLID Principles Explained in Plain English](https://www.freecodecamp.org/news/solid-principles-explained-in-plain-english/)
*   **Nguyên lý KISS:** [KISS Principle by Apache](https://people.apache.org/~fhanik/kiss.html)
*   **Sách hữu ích:**
    *   [Clean Code Book - Robert C. Martin](https://libgen.rocks/ads.php?md5=838cc6ac8cb0d8ddb98fdb1ae0c8a443)
    *   [Best Tips and Tricks in the World of Clean Coding](https://libgen.rocks/ads.php?md5=481473f6104d2f187dfcc11c340ca4f3)
    *   [Google Testing Blog: Write Clean Code to Reduce Cognitive Load](https://testing.googleblog.com/2023/11/write-clean-code-to-reduce-cognitive.html)
*   **Video Talks:**
    *   [Clean Code - Uncle Bob - Lesson 1](https://www.youtube.com/watch?v=YtQGQ9Eg0Lo)
    *   [Clean Code - Uncle Bob - Lesson 2](https://www.youtube.com/watch?v=UjhX2sVf0eg)
    *   [Clean Code - Uncle Bob - Lesson 3](https://www.youtube.com/watch?v=c40HPauhawQ)

---

# Bài tập về nhà (Homework)
*   **Yêu cầu:** Tiến hành tái cấu trúc (refactor) và trình bày ít nhất **300 dòng code** từ bất kỳ dự án nào của bạn để minh họa việc áp dụng các nguyên lý mã sạch.
*   **Điểm cộng (Nice to have):**
    *   Sử dụng nhiều lớp đối tượng tương tác với nhau (Multiple classes).
    *   Chia nhỏ các hàm/phương thức một cách hợp lý (Functions).
    *   Thể hiện logic nghiệp vụ thực tế hoặc thuật toán phức tạp (Domain business or complex logic).
    *   **Càng ít chú thích càng tốt** (Hãy để bản thân mã nguồn tự giải thích logic).
*   **Lưu ý quan trọng (Note):**
    *   Không bao gồm các đoạn code tầm thường, không mang tính tư duy (như getter, setter tự động sinh, boilerplate code...).
    *   **Tuyệt đối không** đưa thông tin nhạy cảm (secrets, keys, business nhạy cảm...) của công ty hoặc dự án thật vào bài làm.

**Cảm ơn bạn! (Thank you)**
