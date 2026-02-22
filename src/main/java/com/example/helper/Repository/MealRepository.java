package com.example.helper.Repository;

import com.example.helper.Entity.Meal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    // 查询所有已选中的菜品
    List<Meal> findByCheckedTrue();

    // 分页查询所有已选中的菜品
    Page<Meal> findByCheckedTrue(Pageable pageable);

    // 根据标题模糊查询菜品
    List<Meal> findByTitleContaining(String title);

    // 统计已选中的菜品数量
    long countByCheckedTrue();
}
