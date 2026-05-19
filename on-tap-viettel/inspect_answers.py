import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# Let's search for matches of "Đáp án" (case-insensitive or with various suffixes)
# and print 1500 characters after it to see how they are written.
matches = list(re.finditer(r"(Đáp án|ĐÁP ÁN|Đáp án chi tiết|Dưới đây là đáp án)", text))
print(f"Found {len(matches)} potential answer section markers:")
for i, m in enumerate(matches):
    start_pos = m.start()
    snippet = text[start_pos:start_pos+300].replace('\n', ' [NL] ')
    print(f"Marker {i+1} at {start_pos}: {snippet}...\n")
