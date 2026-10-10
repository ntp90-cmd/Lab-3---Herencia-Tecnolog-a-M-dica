public enum TipoCita {
    ConsultaGeneral,
    Cirugia,
    Terapia,
    Diagnostico;

    public static TipoCita desdeTexto(String texto) {
        String normalizado = texto.trim()
                .replace(" ", "")
                .replace("_", "");

        for (TipoCita valor : values()) {
            if (valor.name().equalsIgnoreCase(normalizado)) {
                return valor;
            }
        }

        throw new IllegalArgumentException(
                "Tipo de cita no valido: " + texto);
    }
}