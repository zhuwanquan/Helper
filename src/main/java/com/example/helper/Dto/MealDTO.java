package com.example.helper.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MealDTO {

    @JsonProperty("meal_id")
    private Long id;

    private String title;
    private String imageUrl;

    @JsonIgnore
    private Boolean checked;

    private BigDecimal energy;
    private BigDecimal protein;
    private BigDecimal transFat;
    private BigDecimal saturatedFat;
    private BigDecimal carbohydrate;
    private BigDecimal addedSugar;
    private BigDecimal salt;
    private BigDecimal dietaryFiber;
}
