package com.tamdao.data_modeling.repository;

import com.tamdao.data_modeling.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query(value = "SELECT * FROM articles WHERE title_translations->>:jsonPath = :title", nativeQuery = true)
    List<Article> findByTitleTranslation(@Param("jsonPath") String jsonPath, @Param("title") String title);

    @Query(value = "SELECT * FROM articles WHERE :tag MEMBER OF (tags)", nativeQuery = true)
    List<Article> findByTag(@Param("tag") String tag);
}

