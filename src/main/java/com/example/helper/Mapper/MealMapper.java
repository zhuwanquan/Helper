package com.example.helper.Mapper;

import com.example.helper.Dto.MealDTO;
import com.example.helper.Entity.Meal;

public class MealMapper {

    public static MealDTO toDTO(Meal meal) {
        if (meal == null) return null;

        MealDTO dto = new MealDTO();
        copyMealFieldsToDTO(dto, meal);
        dto.setChecked(meal.getChecked());

        return dto;
    }

    // 提取公共字段复制逻辑
    private static void copyMealFieldsToDTO(MealDTO dto, Meal meal) {
        dto.setId(meal.getId());
        dto.setTitle(meal.getTitle());
        dto.setImageUrl(meal.getImageUrl());
        dto.setEnergy(meal.getEnergy());
        dto.setProtein(meal.getProtein());
        dto.setTransFat(meal.getTransFat());
        dto.setSaturatedFat(meal.getSaturatedFat());
        dto.setCarbohydrate(meal.getCarbohydrate());
        dto.setAddedSugar(meal.getAddedSugar());
        dto.setSalt(meal.getSalt());
        dto.setDietaryFiber(meal.getDietaryFiber());
    }
}
