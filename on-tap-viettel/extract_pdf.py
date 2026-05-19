import pypdf
import os

pdf_path = r"d:\backend_docs\on-tap-viettel\bo-10-de-thi-thu-tuyen-dung-ky-su-dev-viettel-2025.pdf"
output_path = r"d:\backend_docs\on-tap-viettel\extracted_text.txt"

print("Starting extraction...")
reader = pypdf.PdfReader(pdf_path)
total_pages = len(reader.pages)
print(f"Total pages: {total_pages}")

extracted_text = []
for i, page in enumerate(reader.pages):
    text = page.extract_text()
    extracted_text.append(f"--- PAGE {i+1} ---")
    extracted_text.append(text)
    if (i + 1) % 10 == 0:
        print(f"Extracted {i+1}/{total_pages} pages")

with open(output_path, "w", encoding="utf-8") as f:
    f.write("\n".join(extracted_text))

print(f"Finished! Extracted text written to {output_path}")
