package com.example.mailsend.service;

import com.example.mailsend.domain.entity.Office;
import com.example.mailsend.domain.entity.User;
import com.example.mailsend.domain.entity.UserOffice;
import com.example.mailsend.dto.request.CreateUserRequest;
import com.example.mailsend.dto.request.UpdateUserRequest;
import com.example.mailsend.dto.response.OfficeResponse;
import com.example.mailsend.dto.response.UserResponse;
import com.example.mailsend.exception.DuplicateResourceException;
import com.example.mailsend.exception.ResourceNotFoundException;
import com.example.mailsend.repository.OfficeRepository;
import com.example.mailsend.repository.UserOfficeRepository;
import com.example.mailsend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private UserOfficeRepository userOfficeRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private Office office;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("田中太郎")
                .nameKana("タナカタロウ")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        office = Office.builder()
                .id(1L)
                .name("GHさくら")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllUsers_returnsActiveUsers() {
        when(userRepository.findByIsActiveTrue()).thenReturn(List.of(user));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("田中太郎");
    }

    @Test
    void getUserById_withOffices_returnsUserWithOffices() {
        UserOffice userOffice = UserOffice.builder()
                .id(1L)
                .user(user)
                .office(office)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userOfficeRepository.findByUserIdWithOffice(1L)).thenReturn(List.of(userOffice));

        UserResponse result = userService.getUserById(1L);

        assertThat(result.getName()).isEqualTo("田中太郎");
        assertThat(result.getOffices()).hasSize(1);
        assertThat(result.getOffices().get(0).getName()).isEqualTo("GHさくら");
    }

    @Test
    void getUserById_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createUser_success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("佐藤花子");
        request.setNameKana("サトウハナコ");

        User savedUser = User.builder()
                .id(2L)
                .name("佐藤花子")
                .nameKana("サトウハナコ")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse result = userService.createUser(request);

        assertThat(result.getName()).isEqualTo("佐藤花子");
        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    void updateUser_success() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("田中花子");
        request.setNameKana("タナカハナコ");

        User updatedUser = User.builder()
                .id(1L)
                .name("田中花子")
                .nameKana("タナカハナコ")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponse result = userService.updateUser(1L, request);

        assertThat(result.getName()).isEqualTo("田中花子");
    }

    @Test
    void deleteUser_setsIsActiveFalse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.deleteUser(1L);

        assertThat(user.getIsActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void addUserOffice_duplicate_throwsDuplicateResourceException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(userOfficeRepository.existsByUserIdAndOfficeId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.addUserOffice(1L, 1L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void removeUserOffice_success() {
        UserOffice userOffice = UserOffice.builder()
                .id(1L)
                .user(user)
                .office(office)
                .build();

        when(userOfficeRepository.findByUserIdAndOfficeId(1L, 1L)).thenReturn(Optional.of(userOffice));

        userService.removeUserOffice(1L, 1L);

        verify(userOfficeRepository).delete(userOffice);
    }
}
