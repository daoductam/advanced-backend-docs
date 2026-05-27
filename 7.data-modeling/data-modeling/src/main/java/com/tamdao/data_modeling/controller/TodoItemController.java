package com.tamdao.data_modeling.controller;

import com.tamdao.data_modeling.entity.TodoItem;
import com.tamdao.data_modeling.repository.TodoItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/todos")
public class TodoItemController {

    // Bước nhảy mặc định giữa các phần tử mới thêm vào cuối danh sách
    private static final double DEFAULT_STEP = 1000.0;

    @Autowired
    private TodoItemRepository todoItemRepository;

    /**
     * Lấy toàn bộ danh sách To-Do, đã sắp xếp theo thứ tự tăng dần của order_index.
     */
    @GetMapping
    public ResponseEntity<List<TodoItem>> getAllTodos() {
        return ResponseEntity.ok(todoItemRepository.findAllByOrderByOrderIndexAsc());
    }

    /**
     * Thêm công việc mới vào CUỐI danh sách.
     * Logic: Lấy order_index của phần tử cuối cùng rồi cộng thêm DEFAULT_STEP (1000.0).
     * Nếu danh sách đang trống, phần tử đầu tiên sẽ có order_index = 1000.0.
     */
    @PostMapping
    public ResponseEntity<TodoItem> addTodo(@RequestBody TodoItem todoItem) {
        Optional<TodoItem> lastItem = todoItemRepository.findLastItem();
        double newOrderIndex = lastItem
                .map(item -> item.getOrderIndex() + DEFAULT_STEP)
                .orElse(DEFAULT_STEP);

        todoItem.setOrderIndex(newOrderIndex);
        return ResponseEntity.ok(todoItemRepository.save(todoItem));
    }

    /**
     * DI CHUYỂN một công việc (targetId) vào ngay SAU một công việc khác (afterId).
     *
     * Thuật toán Fractional Indexing:
     * - Lấy order_index của phần tử đứng ngay SAU `afterId` (gọi là nextItem)
     * - order_index mới = (orderIndex(afterId) + orderIndex(nextItem)) / 2
     * - Nếu `afterId` là phần tử CUỐI cùng, gán order_index mới = orderIndex(afterId) + DEFAULT_STEP
     *
     * Ưu điểm: KHÔNG cần UPDATE bất kỳ phần tử nào khác trong danh sách!
     */
    @PutMapping("/{targetId}/move-after/{afterId}")
    public ResponseEntity<?> moveAfter(
            @PathVariable Long targetId,
            @PathVariable Long afterId) {

        TodoItem target = todoItemRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Todo item không tồn tại: " + targetId));
        TodoItem after = todoItemRepository.findById(afterId)
                .orElseThrow(() -> new RuntimeException("Todo item không tồn tại: " + afterId));

        // Tìm phần tử đứng ngay sau `after` trong danh sách hiện tại
        Optional<TodoItem> nextItem = todoItemRepository.findNextItem(after.getOrderIndex());

        double newOrderIndex;
        if (nextItem.isPresent() && !nextItem.get().getId().equals(targetId)) {
            // Chèn vào GIỮA: order_index mới = (A + B) / 2
            newOrderIndex = (after.getOrderIndex() + nextItem.get().getOrderIndex()) / 2.0;
        } else {
            // Chèn vào CUỐI: order_index mới = A + DEFAULT_STEP
            newOrderIndex = after.getOrderIndex() + DEFAULT_STEP;
        }

        target.setOrderIndex(newOrderIndex);
        todoItemRepository.save(target);

        // Trả về toàn bộ danh sách sau khi sắp xếp lại
        return ResponseEntity.ok(todoItemRepository.findAllByOrderByOrderIndexAsc());
    }
}
