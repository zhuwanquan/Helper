package com.example.helper.Service;

import com.example.helper.Common.Exception.BusinessException;
import com.example.helper.Entity.Meal;
import com.example.helper.Repository.MealRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MealService {

    private final MealRepository mealRepository;

    // 构造函数注入（推荐方式）
    public MealService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    // 分页获取所有菜品
    @Transactional(readOnly = true)
    public Page<Meal> getAllMealsPaginated(Pageable pageable) {
        return mealRepository.findAll(pageable);
    }

    // 分页获取选中的菜品
    @Transactional(readOnly = true)
    public Page<Meal> getSelectedMealsPaginated(Pageable pageable) {
        return mealRepository.findByCheckedTrue(pageable);
    }

    // 根据ID获取菜品（新增方法）
    @Transactional(readOnly = true)
    public Optional<Meal> getMealById(Long id) {
        return mealRepository.findById(id);
    }

    // 添加新菜品
    @Transactional
    public Meal addMeal(Meal meal) {
        return mealRepository.save(meal);
    }

    // 批量添加菜品
    @Transactional
    public List<Meal> addMealsBatch(List<Meal> meals) {
        return meals.stream().map(mealRepository::save).collect(Collectors.toList());
    }

    // 更新菜品信息
    @Transactional
    public Meal updateMeal(Long id, Meal updatedMeal) {
        Meal existingMeal =
                mealRepository
                        .findById(id)
                        .orElseThrow(() -> new BusinessException(404, "菜品不存在，ID: " + id));

        updateMealFields(existingMeal, updatedMeal);
        return mealRepository.save(existingMeal);
    }

    // 批量更新菜品
    @Transactional
    public List<Meal> updateMealsBatch(List<Meal> meals) {
        return meals.stream()
                .map(
                        meal -> {
                            Meal existingMeal =
                                    mealRepository
                                            .findById(meal.getId())
                                            .orElseThrow(
                                                    () ->
                                                            new BusinessException(
                                                                    404,
                                                                    "菜品不存在，ID: " + meal.getId()));
                            updateMealFields(existingMeal, meal);
                            return mealRepository.save(existingMeal);
                        })
                .collect(Collectors.toList());
    }

    // 删除菜品
    @Transactional
    public void deleteMeal(Long id) {
        if (!mealRepository.existsById(id)) {
            throw new BusinessException(404, "菜品不存在，ID: " + id);
        }
        mealRepository.deleteById(id);
    }

    // 批量删除菜品
    @Transactional
    public void deleteMealsBatch(List<Long> ids) {
        List<Meal> mealsToDelete = mealRepository.findAllById(ids);
        if (mealsToDelete.size() != ids.size()) {
            throw new BusinessException(404, "部分菜品不存在");
        }
        mealRepository.deleteAll(mealsToDelete);
    }

    // 切换菜品选中状态
    @Transactional
    public Meal toggleMealSelection(Long id) {
        Meal meal =
                mealRepository
                        .findById(id)
                        .orElseThrow(() -> new BusinessException(404, "菜品不存在，ID: " + id));
        meal.setChecked(!meal.getChecked());
        return mealRepository.save(meal);
    }

    // 获取所有已选中的菜品
    @Transactional(readOnly = true)
    public List<Meal> getSelectedMeals() {
        return mealRepository.findByCheckedTrue();
    }

    // 根据标题搜索菜品
    @Transactional(readOnly = true)
    public List<Meal> searchMealsByTitle(String title) {
        return mealRepository.findByTitleContaining(title);
    }

    // 统计选中菜品数量
    @Transactional(readOnly = true)
    public long countSelectedMeals() {
        return mealRepository.countByCheckedTrue();
    }

    // 统计总菜品数量
    @Transactional(readOnly = true)
    public long countMeals() {
        return mealRepository.count();
    }

    // 私有方法：更新菜品字段
    private void updateMealFields(Meal existingMeal, Meal updatedMeal) {
        // 提取公共字段更新逻辑
        copyMealFields(existingMeal, updatedMeal);
        existingMeal.setChecked(updatedMeal.getChecked());
    }

    // 私有方法：复制菜品基础字段
    private void copyMealFields(Meal target, Meal source) {
        target.setTitle(source.getTitle());
        target.setImageUrl(source.getImageUrl());
        target.setEnergy(source.getEnergy());
        target.setProtein(source.getProtein());
        target.setTransFat(source.getTransFat());
        target.setSaturatedFat(source.getSaturatedFat());
        target.setCarbohydrate(source.getCarbohydrate());
        target.setAddedSugar(source.getAddedSugar());
        target.setSalt(source.getSalt());
        target.setDietaryFiber(source.getDietaryFiber());
    }
}
