package com.rendaxx.labs.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "drivers", uniqueConstraints = {@UniqueConstraint(name = "uk_drivers_passport", columnNames = "passport")})
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String middleName;

    @Size(min = 1, message = "must contain at least one non-whitespace character")
    @Pattern(regexp = ".*\\S.*", message = "must not be blank when provided")
    private String lastName;

    @NotBlank
    private String passport;
}
