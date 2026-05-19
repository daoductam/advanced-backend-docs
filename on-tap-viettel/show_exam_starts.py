import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

lines = text.split('\n')

headers = [
    ("Đề 1", 31),
    ("Đề 2", 433),
    ("Đề 3", 870),
    ("Đề 4", 1290),
    ("Đề 5", 1728),
    ("Đề 6", 2167),
    ("Đề 7", 2564),
    ("Đề 8", 2971),
    ("Đề 9", 3394),
    ("Đề 10", 3798)
]

for name, line_num in headers:
    print(f"=== {name} (around line {line_num}) ===")
    start_line = max(1, line_num - 2)
    end_line = min(len(lines), line_num + 8)
    for idx in range(start_line - 1, end_line):
        print(f"{idx+1}: {lines[idx]}")
    print()
