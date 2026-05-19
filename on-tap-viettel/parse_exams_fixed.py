import re
import json
import sys

sys.stdout.reconfigure(encoding='utf-8')

file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# Let's clean the text first from standard PDF noise
def clean_text(t):
    # Remove footer messages and page separators
    t = re.sub(r'messages\.downloaded_by\s*\n\s*lOMoARcPSD\|35970655', '', t)
    t = re.sub(r'--- PAGE \d+ ---', '', t)
    t = re.sub(r'BIÊN SOẠN TAILIEUONTHI\.IO\.VN', '', t)
    t = re.sub(r'^\s*\d+\s*$', '', t, flags=re.MULTILINE)
    return t

# Clean up trailing section headers that got appended due to greedy match
def clean_trailing_sections(t):
    if not t:
        return t
    # Remove common section headings
    t = re.sub(r'\s*(?:Phần|PHẦN)\s+[I|V|X]+:.*$', '', t, flags=re.IGNORECASE)
    t = re.sub(r'\s*(?:Phần|PHẦN)\s+[I|V|X]+\b.*$', '', t, flags=re.IGNORECASE)
    return t.strip()

# 1. Find all exam titles and positions
pattern = r"(?:^|\n)\s*((?:Đề thi thử số|ĐỀ THI THỬ SỐ)\s+(\d+)\s*:[^\n]+)"
raw_matches = list(re.finditer(pattern, text))

exam_positions = {}
for m in raw_matches:
    title = m.group(1).strip()
    exam_num = int(m.group(2))
    pos = m.start()
    
    # Exclude Table of Contents
    if "..." in title:
        continue
        
    # We keep the first match for each exam_num
    if exam_num not in exam_positions:
        exam_positions[exam_num] = (title, pos)

# Sort them by position
sorted_exams = sorted(list(exam_positions.items()), key=lambda x: x[1][1])

positions = []
for exam_num, (title, pos) in sorted_exams:
    positions.append((exam_num, title, pos))

# Add EOF boundary
positions.append(("EOF", "EOF", len(text)))

print(f"Detected {len(positions)-1} unique exams:")
for i in range(len(positions) - 1):
    num, title, start = positions[i]
    _, _, end = positions[i+1]
    print(f"  Exam {num}: '{title}' | start={start}, end={end}, length={end-start}")

print("\n" + "="*80 + "\n")

# Parser function for each exam
def parse_exam_section(exam_num, title, exam_raw_text):
    exam_text = clean_text(exam_raw_text)
    
    # 1. Find category (Chuyên đề)
    category_match = re.search(r'(?:Chuyên đề|Chuyên đề:)\s*([^\n]+)', exam_text, re.IGNORECASE)
    category = category_match.group(1).strip() if category_match else "Kiến thức chung"
    # Clean category name
    category = re.sub(r'\.+\s*$', '', category).strip()
    
    # 2. Separate questions and answers
    # We find the FIRST occurrence of answer marker at the start of a line
    ans_match = re.search(r'(?:^|\n)\s*(?:Đáp án|ĐÁP ÁN|Đáp án chi tiết|Dưới đây là đáp án)\b', exam_text, re.IGNORECASE)
    if not ans_match:
        print(f"ERROR: Could not find answer section for Exam {exam_num}!")
        return None
        
    ans_start_pos = ans_match.start()
    questions_part = exam_text[:ans_start_pos]
    answers_part = exam_text[ans_start_pos:]
    
    print(f"Exam {exam_num}: questions_part size={len(questions_part)}, answers_part size={len(answers_part)}")
    
    # 3. Parse questions
    # Question regex: Starts with "Câu X:" or "Câu X."
    q_matches = list(re.finditer(r'(?:^|\n)\s*(?:Câu|CÂU)\s+(\d+)\s*[\.:\-]\s*(.*?)(?=(?:\n\s*(?:Câu|CÂU)\s+\d+\s*[\.:\-]\s*|\Z))', questions_part, re.DOTALL | re.IGNORECASE))
    
    questions = []
    for q_match in q_matches:
        q_id = int(q_match.group(1))
        q_content = q_match.group(2).strip()
        
        # Split options A, B, C, D
        opt_matches = list(re.finditer(r'(?:^|\n|\s+)([A-D])\s*[\.:\-\)]\s*(.*?)(?=(?:(?:^|\n|\s+)[A-D]\s*[\.:\-\)]\s*)|\Z)', q_content, re.DOTALL))
        
        # Fallback if needed
        if len(opt_matches) < 4:
            opt_matches = list(re.finditer(r'\b([A-D])\s*[\.:\-\)]\s*(.*?)(?=\b[A-D]\s*[\.:\-\)]\s*|\Z)', q_content, re.DOTALL))
            
        # Get question text only
        first_opt_pos = len(q_content)
        for opt_m in opt_matches:
            if opt_m.start() < first_opt_pos:
                first_opt_pos = opt_m.start()
        q_text = q_content[:first_opt_pos].strip()
        q_text = re.sub(r'\s+', ' ', q_text).strip()
        q_text = clean_trailing_sections(q_text)
        
        options = ["", "", "", ""]
        for opt_m in opt_matches:
            letter = opt_m.group(1)
            opt_val = opt_m.group(2).strip()
            opt_val = re.sub(r'\s+', ' ', opt_val).strip()
            opt_val = clean_trailing_sections(opt_val)
            idx = ord(letter) - ord('A')
            if 0 <= idx < 4:
                options[idx] = opt_val
                
        questions.append({
            "id": q_id,
            "question": q_text,
            "options": options
        })
        
    # 4. Parse answers
    ans_matches = list(re.finditer(r'(?:^|\n|\s+)(?:Câu\s+(\d+)|\b(\d+)\b)\s*[\.:\-]?\s*([A-D])\s*[\.:\-]?\s*(.*?)(?=(?:(?:^|\n|\s+)(?:Câu\s+(?:\d+)|\b(?:\d+)\b)\s*[\.:\-]?\s*[A-D]\b)|\Z)', answers_part, re.DOTALL | re.IGNORECASE))
    
    answers_dict = {}
    for a_match in ans_matches:
        a_id = int(a_match.group(1) or a_match.group(2))
        a_letter = a_match.group(3).upper()
        a_text = a_match.group(4).strip()
        a_text = re.sub(r'\s+', ' ', a_text).strip()
        a_text = clean_trailing_sections(a_text)
        
        # Check if there is an explanation inside parentheses
        explanation = ""
        paren_match = re.search(r'\(([^)]+)\)', a_text)
        if paren_match:
            explanation = paren_match.group(1).strip()
            
        answers_dict[a_id] = {
            "correct": ord(a_letter) - ord('A'),
            "explanation": explanation,
            "full_text": a_text
        }
        
    print(f"Parsed {len(questions)} questions and {len(answers_dict)} answers.")
    
    # 5. Merge
    merged_questions = []
    for q in questions:
        q_id = q["id"]
        if q_id in answers_dict:
            ans = answers_dict[q_id]
            q["correct"] = ans["correct"]
            
            explanation = ans["explanation"]
            if not explanation:
                opt_letter = chr(ord('A') + ans['correct'])
                clean_ans_text = ans["full_text"]
                if clean_ans_text.startswith(opt_letter):
                    clean_ans_text = re.sub(r'^' + opt_letter + r'\s*[\.:\-]?\s*', '', clean_ans_text).strip()
                
                opt_text = q["options"][ans["correct"]]
                if clean_ans_text and clean_ans_text != opt_text:
                    explanation = clean_ans_text
                else:
                    explanation = f"Đáp án chính xác là {opt_letter}: {opt_text}."
            
            explanation = clean_trailing_sections(explanation)
            q["explanation"] = explanation
        else:
            print(f"  WARNING: Question {q_id} has no matching answer!")
            q["correct"] = 0
            q["explanation"] = "Đang cập nhật giải thích."
            
        merged_questions.append(q)
        
    # Sort and verify
    merged_questions.sort(key=lambda x: x["id"])
    
    if len(merged_questions) != 50:
        print(f"  WARNING: Exam {exam_num} has {len(merged_questions)} questions (expected 50)!")
    else:
        print(f"  SUCCESS: Exam {exam_num} parsed perfectly with 50 questions!")
        
    return {
        "id": exam_num,
        "title": title,
        "category": category,
        "questions": merged_questions
    }

parsed_exams = []
for i in range(len(positions) - 1):
    num, title, start = positions[i]
    _, _, end = positions[i+1]
    
    exam_raw_text = text[start:end]
    exam_data = parse_exam_section(num, title, exam_raw_text)
    if exam_data:
        parsed_exams.append(exam_data)

# Save to JSON
output_json_path = r"d:\backend_docs\on-tap-viettel\exams_data.json"
with open(output_json_path, "w", encoding="utf-8") as f:
    json.dump(parsed_exams, f, ensure_ascii=False, indent=2)

print("\n" + "="*80)
print(f"FINISHED! Parsed {len(parsed_exams)} exams and saved to {output_json_path}")
