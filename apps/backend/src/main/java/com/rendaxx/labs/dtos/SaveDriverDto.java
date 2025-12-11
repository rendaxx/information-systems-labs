package com.rendaxx.labs.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveDriverDto {
    @NotBlank
    String firstName;

    @Nullable
    String middleName;

    @NotBlank
    String lastName;

    @NotBlank
    String passport;
}
