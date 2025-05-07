package com.demoApp.owner.controller;

import com.demoApp.owner.dto.AuthResponseDTO;
import com.demoApp.owner.dto.LoginDTO;
import com.demoApp.owner.dto.OwnerDTO;
import com.demoApp.owner.entity.Owner;
import com.demoApp.owner.payload.ApiResponse;
import com.demoApp.owner.security.JwtTokenProvider;
import com.demoApp.owner.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final OwnerService ownerService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getEmail(),
                            loginDTO.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(authentication);

            return ResponseEntity.ok(new AuthResponseDTO(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid email or password"));
        }
    }

    @PostMapping("/register")
public ResponseEntity<?> signup(@Valid @RequestBody OwnerDTO ownerDTO) {
    try {
        // Create the owner
        Owner owner = ownerService.createOwner(ownerDTO, "ROLE_OWNER");

        // Convert to response DTO (fill in all required fields)
        OwnerDTO responseDTO = new OwnerDTO(
            owner.getId(),
            owner.getName(),
            owner.getEmail(),
            owner.getRestaurantName(),
            owner.getContactNumber(),
            null // don't include password in the response
        );

        return ResponseEntity.ok(responseDTO);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
    }
}

}
