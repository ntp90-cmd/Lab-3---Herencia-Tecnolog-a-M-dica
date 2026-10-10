import java.time.LocalDateTime;
import java.util.Objects;

public final class Disponibilidad {

    private final LocalDateTime inicio;
    private final LocalDateTime fin;
    private final boolean disponible;

    public Disponibilidad(
            LocalDateTime inicio,
            LocalDateTime fin,
            boolean disponible) {

        this.inicio = Objects.requireNonNull(inicio);
        this.fin = Objects.requireNonNull(fin);

        if (!inicio.isBefore(fin)) {
            throw new IllegalArgumentException("Intervalo invalido.");
        }

        this.disponible = disponible;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public boolean contiene(
            LocalDateTime desde,
            LocalDateTime hasta) {

        return !desde.isBefore(inicio)
                && !hasta.isAfter(fin);
    }

    public boolean seCruza(
            LocalDateTime desde,
            LocalDateTime hasta) {

        return desde.isBefore(fin)
                && inicio.isBefore(hasta);
    }
}