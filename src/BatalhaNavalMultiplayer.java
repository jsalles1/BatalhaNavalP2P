import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;

public class BatalhaNavalMultiplayer {
    private static final int GRID_SIZE = 10;
    private static final char AGUA = '-';
    private static final char NAVIO = 'N';
    private static final char ACERTO = 'X';
    private static final char ERRO = 'O';

    private boolean meuTurno = false;

    private static final Map<String, Integer> TIPOS_DE_NAVIOS = Map.of(
        //"porta-avioes", 5,
       // "encouracado", 4,
       // "cruzador", 3,
       // "cruzador2", 3,
       // "destroier2", 2,
        "destroier", 2
    );

    private char[][] tabuleiroJogador = new char[GRID_SIZE][GRID_SIZE];
    private char[][] tabuleiroAtaque = new char[GRID_SIZE][GRID_SIZE];
    private List<JSONObject> naviosJogador = new ArrayList<>();

    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    public static void main(String[] args) throws IOException {
        BatalhaNavalMultiplayer jogo = new BatalhaNavalMultiplayer();
        jogo.iniciar();
    }

    public void iniciar() throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Digite '1' para iniciar como servidor, ou '2' para iniciar como cliente:");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            iniciarServidor();
        } else if (choice == 2) {
            iniciarCliente();
        } else {
            System.out.println("Opção inválida!");
            return;
        }

        inicializarTabuleiro(tabuleiroJogador);
        inicializarTabuleiro(tabuleiroAtaque);

        System.out.println("Posicione seus navios:");
        posicionarNavios(tabuleiroJogador, naviosJogador, "meus_navios.json");

        System.out.println("Enviando arquivo JSON de navios...");
        enviarArquivo("meus_navios.json");

        System.out.println("Recebendo arquivo JSON de navios do oponente...");
        receberArquivo("navios_oponente.json");

        System.out.println("Iniciando o jogo!");
        jogar();
    }

    private void iniciarServidor() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Servidor aguardando conexão...");
        socket = serverSocket.accept();
        System.out.println("Conexão estabelecida com: " + socket.getInetAddress());
        prepararConexao();
        meuTurno = true;
    }

    private void iniciarCliente() throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Digite o endereço IP do servidor: ");
        String ip = scanner.nextLine();

        socket = new Socket(ip, 8080);
        System.out.println("Conectado ao servidor!");
        prepararConexao();
    }

    private void prepararConexao() throws IOException {
        output = new PrintWriter(socket.getOutputStream(), true);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void inicializarTabuleiro(char[][] tabuleiro) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                tabuleiro[i][j] = AGUA;
            }
        }
    }

    private void posicionarNavios(char[][] tabuleiro, List<JSONObject> navios, String arquivoJson) {
        Scanner scanner = new Scanner(System.in);
        for (Map.Entry<String, Integer> tipoNavio : TIPOS_DE_NAVIOS.entrySet()) {
            String tipo = tipoNavio.getKey();
            int tamanho = tipoNavio.getValue();

            boolean posicionado = false;
            while (!posicionado) {
                System.out.println("Posicione o navio: " + tipo + " (tamanho: " + tamanho + ")");
                exibirTabuleiro(tabuleiro, tabuleiroAtaque);

                System.out.print("Digite a linha inicial: ");
                int linhaInicial = scanner.nextInt();
                System.out.print("Digite a coluna inicial: ");
                int colunaInicial = scanner.nextInt();
                System.out.print("Digite a direção (H para horizontal, V para vertical): ");
                char direcao = scanner.next().toUpperCase().charAt(0);

                if (podePosicionarNavio(tabuleiro, linhaInicial, colunaInicial, tamanho, direcao)) {
                    adicionarNavio(tabuleiro, navios, tipo, linhaInicial, colunaInicial, tamanho, direcao);
                    posicionado = true;
                } else {
                    System.out.println("Posição inválida! Tente novamente.");
                }
            }
        }

        // Exibir o tabuleiro final após posicionar todos os navios
        System.out.println("Tabuleiro Final:");
        exibirTabuleiro(tabuleiro, tabuleiroAtaque);

        salvarNaviosEmJson(navios, arquivoJson);
    }

    private boolean podePosicionarNavio(char[][] tabuleiro, int linha, int coluna, int tamanho, char direcao) {
        if (direcao == 'H') {
            if (coluna + tamanho > GRID_SIZE) return false;
            for (int i = 0; i < tamanho; i++) {
                if (tabuleiro[linha][coluna + i] != AGUA) return false;
            }
        } else if (direcao == 'V') {
            if (linha + tamanho > GRID_SIZE) return false;
            for (int i = 0; i < tamanho; i++) {
                if (tabuleiro[linha + i][coluna] != AGUA) return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private void adicionarNavio(char[][] tabuleiro, List<JSONObject> navios, String tipo, int linha, int coluna, int tamanho, char direcao) {
        JSONArray posicoes = new JSONArray();
        if (direcao == 'H') {
            for (int i = 0; i < tamanho; i++) {
                tabuleiro[linha][coluna + i] = NAVIO;
                JSONArray posicao = new JSONArray();
                posicao.put(linha).put(coluna + i);
                posicoes.put(posicao);
            }
        } else if (direcao == 'V') {
            for (int i = 0; i < tamanho; i++) {
                tabuleiro[linha + i][coluna] = NAVIO;
                JSONArray posicao = new JSONArray();
                posicao.put(linha + i).put(coluna);
                posicoes.put(posicao);
            }
        }
        JSONObject navio = new JSONObject();
        navio.put("tipo", tipo);
        navio.put("posicoes", posicoes);
        navios.add(navio);
    }

    private void salvarNaviosEmJson(List<JSONObject> navios, String arquivo) {
        try (FileWriter writer = new FileWriter(arquivo)) {
            JSONArray jsonArray = new JSONArray(navios);
            writer.write(jsonArray.toString());
        } catch (IOException e) {
            System.err.println("Erro ao salvar o arquivo JSON: " + e.getMessage());
        }
    }

    private void enviarArquivo(String arquivo) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String linha;
    
            // Constrói o JSON como uma única string
            while ((linha = reader.readLine()) != null) {
                jsonBuilder.append(linha);
            }
    
            // Envia o JSON completo em uma única chamada
            output.println(jsonBuilder.toString());
        }
    }
    

    private void receberArquivo(String arquivo) throws IOException {
        StringBuilder jsonBuilder = new StringBuilder();
        char[] buffer = new char[1024];
        int bytesLidos;
    
        while ((bytesLidos = input.read(buffer)) != -1) {
            jsonBuilder.append(buffer, 0, bytesLidos);
    
            // Verifica se o JSON está completo
            String jsonContent = jsonBuilder.toString().trim();
            if (jsonContent.startsWith("[") && jsonContent.endsWith("]")) {
                break; // Encerra a leitura ao detectar JSON completo
            }
        }
    
        // Salva o JSON no arquivo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo))) {
            writer.write(jsonBuilder.toString());
        }
    }
    
    
    

    private void jogar() throws IOException {
        while (true) {
            if (meuTurno) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Seu turno:");
                exibirTabuleiro(tabuleiroJogador, tabuleiroAtaque);
    
                System.out.println("Sua vez de atacar!");
                System.out.print("Digite a linha: ");
                int linha = scanner.nextInt();
                System.out.print("Digite a coluna: ");
                int coluna = scanner.nextInt();
    
                // Envia o ataque como uma string "linha,coluna"
                output.println(linha + "," + coluna);  
                
                // Espera o resultado da jogada, agora com leitura segura
                String resultado = input.readLine();  
    
                if ("ACERTO".equals(resultado) || tabuleiroAtaque[linha][coluna] == ACERTO) {
                    System.out.println("Você acertou!");
                    tabuleiroAtaque[linha][coluna] = ACERTO;
                } else {
                    System.out.println("Você errou!");
                    tabuleiroAtaque[linha][coluna] = ERRO;
                }
    
                // Verifica se o jogo terminou
                if (ChecarFimDeJogo("navios_oponente.json", tabuleiroAtaque)) {
                    System.out.println("Você venceu!");
                    output.println("FIM_JOGO");
                    break; // Sai do loop
                }
    
            } else {
                System.out.println("Aguardando o ataque do oponente...");
    
                // Lê o ataque enviado pelo servidor, agora com InputStream.read() para garantir uma leitura mais precisa
                InputStream inputStream = socket.getInputStream();
                byte[] data = new byte[2];  // Esperamos 2 bytes: linha e coluna
                int bytesLidos = inputStream.read(data);  // Lê os 2 bytes
    
                if (bytesLidos == 2) {
                    String ataque = new String(data);  // Converte os bytes lidos em string
                    System.out.println("Ataque recebido: " + ataque);
    
                    if (ataque.length() == 2) {
                        // Processa o ataque
                        int linha = Character.getNumericValue(ataque.charAt(0));  // Primeiro dígito é a linha
                        int coluna = Character.getNumericValue(ataque.charAt(1));  // Segundo dígito é a coluna
    
                        // Processa o ataque
                        if (tabuleiroJogador[linha][coluna] == NAVIO || tabuleiroJogador[linha][coluna] == ACERTO) {
                            tabuleiroJogador[linha][coluna] = ACERTO;
                            System.out.println("O oponente acertou na posição: (" + linha + ", " + coluna + ")");
                            output.println("ACERTO");
                        } else {
                            tabuleiroJogador[linha][coluna] = ERRO;
                            System.out.println("O oponente errou na posição: (" + linha + ", " + coluna + ")");
                            output.println("ERRO");
                        }
    
                        // Verifica se o jogo terminou
                        if (ChecarFimDeJogo("meus_navios.json", tabuleiroJogador)) {
                            System.out.println("Você perdeu!");
                            output.println("FIM_JOGO");
                            break; // Sai do loop
                        }
                    } else {
                        System.out.println("Formato de ataque inválido! Esperado: [0-9][0-9].");
                    }
                } else {
                    System.out.println("Erro ao ler o ataque. Bytes lidos: " + bytesLidos);
                }
            }
    
            meuTurno = !meuTurno;
        }
    }
    
    
    
    

    private boolean ChecarFimDeJogo(String arquivoJson, char[][] tabuleiro) {
        try (BufferedReader reader = new BufferedReader(new FileReader(arquivoJson))) {
            StringBuilder jsonContent = new StringBuilder();
            String linha;
            while ((linha = reader.readLine()) != null) {
                jsonContent.append(linha);
            }
    
            JSONArray navios = new JSONArray(jsonContent.toString());
            for (int i = 0; i < navios.length(); i++) {
                JSONObject navio = navios.getJSONObject(i);
                JSONArray posicoes = navio.getJSONArray("posicoes");
    
                for (int j = 0; j < posicoes.length(); j++) {
                    JSONArray posicao = posicoes.getJSONArray(j);
                    int linhaTabuleiro = posicao.getInt(0);
                    int colunaTabuleiro = posicao.getInt(1);
    
                    if (tabuleiro[linhaTabuleiro][colunaTabuleiro] != ACERTO) {
                        return false; // Ainda há posições não acertadas
                    }
                }
            }
            return true; // Todas as posições foram acertadas
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo JSON: " + e.getMessage());
            return false; // Se der erro, consideramos que o jogo continua
        }
    }
    

    private void exibirTabuleiro(char[][] tabuleiroJogador, char[][] tabuleiroAtaque) {
        System.out.println("Seu tabuleiro:                Tabuleiro de ataques:");
        
        // Cabeçalho das colunas para ambos os tabuleiros
        System.out.print("   "); // Espaçamento inicial para alinhar
        for (int i = 0; i < GRID_SIZE; i++) System.out.printf("%1d ", i);
        System.out.print("        ");
        for (int i = 0; i < GRID_SIZE; i++) System.out.printf("%1d ", i);
        System.out.println();
        
        // Linhas de ambos os tabuleiros
        for (int i = 0; i < GRID_SIZE; i++) {
            // Exibir linha do tabuleiro do jogador
            System.out.printf("%2d ", i); // Número da linha
            for (int j = 0; j < GRID_SIZE; j++) {
                System.out.printf("%2s ", colorir(tabuleiroJogador[i][j]));
            }
            
            // Separação entre os tabuleiros
            System.out.print("     ");
            
            // Exibir linha do tabuleiro de ataques
            System.out.printf("%2d ", i); // Número da linha
            for (int j = 0; j < GRID_SIZE; j++) {
                System.out.printf("%2s ", colorir(tabuleiroAtaque[i][j]));
            }
            
            System.out.println(); // Quebra de linha após cada linha de tabuleiro
        }
    }
    
    private String colorir(char c) {
        switch (c) {
            case AGUA:
                return Cor.AZUL + String.valueOf(c) + Cor.RESET;
            case NAVIO:
                return Cor.VERDE + String.valueOf(c) + Cor.RESET;
            case ACERTO:
                return Cor.VERMELHO + String.valueOf(c) + Cor.RESET;
            case ERRO:
                return Cor.AMARELO + String.valueOf(c) + Cor.RESET;
            default:
                return String.valueOf(c); // Sem cor para caracteres desconhecidos
        }
    }
    
}


