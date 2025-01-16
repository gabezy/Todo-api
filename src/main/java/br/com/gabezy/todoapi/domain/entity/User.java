package br.com.gabezy.todoapi.domain.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "IDT_USER")
    private Long id;

    @Column(nullable = false, unique = true, name = "EMAIL")
    private String email;

    @Column(nullable = false, name = "PASSWORD")
    private String password;

    @Column(nullable = false, name = "CREATED_AT")
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Task> tasks;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "IDT_USER"),
            inverseJoinColumns = @JoinColumn(name = "IDT_ROLE"))
    private List<Role> roles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = OffsetDateTime.now();
    }
}
