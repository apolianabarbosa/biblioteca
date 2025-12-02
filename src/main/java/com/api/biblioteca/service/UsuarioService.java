package com.api.biblioteca.service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.api.biblioteca.dtos.AtualizacaoUsuarioDTO;
import com.api.biblioteca.dtos.CadastroRequestDTO;
import com.api.biblioteca.dtos.LoginRequestDTO;
import com.api.biblioteca.dtos.UsuarioDTO;
import com.api.biblioteca.model.RespostaModel;
import com.api.biblioteca.model.Usuario;
import com.api.biblioteca.model.UsuarioRole;
import com.api.biblioteca.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository ur;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RespostaModel rm;
    private final EmailService emailService;
    private final JwtService jwtService;

    @Value("${biblioteca.admin.codigo-secreto}")
    private String codigoSecreto;
    
    public UsuarioService(
        UsuarioRepository ur,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        RespostaModel rm,
        EmailService emailService,
        JwtService jwtService
    ) {
        this.ur = ur;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.rm = rm;
        this.emailService = emailService;
        this.jwtService = jwtService;
    }

// Método de criar cadastro de um novo usuário
public ResponseEntity<?> criarConta(CadastroRequestDTO dto){

    // 1. LÓGICA DE VALIDAÇÃO DO CÓDIGO ADMINISTRATIVO
    if (dto.getRole() == UsuarioRole.BIBLIOTECARIO) {
        if (dto.getCodigoAdministrativo() == null || !dto.getCodigoAdministrativo().equals(codigoSecreto)) {
            // Se o código estiver errado, retorna uma resposta de erro claro para o frontend
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new RespostaModel("Código administrativo inválido."));
        }
    }

    // Verificar email ou CPF existente
    if(ur.existsByEmail(dto.getEmail())){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RespostaModel("O e-mail informado já está em uso."));
    }

    if(ur.existsByCpf(dto.getCpf())){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RespostaModel("O CPF informado já está em uso."));
    }
 
    Usuario novoUsuario = new Usuario();
    novoUsuario.setNome(dto.getNome());
    novoUsuario.setSexo(dto.getSexo());
    novoUsuario.setCpf(dto.getCpf());
    novoUsuario.setEmail(dto.getEmail());
    novoUsuario.setSenha(passwordEncoder.encode(dto.getSenha()));
    novoUsuario.setTelefone(dto.getTelefone());
    novoUsuario.setEstado(dto.getEstado());
    novoUsuario.setCidade(dto.getCidade());
    novoUsuario.setBairro(dto.getBairro());
    novoUsuario.setDataNascimento(dto.getDataNascimento());
    novoUsuario.setRole(dto.getRole());
        
    ur.save(novoUsuario);

    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
}

// Métodos base do sistema (verificar e validar)
// Método de login 
public Usuario autenticar(LoginRequestDTO loginRequestDTO) {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequestDTO.getEmail(),
                loginRequestDTO.getSenha()
            )
        );

        // Se a autenticação for bem-sucedida, buscamos o usuário para retorná-lo.
        return ur.findByEmail(loginRequestDTO.getEmail())
                 .orElseThrow(() -> new IllegalStateException("Usuário não encontrado após autenticação."));
}

// Método para pegar usuário logado
public Optional<Usuario> getUsuarioLogado(){
    
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if(authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())){
        return Optional.empty();
    }

    Object principal = authentication.getPrincipal();
    String emailUsuarioLogado;

    if (principal instanceof UserDetails) {
        emailUsuarioLogado = ((UserDetails) principal).getUsername();
    } else {
        emailUsuarioLogado = principal.toString();
    }

    return ur.findByEmail(emailUsuarioLogado);
}

// Método verificar email e liberar acesso para redefinir senha
public void solicitarRedefinicao(String email) {
        Usuario usuario = ur.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail não encontrado no sistema."));

        String token = UUID.randomUUID().toString();
        usuario.setResetPasswordToken(token);
        usuario.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1)); // Expira em 1 hora

        ur.save(usuario);

        try {
            emailService.enviarEmailRecuperacao(usuario.getEmail(), token);
        } catch (Exception e) {
            // Logar o erro pode ser útil aqui
            throw new RuntimeException("Erro ao enviar o e-mail de recuperação.");
        }
}

// Método redefinir senha
public void redefinirSenhaComToken(String token, String novaSenha) {
        Usuario usuario = ur.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token de redefinição inválido ou não encontrado."));

        if (usuario.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token de redefinição expirado. Por favor, solicite um novo.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        
        // Limpa o token após o uso para segurança
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpiry(null);

        ur.save(usuario);
}

// Método: de atualizar dados cadastrais
public ResponseEntity<?> atualizarDadosUsuario(AtualizacaoUsuarioDTO dadosAtualizados){
    
    Optional<Usuario> usuarioLogadoOpt = getUsuarioLogado();

    if(usuarioLogadoOpt.isEmpty()){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RespostaModel("Usuário não autenticado."));
    }

    Usuario usuarioExistente = usuarioLogadoOpt.get();


    if (dadosAtualizados.getNome() != null) {
        usuarioExistente.setNome(dadosAtualizados.getNome());
    }
    if (dadosAtualizados.getTelefone() != null) {
        usuarioExistente.setTelefone(dadosAtualizados.getTelefone());
    }
    if (dadosAtualizados.getEstado() != null) {
        usuarioExistente.setEstado(dadosAtualizados.getEstado());
    }
    if (dadosAtualizados.getCidade() != null) {
        usuarioExistente.setCidade(dadosAtualizados.getCidade());
    }
    if (dadosAtualizados.getBairro() != null) {
        usuarioExistente.setBairro(dadosAtualizados.getBairro());
    }
    if (dadosAtualizados.getDataNascimento() != null) {
        usuarioExistente.setDataNascimento(dadosAtualizados.getDataNascimento());
    }


    String senhaAtual = dadosAtualizados.getSenhaAtual();
    String novaSenha = dadosAtualizados.getNovaSenha();


    if (senhaAtual != null && !senhaAtual.isEmpty() && novaSenha != null && !novaSenha.isEmpty()) {
        
   
        if (!passwordEncoder.matches(senhaAtual, usuarioExistente.getSenha())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RespostaModel("A senha atual está incorreta."));
        }

        usuarioExistente.setSenha(passwordEncoder.encode(novaSenha));
    }

    ur.save(usuarioExistente);

    String novoToken = jwtService.generateToken(usuarioExistente); 

    Map<String, Object> resposta = new HashMap<>();
    resposta.put("message", "Perfil atualizado com sucesso!");
    resposta.put("token", novoToken);
    
    return ResponseEntity.ok(resposta);
}

// Métodos visão Adm
// Método: listagem global
public List<UsuarioDTO> listaTodosUsuarios(){

    List<Usuario> usuarios = ur.findAllByOrderByNomeAsc();
	return usuarios.stream().map(UsuarioDTO:: new).collect(Collectors.toList());
}

// Método: busca pelo nome do usuário 
public ResponseEntity<?> buscarUsuarioPorNome(String nome){

	List<Usuario> lista = ur.findByNomeContainingIgnoreCase(nome);
	
	if(lista.isEmpty()){ 
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new RespostaModel("Nenhum usuário com esse nome."));
	}
	
    List<UsuarioDTO> listaDto = lista.stream().map(UsuarioDTO::new).collect(Collectors.toList());

    return ResponseEntity.ok(listaDto);
}

// Método: de filtragem por tipo de usuário
public ResponseEntity<?> filtrarUsuarioPorRole(UsuarioRole role){

	List<Usuario> lista = ur.findByRoleOrderByNomeAsc(role);

    if(lista.isEmpty()){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new RespostaModel("Nenhum usuário encontrado para o tipo: " + role));
    }
	
    List<UsuarioDTO> listaDto = lista.stream().map(UsuarioDTO::new).collect(Collectors.toList());
    return ResponseEntity.ok(listaDto);

}

}
