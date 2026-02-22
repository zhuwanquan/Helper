package com.example.helper.Service;

import com.example.helper.Common.Exception.BusinessException;
import com.example.helper.Entity.Meal;
import com.example.helper.Repository.MealRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class MealService {

    @Autowired private MealRepository mealRepository;

    @Autowired
    @Qualifier("taskExecutor") private Executor taskExecutor;

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

    // 异步获取所有菜品
    @Async("taskExecutor")
    public CompletableFuture<List<Meal>> getAllMealsAsync() {
        return CompletableFuture.completedFuture(mealRepository.findAll());
    }

    // 获取所有菜品（同步版本保持兼容性）
    @Cacheable(value = "meals", key = "#root.methodName")
    @Transactional(readOnly = true)
    public List<Meal> getAllMeals() {
        return mealRepository.findAll();
    }

    // 异步根据 ID 获取菜品
    @Async("taskExecutor")
    public CompletableFuture<Optional<Meal>> getMealByIdAsync(Long id) {
        return CompletableFuture.completedFuture(mealRepository.findById(id));
    }

    // 根据 ID 获取菜品（同步版本）
    @Cacheable(value = "meal", key = "#id")
    @Transactional(readOnly = true)
    public Optional<Meal> getMealById(Long id) {
        return mealRepository.findById(id);
    }

    // 异步添加新菜品
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Meal> addMealAsync(Meal meal) {
        Meal savedMeal = mealRepository.save(meal);
        return CompletableFuture.completedFuture(savedMeal);
    }

    // 添加新菜品（同步版本）
    @CacheEvict(value = "meals", allEntries = true)
    @Transactional
    public Meal addMeal(Meal meal) {
        return mealRepository.save(meal);
    }

    // 批量添加菜品
    @Transactional
    public List<Meal> addMealsBatch(List<Meal> meals) {
        return meals.stream().map(mealRepository::save).collect(Collectors.toList());
    }

    // 异步更新菜品信息
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Meal> updateMealAsync(Long id, Meal updatedMeal) {
        Meal existingMeal =
                mealRepository
                        .findById(id)
                        .orElseThrow(() -> new BusinessException(404, "菜品不存在，ID: " + id));

        updateMealFields(existingMeal, updatedMeal);
        Meal savedMeal = mealRepository.save(existingMeal);
        return CompletableFuture.completedFuture(savedMeal);
    }

    // 更新菜品信息（同步版本）
    @CachePut(value = "meal", key = "#id")
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

    // 异步删除菜品
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Void> deleteMealAsync(Long id) {
        if (!mealRepository.existsById(id)) {
            throw new BusinessException(404, "菜品不存在，ID: " + id);
        }
        mealRepository.deleteById(id);
        return CompletableFuture.completedFuture(null);
    }

    // 删除菜品（同步版本）
    @CacheEvict(
            value = {"meal", "meals"},
            allEntries = true)
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

    // 私有方法：更新菜品字段
    private void updateMealFields(Meal existingMeal, Meal updatedMeal) {
        existingMeal.setTitle(updatedMeal.getTitle());
        existingMeal.setImageUrl(updatedMeal.getImageUrl());
        existingMeal.setEnergy(updatedMeal.getEnergy());
        existingMeal.setProtein(updatedMeal.getProtein());
        existingMeal.setTransFat(updatedMeal.getTransFat());
        existingMeal.setSaturatedFat(updatedMeal.getSaturatedFat());
        existingMeal.setCarbohydrate(updatedMeal.getCarbohydrate());
        existingMeal.setAddedSugar(updatedMeal.getAddedSugar());
        existingMeal.setSalt(updatedMeal.getSalt());
        existingMeal.setDietaryFiber(updatedMeal.getDietaryFiber());
        existingMeal.setChecked(updatedMeal.getChecked());
    }

    // 异步获取所有已选中的菜品
    @Async("taskExecutor")
    public CompletableFuture<List<Meal>> getSelectedMealsAsync() {
        return CompletableFuture.completedFuture(mealRepository.findByCheckedTrue());
    }

    // 获取所有已选中的菜品（同步版本）
    @Cacheable(value = "selectedMeals", key = "#root.methodName")
    @Transactional(readOnly = true)
    public List<Meal> getSelectedMeals() {
        return mealRepository.findByCheckedTrue();
    }

    // 异步根据标题搜索菜品
    @Async("taskExecutor")
    public CompletableFuture<List<Meal>> searchMealsByTitleAsync(String title) {
        return CompletableFuture.completedFuture(mealRepository.findByTitleContaining(title));
    }

    // 根据标题搜索菜品（同步版本）
    @Transactional(readOnly = true)
    public List<Meal> searchMealsByTitle(String title) {
        return mealRepository.findByTitleContaining(title);
    }

    // 批量异步添加菜品
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<List<Meal>> addMealsBatchAsync(List<Meal> meals) {
        List<Meal> savedMeals =
                meals.stream().map(mealRepository::save).collect(Collectors.toList());
        return CompletableFuture.completedFuture(savedMeals);
    }

    // 批量异步更新菜品
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<List<Meal>> updateMealsBatchAsync(List<Meal> meals) {
        List<Meal> updatedMeals =
                meals.stream()
                        .map(
                                meal -> {
                                    Meal existingMeal =
                                            mealRepository
                                                    .findById(meal.getId())
                                                    .orElseThrow(
                                                            () ->
                                                                    new BusinessException(
                                                                            404,
                                                                            "菜品不存在，ID: "
                                                                                    + meal
                                                                                            .getId()));
                                    updateMealFields(existingMeal, meal);
                                    return mealRepository.save(existingMeal);
                                })
                        .collect(Collectors.toList());
        return CompletableFuture.completedFuture(updatedMeals);
    }

    // 统计选中菜品数量
    @Transactional(readOnly = true)
    public long countSelectedMeals() {
        return mealRepository.countByCheckedTrue();
    }

    // 统计总菜品数量
    @Cacheable(value = "mealStats", key = "#root.methodName")
    @Transactional(readOnly = true)
    public long countMeals() {
        return mealRepository.count();
    }

    // 异步统计菜品总数
    @Async("taskExecutor")
    public CompletableFuture<Long> countMealsAsync() {
        return CompletableFuture.completedFuture(mealRepository.count());
    }

    // 异步检查菜品是否存在
    @Async("taskExecutor")
    public CompletableFuture<Boolean> existsMealAsync(Long id) {
        return CompletableFuture.completedFuture(mealRepository.existsById(id));
    }
}
