package com.inhabada.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void category_acceptsLabelAndCodeAndSerializesAsLabel() throws Exception {
        assertThat(objectMapper.readValue("\"식품\"", Category.class)).isEqualTo(Category.FOOD);
        assertThat(objectMapper.readValue("\"FOOD\"", Category.class)).isEqualTo(Category.FOOD);
        assertThat(objectMapper.writeValueAsString(Category.FOOD)).isEqualTo("\"식품\"");
    }

    @Test
    void subCategory_acceptsLabelAndCodeAndSerializesAsLabel() throws Exception {
        assertThat(objectMapper.readValue("\"과자\"", SubCategory.class)).isEqualTo(SubCategory.SNACK);
        assertThat(objectMapper.readValue("\"SNACK\"", SubCategory.class)).isEqualTo(SubCategory.SNACK);
        assertThat(objectMapper.writeValueAsString(SubCategory.SNACK)).isEqualTo("\"과자\"");
    }
}
