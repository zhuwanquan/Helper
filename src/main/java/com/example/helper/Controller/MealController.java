package com.example.helper.Controller;

import com.example.helper.Common.Exception.BusinessException;
import com.example.helper.Dto.MealDTO;
import com.example.helper.Entity.Meal;
import com.example.helper.Mapper.MealMapper;
import com.example.helper.Service.MealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/meals")
@Tag(name = "菜单管理", description = "菜品管理相关接口")
public class MealController {

    @Autowired private MealService mealService;

    // 获取所有菜品（支持分页和排序）
    @GetMapping
    @Operation(summary = "获取菜品列表", description = "返回系统中所有的菜品信息，支持分页、排序和筛选")
    @ApiResponse(responseCode = "200", description = "成功获取菜品列表")
    public ResponseEntity<com.example.helper.Common.Util.ApiResponse<Map<String, Object>>>
            getAllMeals(
                    @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
                    @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
                    @Parameter(description = "排序字段") @RequestParam(defaultValue = "id")
                            String sortBy,
                    @Parameter(description = "排序方向") @RequestParam(defaultValue = "asc")
                            String sortDir,
                    @Parameter(description = "是否只返回选中的菜品") @RequestParam(required = false)
                            Boolean selected) {

        try {
            Sort sort =
                    sortDir.equalsIgnoreCase("desc")
                            ? Sort.by(sortBy).descending()
                            : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Meal> mealPage;
            if (selected != null && selected) {
                mealPage = mealService.getSelectedMealsPaginated(pageable);
            } else {
                mealPage = mealService.getAllMealsPaginated(pageable);
            }

            List<MealDTO> mealDTOs =
                    mealPage.getContent().stream()
                            .map(MealMapper::toDTO)
                            .collect(Collectors.toList());

            Map<String, Object> response =
                    Map.of(
                            "content",
                            mealDTOs,
                            "page",
                            page,
                            "size",
                            size,
                            "totalElements",
                            mealPage.getTotalElements(),
                            "totalPages",
                            mealPage.getTotalPages(),
                            "first",
                            mealPage.isFirst(),
                            "last",
                            mealPage.isLast(),
                            "sort",
                            sortBy + "," + sortDir);

            return ResponseEntity.ok(
                    com.example.helper.Common.Util.ApiResponse.success("获取菜品列表成功", response));
        } catch (Exception e) {
            log.error("获取菜品列表失败", e);
            throw new BusinessException(500, "获取菜品列表失败: " + e.getMessage());
        }
    }

    // 根据 ID 获取菜品详情
    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 获取菜品", description = "根据菜品ID获取单个菜品的详细信息")
    @ApiResponse(responseCode = "200", description = "成功获取菜品详情")
    @ApiResponse(responseCode = "404", description = "菜品不存在")
    public ResponseEntity<com.example.helper.Common.Util.ApiResponse<MealDTO>> getMealById(
            @Parameter(description = "菜品ID") @PathVariable Long id) {
        try {
            Meal meal =
                    mealService
                            .getMealById(id)
                            .orElseThrow(() -> new BusinessException(404, "菜品不存在，ID: " + id));
            return ResponseEntity.ok(
                    com.example.helper.Common.Util.ApiResponse.success(
                            "获取菜品详情成功", MealMapper.toDTO(meal)));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取菜品详情失败，ID: {}", id, e);
            throw new BusinessException(500, "获取菜品详情失败: " + e.getMessage());
        }
    }

    // 添加新菜品
    @PostMapping
    @Operation(summary = "添加新菜品", description = "创建一个新的菜品记录")
    @ApiResponse(responseCode = "200", description = "菜品添加成功")
    @ApiResponse(responseCode = "400", description = "请求参数无效")
    public ResponseEntity<com.example.helper.Common.Util.ApiResponse<MealDTO>> addMeal(
            @Parameter(description = "菜品信息") @Valid @RequestBody Meal meal) {
        try {
            Meal savedMeal = mealService.addMeal(meal);
            MealDTO mealDTO = MealMapper.toDTO(savedMeal);
            return ResponseEntity.ok(
                    com.example.helper.Common.Util.ApiResponse.success("菜品添加成功", mealDTO));
        } catch (Exception e) {
            log.error("添加菜品失败", e);
            throw new BusinessException(500, "添加菜品失败: " + e.getMessage());
        }
    }

    // 批量添加菜品
    @PostMapping("/batch")
    @Operation(summary = "批量添加菜品", description = "批量创建多个菜品记录")
    public ResponseEntity<com.example.helper.Common.Util.ApiResponse<List<MealDTO>>> addMealsBatch(
            @Parameter(description = "菜品列表") @Valid @RequestBody List<Meal> meals) {
        try {
            List<Meal> savedMeals = mealService.addMealsBatch(meals);
            List<MealDTO> mealDTOs =
                    savedMeals.stream().map(MealMapper::toDTO).collect(Collectors.toList());
            return ResponseEntity.ok(
                    com.example.helper.Common.Util.ApiResponse.success("批量添加菜品成功", mealDTOs));
        } catch (Exception e) {
            log.error("批量添加菜品失败", e);
            throw new BusinessException(500, "批量添加菜品失败: " + e.getMessage());
        }
    }

    // 更新菜品信息
    @PutMapping("/{id}")
    @Operation(summary = "更新菜品信息", description = "根据菜品ID更新菜品的详细信息")
    @ApiResponse(responseCode = "200", description = "菜品更新成功")
    @ApiResponse(responseCode = "404", description = "菜品不存在")
    public ResponseEntity<com.example.helper.Common.Util.ApiResponse<MealDTO>> updateMeal(
            @Parameter(description = "菜品ID") @PathVariable Long id,
            @Parameter(description = "更新的菜品信息") @Valid @RequestBody Meal updatedMeal) {
        try {
            Meal updated = mealService.updateMeal(id, updatedMeal);
            MealDTO mealDTO = MealMapper.toDTO(updated);
            return ResponseEntity.ok(
                    com.example.helper.Common.Util.ApiResponse.success("菜品更新成功", mealDTO));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新菜品失败，ID: {}", id, e);
            throw new BusinessException(500, "更新菜品失败: " + e.getMessage());
        }
    }

    // 批量更新菜品
    @PutMapping("/batch")
    @Operation(summary = "批量更新菜品", description = "批量更新多个菜品记录")
    public ResponseEntity<com.example.helper.Common.Util.ApiResponse<List<MealDTO>>>
            updateMealsBatch(
                    @Parameter(description = "菜品列表") @Valid @RequestBody List<Meal> meals) {
        try {
            List<Meal> updatedMeals = mealService.updateMealsBatch(meals);
            List<MealDTO> mealDTOs =
                    updatedMeals.stream().map(MealMapper::toDTO).collect(Collectors.toList());
            return ResponseEntity.ok(
                    com.example.helper.Common.Util.ApiResponse.success("批量更新菜品成功", mealDTOs));
        } catch (Exception e) {
            log.error("批量更新菜品失败", e);
            throw new BusinessException(500, "批量更新菜品失败: " + e.getMessage());
        }
    }

    // 删除菜品
    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜品", description = "根据菜品ID删除指定的菜品记录")
    @ApiResponse(responseCode = "200", description = "菜品删除成功")
    @ApiResponse(responseCode = "404", description = "菜品不存在")
    public ResponseEntity<com.example.helper.Common.Util.ApiResponse<Void>> deleteMeal(
            @Parameter(description = "菜品ID") @PathVariable Long id) {
        try {
            mealService.deleteMeal(id);
            return ResponseEntity.ok(com.example.helper.Common.Util.ApiResponse.success("菜品删除成功"));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除菜品失败，ID: {}", id, e);
            throw new BusinessException(500, "删除菜品失败: " + e.getMessage());
        }
    }

    // 批量删除菜品
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除菜品", description = "根据ID列表批量删除菜品")
    public ResponseEntity<com.example.helper.Common.Util.ApiResponse<Void>> deleteMealsBatch(
            @Parameter(description = "菜品ID列表") @RequestBody List<Long> ids) {
        try {
            mealService.deleteMealsBatch(ids);
            return ResponseEntity.ok(
                    com.example.helper.Common.Util.ApiResponse.success("批量删除菜品成功"));
        } catch (Exception e) {
            log.error("批量删除菜品失败", e);
            throw new BusinessException(500, "批量删除菜品失败: " + e.getMessage());
        }
    }

    // 切换菜品选中状态
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "切换菜品选中状态", description = "切换指定菜品的选中/未选中状态")
    public ResponseEntity<com.example.helper.Common.Util.ApiResponse<MealDTO>> toggleMealSelection(
            @Parameter(description = "菜品ID") @PathVariable Long id) {
        try {
            Meal updatedMeal = mealService.toggleMealSelection(id);
            MealDTO mealDTO = MealMapper.toDTO(updatedMeal);
            return ResponseEntity.ok(
                    com.example.helper.Common.Util.ApiResponse.success("切换选中状态成功", mealDTO));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("切换选中状态失败，ID: {}", id, e);
            throw new BusinessException(500, "切换选中状态失败: " + e.getMessage());
        }
    }

    // 获取所有已选中的菜品
    @GetMapping("/selected")
    @Operation(summary = "获取所有已选中的菜品", description = "返回所有标记为已选中的菜品列表")
    public ResponseEntity<com.example.helper.Common.Util.ApiResponse<List<MealDTO>>>
            getSelectedMeals() {
        try {
            List<Meal> meals = mealService.getSelectedMeals();
            List<MealDTO> mealDTOs =
                    meals.stream().map(MealMapper::toDTO).collect(Collectors.toList());
            return ResponseEntity.ok(
                    com.example.helper.Common.Util.ApiResponse.success("获取选中菜品成功", mealDTOs));
        } catch (Exception e) {
            log.error("获取选中菜品失败", e);
            throw new BusinessException(500, "获取选中菜品失败: " + e.getMessage());
        }
    }

    // 根据标题搜索菜品
    @GetMapping("/search")
    @Operation(summary = "根据标题搜索菜品", description = "根据菜品标题进行模糊搜索")
    public ResponseEntity<com.example.helper.Common.Util.ApiResponse<List<MealDTO>>>
            searchMealsByTitle(@Parameter(description = "搜索关键字") @RequestParam String title) {
        try {
            if (title == null || title.trim().isEmpty()) {
                throw new BusinessException(400, "搜索关键字不能为空");
            }
            List<Meal> meals = mealService.searchMealsByTitle(title.trim());
            List<MealDTO> mealDTOs =
                    meals.stream().map(MealMapper::toDTO).collect(Collectors.toList());
            return ResponseEntity.ok(
                    com.example.helper.Common.Util.ApiResponse.success("搜索菜品成功", mealDTOs));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("搜索菜品失败，关键字: {}", title, e);
            throw new BusinessException(500, "搜索菜品失败: " + e.getMessage());
        }
    }

    // 统计菜品信息
    @GetMapping("/statistics")
    @Operation(summary = "获取菜品统计信息", description = "返回菜品的各种统计数据")
    public ResponseEntity<com.example.helper.Common.Util.ApiResponse<Map<String, Object>>>
            getMealStatistics() {
        try {
            long totalMeals = mealService.countMeals();
            long selectedMeals = mealService.countSelectedMeals();

            Map<String, Object> statistics =
                    Map.of(
                            "totalMeals",
                            totalMeals,
                            "selectedMeals",
                            selectedMeals,
                            "unselectedMeals",
                            totalMeals - selectedMeals,
                            "selectionRate",
                            totalMeals > 0 ? (double) selectedMeals / totalMeals * 100 : 0.0);

            return ResponseEntity.ok(
                    com.example.helper.Common.Util.ApiResponse.success("获取统计信息成功", statistics));
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            throw new BusinessException(500, "获取统计信息失败: " + e.getMessage());
        }
    }
}
