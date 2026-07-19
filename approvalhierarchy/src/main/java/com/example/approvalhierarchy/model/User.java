package com.example.approvalhierarchy.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

//authentication table during registration and login
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER) // creates a seperate table user_roles to map each user to their roles
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id")) // user_roles
                                                                                                                                     // (the
                                                                                                                                     // join
                                                                                                                                     // table)
                                                                                                                                     // user_id
                                                                                                                                     // (FK
                                                                                                                                     // →
                                                                                                                                     // users.id),
                                                                                                                                     // role_id
                                                                                                                                     // (FK
                                                                                                                                     // →
                                                                                                                                     // roles.id)

    private Set<Role> roles = new HashSet<>();// an in memeory way of displaying the join table user_roles wich shows
                                              // each role with each user and vice versa

    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;// pointer to employee of employee tableIn SQL databases, the table that holds
                              // the Foreign Key is always the child. The table it points to is the parent.

    // Constructors, getters, setters
    public User() {
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles2) {
        this.roles = roles2 != null ? roles2 : new HashSet<>();
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }
}