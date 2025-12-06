package com.rendaxx.labs.domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "drivers")
public class Driver {

    private static final String HUMAN_NAME_REGEX = "^[\\p{L}]+(?:[-'\\s][\\p{L}]+)*$";
    private static final String PASSPORT_REGEX = "^\\d{4}\\s?\\d{6}$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Pattern(regexp = HUMAN_NAME_REGEX, message = "must contain only letters, spaces, apostrophes, or hyphens")
    private String firstName;

    @Nullable
    @Pattern(regexp = HUMAN_NAME_REGEX, message = "must contain only letters, spaces, apostrophes, or hyphens")
    private String middleName;

    @Size(min = 1, message = "must contain at least one non-whitespace character")
    @Pattern(regexp = HUMAN_NAME_REGEX, message = "must contain only letters, spaces, apostrophes, or hyphens")
    private String lastName;

    @NotBlank
    @Pattern(regexp = PASSPORT_REGEX, message = "must match the '1234 567890' passport format")
    private String passport;
}
