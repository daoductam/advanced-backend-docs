import json
import re

json_path = r"d:\backend_docs\on-tap-viettel\exams_data.json"
html_path = r"d:\ExamPrep-saved.html"

with open(json_path, "r", encoding="utf-8") as f:
    exams_data = json.load(f)

# Convert to string to embed directly in HTML
exams_json_str = json.dumps(exams_data, ensure_ascii=False)

html_content = f"""<!DOCTYPE html>
<html lang="vi" class="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Viettel DEV Engineer - Luyện thi tuyển dụng 2025</title>
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <script>
        tailwind.config = {{
            darkMode: 'class',
            theme: {{
                extend: {{
                    colors: {{
                        brand: {{
                            50: '#fff5f5',
                            100: '#fed7d7',
                            500: '#ee0000', // Viettel Red
                            600: '#cc0000',
                            700: '#990000',
                        }},
                        tech: {{
                            50: '#eef2ff',
                            500: '#2563eb', // Trust Blue
                            600: '#1d4ed8',
                        }},
                        success: '#10b981',
                        error: '#ef4444',
                    }},
                    fontFamily: {{
                        display: ['Outfit', 'sans-serif'],
                        body: ['Inter', 'sans-serif'],
                    }},
                    boxShadow: {{
                        'premium': '0 10px 30px -10px rgba(0, 0, 0, 0.5)',
                        'glow-brand': '0 0 20px 2px rgba(238, 0, 0, 0.15)',
                        'glow-tech': '0 0 20px 2px rgba(37, 99, 235, 0.15)',
                    }}
                }}
            }}
        }}
    </script>
    <style>
        body {{
            font-family: 'Inter', sans-serif;
            background-color: #0b0f19;
            color: #f1f5f9;
            overflow-x: hidden;
        }}
        .glass-panel {{
            background: rgba(17, 24, 39, 0.7);
            backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.05);
        }}
        .glass-panel:hover {{
            border-color: rgba(255, 255, 255, 0.08);
        }}
        .card-brand-glow:hover {{
            box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.5), 0 0 20px 2px rgba(238, 0, 0, 0.15);
            border-color: rgba(238, 0, 0, 0.3);
        }}
        .card-tech-glow:hover {{
            box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.5), 0 0 20px 2px rgba(37, 99, 235, 0.15);
            border-color: rgba(37, 99, 235, 0.3);
        }}
        .option-card {{
            transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
        }}
        .progress-gradient {{
            background: linear-gradient(90deg, #2563eb 0%, #ee0000 100%);
        }}
        .correct-choice {{
            background: rgba(16, 185, 129, 0.15) !important;
            border-color: #10b981 !important;
            box-shadow: 0 0 15px rgba(16, 185, 129, 0.1) !important;
        }}
        .incorrect-choice {{
            background: rgba(239, 68, 68, 0.15) !important;
            border-color: #ef4444 !important;
            box-shadow: 0 0 15px rgba(239, 68, 68, 0.1) !important;
        }}
        .selected-choice {{
            border-color: #2563eb !important;
            background: rgba(37, 99, 235, 0.1) !important;
        }}
        .dot-correct {{
            background-color: #10b981 !important;
            border-color: #10b981 !important;
            color: white !important;
        }}
        .dot-incorrect {{
            background-color: #ef4444 !important;
            border-color: #ef4444 !important;
            color: white !important;
        }}
        .dot-active {{
            border-color: #2563eb !important;
            box-shadow: 0 0 10px rgba(37, 99, 235, 0.5) !important;
        }}
        .dot-answered {{
            background-color: #334155;
            border-color: #475569;
            color: #f1f5f9;
        }}
        ::-webkit-scrollbar {{
            width: 8px;
            height: 8px;
        }}
        ::-webkit-scrollbar-track {{
            background: #0b0f19;
        }}
        ::-webkit-scrollbar-thumb {{
            background: #1e293b;
            border-radius: 10px;
        }}
        ::-webkit-scrollbar-thumb:hover {{
            background: #334155;
        }}
        .tab-active {{
            border-bottom: 2px solid #ee0000;
            color: #ee0000;
        }}
    </style>
</head>
<body class="min-h-screen flex flex-col justify-between">

    <!-- Ambient Glow Spots -->
    <div class="fixed -top-40 -left-40 w-[500px] h-[500px] bg-tech-500/5 rounded-full blur-[120px] pointer-events-none"></div>
    <div class="fixed -bottom-40 -right-40 w-[500px] h-[500px] bg-brand-500/5 rounded-full blur-[120px] pointer-events-none"></div>

    <!-- MAIN CONTAINER -->
    <div class="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 flex-1 flex flex-col">
        
        <!-- ============================================== -->
        <!-- HOME SCREEN -->
        <!-- ============================================== -->
        <section id="home-screen" class="space-y-8 animate-fade-in">
            <!-- Hero Dashboard -->
            <div class="glass-panel rounded-3xl p-6 sm:p-10 shadow-premium relative overflow-hidden flex flex-col md:flex-row items-center justify-between gap-6 border border-slate-800/60">
                <div class="absolute -right-20 -top-20 w-80 h-80 bg-brand-500/10 rounded-full blur-[80px] pointer-events-none"></div>
                <div class="space-y-4 text-center md:text-left z-10">
                    <div class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-brand-500/10 text-brand-500 text-xs font-semibold uppercase tracking-widest border border-brand-500/20">
                        <span class="w-2 h-2 rounded-full bg-brand-500 animate-pulse"></span>
                        Đã sẵn sàng cho Viettel 2025
                    </div>
                    <h1 class="font-display font-extrabold text-4xl sm:text-5xl lg:text-6xl text-white tracking-tight leading-none">
                        Luyện Thi Kỹ Sư <span class="text-transparent bg-clip-text bg-gradient-to-r from-brand-500 via-red-400 to-tech-500">DEV Viettel</span>
                    </h1>
                    <p class="text-slate-400 max-w-xl text-base sm:text-lg">
                        Ứng dụng luyện đề trắc nghiệm chuẩn cấu trúc tuyển dụng Kỹ sư Phát triển Phần mềm Viettel. Bao gồm 10 bộ đề thi thử với 500 câu hỏi có giải thích chi tiết.
                    </p>
                </div>
                <!-- Mini Stats Panel -->
                <div class="w-full md:w-auto grid grid-cols-2 sm:flex items-center gap-4 z-10">
                    <div class="glass-panel bg-slate-900/80 rounded-2xl p-4 text-center min-w-[120px] flex-1">
                        <div class="text-brand-500 font-display font-extrabold text-3xl sm:text-4xl" id="stats-completed">0</div>
                        <div class="text-xs text-slate-400 mt-1 uppercase font-semibold">Đề hoàn thành</div>
                    </div>
                    <div class="glass-panel bg-slate-900/80 rounded-2xl p-4 text-center min-w-[120px] flex-1">
                        <div class="text-tech-500 font-display font-extrabold text-3xl sm:text-4xl" id="stats-avg">--%</div>
                        <div class="text-xs text-slate-400 mt-1 uppercase font-semibold">Đúng trung bình</div>
                    </div>
                    <div class="glass-panel bg-slate-900/80 rounded-2xl p-4 text-center min-w-[120px] flex-1 col-span-2 sm:col-span-1">
                        <div class="text-success font-display font-extrabold text-3xl sm:text-4xl" id="stats-mistakes">0</div>
                        <div class="text-xs text-slate-400 mt-1 uppercase font-semibold">Sổ tay câu sai</div>
                    </div>
                </div>
            </div>

            <!-- Tabs Section -->
            <div class="flex border-b border-slate-800">
                <button onclick="switchHomeTab('exams')" id="tab-exams-btn" class="px-6 py-3 font-display font-bold text-lg tab-active border-b-2 transition-all">
                    📚 Danh Sách Đề Thi
                </button>
                <button onclick="switchHomeTab('notebook')" id="tab-notebook-btn" class="px-6 py-3 font-display font-bold text-lg text-slate-400 hover:text-white border-b-2 border-transparent transition-all flex items-center gap-2">
                    📓 Sổ Tay Câu Sai
                    <span id="notebook-count-badge" class="px-2 py-0.5 text-xs bg-brand-500/20 text-brand-500 rounded-full hidden">0</span>
                </button>
            </div>

            <!-- EXAMS TAB CONTAINER -->
            <div id="tab-exams-content" class="space-y-6">
                <div class="flex items-center justify-between">
                    <div>
                        <h2 class="font-display text-2xl font-bold text-white">Lựa chọn đề thi thử</h2>
                        <p class="text-slate-400 text-sm">Mỗi đề chứa 50 câu hỏi đa dạng chủ đề, làm trong 45 phút.</p>
                    </div>
                    <button onclick="resetAllData()" class="px-4 py-2 text-xs font-semibold text-slate-400 hover:text-brand-500 hover:bg-brand-500/10 border border-slate-800 rounded-xl transition-all">
                        🔄 Reset tất cả dữ liệu
                    </button>
                </div>
                
                <!-- Exams Grid -->
                <div id="exams-grid" class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <!-- Exam Cards will be injected here -->
                </div>
            </div>

            <!-- MISTAKE NOTEBOOK TAB CONTAINER -->
            <div id="tab-notebook-content" class="space-y-6 hidden">
                <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                    <div>
                        <h2 class="font-display text-2xl font-bold text-white">Sổ tay câu hỏi sai</h2>
                        <p class="text-slate-400 text-sm">Lưu trữ tự động các câu bạn đã trả lời sai để ôn luyện lại.</p>
                    </div>
                    <div class="flex items-center gap-3 w-full sm:w-auto">
                        <button onclick="startMistakesPractice()" id="practice-mistakes-btn" class="flex-1 sm:flex-none bg-brand-500 hover:bg-brand-600 text-white font-bold px-5 py-2.5 rounded-xl shadow-glow-brand transition-all flex items-center justify-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed">
                            ⚡ Luyện tập câu sai
                        </button>
                        <button onclick="clearMistakes()" class="flex-1 sm:flex-none border border-slate-800 hover:bg-slate-800 text-slate-300 font-bold px-5 py-2.5 rounded-xl transition-all">
                            🗑️ Xóa sạch
                        </button>
                    </div>
                </div>

                <div id="mistakes-filter-bar" class="flex flex-wrap gap-2 py-2">
                    <!-- Filters injected here -->
                </div>

                <!-- Mistakes List -->
                <div id="mistakes-list" class="space-y-6">
                    <!-- Mistakes will be injected here -->
                </div>
            </div>
        </section>

        <!-- ============================================== -->
        <!-- EXAM SCREEN (QUIZ PANEL) -->
        <!-- ============================================== -->
        <section id="exam-screen" class="hidden flex-1 flex flex-col gap-6 animate-fade-in">
            <!-- Header Block -->
            <div class="glass-panel rounded-2xl p-4 sm:p-6 shadow-premium border border-slate-800/60 sticky top-4 z-40">
                <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                    <div class="flex items-center gap-3">
                        <button onclick="exitExamPrompt()" class="w-10 h-10 rounded-xl bg-slate-800/80 hover:bg-slate-800 flex items-center justify-center border border-slate-700/50 hover:border-brand-500/50 text-slate-300 hover:text-white transition-all">
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor" class="w-5 h-5">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
                            </svg>
                        </button>
                        <div>
                            <h2 class="font-display font-extrabold text-lg text-white" id="exam-title-display">Đang tải đề thi...</h2>
                            <p class="text-xs text-brand-500 font-semibold tracking-wider uppercase mt-0.5" id="exam-category-display">Chuyên đề</p>
                        </div>
                    </div>
                    
                    <div class="flex items-center gap-4 w-full sm:w-auto justify-between sm:justify-end">
                        <!-- Timer -->
                        <div id="exam-timer-container" class="flex items-center gap-2 bg-slate-900/90 border border-slate-800 px-4 py-2 rounded-xl">
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5 text-brand-500 animate-pulse">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                            <span id="exam-timer" class="font-mono font-bold text-lg text-white">45:00</span>
                        </div>
                        
                        <!-- Submit Button -->
                        <button onclick="submitExamPrompt()" class="bg-brand-500 hover:bg-brand-600 text-white font-extrabold px-6 py-2.5 rounded-xl shadow-glow-brand transform active:scale-95 transition-all">
                            Nộp bài
                        </button>
                    </div>
                </div>
                
                <!-- Progress Line -->
                <div class="w-full bg-slate-950 h-2 rounded-full overflow-hidden mt-5 border border-slate-900">
                    <div id="exam-progress-bar" class="progress-gradient h-full transition-all duration-300 ease-out" style="width: 0%"></div>
                </div>
            </div>

            <!-- Quiz Layout Grid -->
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1 items-start">
                
                <!-- Main Question Panel -->
                <div class="lg:col-span-2 space-y-6">
                    <!-- Question Card -->
                    <div id="exam-question-card" class="glass-panel rounded-3xl p-6 sm:p-10 shadow-premium border border-slate-800/60 relative overflow-hidden">
                        <div class="absolute top-0 left-0 w-2 h-full progress-gradient"></div>
                        <div class="flex items-center justify-between mb-6">
                            <span class="px-3 py-1 rounded-full bg-slate-800/60 border border-slate-700/50 text-xs font-semibold text-slate-300">
                                CÂU <span id="current-q-num">1</span> / 50
                            </span>
                            <span id="immediate-feedback-badge" class="px-3 py-1 rounded-full text-xs font-bold hidden"></span>
                        </div>
                        
                        <div id="exam-question-text" class="text-xl sm:text-2xl font-display font-semibold text-white leading-relaxed mb-8">
                            Nội dung câu hỏi đang được tải...
                        </div>
                        
                        <!-- Options Grid -->
                        <div id="exam-options-grid" class="grid grid-cols-1 gap-4">
                            <!-- Options injected here -->
                        </div>

                        <!-- Explanation Card (Hidden by default, slides down on answered) -->
                        <div id="exam-explanation-panel" class="hidden mt-8 p-6 rounded-2xl bg-slate-900/80 border border-slate-800/80 space-y-3 animate-slide-up">
                            <div class="flex items-center gap-2 text-success font-display font-bold text-base">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5">
                                    <path fill-rule="evenodd" d="M2.25 12c0-5.385 4.365-9.75 9.75-9.75s9.75 4.365 9.75 9.75-4.365 9.75-9.75 9.75S2.25 17.385 2.25 12zm14.022-3.217a.75.75 0 00-1.09-1.09l-4.8 4.8-2.4-2.4a.75.75 0 10-1.09 1.09l3 3a.75.75 0 001.09 0l5.4-5.4z" clip-rule="evenodd" />
                                </svg>
                                Giải Thích Đáp Án:
                            </div>
                            <p id="exam-explanation-text" class="text-sm sm:text-base text-slate-300 leading-relaxed font-medium">
                                Giải thích...
                            </p>
                        </div>
                    </div>

                    <!-- Bottom Nav -->
                    <div class="flex items-center justify-between">
                        <button onclick="prevQuestion()" id="exam-prev-btn" class="flex items-center gap-2 border border-slate-800 hover:bg-slate-800 hover:text-white text-slate-400 font-bold px-6 py-3 rounded-xl transition-all disabled:opacity-30 disabled:cursor-not-allowed">
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor" class="w-5 h-5">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
                            </svg>
                            Quay lại
                        </button>
                        <button onclick="nextQuestion()" id="exam-next-btn" class="flex items-center gap-2 bg-slate-800 hover:bg-slate-700 text-white font-bold px-6 py-3 rounded-xl transition-all">
                            Tiếp theo
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor" class="w-5 h-5">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
                            </svg>
                        </button>
                    </div>
                </div>

                <!-- Right Question Grid Panel -->
                <div class="glass-panel rounded-3xl p-6 shadow-premium border border-slate-800/60 space-y-6">
                    <div class="border-b border-slate-800 pb-4">
                        <h3 class="font-display font-bold text-lg text-white">Sơ đồ câu hỏi</h3>
                        <p class="text-xs text-slate-400 mt-1">Bấm vào số câu để di chuyển nhanh.</p>
                    </div>
                    
                    <div class="grid grid-cols-5 sm:grid-cols-10 lg:grid-cols-5 gap-3" id="exam-question-grid">
                        <!-- Questions dots will be injected here -->
                    </div>
                    
                    <div class="border-t border-slate-800 pt-4 space-y-3">
                        <div class="flex items-center gap-3 text-xs text-slate-400">
                            <span class="w-4 h-4 rounded-lg bg-slate-800 border border-slate-700 inline-block"></span>
                            <span>Chưa làm</span>
                        </div>
                        <div class="flex items-center gap-3 text-xs text-slate-400">
                            <span class="w-4 h-4 rounded-lg bg-slate-700 border border-slate-600 inline-block"></span>
                            <span>Đã chọn đáp án (đang xem)</span>
                        </div>
                        <div class="flex items-center gap-3 text-xs text-slate-400">
                            <span class="w-4 h-4 rounded-lg bg-success/20 border border-success inline-block"></span>
                            <span>Đúng ngay (nếu đã kích hoạt)</span>
                        </div>
                        <div class="flex items-center gap-3 text-xs text-slate-400">
                            <span class="w-4 h-4 rounded-lg bg-error/20 border border-error inline-block"></span>
                            <span>Sai (nếu đã kích hoạt)</span>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- ============================================== -->
        <!-- RESULTS VIEW -->
        <!-- ============================================== -->
        <section id="results-screen" class="hidden space-y-8 animate-fade-in">
            <div class="glass-panel rounded-3xl p-8 sm:p-12 shadow-premium border border-slate-800/60 text-center relative overflow-hidden">
                <div class="absolute -right-20 -top-20 w-80 h-80 bg-success/5 rounded-full blur-[80px] pointer-events-none"></div>
                <div class="absolute -left-20 -bottom-20 w-80 h-80 bg-brand-500/5 rounded-full blur-[80px] pointer-events-none"></div>
                
                <h2 class="font-display text-4xl sm:text-5xl font-extrabold text-white mb-6">Kết quả bài làm</h2>
                
                <div class="flex justify-center mb-8">
                    <div class="relative w-48 h-48 flex items-center justify-center">
                        <!-- SVG Ring -->
                        <svg class="w-full h-full transform -rotate-90">
                            <circle cx="96" cy="96" r="84" stroke="currentColor" stroke-width="12" fill="transparent" class="text-slate-800" />
                            <circle cx="96" cy="96" r="84" stroke="currentColor" stroke-width="12" fill="transparent" stroke-dasharray="527.7" id="result-score-circle" class="text-brand-500 transition-all duration-1000 ease-out" />
                        </svg>
                        <div class="absolute inset-0 flex flex-col items-center justify-center">
                            <span id="result-score-text" class="text-5xl font-display font-extrabold text-white">0</span>
                            <span class="text-slate-400 text-xs uppercase tracking-wider font-semibold mt-1">Câu Đúng</span>
                        </div>
                    </div>
                </div>

                <div class="max-w-md mx-auto grid grid-cols-3 gap-4 mb-8">
                    <div class="bg-slate-900/80 rounded-2xl p-4 border border-slate-800">
                        <p class="text-slate-400 text-xs uppercase font-semibold mb-1">Tổng câu</p>
                        <p id="result-total-display" class="text-2xl font-bold text-white">50</p>
                    </div>
                    <div class="bg-success/10 rounded-2xl p-4 border border-success/20">
                        <p class="text-success text-xs uppercase font-semibold mb-1">Đúng</p>
                        <p id="result-correct-display" class="text-2xl font-bold text-success">0</p>
                    </div>
                    <div class="bg-error/10 rounded-2xl p-4 border border-error/20">
                        <p class="text-error text-xs uppercase font-semibold mb-1">Sai</p>
                        <p id="result-wrong-display" class="text-2xl font-bold text-error">0</p>
                    </div>
                </div>

                <h3 id="result-performance-rating" class="font-display font-extrabold text-2xl text-slate-200 mb-8">RATING...</h3>

                <div class="flex flex-wrap items-center justify-center gap-4">
                    <button onclick="backToHome()" class="bg-slate-800 hover:bg-slate-700 text-white font-bold px-8 py-4 rounded-2xl shadow-lg border border-slate-700/50 hover:border-slate-600 transition-all">
                        🏠 Về Trang Chủ
                    </button>
                    <button onclick="restartCurrentExam()" class="bg-brand-500 hover:bg-brand-600 text-white font-extrabold px-8 py-4 rounded-2xl shadow-glow-brand transition-all">
                        🔄 Luyện Lại Đề Này
                    </button>
                </div>
            </div>

            <!-- Review Questions List -->
            <div class="space-y-6">
                <div class="border-b border-slate-800 pb-4">
                    <h3 class="font-display font-bold text-2xl text-white">Xem lại chi tiết bài làm</h3>
                    <p class="text-slate-400 text-sm mt-1">Xem lại tất cả đáp án kèm theo giải thích chi tiết cho từng câu.</p>
                </div>
                
                <div id="results-review-list" class="space-y-6">
                    <!-- Review cards injected here -->
                </div>
            </div>
        </section>

    </div>

    <!-- FOOTER -->
    <footer class="w-full border-t border-slate-900 bg-slate-950/80 py-6 text-center text-sm text-slate-500 z-10">
        <div class="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-4">
            <div>
                © 2026 Viettel DEV Prep Pro. Biên soạn dựa trên bộ đề ôn tập Viettel 2025.
            </div>
            <div class="flex items-center gap-4">
                <span class="inline-flex items-center gap-1.5 text-xs text-slate-400 bg-slate-900 border border-slate-800 px-3 py-1 rounded-full">
                    <span class="w-1.5 h-1.5 rounded-full bg-success"></span>
                    Hoàn toàn bảo mật & Offline
                </span>
            </div>
        </div>
    </footer>

    <!-- JS Logic -->
    <script>
        // Inject exams data
        const exams = {exams_json_str};

        // App States
        let currentExam = null;
        let currentQuestions = [];
        let currentQuestionIndex = 0;
        let userAnswers = []; // Holds user choice index (0-3) or null
        let isPracticeMistakes = false; // Flag to indicate a special mistake practice session
        let timerInterval = null;
        let timeLeft = 45 * 60; // 45 minutes in seconds
        
        // localStorage states
        let scoreHistory = {{}};
        let mistakeNotebook = [];

        // Load states from localStorage
        function loadHistory() {{
            const scoreSaved = localStorage.getItem('viettel_scores_2025');
            if (scoreSaved) {{
                scoreHistory = JSON.parse(scoreSaved);
            }}
            const mistakesSaved = localStorage.getItem('viettel_mistakes_2025');
            if (mistakesSaved) {{
                mistakeNotebook = JSON.parse(mistakesSaved);
            }}
            
            updateHomeStats();
        }}

        // Save high scores to localStorage
        function saveHighScore(examId, score) {{
            const currentHigh = scoreHistory[examId] || 0;
            if (score > currentHigh) {{
                scoreHistory[examId] = score;
                localStorage.setItem('viettel_scores_2025', JSON.stringify(scoreHistory));
            }}
        }}

        // Add incorrect questions to notebook
        function addToMistakeNotebook(q, chosenOptionIdx) {{
            // Avoid duplicates: check if already in notebook
            const exists = mistakeNotebook.some(item => item.examId === currentExam.id && item.questionId === q.id);
            if (!exists) {{
                mistakeNotebook.push({{
                    examId: currentExam.id,
                    examTitle: currentExam.title,
                    questionId: q.id,
                    questionText: q.question,
                    options: q.options,
                    correctIndex: q.correct,
                    userChosenIndex: chosenOptionIdx,
                    explanation: q.explanation,
                    timestamp: Date.now()
                }});
                localStorage.setItem('viettel_mistakes_2025', JSON.stringify(mistakeNotebook));
            }}
        }}

        // Update Dashboard Stats
        function updateHomeStats() {{
            const completedCount = Object.keys(scoreHistory).length;
            document.getElementById('stats-completed').innerText = completedCount;
            
            if (completedCount > 0) {{
                let total = 0;
                Object.values(scoreHistory).forEach(s => total += s);
                const avg = Math.round((total / (completedCount * 50)) * 100);
                document.getElementById('stats-avg').innerText = avg + '%';
            }} else {{
                document.getElementById('stats-avg').innerText = '--%';
            }}
            
            document.getElementById('stats-mistakes').innerText = mistakeNotebook.length;
            
            const badge = document.getElementById('notebook-count-badge');
            if (mistakeNotebook.length > 0) {{
                badge.innerText = mistakeNotebook.length;
                badge.classList.remove('hidden');
            }} else {{
                badge.classList.add('hidden');
            }}
        }}

        // Reset all data helper
        function resetAllData() {{
            if (confirm('Bạn có chắc chắn muốn xóa toàn bộ lịch sử điểm số và câu hỏi sai? Hành động này không thể hoàn tác.')) {{
                localStorage.clear();
                scoreHistory = {{}};
                mistakeNotebook = [];
                updateHomeStats();
                renderExamsList();
                renderMistakesList();
                alert('Đã xóa toàn bộ lịch sử làm bài!');
            }}
        }}

        // Switch Home Screen Tabs
        function switchHomeTab(tabName) {{
            const examsBtn = document.getElementById('tab-exams-btn');
            const notebookBtn = document.getElementById('tab-notebook-btn');
            const examsContent = document.getElementById('tab-exams-content');
            const notebookContent = document.getElementById('tab-notebook-content');
            
            if (tabName === 'exams') {{
                examsBtn.className = 'px-6 py-3 font-display font-bold text-lg tab-active border-b-2 transition-all';
                notebookBtn.className = 'px-6 py-3 font-display font-bold text-lg text-slate-400 hover:text-white border-b-2 border-transparent transition-all flex items-center gap-2';
                examsContent.classList.remove('hidden');
                notebookContent.classList.add('hidden');
                renderExamsList();
            }} else {{
                notebookBtn.className = 'px-6 py-3 font-display font-bold text-lg tab-active border-b-2 transition-all flex items-center gap-2';
                examsBtn.className = 'px-6 py-3 font-display font-bold text-lg text-slate-400 hover:text-white border-b-2 border-transparent transition-all';
                notebookContent.classList.remove('hidden');
                examsContent.classList.add('hidden');
                renderMistakesList();
            }}
        }}

        // Render standard exams list on Home screen
        function renderExamsList() {{
            const grid = document.getElementById('exams-grid');
            grid.innerHTML = '';
            
            exams.forEach(exam => {{
                const highScore = scoreHistory[exam.id];
                const card = document.createElement('div');
                card.className = `glass-panel rounded-2xl p-6 transition-all duration-300 transform hover:-translate-y-1 relative border border-slate-800/80 cursor-pointer overflow-hidden flex flex-col justify-between min-h-[220px] ${{(exam.id % 2 === 0) ? 'card-tech-glow' : 'card-brand-glow'}}`;
                
                let scoreDisplay = `<span class="text-xs font-semibold text-slate-500 uppercase tracking-widest bg-slate-900 px-3 py-1 rounded-full border border-slate-800">Chưa làm</span>`;
                if (highScore !== undefined) {{
                    const pct = Math.round((highScore / 50) * 100);
                    const colorClass = highScore >= 40 ? 'text-success bg-success/10 border-success/20' : highScore >= 25 ? 'text-tech-500 bg-tech-500/10 border-tech-500/20' : 'text-brand-500 bg-brand-500/10 border-brand-500/20';
                    scoreDisplay = `<span class="text-xs font-extrabold uppercase tracking-wider px-3 py-1 rounded-full border ${{colorClass}}">Điểm cao nhất: ${{highScore}}/50 (${{pct}}%)</span>`;
                }}
                
                card.innerHTML = `
                    <div class="space-y-4">
                        <div class="flex items-center justify-between gap-3">
                            <span class="text-xs font-bold text-brand-500 tracking-wider uppercase">${{exam.category}}</span>
                            ${{scoreDisplay}}
                        </div>
                        <div>
                            <h3 class="font-display font-extrabold text-xl text-white group-hover:text-brand-500 transition-colors leading-tight mb-2">
                                Đề số ${{exam.id}}: ${{exam.title.replace(/^Đề thi thử số \d+:\s*/i, '')}}
                            </h3>
                            <p class="text-slate-400 text-sm leading-relaxed line-clamp-2">
                                50 câu hỏi luyện thi trắc nghiệm công nghệ thông tin và viễn thông chuyên sâu.
                            </p>
                        </div>
                    </div>
                    
                    <div class="flex items-center justify-between border-t border-slate-800/50 pt-4 mt-6">
                        <div class="flex items-center gap-1 text-slate-400 text-xs font-medium">
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4 text-brand-500">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                            </svg>
                            50 Câu Hỏi
                        </div>
                        <div class="flex items-center gap-1 text-slate-400 text-xs font-medium">
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4 text-tech-500">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                            45 Phút
                        </div>
                        <button class="bg-white hover:bg-brand-500 text-slate-900 hover:text-white font-extrabold text-xs px-4 py-2 rounded-xl transition-all shadow-md">
                            Bắt đầu ôn
                        </button>
                    </div>
                `;
                
                card.onclick = () => startExam(exam.id);
                grid.appendChild(card);
            }});
        }}

        // Render Mistakes filter and notebook list on Home screen
        let selectedMistakeFilter = 'all';
        function renderMistakesList() {{
            const listContainer = document.getElementById('mistakes-list');
            const filterBar = document.getElementById('mistakes-filter-bar');
            
            if (mistakeNotebook.length === 0) {{
                filterBar.innerHTML = '';
                listContainer.innerHTML = `
                    <div class="glass-panel rounded-3xl p-12 text-center border border-slate-800/60 max-w-lg mx-auto mt-6">
                        <div class="w-16 h-16 bg-success/10 rounded-full flex items-center justify-center mx-auto mb-4 border border-success/20">
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-8 h-8 text-success">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                        </div>
                        <h3 class="font-display font-bold text-xl text-white mb-2">Tuyệt vời! Bạn không có câu sai nào</h3>
                        <p class="text-slate-400 text-sm">Hãy tiếp tục làm đề. Khi bạn chọn sai, câu hỏi sẽ tự động xuất hiện ở đây để bạn xem lại.</p>
                    </div>
                `;
                document.getElementById('practice-mistakes-btn').disabled = true;
                return;
            }}
            
            document.getElementById('practice-mistakes-btn').disabled = false;
            
            // Build filter bar
            const examIds = [...new Set(mistakeNotebook.map(m => m.examId))].sort((a, b) => a - b);
            filterBar.innerHTML = '';
            
            // "Tất cả" filter
            const allBtn = document.createElement('button');
            allBtn.className = `px-4 py-2 rounded-xl text-xs font-bold transition-all border ${{selectedMistakeFilter === 'all' ? 'bg-brand-500 text-white border-brand-500' : 'bg-slate-900 text-slate-400 border-slate-800 hover:text-white'}}`;
            allBtn.innerText = `Tất cả (${{mistakeNotebook.length}})`;
            allBtn.onclick = () => {{
                selectedMistakeFilter = 'all';
                renderMistakesList();
            }};
            filterBar.appendChild(allBtn);
            
            // Individual exam filters
            examIds.forEach(id => {{
                const count = mistakeNotebook.filter(m => m.examId === id).length;
                const btn = document.createElement('button');
                btn.className = `px-4 py-2 rounded-xl text-xs font-bold transition-all border ${{selectedMistakeFilter === id ? 'bg-brand-500 text-white border-brand-500' : 'bg-slate-900 text-slate-400 border-slate-800 hover:text-white'}}`;
                btn.innerText = `Đề số ${{id}} (${{count}})`;
                btn.onclick = () => {{
                    selectedMistakeFilter = id;
                    renderMistakesList();
                }};
                filterBar.appendChild(btn);
            }});
            
            // Filter mistakes list
            const filteredMistakes = selectedMistakeFilter === 'all' ? mistakeNotebook : mistakeNotebook.filter(m => m.examId === selectedMistakeFilter);
            
            listContainer.innerHTML = '';
            filteredMistakes.forEach((item, index) => {{
                const card = document.createElement('div');
                card.className = 'glass-panel rounded-2xl p-6 border border-slate-800/80 space-y-4 relative overflow-hidden';
                
                // Construct options list with custom colors
                let optionsHtml = '';
                item.options.forEach((opt, idx) => {{
                    const letter = String.fromCharCode(65 + idx);
                    let optClass = 'border-slate-800 bg-slate-950/20 text-slate-400';
                    let badge = '';
                    
                    if (idx === item.correctIndex) {{
                        optClass = 'border-success bg-success/10 text-success';
                        badge = '<span class="ml-auto text-xs bg-success/20 text-success px-2 py-0.5 rounded-md font-bold">Đáp án đúng</span>';
                    }} else if (idx === item.userChosenIndex) {{
                        optClass = 'border-error bg-error/10 text-error';
                        badge = '<span class="ml-auto text-xs bg-error/20 text-error px-2 py-0.5 rounded-md font-bold">Lựa chọn của bạn</span>';
                    }}
                    
                    optionsHtml += `
                        <div class="flex items-center gap-3 p-3.5 rounded-xl border-2 ${{optClass}} text-sm font-medium">
                            <span class="w-7 h-7 rounded-lg bg-slate-900 border border-slate-850 flex items-center justify-center font-bold text-xs">${{letter}}</span>
                            <span>${{opt}}</span>
                            ${{badge}}
                        </div>
                    `;
                }});
                
                card.innerHTML = `
                    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center border-b border-slate-800 pb-3 gap-2">
                        <div class="flex items-center gap-2">
                            <span class="text-xs font-bold text-brand-500 uppercase tracking-widest bg-brand-500/10 px-2.5 py-1 rounded-md border border-brand-500/15">${{item.examTitle.split(':')[0]}}</span>
                            <span class="text-xs text-slate-500 font-semibold">Câu số ${{item.questionId}}</span>
                        </div>
                        <button onclick="removeMistake(${{item.examId}}, ${{item.questionId}})" class="text-xs text-slate-500 hover:text-brand-500 transition-all font-semibold flex items-center gap-1">
                            🗑️ Xóa câu này
                        </button>
                    </div>
                    
                    <h3 class="font-display font-bold text-lg text-white leading-relaxed mt-2">
                        ${{item.questionText}}
                    </h3>
                    
                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 mt-4">
                        ${{optionsHtml}}
                    </div>
                    
                    <div class="mt-4 p-4 rounded-xl bg-slate-900/80 border border-slate-800 space-y-2">
                        <div class="flex items-center gap-1.5 text-xs text-success font-bold font-display uppercase">
                            📖 Giải thích chi tiết:
                        </div>
                        <p class="text-sm text-slate-300 leading-relaxed font-medium">
                            ${{item.explanation}}
                        </p>
                    </div>
                `;
                listContainer.appendChild(card);
            }});
        }}

        // Remove a single mistake from localStorage notebook
        function removeMistake(examId, questionId) {{
            mistakeNotebook = mistakeNotebook.filter(m => !(m.examId === examId && m.questionId === questionId));
            localStorage.setItem('viettel_mistakes_2025', JSON.stringify(mistakeNotebook));
            updateHomeStats();
            renderMistakesList();
        }}

        // Clear all mistakes
        function clearMistakes() {{
            if (confirm('Bạn có chắc muốn xóa toàn bộ sổ tay câu hỏi sai?')) {{
                mistakeNotebook = [];
                localStorage.setItem('viettel_mistakes_2025', JSON.stringify(mistakeNotebook));
                updateHomeStats();
                renderMistakesList();
                alert('Đã dọn dẹp sổ tay câu sai!');
            }}
        }}

        // Start special practice session containing only wrong answers
        function startMistakesPractice() {{
            if (mistakeNotebook.length === 0) return;
            
            isPracticeMistakes = true;
            
            // Build temporary exam object containing the wrong questions
            // Maximum 50 questions for standard flow compatibility
            const practiceQuestions = mistakeNotebook.slice(0, 50).map((m, idx) => ({{
                id: idx + 1,
                originalQuestionId: m.questionId,
                originalExamId: m.examId,
                question: m.questionText,
                options: m.options,
                correct: m.correctIndex,
                explanation: m.explanation
            }}));
            
            currentExam = {{
                id: "mistakes",
                title: "Luyện Tập Lại Câu Sai",
                category: "Sổ Tay Cá Nhân",
                questions: practiceQuestions
            }};
            
            currentQuestions = practiceQuestions;
            launchExamSession();
        }}

        // Start standard exam
        function startExam(examId) {{
            const exam = exams.find(e => e.id === examId);
            if (!exam) return;
            
            isPracticeMistakes = false;
            currentExam = exam;
            currentQuestions = exam.questions;
            
            launchExamSession();
        }}

        // Launch exam screen and initial states
        function launchExamSession() {{
            currentQuestionIndex = 0;
            userAnswers = new Array(currentQuestions.length).fill(null);
            
            // Show Exam Screen
            document.getElementById('home-screen').classList.add('hidden');
            document.getElementById('results-screen').classList.add('hidden');
            document.getElementById('exam-screen').classList.remove('hidden');
            
            // Set Titles
            document.getElementById('exam-title-display').innerText = currentExam.title;
            document.getElementById('exam-category-display').innerText = currentExam.category;
            
            // Render question grid
            renderQuestionGrid();
            
            // Start Timer (45 minutes)
            timeLeft = 45 * 60;
            document.getElementById('exam-timer').innerText = "45:00";
            
            if (timerInterval) clearInterval(timerInterval);
            timerInterval = setInterval(() => {{
                timeLeft--;
                const mins = Math.floor(timeLeft / 60).toString().padStart(2, '0');
                const secs = (timeLeft % 60).toString().padStart(2, '0');
                document.getElementById('exam-timer').innerText = `${{mins}}:${{secs}}`;
                
                if (timeLeft <= 0) {{
                    clearInterval(timerInterval);
                    alert('Hết giờ làm bài! Hệ thống tự động nộp bài.');
                    submitExam();
                }}
            }}, 1000);
            
            // Render first question
            renderQuestion();
        }}

        // Render the 50 dot grids
        function renderQuestionGrid() {{
            const grid = document.getElementById('exam-question-grid');
            grid.innerHTML = '';
            
            currentQuestions.forEach((q, idx) => {{
                const btn = document.createElement('button');
                btn.className = `w-10 h-10 rounded-xl border font-bold text-sm flex items-center justify-center transition-all bg-slate-900 border-slate-800 text-slate-400 hover:border-slate-500 hover:text-white`;
                btn.innerText = q.id;
                btn.id = `q-grid-dot-${{idx}}`;
                btn.onclick = () => {{
                    currentQuestionIndex = idx;
                    renderQuestion();
                }};
                grid.appendChild(btn);
            }});
        }}

        // Render current question contents
        function renderQuestion() {{
            const q = currentQuestions[currentQuestionIndex];
            
            // Highlight current index in the question grid
            for (let i = 0; i < currentQuestions.length; i++) {{
                const dot = document.getElementById(`q-grid-dot-${{i}}`);
                if (dot) {{
                    dot.classList.remove('dot-active');
                    if (userAnswers[i] !== null) {{
                        dot.className = "w-10 h-10 rounded-xl border font-bold text-sm flex items-center justify-center transition-all dot-answered text-white";
                    }} else {{
                        dot.className = "w-10 h-10 rounded-xl border font-bold text-sm flex items-center justify-center transition-all bg-slate-900 border-slate-800 text-slate-400";
                    }}
                }}
            }}
            
            const activeDot = document.getElementById(`q-grid-dot-${{currentQuestionIndex}}`);
            if (activeDot) {{
                activeDot.classList.add('dot-active');
            }}
            
            // Update counter and progress bar
            document.getElementById('current-q-num').innerText = q.id;
            const pct = Math.round(((currentQuestionIndex + 1) / currentQuestions.length) * 100);
            document.getElementById('exam-progress-bar').style.width = pct + '%';
            
            // Set Question Text
            document.getElementById('exam-question-text').innerText = q.question;
            
            // Render Options
            const grid = document.getElementById('exam-options-grid');
            grid.innerHTML = '';
            
            // Feedback elements
            const badge = document.getElementById('immediate-feedback-badge');
            const expPanel = document.getElementById('exam-explanation-panel');
            badge.classList.add('hidden');
            expPanel.classList.add('hidden');
            
            q.options.forEach((opt, idx) => {{
                const btn = document.createElement('button');
                btn.className = `option-card w-full text-left p-5 rounded-2xl border-2 border-slate-800/80 bg-slate-900/40 hover:border-tech-500/50 hover:bg-tech-500/5 flex items-center gap-4 group`;
                
                const letter = String.fromCharCode(65 + idx);
                btn.innerHTML = `
                    <span class="w-10 h-10 rounded-xl bg-slate-950 border border-slate-800 flex items-center justify-center font-bold text-slate-400 group-hover:bg-tech-500 group-hover:text-white transition-colors">${{letter}}</span>
                    <span class="flex-1 font-medium text-slate-200 leading-snug">${{opt}}</span>
                `;
                
                btn.onclick = () => selectOption(idx);
                
                // Show choice states immediately for instant-learning!
                const chosenIdx = userAnswers[currentQuestionIndex];
                if (chosenIdx !== null) {{
                    btn.disabled = true; // Disable clicking after selection
                    
                    if (idx === q.correct) {{
                        btn.className = "option-card w-full text-left p-5 rounded-2xl border-2 flex items-center gap-4 group correct-choice";
                        btn.querySelector('span').style.backgroundColor = '#10b981';
                        btn.querySelector('span').style.color = 'white';
                        btn.querySelector('span').style.borderColor = '#10b981';
                    }} else if (idx === chosenIdx) {{
                        btn.className = "option-card w-full text-left p-5 rounded-2xl border-2 flex items-center gap-4 group incorrect-choice";
                        btn.querySelector('span').style.backgroundColor = '#ef4444';
                        btn.querySelector('span').style.color = 'white';
                        btn.querySelector('span').style.borderColor = '#ef4444';
                    }} else {{
                        btn.className = "option-card w-full text-left p-5 rounded-2xl border-2 border-slate-850 bg-slate-900/10 flex items-center gap-4 opacity-50";
                    }}
                }}
                
                grid.appendChild(btn);
            }});
            
            // If already answered, show the explanation and feedback badge
            const chosenIdx = userAnswers[currentQuestionIndex];
            if (chosenIdx !== null) {{
                const isCorrect = chosenIdx === q.correct;
                badge.innerText = isCorrect ? 'ĐÚNG' : 'SAI';
                badge.className = `px-3 py-1 rounded-full text-xs font-bold ${{isCorrect ? 'bg-success/20 text-success' : 'bg-error/20 text-error'}}`;
                badge.classList.remove('hidden');
                
                // Load Explanation text
                document.getElementById('exam-explanation-text').innerText = q.explanation;
                expPanel.classList.remove('hidden');
            }}
            
            // Manage Navigation buttons
            document.getElementById('exam-prev-btn').disabled = currentQuestionIndex === 0;
            const nextBtn = document.getElementById('exam-next-btn');
            if (currentQuestionIndex === currentQuestions.length - 1) {{
                nextBtn.innerHTML = `
                    Xong / Nộp Bài
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5">
                        <path fill-rule="evenodd" d="M19.916 4.626a.75.75 0 01.208 1.04l-9 13.5a.75.75 0 01-1.154.114l-6-6a.75.75 0 011.06-1.06l5.353 5.353 8.493-12.739a.75.75 0 011.04-.208z" clip-rule="evenodd" />
                    </svg>
                `;
            }} else {{
                nextBtn.innerHTML = `
                    Tiếp theo
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor" class="w-5 h-5">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
                    </svg>
                `;
            }}
        }}

        // Choose option and trigger immediate feedback
        function selectOption(optionIdx) {{
            if (userAnswers[currentQuestionIndex] !== null) return; // Answered already
            
            userAnswers[currentQuestionIndex] = optionIdx;
            
            // Automatically capture wrong answers to Mistake Notebook
            const q = currentQuestions[currentQuestionIndex];
            if (optionIdx !== q.correct) {{
                addToMistakeNotebook(q, optionIdx);
            }}
            
            renderQuestion();
            updateHomeStats(); // Update mistakes badges count
        }}

        // Navigation
        function nextQuestion() {{
            if (currentQuestionIndex < currentQuestions.length - 1) {{
                currentQuestionIndex++;
                renderQuestion();
            }} else {{
                submitExamPrompt();
            }}
        }}

        function prevQuestion() {{
            if (currentQuestionIndex > 0) {{
                currentQuestionIndex--;
                renderQuestion();
            }}
        }}

        // Prompts
        function exitExamPrompt() {{
            if (confirm('Bạn có thực sự muốn rời bài làm hiện tại? Mọi tiến trình sẽ bị mất.')) {{
                if (timerInterval) clearInterval(timerInterval);
                backToHome();
            }}
        }}

        function submitExamPrompt() {{
            const unanswered = userAnswers.filter(a => a === null).length;
            let msg = 'Bạn có chắc chắn muốn nộp bài thi?';
            if (unanswered > 0) {{
                msg = `Bạn còn ${{unanswered}} câu chưa làm. Bạn có thực sự muốn nộp bài ngay bây giờ?`;
            }}
            if (confirm(msg)) {{
                submitExam();
            }}
        }}

        // Process submission and show scores
        function submitExam() {{
            if (timerInterval) clearInterval(timerInterval);
            
            // Switch screen
            document.getElementById('exam-screen').classList.add('hidden');
            document.getElementById('results-screen').classList.remove('hidden');
            
            // Calculate scores
            let correctCount = 0;
            currentQuestions.forEach((q, idx) => {{
                if (userAnswers[idx] === q.correct) {{
                    correctCount++;
                }}
            }});
            
            const wrongCount = currentQuestions.length - correctCount;
            const pct = Math.round((correctCount / currentQuestions.length) * 100);
            
            // Save Score (If not a practice-mistakes session)
            if (!isPracticeMistakes) {{
                saveHighScore(currentExam.id, correctCount);
            }}
            
            // Update HTML scores
            document.getElementById('result-total-display').innerText = currentQuestions.length;
            document.getElementById('result-correct-display').innerText = correctCount;
            document.getElementById('result-wrong-display').innerText = wrongCount;
            
            // Score circle animation
            let scoreProgress = 0;
            const circle = document.getElementById('result-score-circle');
            const scoreText = document.getElementById('result-score-text');
            const circumference = 527.7; // 2 * pi * 84
            
            const interval = setInterval(() => {{
                if (scoreProgress >= correctCount) {{
                    clearInterval(interval);
                }} else {{
                    scoreProgress++;
                    scoreText.innerText = scoreProgress;
                    const offset = circumference - (scoreProgress / currentQuestions.length) * circumference;
                    circle.style.strokeDashoffset = offset;
                }}
            }}, 25);
            
            // Set exact offset instantly in case score is 0
            if (correctCount === 0) {{
                scoreText.innerText = "0";
                circle.style.strokeDashoffset = circumference;
            }} else {{
                // Trigger transition
                setTimeout(() => {{
                    const offset = circumference - (correctCount / currentQuestions.length) * circumference;
                    circle.style.strokeDashoffset = offset;
                }}, 50);
            }}

            // Score evaluation
            let rating = '';
            if (pct >= 90) rating = '🌟 Xuất Sắc! Bạn đã hoàn toàn sẵn sàng cho kì thi tuyển dụng.';
            else if (pct >= 80) rating = '✨ Rất Tốt! Kiến thức rất vững vàng.';
            else if (pct >= 50) rating = '👍 Đạt Yêu Cầu! Hãy cố gắng luyện tập thêm để tự tin hơn.';
            else rating = '📚 Cần Luyện Tập Thêm! Cố gắng cải thiện các chủ đề bạn làm sai.';
            
            document.getElementById('result-performance-rating').innerText = rating;
            
            // Render detailed reviews list
            renderReviewList();
            
            updateHomeStats();
        }}

        // Render full reviews list with answers
        function renderReviewList() {{
            const list = document.getElementById('results-review-list');
            list.innerHTML = '';
            
            currentQuestions.forEach((q, idx) => {{
                const userChoice = userAnswers[idx];
                const isCorrect = userChoice === q.correct;
                
                const card = document.createElement('div');
                card.className = `glass-panel rounded-2xl p-6 border-l-4 border ${{isCorrect ? 'border-l-success border-slate-800' : 'border-l-error border-slate-800'}} space-y-4`;
                
                let optionsHtml = '';
                q.options.forEach((opt, oIdx) => {{
                    const letter = String.fromCharCode(65 + oIdx);
                    let optClass = 'border-slate-800 bg-slate-950/20 text-slate-400';
                    let badge = '';
                    
                    if (oIdx === q.correct) {{
                        optClass = 'border-success bg-success/10 text-success font-semibold';
                        badge = '<span class="ml-auto text-xs bg-success/20 text-success px-2 py-0.5 rounded font-bold">Đáp án đúng</span>';
                    }} else if (oIdx === userChoice) {{
                        optClass = 'border-error bg-error/10 text-error font-semibold';
                        badge = '<span class="ml-auto text-xs bg-error/20 text-error px-2 py-0.5 rounded font-bold">Lựa chọn của bạn</span>';
                    }}
                    
                    optionsHtml += `
                        <div class="flex items-center gap-3 p-3 rounded-xl border-2 ${{optClass}} text-sm">
                            <span class="w-6 h-6 rounded-lg bg-slate-900 border border-slate-800 flex items-center justify-center font-bold text-[10px]">${{letter}}</span>
                            <span>${{opt}}</span>
                            ${{badge}}
                        </div>
                    `;
                }});
                
                card.innerHTML = `
                    <div class="flex items-center justify-between border-b border-slate-850 pb-2.5">
                        <div class="flex items-center gap-2">
                            <span class="w-7 h-7 rounded-full flex items-center justify-center font-bold text-xs ${{isCorrect ? 'bg-success/25 text-success' : 'bg-error/25 text-error'}}">
                                ${{q.id}}
                            </span>
                            <span class="text-xs font-semibold text-slate-500">
                                ${{isCorrect ? 'Chính xác' : userChoice === null ? 'Chưa trả lời' : 'Không chính xác'}}
                            </span>
                        </div>
                    </div>
                    
                    <h3 class="font-display font-bold text-lg text-slate-200 leading-relaxed">
                        ${{q.question}}
                    </h3>
                    
                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 mt-4">
                        ${{optionsHtml}}
                    </div>
                    
                    <div class="mt-4 p-4 rounded-xl bg-slate-900/80 border border-slate-800 space-y-2">
                        <div class="flex items-center gap-1.5 text-xs text-success font-bold font-display uppercase">
                            📖 Giải thích chi tiết:
                        </div>
                        <p class="text-sm text-slate-300 leading-relaxed font-medium">
                            ${{q.explanation}}
                        </p>
                    </div>
                `;
                
                list.appendChild(card);
            }});
        }}

        // Back to selection screen
        function backToHome() {{
            currentExam = null;
            currentQuestions = [];
            
            document.getElementById('exam-screen').classList.add('hidden');
            document.getElementById('results-screen').classList.add('hidden');
            document.getElementById('home-screen').classList.remove('hidden');
            
            renderExamsList();
            updateHomeStats();
        }}

        // Restart current quiz
        function restartCurrentExam() {{
            if (isPracticeMistakes) {{
                startMistakesPractice();
            }} else {{
                startExam(currentExam.id);
            }}
        }}

        // Listen for Keyboard Navigation (Prev / Next question arrows)
        document.addEventListener('keydown', (e) => {{
            if (document.getElementById('exam-screen').classList.contains('hidden')) return;
            
            if (e.key === 'ArrowRight') {{
                nextQuestion();
            }} else if (e.key === 'ArrowLeft') {{
                prevQuestion();
            }}
        }});

        // INITIAL BOOTSTRAP
        window.onload = () => {{
            loadHistory();
            renderExamsList();
        }};
    </script>
</body>
</html>
"""

with open(html_path, "w", encoding="utf-8") as f:
    f.write(html_content)

print(f"App generated and saved perfectly to {html_path}!")
