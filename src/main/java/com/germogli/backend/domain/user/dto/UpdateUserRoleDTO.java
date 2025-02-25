package com.germogli.backend.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleDTO {
    private Integer userId;
    private Integer roleId;
}
