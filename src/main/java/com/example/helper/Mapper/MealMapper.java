package com.example.helper.Mapper;

import com.example.helper.Dto.MealDTO;
import com.example.helper.Entity.Meal;

public class MealMapper {

    public static MealDTO toDTO(Meal meal) {
        if (meal == null) return null;

        MealDTO dto = new MealDTO();
        dto.setId(meal.getId());
        dto.setTitle(meal.getTitle());
        dto.setImageUrl(meal.getImageUrl());
        dto.setChecked(meal.getChecked());
        dto.setEnergy(meal.getEnergy());
        dto.setProtein(meal.getProtein());
        dto.setTransFat(meal.getTransFat());
        dto.setSaturatedFat(meal.getSaturatedFat());
        dto.setCarbohydrate(meal.getCarbohydrate());
        dto.setAddedSugar(meal.getAddedSugar());
        dto.setSalt(meal.getSalt());
        dto.setDietaryFiber(meal.getDietaryFiber());

        return dto;
    }

    public static Meal toEntity(MealDTO dto) {
        if (dto == null) return null;

        Meal meal = new Meal();
        meal.setId(dto.getId());
        meal.setTitle(dto.getTitle());
        meal.setImageUrl(dto.getImageUrl());
        meal.setChecked(dto.getChecked());
        meal.setEnergy(dto.getEnergy());
        meal.setProtein(dto.getProtein());
        meal.setTransFat(dto.getTransFat());
        meal.setSaturatedFat(dto.getSaturatedFat());
        meal.setCarbohydrate(dto.getCarbohydrate());
        meal.setAddedSugar(dto.getAddedSugar());
        meal.setSalt(dto.getSalt());
        meal.setDietaryFiber(dto.getDietaryFiber());

        return meal;
    }
}
