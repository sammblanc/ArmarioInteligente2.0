package br.com.unit.tokseg.armariointeligente.controller;

import br.com.unit.tokseg.armariointeligente.model.Usuario;
import br.com.unit.tokseg.armariointeligente.model.auth.AuthRequest;
import br.com.unit.tokseg.armariointeligente.model.auth.AuthResponse;
import br.com.unit.tokseg.armariointeligente.repository.UsuarioRepository;
import br.com.unit.tokseg.armariointeligente.security.JwtUtils;
import br.com.unit.tokseg.armariointeligente.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação de usuários")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Autentica um usuário e retorna um token JWT")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String tipoUsuario = userDetails.getAuthorities().stream()
                .findFirst()
                .map(item -> item.getAuthority().replace("ROLE_", ""))
                .orElse("");

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                "Bearer",
                userDetails.getId(),
                userDetails.getNome(),
                userDetails.getEmail(),
                tipoUsuario));
    }
}
