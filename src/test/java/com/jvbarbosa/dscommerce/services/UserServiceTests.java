package com.jvbarbosa.dscommerce.services;

import com.jvbarbosa.dscommerce.dto.UserDTO;
import com.jvbarbosa.dscommerce.entities.User;
import com.jvbarbosa.dscommerce.factories.UserDetailsFactory;
import com.jvbarbosa.dscommerce.factories.UserFactory;
import com.jvbarbosa.dscommerce.projections.UserDetailsProjection;
import com.jvbarbosa.dscommerce.repositories.UserRepository;
import com.jvbarbosa.dscommerce.utils.CustomUserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class UserServiceTests {

    @InjectMocks
    private UserService service;

    @Mock
    private UserRepository repository;

    @Mock
    private CustomUserUtil userUtil;

    private String existingUsername;
    private String nonExistingUsername;
    private User user;
    private List<UserDetailsProjection> userDetails;


    @BeforeEach
    void setUp() throws Exception {
        existingUsername = "maria@gmail.com";
        nonExistingUsername = "user@gmail.com";

        user = UserFactory.createCustomClientUser(1L, existingUsername);
        userDetails = UserDetailsFactory.createCustomAdminUser(existingUsername);

        when(repository.searchUserAndRolesByEmail(existingUsername)).thenReturn(userDetails);
        when(repository.searchUserAndRolesByEmail(nonExistingUsername)).thenReturn(new ArrayList<>());

        when(repository.findByEmail(existingUsername)).thenReturn(Optional.of(user));
        when(repository.findByEmail(nonExistingUsername)).thenReturn(Optional.empty());
    }

    @Test
    public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
        UserDetails result = service.loadUserByUsername(existingUsername);

        assertNotNull(result);
        assertEquals(result.getUsername(), existingUsername);
    }

    @Test
    public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername(nonExistingUsername);
        });
    }

    @Test
    public void authenticatedShouldReturnUserWhenUserExists() {
        when(userUtil.getLoggedUsername()).thenReturn(existingUsername);

        User result = service.authenticated();

        assertNotNull(result);
        assertEquals(result.getUsername(), existingUsername);
    }

    @Test
    public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        doThrow(ClassCastException.class).when(userUtil).getLoggedUsername();

        assertThrows(UsernameNotFoundException.class, () -> {
            service.authenticated();
        });
    }

    @Test
    public void getMeShouldReturnUserDTOWhenUserExists() {
        UserService spyUserService = spy(service);
        doReturn(user).when(spyUserService).authenticated();

        UserDTO result = spyUserService.getMe();

        assertNotNull(result);
        assertEquals(result.getEmail(), existingUsername);
    }

    @Test
    public void getMeShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        UserService spyUserService = spy(service);
        doThrow(UsernameNotFoundException.class).when(spyUserService).authenticated();

        assertThrows(UsernameNotFoundException.class, () -> {
            UserDTO result = spyUserService.getMe();
        });
    }
}
