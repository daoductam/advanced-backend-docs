import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# Let's find the boundaries of Exam 7
m7 = list(re.finditer(r"ĐỀ THI THỬ SỐ 7: KỸ SƯ DEV VÀO VIETTEL 2025", text))
m8 = list(re.finditer(r"ĐỀ THI THỬ SỐ 8: KỸ SƯ DEV VÀO VIETTEL 2025", text))

start_pos = m7[0].start()
end_pos = m8[0].start()

exam_text = text[start_pos:end_pos]
print(f"Exam 7 text size: {len(exam_text)}")

# Find all occurrences of "Đáp án"
print("=== All markers matching 'Đáp án' (case insensitive) ===")
for m in re.finditer(r'(Đáp án|ĐÁP ÁN|Đáp án chi tiết|Dưới đây là đáp án)', exam_text, re.IGNORECASE):
    snippet = exam_text[m.start():m.start()+200].replace('\n', ' [NL] ')
    print(f"Index {m.start()}: {snippet}\n")
