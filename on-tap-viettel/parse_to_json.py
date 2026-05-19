import re
import json
import sys

sys.stdout.reconfigure(encoding='utf-8')

file_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

with open(file_path, "r", encoding="utf-8") as f:
    text = f.read()

# We can find all exams by finding where the headers start
# Let's define the headings in order:
exam_headers = [
    (1, r"(?:Đề thi thử số 1: Kỹ sư Dev vào Viettel 2025)"),
    (2, r"(?:Đề thi thử số 2: Kỹ sư Dev vào Viettel 2025)"),
    (3, r"(?:Đề thi thử số 3: Kỹ sư Dev vào Viettel 2025)"),
    (4, r"(?:Đề thi thử số 4: Kỹ sư Dev vào Viettel 2025)"),
    (5, r"(?:Đề thi thử số 5: Kỹ sư Dev vào Viettel 2025)"),
    (6, r"(?:Đề thi thử số 6: Kỹ sư Dev vào Viettel 2025|ĐỀ THI THỬ SỐ 6: KỸ SƯ DEV VÀO VIETTEL 2025)"),
    (7, r"(?:ĐỀ THI THỬ SỐ 7: KỸ SƯ DEV VÀO VIETTEL 2025)"),
    (8, r"(?:ĐỀ THI THỬ SỐ 8: KỸ SƯ DEV VÀO VIETTEL 2025)"),
    (9, r"(?:ĐỀ THI THỬ SỐ 9: KỸ SƯ DEV VÀO VIETTEL 2025)"),
    (10, r"(?:ĐỀ THI THỬ SỐ 10: KỸ SƯ DEV VÀO VIETTEL 2025)")
]

positions = []
for exam_id, regex in exam_headers:
    matches = list(re.finditer(regex, text))
    if matches:
        # Find the first match that is not in the initial table of contents (character index > 2000)
        found = False
        for m in matches:
            if m.start() > 2000:
                positions.append((exam_id, m.start()))
                found = True
                break
        if not found:
            positions.append((exam_id, matches[0].start()))

positions.sort(key=lambda x: x[1])
positions.append(("EOF", len(text)))

print("Found exam boundaries:")
for i in range(len(positions) - 1):
    name, start = positions[i]
    next_name, end = positions[i+1]
    print(f"Exam {name}: start={start}, end={end}, size={end-start}")

# Let's define the parsing function for a single exam text
# We need to extract:
# - Exam title
# - Category (or Chuyên đề)
# - 50 questions
# - 50 answers
# - Explanations (if available, otherwise we generate a reasonable explanation based on the correct answer and the options)

def clean_text(t):
    # Remove page footer messages like "messages.downloaded_by\nlOMoARcPSD|35970655"
    t = re.sub(r'messages\.downloaded_by\s*\n\s*lOMoARcPSD\|35970655', '', t)
    t = re.sub(r'--- PAGE \d+ ---', '', t)
    t = re.sub(r'BIÊN SOẠN TAILIEUONTHI\.IO\.VN', '', t)
    t = re.sub(r'^\s*\d+\s*$', '', t, flags=re.MULTILINE) # page numbers
    return t

def parse_exam(exam_id, exam_raw_text):
    exam_text = clean_text(exam_raw_text)
    
    # Extract title
    title_match = re.search(r'((?:Đề thi thử số|ĐỀ THI THỬ SỐ)\s+\d+:[^\n]+)', exam_text)
    title = title_match.group(1).strip() if title_match else f"Đề thi thử số {exam_id}"
    # Remove dots and numbers at the end of title if any
    title = re.sub(r'\.+\s*\d+$', '', title).strip()
    
    # Extract category/chuyên đề
    category_match = re.search(r'(?:Chuyên đề|Chuyên đề:)\s*([^\n]+)', exam_text)
    category = category_match.group(1).strip() if category_match else "Kiến thức chung"
    
    print(f"\n--- Parsing Exam {exam_id}: {title} (Chuyên đề: {category}) ---")
    
    # Let's find where the answers section starts.
    # We look for "Đáp án" or "Đáp án chi tiết" or "Dưới đây là đáp án" in the lower part of the text
    # Let's find all occurrences of "Đáp án" (case insensitive) and choose the one that is towards the end
    ans_markers = list(re.finditer(r'(?:Đáp án|ĐÁP ÁN|Đáp án chi tiết|Dưới đây là đáp án)', exam_text))
    
    # We want the one that is at the end of the question list, so after question 50 is mentioned,
    # or the one that is followed by actual list of answers (like "Câu 1: " or "1. B" etc.)
    ans_start_idx = -1
    for m in reversed(ans_markers):
        # Check if there are answers following it
        suffix = exam_text[m.start():m.start()+500]
        if re.search(r'(?:Câu \d+|\b\d+\b\s*[\.:\-]\s*[A-D])', suffix):
            ans_start_idx = m.start()
            break
            
    if ans_start_idx == -1 and ans_markers:
        ans_start_idx = ans_markers[-1].start()
        
    if ans_start_idx == -1:
        print(f"ERROR: Could not find answer section for Exam {exam_id}!")
        return None
        
    questions_part = exam_text[:ans_start_idx]
    answers_part = exam_text[ans_start_idx:]
    
    # Let's parse questions
    # A question starts with "Câu X:" or "Câu X." or just a number starting a line, but wait,
    # let's look for "Câu \d+[\.:\-]" which is very reliable.
    # In some exams, it's "Câu 1. " and in others "Câu 1: " or "1. " or "1: "
    # Let's check how questions are split.
    # We can match all questions using regex:
    q_pattern = r'(?:^|\n)\s*(?:Câu\s+(\d+)|(\d+))\s*[\.:\-]\s*(.*?)(?=(?:\n\s*(?:Câu\s+\d+|\d+)\s*[\.:\-]\s*|\Z))'
    # Wait, let's look closely at the questions_part.
    # Let's find all questions.
    q_matches = list(re.finditer(r'(?:^|\n)\s*(?:Câu\s+(\d+)|\b(\d+)\b)\s*[\.:\-]\s*(.*?)(?=(?:\n\s*(?:Câu\s+(?:\d+)|\b(?:\d+)\b)\s*[\.:\-]\s*|\Z))', questions_part, re.DOTALL))
    
    print(f"Found {len(q_matches)} question raw matches.")
    
    # Let's process questions and options
    questions = []
    for q_match in q_matches:
        q_num = q_match.group(1) or q_match.group(2)
        q_content = q_match.group(3).strip()
        
        # We need to extract the question text and options A, B, C, D
        # Options usually look like:
        # A. text
        # B. text
        # C. text
        # D. text
        # Let's split q_content by options.
        opt_pattern = r'(?:\n|\s|^)([A-D])\s*[\.:\-\)]\s*(.*?)(?=(?:\n|\s|[A-D])\s*[A-D]\s*[\.:\-\)]\s*|\Z)'
        opt_matches = list(re.finditer(r'(?:^|\n|\s+)([A-D])\s*[\.:\-\)]\s*(.*?)(?=(?:(?:^|\n|\s+)[A-D]\s*[\.:\-\)]\s*)|\Z)', q_content, re.DOTALL))
        
        # If we didn't find options, it might be due to line wrapping or layout
        if len(opt_matches) < 4:
            # Let's try another regex without anchor
            opt_matches = list(re.finditer(r'\b([A-D])\s*[\.:\-\)]\s*(.*?)(?=\b[A-D]\s*[\.:\-\)]\s*|\Z)', q_content, re.DOTALL))
            
        # Let's extract the actual question text (everything before option A)
        q_text_only = q_content
        first_opt_pos = len(q_content)
        for opt_m in opt_matches:
            if opt_m.start() < first_opt_pos:
                first_opt_pos = opt_m.start()
        q_text_only = q_content[:first_opt_pos].strip()
        # Clean up any trailing dashes or newlines in question text
        q_text_only = re.sub(r'^\s*[\-\:\.]\s*', '', q_text_only)
        q_text_only = re.sub(r'\s+', ' ', q_text_only).strip()
        
        options = ["", "", "", ""]
        for opt_m in opt_matches:
            opt_letter = opt_m.group(1)
            opt_text = opt_m.group(2).strip()
            # Clean up option text
            opt_text = re.sub(r'\s+', ' ', opt_text).strip()
            idx = ord(opt_letter) - ord('A')
            if 0 <= idx < 4:
                options[idx] = opt_text
                
        questions.append({
            "id": int(q_num),
            "question": q_text_only,
            "options": options,
            "raw": q_content # Keep raw in case we need to debug
        })
        
    print(f"Parsed {len(questions)} questions.")
    
    # Now let's parse answers
    # Answers usually look like:
    # "Câu 1: C. list = []"
    # or "1. B. ..."
    # or "Câu 1. C. ..."
    # Let's find all answer matches
    ans_matches = list(re.finditer(r'(?:^|\n|\s+)(?:Câu\s+(\d+)|\b(\d+)\b)\s*[\.:\-]?\s*([A-D])\s*[\.:\-]?\s*(.*?)(?=(?:(?:^|\n|\s+)(?:Câu\s+(?:\d+)|\b(?:\d+)\b)\s*[\.:\-]?\s*[A-D]\b)|\Z)', answers_part, re.DOTALL))
    
    print(f"Found {len(ans_matches)} answer matches.")
    
    answers_dict = {}
    for a_match in ans_matches:
        a_num = int(a_match.group(1) or a_match.group(2))
        a_letter = a_match.group(3)
        a_text = a_match.group(4).strip()
        a_text = re.sub(r'\s+', ' ', a_text).strip()
        
        # Check if there is an explanation in parentheses in a_text
        explanation = ""
        # e.g., "4 (Tính đóng gói, tính kế thừa, tính đa hình, tính trừu tượng)"
        # Let's look for text inside parentheses at the end or within a_text
        paren_match = re.search(r'\(([^)]+)\)', a_text)
        if paren_match:
            explanation = paren_match.group(1).strip()
            
        answers_dict[a_num] = {
            "correct_option": a_letter,
            "explanation": explanation,
            "full_answer_text": a_text
        }
        
    # Let's merge answers into questions
    merged_questions = []
    for q in questions:
        q_id = q["id"]
        if q_id in answers_dict:
            ans = answers_dict[q_id]
            correct_letter = ans["correct_option"]
            correct_idx = ord(correct_letter) - ord('A')
            
            # If explanation is empty, we can generate a friendly explanation or use the full_answer_text
            explanation = ans["explanation"]
            if not explanation:
                # If the full answer text has more detail than just the option, we can use it.
                # Otherwise, we provide a generic context.
                explanation = f"Đáp án chính xác là {correct_letter}: {q['options'][correct_idx]}"
                
            q["correct"] = correct_idx
            q["explanation"] = explanation
        else:
            print(f"WARNING: Question {q_id} in Exam {exam_id} has no matching answer!")
            q["correct"] = 0
            q["explanation"] = "Đang cập nhật lời giải."
            
        # Remove raw field to keep JSON clean
        del q["raw"]
        merged_questions.append(q)
        
    # Verify counts and sort by id
    merged_questions.sort(key=lambda x: x["id"])
    
    # If the merged list is not 50, let's log it
    if len(merged_questions) != 50:
        print(f"WARNING: Exam {exam_id} has {len(merged_questions)} questions instead of 50!")
        
    return {
        "id": exam_id,
        "title": title,
        "category": category,
        "questions": merged_questions
    }

parsed_exams = []
for i in range(len(positions) - 1):
    exam_id, start = positions[i]
    next_exam_id, end = positions[i+1]
    
    # Only process numeric exam IDs
    if isinstance(exam_id, int):
        exam_raw_text = text[start:end]
        exam_data = parse_exam(exam_id, exam_raw_text)
        if exam_data:
            parsed_exams.append(exam_data)

# Save to JSON
output_json_path = r"d:\backend_docs\on-tap-viettel\exams_data.json"
with open(output_json_path, "w", encoding="utf-8") as f:
    json.dump(parsed_exams, f, ensure_ascii=False, indent=2)

print(f"\nSUCCESS! Parsed {len(parsed_exams)} exams and saved to {output_json_path}")
