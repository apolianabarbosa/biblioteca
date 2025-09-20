package com.api.biblioteca.service;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    @Autowired
    public UsuarioService(
        UsuarioRepository ur,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        RespostaModel rm
    ) {
        this.ur = ur;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.rm = rm;
    }

// Método de criar cadastro de um novo usuário
    public ResponseEntity<?> criarConta(Usuario u){

    // Verificar email ou CPF existente
    if(ur.existsByEmail(u.getEmail())){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RespostaModel("O e-mail informado já está em uso."));
    }

    if(ur.existsByCpf(u.getCpf())){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RespostaModel("O CPF informado já está em uso."));
    }

    // Senha  criptografada antes de salvar!
    u.setSenha(passwordEncoder.encode(u.getSenha()));
    Usuario usuarioSalvo = ur.save(u);

    UsuarioDTO dto = new UsuarioDTO(usuarioSalvo);

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

// Método: de atualizar dados cadastrais
public ResponseEntity<?> atualizarDadosUsuario(Map<String, Object> dadosAtualizados){
    
    Optional<Usuario> usuarioLogadoOpt = getUsuarioLogado();

    if(usuarioLogadoOpt.isEmpty()){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new RespostaModel("Usuário não encontrado."));
    }

    Usuario usuarioExistente = usuarioLogadoOpt.get();

    dadosAtualizados.forEach((campo, valor) -> {
            switch (campo) {
                case "nome":
                    if (valor != null) usuarioExistente.setNome((String) valor);
                    break;
                case "senha":
                    String senha = (String) valor;
                    if (senha != null && !senha.isEmpty()) {
                        usuarioExistente.setSenha(passwordEncoder.encode(senha));
                    }
                    break;
                case "telefone":
                    if (valor != null) usuarioExistente.setTelefone((String) valor);
                    break;
                case "estado":
                    if (valor != null) usuarioExistente.setEstado((String) valor);
                    break;
                case "cidade":
                    if (valor != null) usuarioExistente.setCidade((String) valor);
                    break;
                case "bairro":
                    if (valor != null) usuarioExistente.setBairro((String) valor);
                    break;
                case "dataNascimento":
                    if (valor != null) {
                        try {
                            LocalDate data = LocalDate.parse(valor.toString());
                            usuarioExistente.setDataNascimento(data);
                        } catch (DateTimeParseException e) {
                    
                        }
                    }
                    break;
                }
            });

    Usuario usuarioSalvo = ur.save(usuarioExistente);

    UsuarioDTO dto = new UsuarioDTO(usuarioSalvo);

    return ResponseEntity.ok(dto);
    
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
