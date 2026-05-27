package com.tamdao.data_modeling.repository;

import com.tamdao.data_modeling.entity.Homestay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomestayRepository extends JpaRepository<Homestay, Long> {
}
