import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# Search for "Giải thích" or "giải thích" or "giai thich"
matches = list(re.finditer(r"(Giải thích|giải thích|giai thich|GIẢI THÍCH)", text))
print(f"Found {len(matches)} occurrences of 'Giải thích':")
for i, m in enumerate(matches[:20]):
    start_pos = m.start()
    snippet = text[max(0, start_pos - 50):min(len(text), start_pos + 150)].replace('\n', ' [NL] ')
    print(f"  {i+1}: ... {snippet} ...")
