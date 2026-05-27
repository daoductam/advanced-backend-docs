package com.tamdao.data_modeling.controller;

import com.tamdao.data_modeling.entity.Article;
import com.tamdao.data_modeling.repository.ArticleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@Tag(name = "Multiple Languages & Tagging", description = "APIs quản lý bài viết hỗ trợ đa ngôn ngữ và gắn thẻ nhãn dán")
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @PostMapping
    @Operation(summary = "Tạo mới bài viết có hỗ trợ dịch tiêu đề và mảng nhãn dán (tags)")
    public ResponseEntity<Article> createArticle(@RequestBody Article article) {
        Article saved = articleRepository.save(article);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả bài viết")
    public ResponseEntity<List<Article>> getAllArticles() {
        return ResponseEntity.ok(articleRepository.findAll());
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm bài viết theo bản dịch tiêu đề trong cột JSON")
    public ResponseEntity<List<Article>> searchArticles(
            @RequestParam("lang") String lang,
            @RequestParam("title") String title) {
        // Xây dựng đường dẫn JSON Path trong MySQL (ví dụ: "$.en" hoặc "$.vi")
        String jsonPath = "$." + lang;
        List<Article> results = articleRepository.findByTitleTranslation(jsonPath, title);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/by-tag")
    @Operation(summary = "Tìm kiếm bài viết theo nhãn dán (MEMBER OF tags)")
    public ResponseEntity<List<Article>> getArticlesByTag(@RequestParam("tag") String tag) {
        List<Article> results = articleRepository.findByTag(tag);
        return ResponseEntity.ok(results);
    }
}

