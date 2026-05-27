package com.tamdao.data_modeling.repository;

import com.tamdao.data_modeling.entity.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {

    // Lấy toàn bộ danh sách đã sắp xếp theo order_index tăng dần
    List<TodoItem> findAllByOrderByOrderIndexAsc();

    // Lấy phần tử ngay trước một vị trí (order_index nhỏ hơn và lớn nhất)
    @Query("SELECT t FROM TodoItem t WHERE t.orderIndex < :orderIndex ORDER BY t.orderIndex DESC LIMIT 1")
    Optional<TodoItem> findPreviousItem(@Param("orderIndex") Double orderIndex);

    // Lấy phần tử ngay sau một vị trí (order_index lớn hơn và nhỏ nhất)
    @Query("SELECT t FROM TodoItem t WHERE t.orderIndex > :orderIndex ORDER BY t.orderIndex ASC LIMIT 1")
    Optional<TodoItem> findNextItem(@Param("orderIndex") Double orderIndex);

    // Lấy phần tử có order_index lớn nhất hiện tại (để thêm phần tử mới vào cuối)
    @Query("SELECT t FROM TodoItem t ORDER BY t.orderIndex DESC LIMIT 1")
    Optional<TodoItem> findLastItem();
}
