import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# Let's search for "Đề" at the start of lines
lines = text.split('\n')
for i, line in enumerate(lines):
    if re.match(r'^\s*(Đề|ĐỀ)\b', line):
        print(f"Line {i+1}: {line[:100]}")
