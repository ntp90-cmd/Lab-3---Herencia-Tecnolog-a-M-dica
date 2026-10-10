import java.time.LocalDateTime;
import java.util.Objects;

public final class CambioAgenda {

    private final LocalDateTime momentoCambio;
    private final LocalDateTime fechaAnterior;
    private final LocalDateTime fechaNueva;
    private final TrabajadorMedico trabajadorAnterior;
    private final TrabajadorMedico trabajadorNuevo;
    private final String motivo;

    public CambioAgenda(
            LocalDateTime anterior,
            LocalDateTime nueva,
            TrabajadorMedico previo,
            TrabajadorMedico nuevo,
            String motivo) {

        momentoCambio = LocalDateTime.now();
        fechaAnterior = Objects.requireNonNull(anterior);
        fechaNueva = Objects.requireNonNull(nueva);
        trabajadorAnterior = Objects.requireNonNull(previo);
        trabajadorNuevo = Objects.requireNonNull(nuevo);
        this.motivo = TrabajadorMedico.texto(motivo);
    }

    public LocalDateTime getMomentoCambio() {
        return momentoCambio;
    }

    public LocalDateTime getFechaAnterior() {
        return fechaAnterior;
    }

    public LocalDateTime getFechaNueva() {
        return fechaNueva;
    }

    public TrabajadorMedico getTrabajadorAnterior() {
        return trabajadorAnterior;
    }

    public TrabajadorMedico getTrabajadorNuevo() {
        return trabajadorNuevo;
    }

    public String getMotivo() {
        return motivo;
    }

    @Override
    public String toString() {
        return momentoCambio
                + " | " + fechaAnterior
                + " / " + trabajadorAnterior.getIdEmpleado()
                + " -> " + fechaNueva
                + " / " + trabajadorNuevo.getIdEmpleado()
                + " | " + motivo;
    }
}