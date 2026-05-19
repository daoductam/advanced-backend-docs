import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

print("Scanning for 'Đề thi thử số'...")
matches = re.finditer(r"(Đề thi thử số \d+)", text)
found_set = set()
for m in matches:
    found_set.add(m.group(1))

for item in sorted(list(found_set)):
    print(f"  {item}")

print("Total length of text:", len(text))
