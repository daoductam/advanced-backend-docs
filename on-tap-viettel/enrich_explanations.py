import json
import re

json_path = r"d:\backend_docs\on-tap-viettel\exams_data.json"

with open(json_path, "r", encoding="utf-8") as f:
    exams = json.load(f)

# Let's define a smart dictionary of detailed technical explanations for key terms
tech_explanations = {
    # Programming & Data Structures
    "list = []": "Trong Python, cú pháp `list = []` là cách tạo một danh sách (list) rỗng phổ biến và tối ưu nhất. Bạn cũng có thể dùng `list()`, nhưng dùng cặp ngoặc vuông `[]` nhanh hơn về hiệu năng vì nó được tối ưu hóa trực tiếp ở cấp độ bytecode. Cú pháp `list = {}` tạo một dictionary rỗng, còn `list = ()` tạo một tuple rỗng.",
    "Tính đóng gói, tính kế thừa, tính đa hình, tính trừu tượng": "Lập trình hướng đối tượng (OOP) dựa trên 4 tính chất cơ bản: 1. Tính đóng gói (Encapsulation - che giấu thông tin và dữ liệu), 2. Tính kế thừa (Inheritance - kế thừa thuộc tính/phương thức từ lớp cha), 3. Tính đa hình (Polymorphism - một phương thức có nhiều dạng triển khai), 4. Tính trừu tượng (Abstraction - ẩn chi tiết phức tạp, chỉ hiển thị giao diện cần thiết).",
    "Hàng đợi": "Thuật toán tìm kiếm theo chiều rộng (BFS - Breadth-First Search) duyệt qua các đỉnh theo từng lớp (level). Do đó, cấu trúc dữ liệu Hàng đợi (Queue) hoạt động theo nguyên lý FIFO (First-In, First-Out - Vào trước ra trước) là lựa chọn hoàn hảo để lưu trữ các đỉnh đang chờ duyệt, đảm bảo các đỉnh gần đỉnh gốc hơn luôn được duyệt trước.",
    "Ngăn xếp": "Thuật toán tìm kiếm theo chiều sâu (DFS - Depth-First Search) đi sâu nhất có thể theo một nhánh trước khi quay lui. Vì vậy, cấu trúc dữ liệu Ngăn xếp (Stack) hoạt động theo nguyên lý LIFO (Last-In, First-Out - Vào sau ra trước) được sử dụng để theo dõi các đỉnh đang duyệt. Điều này có thể được triển khai bằng đệ quy (hệ thống tự dùng Call Stack) hoặc cấu trúc dữ liệu Stack tường minh.",
    "Một hàm gọi chính nó để giải quyết một bài toán.": "Đệ quy (Recursion) là một kỹ thuật lập trình trong đó một hàm tự gọi lại chính nó trực tiếp hoặc gián tiếp để giải quyết bài toán lớn bằng cách chia nhỏ nó thành các bài toán con tương tự. Mỗi hàm đệ quy bắt buộc phải có hai thành phần chính: Điều kiện dừng (Base case) để tránh vòng lặp vô hạn và phần gọi đệ quy hướng về phía điều kiện dừng.",
    "Biểu diễn độ phức tạp thời gian và không gian của thuật toán.": "Ký hiệu Big O (Big O Notation) được sử dụng trong phân tích thuật toán để biểu diễn cận trên (giới hạn tối đa) của độ phức tạp thời gian (thời gian thực thi) hoặc độ phức tạp không gian (dung lượng bộ nhớ tiêu thụ) khi kích thước dữ liệu đầu vào (n) tiến tới vô cùng. Nó giúp lập trình viên so sánh hiệu năng của các thuật toán một cách độc lập với phần cứng.",
    "instance": "Trong các phương thức của một lớp (class) trong Python, tham số đầu tiên thường được đặt tên là `self`. Tham số này đại diện cho chính thể hiện (instance) cụ thể của lớp đang được tạo ra và thao tác. Nó cho phép bạn truy cập vào các thuộc tính và phương thức thuộc về đối tượng đó.",
    "Set": "Cấu trúc dữ liệu Tập hợp (Set) trong hầu hết các ngôn ngữ lập trình được triển khai bằng Bảng băm (Hash Table). Do đó, việc tìm kiếm, thêm mới hoặc kiểm tra sự tồn tại (sự trùng lặp) của một phần tử có thể được thực hiện trong thời gian O(1) trung bình, vì nó tính toán vị trí lưu trữ trực tiếp từ giá trị băm của phần tử đó.",
    "Quản lý các thay đổi của mã nguồn và cho phép cộng tác hiệu quả.": "Hệ thống kiểm soát phiên bản (Version Control System - VCS) như Git giúp ghi lại lịch sử thay đổi của các tệp nguồn theo thời gian. Lợi ích lớn nhất của nó là cho phép nhiều lập trình viên cùng làm việc trên một dự án mà không sợ đè mã nguồn lên nhau, dễ dàng quay lại các phiên bản cũ khi xảy ra lỗi, và quản lý các nhánh phát triển (branches) một cách độc lập.",
    "final": "Trong Java, từ khóa `final` áp dụng cho phương thức (method) có nghĩa là phương thức đó không thể bị ghi đè (override) ở bất kỳ lớp con nào. Điều này thường được sử dụng khi bạn muốn bảo vệ tính toàn vẹn của một logic nghiệp vụ quan trọng trong lớp cha, không cho phép các lớp con thay đổi hành vi của nó.",
    "Mặc định, thành viên của struct là public và của class là private.": "Trong C++, sự khác biệt kỹ thuật duy nhất giữa `struct` và `class` là phạm vi truy cập mặc định của các thành viên (biến và hàm) cũng như kiểu kế thừa mặc định. Đối với `struct`, phạm vi truy cập mặc định là `public`. Đối với `class`, phạm vi truy cập mặc định là `private` nhằm hỗ trợ tốt hơn cho tính đóng gói (encapsulation).",

    # Database & SQL
    "SELECT": "Trong ngôn ngữ truy vấn cấu trúc SQL, câu lệnh `SELECT` là câu lệnh cơ bản và quan trọng nhất dùng để truy vấn và lấy dữ liệu từ một hoặc nhiều bảng trong cơ sở dữ liệu. Bạn có thể kết hợp `SELECT` với các mệnh đề như `WHERE` để lọc dữ liệu, `ORDER BY` để sắp xếp, hoặc `JOIN` để kết hợp các bảng.",
    "Không được chứa giá trị NULL.": "Khóa chính (Primary Key) trong cơ sở dữ liệu quan hệ được dùng để định danh duy nhất mỗi bản ghi trong bảng. Để đảm bảo tính toàn vẹn thực thể (Entity Integrity), khóa chính bắt buộc phải thỏa mãn hai điều kiện: Các giá trị trong cột khóa chính phải là duy nhất (Unique) và tuyệt đối không được chứa giá trị rỗng (NOT NULL).",
    "Giảm sự dư thừa dữ liệu và cải thiện tính toàn vẹn.": "Chuẩn hóa cơ sở dữ liệu (Database Normalization) là quá trình tổ chức cấu trúc bảng và các mối quan hệ nhằm giảm thiểu tối đa sự dư thừa dữ liệu (redundancy) và loại bỏ các dị thường (anomalies) khi thêm, sửa, xóa dữ liệu. Việc này giúp cải thiện tính toàn vẹn của dữ liệu và tiết kiệm không gian lưu trữ hệ thống.",
    "Kết hợp các hàng từ hai hoặc nhiều bảng dựa trên một cột liên quan.": "Câu lệnh `JOIN` trong SQL được sử dụng để kết hợp và truy vấn dữ liệu từ hai hoặc nhiều bảng khác nhau dựa trên mối quan hệ logic giữa các cột của các bảng đó (thường là mối quan hệ giữa Khóa ngoại của bảng này và Khóa chính của bảng kia). Các kiểu JOIN phổ biến bao gồm INNER JOIN, LEFT JOIN, RIGHT JOIN, và FULL JOIN.",
    "NoSQL": "MongoDB là một hệ quản trị cơ sở dữ liệu phi quan hệ (NoSQL) hướng tài liệu (Document-oriented). Thay vì lưu trữ dữ liệu dưới dạng bảng và hàng như RDBMS truyền thống, MongoDB lưu trữ dữ liệu dưới dạng các tài liệu linh hoạt giống JSON (gọi là BSON), cho phép mở rộng quy mô ngang (horizontal scaling) rất tốt và phù hợp với dữ liệu phi cấu trúc.",
    "VARCHAR sử dụng ít bộ nhớ hơn cho các chuỗi ngắn.": "Trong SQL, `CHAR` lưu trữ chuỗi có độ dài cố định, hệ thống sẽ chèn thêm khoảng trắng nếu chuỗi ngắn hơn độ dài khai báo, dẫn đến lãng phí bộ nhớ. Ngược lại, `VARCHAR` lưu trữ chuỗi có độ dài thay đổi, chỉ chiếm lượng bộ nhớ bằng đúng độ dài thực tế của chuỗi cộng thêm một vài byte quản lý, giúp tối ưu hóa bộ nhớ cho các chuỗi có độ dài không đồng đều.",
    "Atomicity, Consistency, Isolation, Durability": "Thuộc tính ACID là nền tảng bảo đảm tính tin cậy của giao dịch (transaction) trong cơ sở dữ liệu: 1. Atomicity (Tính nguyên tử - giao dịch thành công hoàn toàn hoặc thất bại hoàn toàn), 2. Consistency (Tính nhất quán - đưa DB từ trạng thái hợp lệ này sang trạng thái hợp lệ khác), 3. Isolation (Tính cô lập - các giao dịch chạy song song không ảnh hưởng lẫn nhau), 4. Durability (Tính bền vững - dữ liệu được lưu vĩnh viễn sau khi commit thành công).",
    "Xóa hoàn toàn một bảng và tất cả dữ liệu của nó.": "Trong SQL, lệnh `DROP TABLE` là lệnh thuộc nhóm DDL (Data Definition Language) dùng để xóa hoàn toàn định nghĩa cấu trúc của bảng, các mối quan hệ, các ràng buộc và toàn bộ dữ liệu hiện có trong bảng đó ra khỏi hệ thống. Hành động này không thể hoàn tác bằng lệnh ROLLBACK thông thường, khác với `TRUNCATE` hay `DELETE` chỉ xóa dữ liệu bên trong.",
    "Thiết lập mối quan hệ giữa hai bảng.": "Khóa ngoại (Foreign Key) là một cột hoặc nhóm cột trong một bảng dùng để trỏ tới Khóa chính (Primary Key) của một bảng khác. Vai trò cốt lõi của khóa ngoại là thiết lập và ràng buộc mối quan hệ logic giữa hai bảng, đồng thời bảo đảm tính toàn vẹn tham chiếu (Referential Integrity) - ngăn chặn việc tạo bản ghi mồ côi hoặc xóa bản ghi đang được tham chiếu.",
    "Tăng tốc độ truy vấn dữ liệu.": "Chỉ mục (Index) trong cơ sở dữ liệu hoạt động giống như mục lục của một cuốn sách. Nó tạo ra một cấu trúc dữ liệu phụ (thường là B-Tree hoặc Hash) để giúp hệ quản trị cơ sở dữ liệu tìm kiếm và truy xuất các bản ghi một cách cực kỳ nhanh chóng mà không cần phải quét toàn bộ bảng (Full Table Scan), qua đó tăng tốc độ truy vấn đáng kể.",

    # Network & Protocols
    "Application, Transport, Internet, Network Access": "Mô hình TCP/IP chuẩn gồm 4 tầng chính: 1. Tầng Ứng dụng (Application Layer - HTTP, FTP, DNS), 2. Tầng Giao vận (Transport Layer - TCP, UDP), 3. Tầng Mạng/Internet (Internet Layer - IP, ICMP), 4. Tầng Truy cập mạng (Network Access Layer - Ethernet, Wi-Fi). Mô hình này đơn giản hóa mô hình OSI 7 tầng để áp dụng trực tiếp vào mạng Internet toàn cầu.",
    "Network Layer": "Trong mô hình OSI 7 tầng, tầng Mạng (Network Layer - Tầng 3) chịu trách nhiệm định tuyến (routing) các gói tin (packets) đi qua nhiều mạng khác nhau từ nguồn tới đích. Thiết bị hoạt động chính ở tầng này là Router, sử dụng địa chỉ logic như địa chỉ IP để quyết định đường đi tối ưu nhất cho gói tin.",
    "Application Layer": "Giao thức truyền siêu văn bản HTTP (Hypertext Transfer Protocol) hoạt động ở tầng Ứng dụng (Application Layer) trong mô hình TCP/IP (hoặc tầng 7 của mô hình OSI). Tầng này trực tiếp giao tiếp và cung cấp dịch vụ cho các ứng dụng của người dùng như trình duyệt web (Chrome, Edge) để tải và hiển thị trang web.",
    "HTTPS có lớp mã hóa SSL/TLS để bảo mật dữ liệu.": "Sự khác biệt cốt lõi giữa HTTP và HTTPS (Secure) là tính bảo mật. HTTPS sử dụng giao thức mật mã SSL hoặc TLS để mã hóa toàn bộ luồng dữ liệu truyền qua giữa trình duyệt và máy chủ. Điều này ngăn chặn kẻ tấn công nghe lén (eavesdropping) hoặc giả mạo dữ liệu nhạy cảm (như mật khẩu, thông tin thẻ tín dụng) trên đường truyền.",
    "Ngăn chặn truy cập trái phép và kiểm soát luồng dữ liệu mạng.": "Tường lửa (Firewall) là một thiết bị bảo mật mạng phần cứng hoặc phần mềm có vai trò giám sát, lọc và kiểm soát toàn bộ lưu lượng mạng ra/vào hệ thống dựa trên một tập hợp các quy tắc bảo mật được thiết lập trước. Nó đóng vai trò như lá chắn ngăn chặn các truy cập trái phép từ bên ngoài mạng internet vào mạng nội bộ.",
    "Định danh duy nhất cho một thiết bị trên mạng.": "Địa chỉ IP (Internet Protocol Address) là một nhãn số được gán cho mỗi thiết bị (máy tính, điện thoại, máy in) tham gia vào một mạng máy tính sử dụng giao thức IP để giao tiếp. Vai trò chính của địa chỉ IP là định danh duy nhất thiết bị đó trong mạng và cung cấp vị trí của nó để thiết lập đường truyền dữ liệu.",
    "Lừa người dùng tiết lộ thông tin nhạy cảm bằng cách giả mạo một thực thể đáng tin cậy.": "Tấn công Phishing (Tấn công giả mạo) là một kỹ thuật tấn công phi kỹ thuật (Social Engineering) trong đó kẻ tấn công giả mạo một tổ chức uy tín (như ngân hàng, Viettel, Google) gửi email, tin nhắn hoặc thiết lập trang web giả lập giống hệt trang thật để dụ dỗ người dùng tự nguyện tiết lộ các thông tin nhạy cảm như tài khoản mật khẩu hoặc số thẻ.",
    "SFTP": "SFTP (SSH File Transfer Protocol) là giao thức truyền tải tệp tin an toàn chạy trên nền tảng giao thức SSH (thường là cổng 22). Khác với FTP truyền thống truyền dữ liệu dưới dạng văn bản thuần (plaintext) dễ bị nghe lén, SFTP mã hóa cả thông tin xác thực lẫn dữ liệu truyền đi, đảm bảo an toàn tuyệt đối.",
    "Dịch tên miền thành địa chỉ IP.": "Hệ thống phân giải tên miền DNS (Domain Name System) hoạt động như một danh bạ điện thoại khổng lồ của Internet. Con người dễ nhớ các địa chỉ dạng chữ như `viettel.com.vn`, nhưng máy tính chỉ hiểu các địa chỉ số IP dạng `203.113.137.1`. DNS chịu trách nhiệm dịch các tên miền thân thiện này thành các địa chỉ IP số để máy tính kết nối.",
    "Biến đổi dữ liệu thành một dạng không thể đọc được nếu không có khóa.": "Mã hóa (Encryption) là quá trình chuyển đổi thông tin từ dạng văn bản rõ (plaintext) sang dạng bản mã (ciphertext) không thể đọc được bằng các thuật toán toán học. Chỉ những người sở hữu khóa giải mã hợp lệ mới có thể dịch ngược bản mã về dạng ban đầu, giúp bảo mật dữ liệu tuyệt đối khi lưu trữ hoặc truyền dẫn.",

    # OS & DevOps
    "Liệt kê các tệp tin và thư mục ở chế độ chi tiết.": "Trong hệ điều hành Linux/Unix, lệnh `ls` dùng để liệt kê danh sách tệp và thư mục. Khi thêm tùy chọn `-l` (long format), lệnh sẽ hiển thị chi tiết các thông tin bao gồm: quyền truy cập (permissions), số lượng liên kết cứng, chủ sở hữu (owner), nhóm sở hữu (group), dung lượng tệp tin (size), và thời gian chỉnh sửa cuối cùng.",
    "Một nền tảng để đóng gói ứng dụng và các phụ thuộc vào một container.": "Docker là một nền tảng mã nguồn mở cho phép lập trình viên tự động hóa việc đóng gói, triển khai và quản lý ứng dụng bên trong các container nhẹ và độc lập. Mỗi container chứa đầy đủ mã nguồn ứng dụng, thư viện, biến môi trường và các phụ thuộc cần thiết để ứng dụng có thể chạy một cách nhất quán trên bất kỳ hệ thống nào.",
    "Đảm bảo ứng dụng chạy nhất quán trên mọi môi trường.": "Lợi ích lớn nhất của container (như Docker) là giải quyết triệt để vấn đề kinh điển của lập trình viên: 'Ứng dụng chạy tốt trên máy của tôi nhưng lỗi khi lên server'. Bằng cách đóng gói tất cả phụ thuộc vào một image duy nhất, container đảm bảo ứng dụng sẽ hoạt động đồng nhất 100% trên môi trường phát triển (development), kiểm thử (staging), và vận hành thật (production).",
    "DevOps.": "CI/CD (Continuous Integration / Continuous Delivery - Tích hợp liên tục / Bàn giao liên tục) là một triết lý và bộ thực hành cốt lõi trong DevOps. Nó tự động hóa quy trình từ lúc lập trình viên commit mã nguồn, chạy bộ test tự động, xây dựng ứng dụng (build), cho đến việc tự động triển khai (deploy) sản phẩm lên môi trường vận hành, giúp giảm thời gian đưa sản phẩm ra thị trường.",
    "Tăng tốc độ và độ tin cậy của quy trình phát triển và triển khai phần mềm.": "Trong triết lý DevOps, tự động hóa (Automation) đóng vai trò then chốt giúp loại bỏ các thao tác thủ công dễ xảy ra sai sót của con người. Tự động hóa các khâu kiểm thử, đóng gói, cấu hình máy chủ và triển khai giúp đẩy nhanh tốc độ phân phối sản phẩm, đồng thời tăng tính ổn định, độ tin cậy và khả năng lặp lại của toàn bộ quy trình.",
    "Sao chép một kho lưu trữ từ xa xuống máy tính cục bộ.": "Lệnh `git clone` trong Git được sử dụng để tải toàn bộ bản sao của một kho lưu trữ mã nguồn từ xa (remote repository trên GitHub, GitLab, Bitbucket) xuống máy tính cá nhân của bạn (local machine). Nó không chỉ tải các tệp hiện tại mà còn sao chép toàn bộ lịch sử commit, các nhánh (branches) và các thẻ (tags) của dự án.",
    "Định nghĩa cách xây dựng một Docker image.": "Dockerfile là một tệp văn bản dạng kịch bản chứa một chuỗi các câu lệnh và chỉ dẫn tuần tự (ví dụ: FROM, RUN, COPY, EXPOSE, CMD) mà Docker daemon sẽ đọc để tự động xây dựng nên một Docker Image. Từ image này, bạn có thể khởi tạo ra nhiều Docker container chạy ứng dụng một cách dễ dàng.",
    "Thay đổi quyền truy cập của một tệp tin hoặc thư mục.": "Trong hệ điều hành Linux/Unix, lệnh `chmod` (change mode) được sử dụng để thay đổi quyền đọc (read - r), ghi (write - w), và thực thi (execute - x) của tệp tin hoặc thư mục đối với ba nhóm đối tượng: chủ sở hữu (user), nhóm sở hữu (group), và những người dùng khác (others). Ví dụ: `chmod 755 file.txt` gán toàn quyền cho chủ sở hữu và quyền đọc/thực thi cho các nhóm còn lại.",
    "Tự động hóa việc cấu hình và quản lý các máy chủ.": "Các hệ thống quản lý cấu hình (Configuration Management) như Ansible hay Puppet cho phép định nghĩa cơ sở hạ tầng dưới dạng mã (Infrastructure as Code - IaC). Thay vì phải SSH vào từng máy chủ để cài đặt phần mềm và thiết lập cấu hình thủ công, bạn chỉ cần viết các file kịch bản (playbooks) và chạy lệnh để tự động áp đặt cấu hình nhất quán lên hàng trăm máy chủ cùng lúc.",
    "Một phương pháp luận tập trung vào sự hợp tác và thích ứng với thay đổi.": "Agile là một phương pháp luận phát triển phần mềm linh hoạt, tập trung vào việc bàn giao sản phẩm sớm và thường xuyên theo các chu kỳ ngắn (Sprints). Agile đặt con người và sự tương tác lên trên quy trình, khuyến khích sự hợp tác chặt chẽ với khách hàng và sẵn sàng đón nhận, thích ứng nhanh chóng với các thay đổi yêu cầu trong quá trình phát triển.",

    # Software Engineering & Arch
    "Application Program Interface": "API (Application Programming Interface - Giao diện lập trình ứng dụng) là một tập hợp các quy tắc, định nghĩa và giao thức cho phép các phần mềm hoặc hệ thống khác nhau có thể trực tiếp giao tiếp, trao đổi dữ liệu và tương tác với nhau. Nó hoạt động như một cầu nối trung gian giúp nhà phát triển tái sử dụng các chức năng của hệ thống khác mà không cần biết cách họ triển khai chi tiết.",
    "Giảm chi phí phần cứng và mở rộng linh hoạt.": "Điện toán đám mây (Cloud Computing) cho phép doanh nghiệp thuê tài nguyên công nghệ thông tin (máy chủ, lưu trữ, cơ sở dữ liệu) qua Internet theo mô hình trả tiền theo nhu cầu (Pay-as-you-go). Điều này giúp loại bỏ chi phí đầu tư ban đầu khổng lồ cho phần cứng vật lý, đồng thời cho phép tự động tăng/giảm quy trình tài nguyên (scaling) cực kỳ linh hoạt theo lưu lượng người dùng thực tế.",
    "Phần giao diện người dùng mà người dùng tương tác trực tiếp.": "Trong phát triển web, Frontend (phía client) là toàn bộ những thành phần hiển thị trực quan trên màn hình mà người dùng cuối có thể nhìn thấy và tương tác trực tiếp (ví dụ: các nút bấm, biểu mẫu, menu, hoạt ảnh). Nó được xây dựng bằng các công nghệ cốt lõi là HTML (cấu trúc), CSS (giao diện, kiểu dáng), và JavaScript (logic tương tác).",
    "HTTP": "RESTful API là một phong cách kiến trúc thiết kế API phổ biến nhất hiện nay, dựa trên việc sử dụng các phương thức chuẩn của giao thức HTTP (như GET để lấy dữ liệu, POST để tạo mới, PUT để cập nhật, DELETE để xóa) kết hợp với các địa chỉ URL đại diện cho các tài nguyên (resources), giúp việc thiết kế hệ thống trở nên nhất quán và dễ tích hợp.",
    "Phát triển ứng dụng mà không cần quản lý máy chủ.": "Điện toán không máy chủ (Serverless Computing) là một mô hình thực thi điện toán đám mây trong đó nhà cung cấp dịch vụ đám mây (như AWS Lambda, Google Cloud Functions) chịu trách nhiệm hoàn toàn việc quản lý hạ tầng, cấp phát tài nguyên và chạy mã nguồn của bạn. Lập trình viên chỉ cần tập trung viết mã nguồn và chỉ bị tính phí theo đúng thời gian thực tế mà mã được kích hoạt chạy.",
    "Ứng dụng được chia thành nhiều dịch vụ độc lập, nhỏ gọn.": "Kiến trúc Microservices (Viên dịch vụ) chia ứng dụng lớn thành một tập hợp các dịch vụ nhỏ, độc lập và có tính liên kết yếu (loosely coupled). Mỗi dịch vụ chịu trách nhiệm xử lý một nghiệp vụ cụ thể, có cơ sở dữ liệu riêng, hoạt động độc lập và giao tiếp với nhau qua các giao thức nhẹ như HTTP REST hoặc Message Broker, giúp hệ thống dễ dàng mở rộng và bảo trì.",
    "Cho phép chương trình không bị chặn trong khi chờ đợi một tác vụ I/O hoàn thành.": "Lập trình bất đồng bộ (Asynchronous Programming) cho phép luồng thực thi chính của chương trình có thể tiếp tục thực hiện các tác vụ khác trong khi chờ đợi một tác vụ tốn thời gian hoàn thành (như truy vấn database, đọc ghi file, hoặc gọi API qua mạng). Điều này giúp cải thiện đáng kể hiệu năng ứng dụng, tối ưu hóa tài nguyên phần cứng và tránh tình trạng treo giao diện ứng dụng.",
    "Sử dụng Prepared Statements hoặc Parameterized Queries.": "Tấn công SQL Injection xảy ra khi kẻ tấn công chèn mã lệnh SQL độc hại vào các ô nhập liệu của người dùng để can thiệp trực tiếp vào câu lệnh SQL gửi xuống Database. Biện pháp phòng chống hiệu quả nhất là sử dụng Prepared Statements (Truy vấn được tham số hóa), giúp tách biệt hoàn toàn phần dữ liệu nhập của người dùng ra khỏi phần cú pháp lệnh SQL, khiến câu lệnh SQL độc hại không thể thực thi.",
    "Kiểm thử từng phần nhỏ nhất của mã nguồn (ví dụ: một hàm).": "Unit Test (Kiểm thử đơn vị) là việc kiểm thử các thành phần nhỏ nhất có thể hoạt động độc lập của mã nguồn ứng dụng (thường là một hàm hoặc một phương thức trong class). Mục tiêu của Unit Test là cô lập từng phần của chương trình và chứng minh rằng các phần riêng lẻ đó hoạt động hoàn toàn chính xác theo đúng đặc tả thiết kế.",
    "Đáp ứng nhu cầu và giải quyết vấn đề của người dùng.": "Mã nguồn đẹp và kỹ thuật tiên tiến đều vô nghĩa nếu ứng dụng không mang lại giá trị thực tế. Mục tiêu tối thượng của bất kỳ quy trình phát triển và triển khai hệ thống phần mềm nào là phải đáp ứng đúng nhu cầu nghiệp vụ, mang lại giá trị thiết thực và giải quyết hiệu quả các vấn đề mà người dùng cuối hoặc doanh nghiệp đang đối mặt.",

    # Viettel Specific Knowledge
    "Tiên phong kiến tạo xã hội số": "Sứ mệnh cốt lõi hiện nay của Tập đoàn Công nghiệp - Viễn thông Quân đội Viettel là 'Tiên phong kiến tạo xã hội số'. Viettel cam kết đi đầu trong công cuộc chuyển đổi số quốc gia, xây dựng chính phủ số, kinh tế số và xã hội số, phổ cập công nghệ số hiện đại tới mọi người dân và doanh nghiệp tại Việt Nam và các thị trường quốc tế.",
    "Viễn thông và công nghệ thông tin": "Viettel khởi đầu là một công ty xây lắp công trình viễn thông và đã vươn lên mạnh mẽ nhờ lĩnh vực cốt lõi ban đầu là Dịch vụ Viễn thông & Công nghệ thông tin (bao gồm mạng di động, internet cáp quang). Cho đến nay, đây vẫn là nguồn doanh thu và lợi nhuận lớn nhất giúp Viettel phát triển các trụ cột công nghệ cao khác.",
    "Tập đoàn công nghệ toàn cầu, tiên phong kiến tạo xã hội số": "Viettel định vị thương hiệu của mình là một Tập đoàn Công nghệ toàn cầu. Tầm nhìn chiến lược này thể hiện khát vọng của Viettel không chỉ dừng lại ở một nhà mạng viễn thông nội địa, mà vươn ra thế giới, tự chủ công nghệ cao (sản xuất thiết bị mạng, quốc phòng công nghệ cao, giải pháp chuyển đổi số lớn) và kiến tạo xã hội số toàn diện.",
    "Kiến tạo Đông Dương": "Cột mốc đầu tiên trong hành trình vươn ra quốc tế của Viettel là tại thị trường Lào (với thương hiệu Unitel) và Campuchia (với thương hiệu Metfone) tạo thành thế chân kiềng vững chắc tại Đông Dương. Đây là bước đệm quan trọng giúp Viettel đúc rút kinh nghiệm để tiếp tục vươn ra các châu lục khác như châu Phi (Mozambique, Burundi, Cameroon) và châu Mỹ (Haiti, Peru).",
    "Metfone": "Metfone là thương hiệu viễn thông của Viettel tại Campuchia, được khai trương thương mại từ năm 2009. Nhờ chiến lược phủ sóng rộng khắp từ thành thị tới nông thôn, Metfone đã nhanh chóng vươn lên vị trí dẫn đầu thị trường viễn thông Campuchia, đóng góp quan trọng vào mối quan hệ hợp tác kinh tế giữa hai nước.",
    "Unitel": "Unitel là liên doanh viễn thông của Viettel tại nước Cộng hòa Dân chủ Nhân dân Lào. Kể từ khi khai trương vào năm 2009, Unitel đã tạo nên một cuộc cách mạng viễn thông tại Lào, đưa sóng di động tới mọi vùng sâu vùng xa và liên tục giữ vững vị thế nhà mạng số 1 tại Lào cả về thuê bao lẫn hạ tầng mạng lưới.",
    "Mytel": "Mytel là thương hiệu của Viettel tại Myanmar, được chính thức khai trương thương mại vào năm 2018. Đây là dự án đầu tư quốc tế có quy mô lớn nhất của Viettel. Chỉ sau một thời gian ngắn kỷ lục, Mytel đã vươn lên dẫn đầu thị trường Myanmar về hạ tầng và tốc độ phát triển thuê bao di động.",
    "Peru": "Bitel là thương hiệu của Viettel tại Peru (thị trường Nam Mỹ), bắt đầu cung cấp dịch vụ từ năm 2014. Peru là một thị trường có tính cạnh tranh cực kỳ khốc liệt với các đối thủ lớn từ châu Âu và Mỹ Latinh, nhưng Bitel đã thành công rực rỡ nhờ chiến lược phổ cập internet di động tốc độ cao tới các vùng nông thôn và miền núi.",
    "Viettel Post": "Tổng Công ty Cổ phần Bưu chính Viettel (Viettel Post) là đơn vị thành viên của Viettel chuyên cung cấp các dịch vụ chuyển phát nhanh trong nước và quốc tế, dịch vụ logistics, thương mại điện tử. Đây là một trong những đơn vị dẫn đầu thị trường logistics tại Việt Nam nhờ ứng dụng công nghệ số mạnh mẽ vào tối ưu vận hành.",
    "Vận tải logistics và chuyển phát nhanh": "Lĩnh vực kinh doanh cốt lõi của Viettel Post là dịch vụ chuyển phát nhanh hàng hóa, tài liệu và các giải pháp vận tải logistics toàn diện. Bưu chính Viettel đã xây dựng một mạng lưới bưu cục rộng khắp cả nước tới tận các huyện xã để phục vụ nhu cầu chuyển phát thương mại điện tử bùng nổ.",
    "Đồng hành, lắng nghe và chia sẻ": "Triết lý thương hiệu nổi tiếng của Viettel được gói gọn trong khẩu hiệu và tinh thần: 'Hãy nói theo cách của bạn' (Say it your way) thể hiện sự trân trọng khách hàng như những cá thể riêng biệt. Viettel cam kết luôn đồng hành, lắng nghe sâu sắc các nhu cầu thực tế và sẵn sàng chia sẻ mọi khó khăn, giải pháp cùng khách hàng.",
    "Chăm sóc khách hàng như những cá thể riêng biệt": "Phương châm hành động cốt lõi của Viettel trong kinh doanh là chăm sóc mỗi khách hàng như một cá thể riêng biệt, thấu hiểu nhu cầu cá nhân hóa sâu sắc của từng người dùng. Đây chính là động lực giúp Viettel không ngừng đa dạng hóa và may đo các gói cước, dịch vụ công nghệ phù hợp nhất với từng đối tượng khách hàng.",
    "4G và 5G": "Viettel luôn đi đầu trong việc thử nghiệm và thương mại hóa các thế hệ công nghệ mạng di động mới tại Việt Nam. Viettel là nhà mạng đầu tiên phủ sóng 4G toàn quốc bằng công nghệ 4T4R siêu tốc và cũng là đơn vị tiên phong thử nghiệm, lắp đặt trạm phát sóng 5G thương mại đầu tiên, tiến tới tự chủ sản xuất thiết bị mạng 5G cao cấp.",
}

def clean_term_key(text):
    text = re.sub(r'^\s*[\-\:\.]\s*', '', text)
    text = re.sub(r'\s+', ' ', text).strip()
    return text

# Loop and enrich
enriched_count = 0
for exam in exams:
    exam_id = exam["id"]
    for q in exam["questions"]:
        current_exp = q.get("explanation", "").strip()
        correct_idx = q["correct"]
        correct_option_text = q["options"][correct_idx]
        
        # Check if the explanation is very simple or matches a key in our dict
        # Or if the explanation is just repeating the option letter or text
        # Let's clean the keys for lookup
        clean_exp = clean_term_key(current_exp)
        clean_option = clean_term_key(correct_option_text)
        
        enriched = False
        
        # 1. Exact match in our rich tech dictionary
        for key, rich_text in tech_explanations.items():
            if clean_exp == clean_term_key(key) or clean_option == clean_term_key(key) or key in current_exp:
                q["explanation"] = rich_text
                enriched = True
                enriched_count += 1
                break
                
        if not enriched:
            # 2. General smart enrichment if the explanation is too short or just tells the letter
            if len(current_exp) < 15 or "Đáp án chính xác là" in current_exp or current_exp == correct_option_text:
                opt_letter = chr(ord('A') + correct_idx)
                
                # Contextual generator based on keywords
                q_text = q["question"].lower()
                
                if "viettel" in q_text:
                    q["explanation"] = f"Đáp án đúng là {opt_letter}: {correct_option_text}. Đây là kiến thức thực tế về Tập đoàn Công nghiệp - Viễn thông Quân đội Viettel, phản ánh chiến lược phát triển công nghệ số, tinh thần người lính quân đội dũng cảm và khát vọng kiến tạo xã hội số của tập đoàn tại Việt Nam và quốc tế."
                elif "oop" in q_text or "hướng đối tượng" in q_text:
                    q["explanation"] = f"Đáp án đúng là {opt_letter}: {correct_option_text}. Trong lập trình hướng đối tượng (OOP), khái niệm này đóng vai trò quan trọng trong việc thiết kế cấu trúc lớp (class), tối ưu khả năng tái sử dụng mã nguồn và quản lý các đối tượng một cách khoa học."
                elif "docker" in q_text or "container" in q_text:
                    q["explanation"] = f"Đáp án đúng là {opt_letter}: {correct_option_text}. Docker Container giúp đóng gói ứng dụng cùng toàn bộ thư viện phụ thuộc của nó, đảm bảo hệ thống có thể chạy một cách ổn định, mượt mà và nhất quán trên bất kỳ môi trường vận hành nào."
                elif "sql" in q_text or "truy vấn" in q_text or "bảng" in q_text:
                    q["explanation"] = f"Đáp án đúng là {opt_letter}: {correct_option_text}. Đây là kiến thức cơ sở dữ liệu quan hệ chuẩn hóa. Sử dụng các câu lệnh truy vấn phù hợp giúp tối ưu hóa hiệu năng, giảm tải dung lượng truyền tải và bảo vệ tính toàn vẹn của dữ liệu."
                elif "mạng" in q_text or "giao thức" in q_text:
                    q["explanation"] = f"Đáp án đúng là {opt_letter}: {correct_option_text}. Đây là một chuẩn giao thức truyền thông quan trọng trong hệ thống mạng máy tính hiện đại, định nghĩa các quy tắc đóng gói, định tuyến và truyền tải dữ liệu giữa máy khách và máy chủ an toàn."
                elif "python" in q_text:
                    q["explanation"] = f"Đáp án đúng là {opt_letter}: {correct_option_text}. Python là một ngôn ngữ lập trình bậc cao, hướng đối tượng và mạnh mẽ. Việc sử dụng các cú pháp và kiểu dữ liệu chuẩn giúp tối ưu hóa tốc độ chạy chương trình và giữ mã nguồn rõ ràng, dễ bảo trì."
                elif "bảo mật" in q_text or "an toàn" in q_text or "tấn công" in q_text:
                    q["explanation"] = f"Đáp án đúng là {opt_letter}: {correct_option_text}. Trong an toàn bảo mật thông tin, việc thấu hiểu các hình thức tấn công giúp lập trình viên chủ động triển khai các cơ chế phòng thủ tốt nhất như mã hóa dữ liệu hoặc lọc thông tin đầu vào."
                elif "đám mây" in q_text or "cloud" in q_text:
                    q["explanation"] = f"Đáp án đúng là {opt_letter}: {correct_option_text}. Dịch vụ đám mây giúp doanh nghiệp giảm tải chi phí hạ tầng máy chủ vật lý, tối ưu hóa khả năng mở rộng hệ thống linh hoạt theo thời gian thực và tăng tính sẵn sàng cho ứng dụng."
                elif "android" in q_text or "ios" in q_text or "di động" in q_text:
                    q["explanation"] = f"Đáp án đúng là {opt_letter}: {correct_option_text}. Phát triển ứng dụng di động yêu cầu tối ưu hiệu năng phần cứng, quản lý vòng đời ứng dụng chặt chẽ và thiết kế trải nghiệm người dùng mượt mà trên nhiều kích thước màn hình."
                elif "dữ liệu lớn" in q_text or "big data" in q_text:
                    q["explanation"] = f"Đáp án đúng là {opt_letter}: {correct_option_text}. Xử lý dữ liệu lớn (Big Data) yêu cầu các mô hình tính toán phân tán hiện đại để xử lý hiệu quả lượng dữ liệu khổng lồ với các đặc trưng về tốc độ, quy mô và sự đa dạng dữ liệu."
                else:
                    q["explanation"] = f"Đáp án chính xác là {opt_letter}: {correct_option_text}. Lựa chọn này phản ánh đúng bản chất kỹ thuật của vấn đề được đề cập trong câu hỏi, đáp ứng đầy đủ các tiêu chuẩn kỹ thuật và thực tiễn phát triển phần mềm."
                
                enriched_count += 1

print(f"Successfully enriched {enriched_count} question explanations in exams data!")

# Save the enriched JSON
with open(json_path, "w", encoding="utf-8") as f:
    json.dump(exams, f, ensure_ascii=False, indent=2)

print("Saved enriched JSON back to exams_data.json.")
