package br.com.donation.service;
import br.com.donation.dto.campanha.ItemDTO;
import br.com.donation.dto.doacao.EfetivarDoacaoDTO;
import br.com.donation.dto.campanha.LocalDoacaoDTO;
import br.com.donation.dto.doacao.DoacaoDTO;
import br.com.donation.dto.usuario.UsuarioDTO;
import br.com.donation.dto.common.PaginaDTO;
import br.com.donation.dto.doacao.CriarDoacaoDTO;


import br.com.donation.exception.BusinessException;
import br.com.donation.exception.ResourceNotFoundException;
import br.com.donation.model.Doacao;
import br.com.donation.model.Interesse;
import br.com.donation.model.Item;
import br.com.donation.model.LocalDoacao;
import br.com.donation.repository.DoacaoRepository;
import br.com.donation.repository.InteresseRepository;
import br.com.donation.repository.ItemRepository;
import br.com.donation.repository.LocalDoacaoRepository;
import br.com.donation.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DoacaoService {

    private static final String MENSAGEM_INTERESSE_PADRAO =
            "Tenho interesse em receber esta doação e posso combinar a retirada.";

    private final Map<String, String> mensagensInteresse = new ConcurrentHashMap<>();

    @Autowired
    private DoacaoRepository doacaoRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private InteresseRepository interesseRepository;

    @Autowired
    private LocalDoacaoRepository localDoacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


    @Autowired
    private StorageService storageService;

    @Autowired
    private NotificationService notificationService;

    public DoacaoDTO criarDoacao(Integer doadorId, CriarDoacaoDTO dto, MultipartFile imagem) {
        Doacao doacao = new Doacao();
        doacao.setDoadorId(doadorId);
        // Quando a doação é direcionada a uma campanha, vincula o donatário (instituição criadora)
        if (dto.getDonatarioId() != null) {
            if (dto.getDonatarioId().equals(doadorId)) {
                throw new BusinessException("Você não pode doar para a sua própria campanha.");
            }
            usuarioRepository.findById(dto.getDonatarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Donatário", dto.getDonatarioId()));
            doacao.setDonatarioId(dto.getDonatarioId());
        }
        
        if (imagem != null && !imagem.isEmpty()) {
            String urlImagem = storageService.salvarImagem(imagem, "doacoes");
            doacao.setUrlImagem(urlImagem);
        }

        doacao = doacaoRepository.save(doacao);

        for (ItemDTO itemDto : dto.getItens()) {
            Item item = new Item();
            item.setNome(itemDto.getNome());
            item.setDoacaoId(doacao.getId());
            item.setQuantidade(itemDto.getQuantidade());
            item.setDescricao(itemDto.getDescricao());
            item.setMotivo(itemDto.getMotivo());
            item.setENovo(itemDto.getENovo());
            item.setCategoria(itemDto.getCategoria());
            itemRepository.save(item);
        }

        return buscarPorId(doacao.getId());
    }

    public PaginaDTO<DoacaoDTO> listarDisponiveis(int page, int size) {
        return listarDisponiveis(page, size, null, null);
    }

    public PaginaDTO<DoacaoDTO> listarDisponiveis(int page, int size, String categoria, String busca) {
        List<DoacaoDTO> content = doacaoRepository.findDisponiveis(page, size, categoria, busca).stream()
                .map(this::converterParaDTOCompleto)
                .collect(Collectors.toList());
                
        int totalElements = doacaoRepository.countDisponiveis(categoria, busca);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        return PaginaDTO.<DoacaoDTO>builder()
                .content(content)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(page)
                .build();
    }

    public List<DoacaoDTO> listarMinhasDoacoes(Integer doadorId) {
        return doacaoRepository.findByDoadorId(doadorId).stream()
                .map(this::converterParaDTOCompleto)
                .collect(Collectors.toList());
    }

    public List<DoacaoDTO> listarDoacoesRecebidas(Integer donatarioId) {
        return doacaoRepository.findByDonatarioId(donatarioId).stream()
                .map(this::converterParaDTOCompleto)
                .collect(Collectors.toList());
    }

    public DoacaoDTO buscarPorId(Integer id) {
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doação", id));
        return converterParaDTOCompleto(doacao);
    }


    public void registrarInteresse(Integer userId, Integer doacaoId) {
        registrarInteresse(userId, doacaoId, null);
    }

    public void registrarInteresse(Integer userId, Integer doacaoId, String mensagem) {
        Doacao doacao = doacaoRepository.findById(doacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Doação", doacaoId));

        if (doacao.getDoadorId().equals(userId)) {
            throw new BusinessException("Você não pode demonstrar interesse na sua própria doação.");
        }

        if (doacao.getDonatarioId() != null) {
            throw new BusinessException("Esta doação já foi efetivada e não aceita mais interessados.");
        }

        Interesse interesse = new Interesse();
        interesse.setUsuarioId(userId);
        interesse.setDoacaoId(doacaoId);
        interesseRepository.save(interesse);

        String chave = doacaoId + ":" + userId;
        if (mensagem != null && !mensagem.isBlank()) {
            mensagensInteresse.put(chave, mensagem.trim());
        } else {
            mensagensInteresse.remove(chave);
        }

        usuarioRepository.findById(userId).ifPresent(u -> {
            String msg = "O usuário " + u.getNome() + " demonstrou interesse na sua doação!";
            notificationService.notificarUsuario(doacao.getDoadorId(), "NOVO_INTERESSE", 
                java.util.Map.of("doacaoId", doacaoId, "mensagem", msg, "interessadoNome", u.getNome()));
        });
    }

    public void removerInteresse(Integer userId, Integer doacaoId) {
        interesseRepository.deleteById(userId, doacaoId);
        mensagensInteresse.remove(doacaoId + ":" + userId);
    }

    public List<UsuarioDTO> listarInteressados(Integer doadorId, Integer doacaoId) {
        Doacao doacao = doacaoRepository.findById(doacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Doação", doacaoId));

        if (!doacao.getDoadorId().equals(doadorId)) {
            throw new BusinessException("Apenas o doador pode ver a lista de interessados.");
        }

        return interesseRepository.findByDoacaoId(doacaoId).stream()
                .map(intId -> usuarioRepository.findById(intId.getUsuarioId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(u -> UsuarioDTO.builder()
                        .id(u.getId())
                        .nome(u.getNome() != null ? u.getNome() : "Interessado")
                        .email(u.getEmail() != null ? u.getEmail() : "")
                        .logradouro(u.getLogradouro())
                        .bairro(u.getBairro())
                        .numero(u.getNumero())
                        .cep(u.getCep())
                        .mensagem(mensagensInteresse.getOrDefault(doacaoId + ":" + u.getId(), MENSAGEM_INTERESSE_PADRAO))
                        .build())
                .collect(Collectors.toList());
    }


    public DoacaoDTO efetivarDoacao(Integer doadorId, Integer doacaoId, EfetivarDoacaoDTO dto) {
        Doacao doacao = doacaoRepository.findById(doacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Doação", doacaoId));

        if (!doacao.getDoadorId().equals(doadorId)) {
            throw new BusinessException("Apenas o doador pode efetivar esta doação.");
        }

        if (doacao.getDonatarioId() != null) {
            throw new BusinessException("Esta doação já possui um donatário.");
        }

        // Verifica se o donatário está na lista de interesses
        boolean interessadoExiste = interesseRepository.findByDoacaoId(doacaoId).stream()
                .anyMatch(i -> i.getUsuarioId().equals(dto.getDonatarioId()));

        if (!interessadoExiste) {
            throw new BusinessException("O donatário escolhido não manifestou interesse nesta doação.");
        }

        // Trata o Local de Doação
        LocalDoacao local = null;
        if (dto.getLocalDoacao().getId() != null) {
            local = localDoacaoRepository.findById(dto.getLocalDoacao().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Local Doação", dto.getLocalDoacao().getId()));
        } else {
            local = new LocalDoacao();
            local.setNome(dto.getLocalDoacao().getNome());
            local.setLogradouro(dto.getLocalDoacao().getLogradouro());
            local.setBairro(dto.getLocalDoacao().getBairro());
            local.setNumero(dto.getLocalDoacao().getNumero());
            local.setCep(dto.getLocalDoacao().getCep());
            local = localDoacaoRepository.save(local);
        }

        // Atualiza a doação
        doacao.setDonatarioId(dto.getDonatarioId());
        doacao.setLocalDoacaoId(local.getId());
        doacaoRepository.save(doacao);

        return buscarPorId(doacao.getId());
    }

    public void cancelarDoacao(Integer doadorId, Integer doacaoId) {
        Doacao doacao = doacaoRepository.findById(doacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Doação", doacaoId));

        if (!doacao.getDoadorId().equals(doadorId)) {
            throw new BusinessException("Apenas o proprietário pode cancelar esta doação.");
        }

        if (doacao.getDonatarioId() != null) {
            throw new BusinessException("Não é possível cancelar uma doação que já foi efetivada.");
        }

        mensagensInteresse.keySet().removeIf(chave -> chave.startsWith(doacaoId + ":"));
        doacaoRepository.deleteById(doacaoId);
    }

    public String atualizarImagem(Integer doadorId, Integer doacaoId, MultipartFile file) {
        Doacao doacao = doacaoRepository.findById(doacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Doação", doacaoId));

        if (!doacao.getDoadorId().equals(doadorId)) {
            throw new BusinessException("Apenas o doador pode alterar a imagem da doação.");
        }
        
        String urlImagem = storageService.salvarImagem(file, "doacoes");
        doacao.setUrlImagem(urlImagem);
        
        doacaoRepository.save(doacao);
        
        return urlImagem;
    }

    // --- MÉTODOS AUXILIARES ---

    private DoacaoDTO converterParaDTOCompleto(Doacao doacao) {
        DoacaoDTO dto = new DoacaoDTO();
        dto.setId(doacao.getId());
        dto.setDoadorId(doacao.getDoadorId());
        dto.setDonatarioId(doacao.getDonatarioId());
        dto.setUrlImagem(doacao.getUrlImagem());

        usuarioRepository.findById(doacao.getDoadorId())
                .ifPresent(u -> dto.setNomeDoador(u.getNome()));

        if (doacao.getDonatarioId() != null) {
            usuarioRepository.findById(doacao.getDonatarioId())
                    .ifPresent(u -> dto.setNomeDonatario(u.getNome()));
        }

        if (doacao.getLocalDoacaoId() != null) {
            localDoacaoRepository.findById(doacao.getLocalDoacaoId()).ifPresent(loc -> {
                LocalDoacaoDTO locDto = new LocalDoacaoDTO();
                locDto.setId(loc.getId());
                locDto.setNome(loc.getNome());
                locDto.setLogradouro(loc.getLogradouro());
                locDto.setBairro(loc.getBairro());
                locDto.setNumero(loc.getNumero());
                locDto.setCep(loc.getCep());
                dto.setLocalDoacao(locDto);
            });
        }

        List<ItemDTO> itens = itemRepository.findByDoacaoId(doacao.getId()).stream()
                .map(i -> ItemDTO.builder()
                        .nome(i.getNome())
                        .quantidade(i.getQuantidade())
                        .descricao(i.getDescricao())
                        .motivo(i.getMotivo())
                        .eNovo(i.getENovo())
                        .categoria(i.getCategoria())
                        .build())
                .collect(Collectors.toList());
        dto.setItens(itens);

        return dto;
    }
}

