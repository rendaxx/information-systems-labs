package com.rendaxx.labs.dtos;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DriverDto {
    Long id;
    String firstName;

    @Nullable
    String middleName;

    String lastName;
    String passport;
}
