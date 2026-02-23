package com.example.helper.Entity;

import com.example.helper.Common.Base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "meals")
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Meal extends BaseEntity {

    @NotBlank(message = "菜品标题不能为空")
    @Size(max = 100, message = "菜品标题长度不能超过100个字符")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "energy", precision = 10, scale = 2)
    private BigDecimal energy;

    @Column(name = "protein", precision = 10, scale = 2)
    private BigDecimal protein;

    @Column(name = "trans_fat", precision = 10, scale = 2)
    private BigDecimal transFat;

    @Column(name = "saturated_fat", precision = 10, scale = 2)
    private BigDecimal saturatedFat;

    @Column(name = "carbohydrate", precision = 10, scale = 2)
    private BigDecimal carbohydrate;

    @Column(name = "added_sugar", precision = 10, scale = 2)
    private BigDecimal addedSugar;

    @Column(name = "salt", precision = 10, scale = 2)
    private BigDecimal salt;

    @Column(name = "dietary_fiber", precision = 10, scale = 2)
    private BigDecimal dietaryFiber;

    @NotNull(message = "选中状态不能为空")
    @Column(name = "checked")
    private Boolean checked = false;
}
