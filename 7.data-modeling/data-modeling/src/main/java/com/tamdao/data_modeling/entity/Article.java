package com.tamdao.data_modeling.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "title_translations", columnDefinition = "json")
    private Map<String, String> titleTranslations = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "json")
    private List<String> tags = new ArrayList<>();

    @Column(name = "status")
    private String status;
}

