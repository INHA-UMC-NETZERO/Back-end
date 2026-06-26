package com.inhabada.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubCategoryCarbonSavingTest {

    @Test
    void subCategoriesExposeUnitCarbonSavingGram() {
        assertThat(SubCategory.CUP_RAMEN.getUnitCarbonSavingGram()).isEqualTo(400);
        assertThat(SubCategory.SNACK.getUnitCarbonSavingGram()).isEqualTo(250);
        assertThat(SubCategory.WATER.getUnitCarbonSavingGram()).isEqualTo(110);
        assertThat(SubCategory.SODA.getUnitCarbonSavingGram()).isEqualTo(240);
        assertThat(SubCategory.CHAIR.getUnitCarbonSavingGram()).isEqualTo(20_000);
        assertThat(SubCategory.SHELF.getUnitCarbonSavingGram()).isEqualTo(15_000);
    }
}
