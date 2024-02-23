package com.epam.xstack.models.dto.trainee_dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TraineeActivateDeActivateDTO {
    @NotEmpty(message = "User name should not be empty")
    String userName;
    @NotNull
    Boolean isActive;
}
