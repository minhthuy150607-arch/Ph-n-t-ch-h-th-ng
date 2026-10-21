package com.library.service;

import com.library.dto.RegisterDto;
import com.library.model.User;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllReaders() {
        return userRepository.findByRole("READER");
    }

    public List<User> getAllAccounts() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User registerReader(RegisterDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không trùng khớp!");
        }

        if (userRepository.findByUsername(dto.getUsername().trim()).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!");
        }

        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty() &&
                userRepository.findByEmail(dto.getEmail().trim()).isPresent()) {
            throw new IllegalArgumentException("Email này đã được sử dụng!");
        }

        User user = User.builder()
                .username(dto.getUsername().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName().trim())
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .readerCode("BD-" + (System.currentTimeMillis() % 100000))
                .role("READER")
                .status("ACTIVE")
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác!");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có tối thiểu 6 ký tự!");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới xác nhận không khớp!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void updateProfile(Long userId, String fullName, String email, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }
        user.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : null);
        user.setPhone(phone != null && !phone.trim().isEmpty() ? phone.trim() : null);
        userRepository.save(user);
    }

    @Transactional
    public User saveAccount(User user, String rawPassword) {
        if (user.getId() != null) {
            User existing = userRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + user.getId()));

            if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
                existing.setFullName(user.getFullName().trim());
            }
            existing.setEmail(user.getEmail() != null && !user.getEmail().trim().isEmpty() ? user.getEmail().trim() : null);
            existing.setPhone(user.getPhone() != null && !user.getPhone().trim().isEmpty() ? user.getPhone().trim() : null);
            if (user.getReaderCode() != null && !user.getReaderCode().trim().isEmpty()) {
                existing.setReaderCode(user.getReaderCode().trim());
            }
            if (user.getRole() != null) {
                existing.setRole(user.getRole());
            }
            if (user.getStatus() != null) {
                existing.setStatus(user.getStatus());
            }
            if (rawPassword != null && !rawPassword.trim().isEmpty()) {
                existing.setPassword(passwordEncoder.encode(rawPassword.trim()));
            }
            return userRepository.save(existing);
        } else {
            if (user.getRole() == null) {
                user.setRole("READER");
            }
            if (user.getStatus() == null) {
                user.setStatus("ACTIVE");
            }
            if ("READER".equalsIgnoreCase(user.getRole()) && (user.getReaderCode() == null || user.getReaderCode().trim().isEmpty())) {
                user.setReaderCode("BD-" + System.currentTimeMillis() % 100000);
            }
            if (rawPassword != null && !rawPassword.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(rawPassword.trim()));
            } else {
                user.setPassword(passwordEncoder.encode("123456"));
            }
            return userRepository.save(user);
        }
    }

    @Transactional
    public User saveReader(User user, String rawPassword) {
        user.setRole("READER");
        return saveAccount(user, rawPassword);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + userId));
        if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
            user.setStatus("LOCKED");
        } else {
            user.setStatus("ACTIVE");
        }
        userRepository.save(user);
    }
}
