package com.inhabada.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserCarbonSavingTest {

    @Test
    void userStartsWithZeroCarbonSavingGram() {
        User user = new User("student@inha.edu", "학생");

        assertThat(user.getTotalCarbonSavingGram()).isZero();
    }

    @Test
    void addCarbonSavingGramAccumulatesTotal() {
        User user = new User("student@inha.edu", "학생");

        user.addCarbonSavingGram(51);
        user.addCarbonSavingGram(120);

        assertThat(user.getTotalCarbonSavingGram()).isEqualTo(171);
    }
}
