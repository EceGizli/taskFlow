package com.beat.taskFlow.user.dto.responses;

import com.beat.taskFlow.user.entity.enums.Role;

public record MeResponse(

        Long id,
        String name,
        String email,
        Role role

) {}