import re
import sys

sys.stdout.reconfigure(encoding='utf-8')

file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# Let's print a larger snippet of Exam 6 answers (around character 93453)
print("=== Snippet from Exam 6 Answers ===")
print(text[93450:95000])

print("\n=== Snippet from Exam 7 Answers ===")
print(text[109300:110500])
