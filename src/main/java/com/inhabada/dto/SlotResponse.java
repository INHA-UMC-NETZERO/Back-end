package com.inhabada.dto;

import com.inhabada.entity.Slot;

import java.time.LocalDateTime;

public record SlotResponse(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
    public static SlotResponse from(Slot slot) {
        return new SlotResponse(slot.getId(), slot.getStartTime(), slot.getEndTime());
    }
}
