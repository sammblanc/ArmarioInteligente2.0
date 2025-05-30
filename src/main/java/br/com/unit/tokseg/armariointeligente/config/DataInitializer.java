package br.com.unit.tokseg.armariointeligente.config;

import br.com.unit.tokseg.armariointeligente.model.*;
import br.com.unit.tokseg.armariointeligente.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private CondominioRepository condominioRepository;
    
    @Autowired
    private ArmarioRepository armarioRepository;
    
    @Autowired
    private CompartimentoRepository compartimentoRepository;
    
    @Autowired
    private EntregaRepository entregaRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Adiciona alguns tipos de usuário iniciais
            if (tipoUsuarioRepository.count() == 0) {
                System.out.println("Carregando dados iniciais...");

                // Criando tipos de usuário
                TipoUsuario admin = new TipoUsuario();
                admin.setNome("Administrador");
                admin.setDescricao("Usuário com acesso total ao sistema");
                admin = tipoUsuarioRepository.save(admin);

                TipoUsuario cliente = new TipoUsuario();
                cliente.setNome("Cliente");
                cliente.setDescricao("Usuário com acesso limitado ao sistema");
                cliente = tipoUsuarioRepository.save(cliente);

                TipoUsuario funcionario = new TipoUsuario();
                funcionario.setNome("Funcionário");
                funcionario.setDescricao("Funcionário da empresa com acesso intermediário");
                funcionario = tipoUsuarioRepository.save(funcionario);

                TipoUsuario entregador = new TipoUsuario();
                entregador.setNome("Entregador");
                entregador.setDescricao("Entregador com acesso administrativo ao sistema");
                entregador = tipoUsuarioRepository.save(entregador);


                System.out.println("Tipos de usuário criados com sucesso!");

                // Criando usuários
                if (usuarioRepository.count() == 0) {
                    // Usuário Administrador
                    Usuario adminUser = new Usuario();
                    adminUser.setNome("Admin Sistema");
                    adminUser.setEmail("admin@smartlocker.com");
                    adminUser.setSenha(passwordEncoder.encode("admin123"));
                    adminUser.setTelefone("(81) 99999-0000");
                    adminUser.setTipoUsuario(admin);
                    usuarioRepository.save(adminUser);

                    // Usuário Cliente
                    Usuario clienteUser = new Usuario();
                    clienteUser.setNome("João Silva");
                    clienteUser.setEmail("joao.silva@exemplo.com");
                    clienteUser.setSenha(passwordEncoder.encode("senha123"));
                    clienteUser.setTelefone("(81) 98888-1111");
                    clienteUser.setTipoUsuario(cliente);
                    usuarioRepository.save(clienteUser);

                    // Usuário Entregador
                    Usuario entregadorUser = new Usuario();
                    entregadorUser.setNome("Maria Oliveira");
                    entregadorUser.setEmail("maria.oliveira@exemplo.com");
                    entregadorUser.setSenha(passwordEncoder.encode("senha456"));
                    entregadorUser.setTelefone("(81) 97777-2222");
                    entregadorUser.setTipoUsuario(entregador);
                    usuarioRepository.save(entregadorUser);

                    System.out.println("Usuários criados com sucesso!");
                    
                    // Criando condomínios
                    if (condominioRepository.count() == 0) {
                        Condominio condominio1 = new Condominio();
                        condominio1.setNome("Residencial Parque das Flores");
                        condominio1.setEndereco("Av. Principal, 1000");
                        condominio1.setCep("50000-000");
                        condominio1.setCidade("Recife");
                        condominio1.setEstado("PE");
                        condominio1.setTelefone("(81) 3333-4444");
                        condominio1.setEmail("contato@parquedasflores.com");
                        condominio1 = condominioRepository.save(condominio1);
                        
                        Condominio condominio2 = new Condominio();
                        condominio2.setNome("Edifício Solar das Palmeiras");
                        condominio2.setEndereco("Rua das Palmeiras, 500");
                        condominio2.setCep("50010-200");
                        condominio2.setCidade("Recife");
                        condominio2.setEstado("PE");
                        condominio2.setTelefone("(81) 3333-5555");
                        condominio2.setEmail("contato@solardaspalmeiras.com");
                        condominio2 = condominioRepository.save(condominio2);
                        
                        System.out.println("Condomínios criados com sucesso!");
                        
                        // Criando armários
                        if (armarioRepository.count() == 0) {
                            Armario armario1 = new Armario();
                            armario1.setIdentificacao("ARM-001");
                            armario1.setLocalizacao("Hall de entrada");
                            armario1.setDescricao("Armário principal do condomínio");
                            armario1.setAtivo(true);
                            armario1.setCondominio(condominio1);
                            armario1 = armarioRepository.save(armario1);
                            
                            Armario armario2 = new Armario();
                            armario2.setIdentificacao("ARM-002");
                            armario2.setLocalizacao("Portaria");
                            armario2.setDescricao("Armário secundário do condomínio");
                            armario2.setAtivo(true);
                            armario2.setCondominio(condominio1);
                            armario2 = armarioRepository.save(armario2);
                            
                            Armario armario3 = new Armario();
                            armario3.setIdentificacao("ARM-001");
                            armario3.setLocalizacao("Entrada principal");
                            armario3.setDescricao("Armário principal do condomínio");
                            armario3.setAtivo(true);
                            armario3.setCondominio(condominio2);
                            armario3 = armarioRepository.save(armario3);
                            
                            System.out.println("Armários criados com sucesso!");
                            
                            // Criando compartimentos
                            if (compartimentoRepository.count() == 0) {
                                // Compartimentos para o armário 1
                                for (int i = 1; i <= 6; i++) {
                                    Compartimento compartimento = new Compartimento();
                                    compartimento.setNumero("A" + i);
                                    compartimento.setTamanho(i <= 2 ? "P" : (i <= 4 ? "M" : "G"));
                                    compartimento.setOcupado(false);
                                    compartimento.setCodigoAcesso("123456");
                                    compartimento.setArmario(armario1);
                                    compartimentoRepository.save(compartimento);
                                }
                                
                                // Compartimentos para o armário 2
                                for (int i = 1; i <= 4; i++) {
                                    Compartimento compartimento = new Compartimento();
                                    compartimento.setNumero("B" + i);
                                    compartimento.setTamanho(i <= 2 ? "P" : "M");
                                    compartimento.setOcupado(false);
                                    compartimento.setCodigoAcesso("654321");
                                    compartimento.setArmario(armario2);
                                    compartimentoRepository.save(compartimento);
                                }
                                
                                // Compartimentos para o armário 3
                                for (int i = 1; i <= 8; i++) {
                                    Compartimento compartimento = new Compartimento();
                                    compartimento.setNumero("C" + i);
                                    compartimento.setTamanho(i <= 3 ? "P" : (i <= 6 ? "M" : "G"));
                                    compartimento.setOcupado(false);
                                    compartimento.setCodigoAcesso("987654");
                                    compartimento.setArmario(armario3);
                                    compartimentoRepository.save(compartimento);
                                }
                                
                                System.out.println("Compartimentos criados com sucesso!");
                                
                                // Criando algumas entregas e reservas de exemplo
                                Compartimento compartimentoA1 = compartimentoRepository.findByNumeroAndArmarioId("A1", armario1.getId()).orElse(null);
                                Compartimento compartimentoB1 = compartimentoRepository.findByNumeroAndArmarioId("B1", armario2.getId()).orElse(null);
                                
                                if (compartimentoA1 != null && compartimentoB1 != null) {
                                    // Entrega exemplo
                                    Entrega entrega = new Entrega();
                                    entrega.setCodigoRastreio("BR123456789");
                                    entrega.setDataEntrega(LocalDateTime.now().minusDays(1));
                                    entrega.setStatus(StatusEntrega.ENTREGUE);
                                    entrega.setObservacao("Pacote frágil");
                                    entrega.setCompartimento(compartimentoA1);
                                    entrega.setEntregador(entregadorUser);
                                    entrega.setDestinatario(clienteUser);
                                    entregaRepository.save(entrega);
                                    
                                    // Atualizar status do compartimento
                                    compartimentoA1.setOcupado(true);
                                    compartimentoRepository.save(compartimentoA1);
                                    
                                    // Reserva exemplo
                                    Reserva reserva = new Reserva();
                                    reserva.setDataInicio(LocalDateTime.now().plusDays(1));
                                    reserva.setDataFim(LocalDateTime.now().plusDays(2));
                                    reserva.setStatus(StatusReserva.CONFIRMADA);
                                    reserva.setObservacao("Reserva para recebimento de encomenda");
                                    reserva.setCompartimento(compartimentoB1);
                                    reserva.setUsuario(clienteUser);
                                    reservaRepository.save(reserva);
                                    
                                    // Atualizar status do compartimento
                                    compartimentoB1.setOcupado(true);
                                    compartimentoRepository.save(compartimentoB1);
                                    
                                    System.out.println("Entregas e reservas de exemplo criadas com sucesso!");
                                }
                            }
                        }
                    }
                }

                System.out.println("Dados iniciais carregados com sucesso!");
            }
        };
    }
}
