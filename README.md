# Batalha Naval Multiplayer

## Descrição
O projeto **Batalha Naval Multiplayer** é uma implementação do tradicional jogo de Batalha Naval para múltiplos jogadores, onde dois jogadores competem online, posicionando seus navios e tentando acertar os navios do oponente. O jogo é baseado em uma arquitetura P2P, onde ambos os jogadores podem interagir em tempo real.

## Funcionalidades
- **Jogo Multiplayer:** O jogo suporta partidas entre dois jogadores, um deve atuar como servidor e outro como cliente.
- **Posicionamento de Navios:** Os jogadores podem posicionar seus navios no tabuleiro.
- **Ataques:** Cada jogador pode atacar o outro, tentando acertar os navios inimigos.
- **Interface de Texto:** O jogo utiliza um terminal/console para exibir os tabuleiros e resultados.

## Requisitos
- **Java 11+**: A versão mínima recomendada do Java para rodar o projeto.

## Como Rodar o Projeto

1. **Clone o repositório**:

```bash
git clone https://github.com/jsalles1/BatalhaNavalP2P.git
```

2. **Navegue até o diretório**

```bash
cd BatalhaNavalP2P/src
```

3. **Compile o código**:

```bash
javac -cp json-20240303.jar BatalhaNavalMultiplayer.java Cor.java
```

```bash
java -cp .:json-20240303.jar BatalhaNavalMultiplayer Cor
```

4. **Iniciar o Jogo**:
- O servidor aguardará a conexão do cliente, enquanto o cliente se conectará ao servidor entrando seu endereço de IP.
- Ambos os jogadores poderão posicionar seus navios, e então o jogo começará com cada jogador tentando atacar os navios do oponente.
- O jogador atuando como servidor ataca primeiro.

