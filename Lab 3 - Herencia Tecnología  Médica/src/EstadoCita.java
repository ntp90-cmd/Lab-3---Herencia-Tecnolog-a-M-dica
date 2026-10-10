public enum EstadoCita {
    Programada,
    Confirmada,
    EnProgreso,
    Completada,
    Cancelada,
    Reagendada;

    public static EstadoCita desdeTexto(String texto) {
        String normalizado = texto.trim()
                .replace(" ", "")
                .replace("_", "");

        for (EstadoCita valor : values()) {
            if (valor.name().equalsIgnoreCase(normalizado)) {
                return valor;
            }
        }

        throw new IllegalArgumentException(
                "Estado no valido: " + texto);
    }
}