public enum Cor {
    RESET("\033[0m"),
    AZUL("\033[34m"),
    VERDE("\033[32m"),
    VERMELHO("\033[31m"),
    AMARELO("\033[33m");

    private final String codigo;

    Cor(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String toString() {
        return codigo;
    }
}