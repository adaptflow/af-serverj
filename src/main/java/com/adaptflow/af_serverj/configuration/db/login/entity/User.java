package com.adaptflow.af_serverj.configuration.db.login.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "af_global")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    @Column
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = true)
    private Long deletedAt;

    @Column(nullable = true)
    private Long lastLogin;

    @Column(nullable = false, length = 50)
    private String firstname;

    @Column(nullable = true, length = 50)
    private String lastname;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

}
