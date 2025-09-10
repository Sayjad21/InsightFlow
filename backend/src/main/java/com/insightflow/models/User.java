package com.insightflow.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    // Adding username field back for Spring Security compatibility
    @Indexed(unique = true, sparse = true) // sparse = true allows null values
    private String username;

    private String firstName;
    private String lastName;
    private String password;
    private String role;
    private String avatar;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private List<String> analysisHistoryIds = new ArrayList<>();

    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.role = "USER";
    }

    public User(String firstName, String lastName, String email, String password) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.avatar = generateAvatar(firstName, lastName);
    }

    // Helper method to generate avatar URL
    private String generateAvatar(String firstName, String lastName) {
        String name = firstName + "+" + lastName;
        return "https://ui-avatars.com/api/?name=" + name + "&background=0D8ABC&color=fff";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username != null ? username : email; // Return username if set, otherwise email
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        updateAvatar();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        updateAvatar();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public List<String> getAnalysisHistoryIds() {
        return analysisHistoryIds;
    }

    public void setAnalysisHistoryIds(List<String> analysisHistoryIds) {
        this.analysisHistoryIds = analysisHistoryIds;
    }

    public void addAnalysisId(String analysisId) {
        if (this.analysisHistoryIds == null) {
            this.analysisHistoryIds = new ArrayList<>();
        }
        this.analysisHistoryIds.add(analysisId);
    }

    private void updateAvatar() {
        if (this.firstName != null && this.lastName != null) {
            this.avatar = generateAvatar(this.firstName, this.lastName);
        }
    }
}