import sys
sys.stdout.reconfigure(encoding='utf-8')
file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"
with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()
print("=== Text between 81680 and 82000 ===")
print(text[81680:82000].replace('\n', ' [NL] '))
