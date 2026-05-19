import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# Let's find all lines that start with "Đề thi thử số" or "ĐỀ THI THỬ SỐ" and do NOT contain dots
pattern = r"(?:^|\n)\s*((?:Đề thi thử số|ĐỀ THI THỬ SỐ)\s+\d+:[^\n]+)"
matches = []
for m in re.finditer(pattern, text):
    title = m.group(1).strip()
    # Check if there are multiple dots (which means it's in the Table of Contents)
    if "..." not in title:
        matches.append((title, m.start()))

print(f"Found {len(matches)} exam starts without dots:")
for idx, (title, pos) in enumerate(matches):
    print(f"  {idx+1}: '{title}' at position {pos}")
