package com.germogli.backend.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoDTO {
    private Integer userId;
    private String username;
    private String avatar;
    private String firstName;
    private String lastName;
    private String description;
}
