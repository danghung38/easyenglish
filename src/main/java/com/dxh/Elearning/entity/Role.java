package com.dxh.Elearning.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "roles")
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Role extends AbstractEntity<Long>{
    @Column(nullable = false, unique = true)
    String name;

    @Column(name = "description")
    String description;

    @ManyToMany
    Set<Permission> permissions;
}
