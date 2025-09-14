package com.insightflow.services;

import com.insightflow.models.User;
import com.insightflow.models.UserAnalysis;
import com.insightflow.repositories.UserRepository;
import com.insightflow.repositories.UserAnalysisRepository;
import com.insightflow.repositories.ComparisonResultRepository;
import com.insightflow.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAnalysisRepository userAnalysisRepository;

    @Autowired
    private ComparisonResultRepository comparisonResultRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    // New signup method with additional fields
    public String signup(String username, String password, String firstName, String lastName, String email) {
        // Check if username already exists (if provided)
        if (username != null && !username.isEmpty() && userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(username != null && !username.isEmpty() ? username : email); // Use username or fall back to
                                                                                      // email
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole("USER");
        user.setAvatar(generateAvatarUrl(firstName, lastName)); // Generate avatar URL
        userRepository.save(user);

        return jwtUtil.generateToken(user.getUsername()); // Use the actual username for JWT
    }

    // Helper method to generate avatar URL
    private String generateAvatarUrl(String firstName, String lastName) {
        String name = firstName + "+" + lastName;
        return "https://ui-avatars.com/api/?name=" + name + "&background=0D8ABC&color=fff";
    }

    // Backward compatible signup method
    public String signup(String username, String password) {
        return signup(username, password, "", "", username + "@example.com");
    }

    public String login(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            // Update last login time - try both username and email lookup
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByEmail(username);
            }

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
            }

            return jwtUtil.generateToken(username);
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid username or password");
        }
    }

    // Login with email option
    public String loginWithEmail(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        // Use the user's actual username for authentication
        return login(userOpt.get().getUsername(), password);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Analysis management methods
    public UserAnalysis saveAnalysis(UserAnalysis analysis) {
        UserAnalysis savedAnalysis = userAnalysisRepository.save(analysis);

        // Add analysis ID to user's history
        Optional<User> userOpt = userRepository.findById(analysis.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.addAnalysisId(savedAnalysis.getId()); // Using the correct method name
            userRepository.save(user);
        }

        return savedAnalysis;
    }

    public List<UserAnalysis> getUserAnalyses(String userId) {
        return userAnalysisRepository.findByUserIdOrderByAnalysisDateDesc(userId);
    }

    public Optional<UserAnalysis> getAnalysisById(String analysisId) {
        return userAnalysisRepository.findById(analysisId);
    }

    public long getUserAnalysisCount(String userId) {
        return userAnalysisRepository.countByUserId(userId);
    }

    public long getUserSuccessfulAnalysisCount(String userId) {
        return userAnalysisRepository.countByUserIdAndStatus(userId, UserAnalysis.AnalysisStatus.COMPLETED);
    }

    public long getUserComparisonCount(String username) {
        return comparisonResultRepository.countByRequestedBy(username);
    }
}