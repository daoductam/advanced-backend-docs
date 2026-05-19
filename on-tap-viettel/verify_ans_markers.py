import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# Exam headers positions
exam_headers = [
    (1, r"(?:Đề thi thử số 1: Kỹ sư Dev vào Viettel 2025)"),
    (2, r"(?:Đề thi thử số 2: Kỹ sư Dev vào Viettel 2025)"),
    (3, r"(?:Đề thi thử số 3: Kỹ sư Dev vào Viettel 2025)"),
    (4, r"(?:Đề thi thử số 4: Kỹ sư Dev vào Viettel 2025)"),
    (5, r"(?:Đề thi thử số 5: Kỹ sư Dev vào Viettel 2025)"),
    (6, r"(?:Đề thi thử số 6: Kỹ sư Dev vào Viettel 2025|ĐỀ THI THỬ SỐ 6: KỸ SƯ DEV VÀO VIETTEL 2025)"),
    (7, r"(?:ĐỀ THI THỬ SỐ 7: KỸ SƯ DEV VÀO VIETTEL 2025)"),
    (8, r"(?:ĐỀ THI THỬ SỐ 8: KỸ SƯ DEV VÀO VIETTEL 2025)"),
    (9, r"(?:ĐỀ THI THỬ SỐ 9: KỸ SƯ DEV VÀO VIETTEL 2025)"),
    (10, r"(?:ĐỀ THI THỬ SỐ 10: KỸ SƯ DEV VÀO VIETTEL 2025)")
]

positions = []
for exam_id, regex in exam_headers:
    matches = list(re.finditer(regex, text))
    if matches:
        for m in matches:
            if m.start() > 2000:
                positions.append((exam_id, m.start()))
                break

positions.sort(key=lambda x: x[1])
positions.append(("EOF", len(text)))

def clean_text(t):
    t = re.sub(r'messages\.downloaded_by\s*\n\s*lOMoARcPSD\|35970655', '', t)
    t = re.sub(r'--- PAGE \d+ ---', '', t)
    t = re.sub(r'BIÊN SOẠN TAILIEUONTHI\.IO\.VN', '', t)
    t = re.sub(r'^\s*\d+\s*$', '', t, flags=re.MULTILINE)
    return t

for i in range(len(positions) - 1):
    exam_id, start = positions[i]
    next_exam_id, end = positions[i+1]
    
    exam_text = clean_text(text[start:end])
    
    # Let's find "Đáp án" at the start of a line.
    ans_matches = list(re.finditer(r'(?:^|\n)\s*(?:Đáp án|ĐÁP ÁN|Đáp án chi tiết|Dưới đây là đáp án)\b', exam_text))
    
    print(f"Exam {exam_id}: found {len(ans_matches)} line-level answer markers.")
    for idx, m in enumerate(ans_matches):
        snippet = exam_text[m.start():m.start()+150].replace('\n', ' [NL] ')
        print(f"  Marker {idx+1} at index {m.start()}: {snippet}")
    print("-" * 50)
