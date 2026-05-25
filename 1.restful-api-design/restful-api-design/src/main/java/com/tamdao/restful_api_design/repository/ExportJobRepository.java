package com.tamdao.restful_api_design.repository;

import com.tamdao.restful_api_design.model.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, String> {
}
