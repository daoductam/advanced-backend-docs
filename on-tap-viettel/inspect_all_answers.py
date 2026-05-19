import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

exams = [
    ("Đề 1", r"Đề thi thử số 1: Kỹ sư Dev vào Viettel 2025"),
    ("Đề 2", r"Đề thi thử số 2: Kỹ sư Dev vào Viettel 2025"),
    ("Đề 3", r"Đề thi thử số 3: Kỹ sư Dev vào Viettel 2025"),
    ("Đề 4", r"Đề thi thử số 4: Kỹ sư Dev vào Viettel 2025"),
    ("Đề 5", r"Đề thi thử số 5: Kỹ sư Dev vào Viettel 2025"),
    ("Đề 6", r"ĐỀ THI THỬ SỐ 6: KỸ SƯ DEV VÀO VIETTEL 2025"),
    ("Đề 7", r"ĐỀ THI THỬ SỐ 7: KỸ SƯ DEV VÀO VIETTEL 2025"),
    ("Đề 8", r"ĐỀ THI THỬ SỐ 8: KỸ SƯ DEV VÀO VIETTEL 2025"),
    ("Đề 9", r"ĐỀ THI THỬ SỐ 9: KỸ SƯ DEV VÀO VIETTEL 2025"),
    ("Đề 10", r"ĐỀ THI THỬ SỐ 10: KỸ SƯ DEV VÀO VIETTEL 2025")
]

positions = []
for name, regex in exams:
    matches = list(re.finditer(regex, text))
    if matches:
        # We take the last one or the one that is not in the index table
        # The index table is in the first 2 pages. So we take match with start > 2000
        for m in matches:
            if m.start() > 2000:
                positions.append((name, m.start()))
                break

positions.append(("EOF", len(text)))

for i in range(len(positions) - 1):
    name, start = positions[i]
    next_name, end = positions[i+1]
    exam_text = text[start:end]
    
    # Let's find "Đáp án" inside exam_text
    ans_match = list(re.finditer(r"(Đáp án|ĐÁP ÁN|Đáp án chi tiết|Dưới đây là đáp án)", exam_text, re.IGNORECASE))
    if ans_match:
        ans_start = ans_match[0].start()
        ans_snippet = exam_text[ans_start:ans_start+400].replace('\n', ' [NL] ')
        print(f"Exam {name} Answer Section starts: ... {ans_snippet} ...\n")
    else:
        print(f"Exam {name} has NO Answer Section found!\n")
