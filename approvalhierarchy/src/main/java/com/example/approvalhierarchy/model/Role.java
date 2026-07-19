package com.example.approvalhierarchy.model;

import java.util.Objects;

import jakarta.persistence.*;

//The 4 roles are common, generic placeholders used in many sample or tutorial projects to quickly demonstrate role‑based access control. THEY WERE ADDED MANUALLY IN DB
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Constructors, getters, setters
    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object objectSupposedToBeRole) { // OverRiding equals method in java,which takes in some
                                                           // object,and we type cast it to a role.
        if (this == objectSupposedToBeRole)
            return true; // If both point to same memory
        if (objectSupposedToBeRole == null || this.getClass() != objectSupposedToBeRole.getClass())
            return false; // If object null or object not a role
        Role role = (Role) objectSupposedToBeRole; // typecasting the object to be a role
        return Objects.equals(id, role.id) && // If id is null,it is going to throw null pointer exception.
                Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);// equals uses hashing, so if we are changing equals, we also need to change the
                                      // hashing method.If two objects are equal (a.equals(b) == true), they must
                                      // return the same hashCode().
    }
}
// Admin-Can do everything – create users, assign any role, delete data, view
// all requests.
// manager -Can approve/reject requests of subordinates, view their own team.
// hr- Can view/edit employee data, but not approve requests (unless also a
// manager).
// user-Can only create his/her own requests and see his/her own profile.