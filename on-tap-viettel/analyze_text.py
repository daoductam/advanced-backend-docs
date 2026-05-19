import re
import sys

# Reconfigure stdout to use UTF-8
sys.stdout.reconfigure(encoding='utf-8')

file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# Let's find all occurrences of "Đề thi thử số" or "Đáp án"
print("=== Headings and Exam Titles ===")
matches = list(re.finditer(r"(Đề thi thử số \d+:[^\n]+)", text))
for m in matches:
    print(f"Found: {m.group(1)} at character position {m.start()}")

print("\n=== Answer section headings ===")
matches_ans = list(re.finditer(r"(\bĐáp án\b)", text))
for m in matches_ans:
    # let's show surrounding text to confirm it's the answer heading
    start = max(0, m.start() - 20)
    end = min(len(text), m.end() + 20)
    surr = text[start:end].replace('\n', ' [NL] ')
    print(f"Found 'Đáp án' at position {m.start()}: ... {surr} ...")
