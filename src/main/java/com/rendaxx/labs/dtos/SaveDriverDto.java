package com.rendaxx.labs.dtos;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveDriverDto {
    String firstName;
    String middleName;
    String lastName;
    String passport;
}
