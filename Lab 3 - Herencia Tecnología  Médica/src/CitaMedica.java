import java.time.LocalDateTime;
import java.util.*;

public class CitaMedica {

    private final String idCita;
    private final String nombrePaciente;
    private TrabajadorMedico trabajadorAsignado;
    private LocalDateTime fechaHora;
    private final int duracionMinutos;
    private final TipoCita tipo;

    private EstadoCita estado = EstadoCita.Programada;

    private final List<CambioAgenda> historial =
            new ArrayList<>();

    public CitaMedica(
            String id,
            String paciente,
            TrabajadorMedico trabajador,
            LocalDateTime fecha,
            int minutos,
            TipoCita tipo) {

        idCita = TrabajadorMedico.texto(id);
        nombrePaciente = TrabajadorMedico.texto(paciente);
        trabajadorAsignado = Objects.requireNonNull(trabajador);
        fechaHora = Objects.requireNonNull(fecha);

        if (minutos <= 0 || minutos > 1440) {
            throw new IllegalArgumentException(
                    "La duracion debe ser de 1 a 1440 minutos.");
        }

        duracionMinutos = minutos;
        this.tipo = Objects.requireNonNull(tipo);
    }

    public String getIdCita() {
        return idCita;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public TrabajadorMedico getTrabajadorAsignado() {
        return trabajadorAsignado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public TipoCita getTipo() {
        return tipo;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public List<CambioAgenda> getHistorial() {
        return List.copyOf(historial);
    }

    public LocalDateTime obtenerFin() {
        return fechaHora.plusMinutes(duracionMinutos);
    }

    public boolean esReagendable() {
        return estado == EstadoCita.Programada
                || estado == EstadoCita.Confirmada
                || estado == EstadoCita.Reagendada;
    }

    void cambiarEstado(EstadoCita nuevo) {
        boolean valido =
                (esReagendable()
                        && (nuevo == EstadoCita.Cancelada
                        || nuevo == EstadoCita.Confirmada))
                || (estado == EstadoCita.Confirmada
                        && nuevo == EstadoCita.EnProgreso)
                || (estado == EstadoCita.EnProgreso
                        && nuevo == EstadoCita.Completada);

        if (!valido || estado == nuevo) {
            throw new IllegalArgumentException(
                    "Transicion de estado no permitida.");
        }

        estado = nuevo;
    }

    void reagendar(
            LocalDateTime fecha,
            TrabajadorMedico trabajador,
            String motivo) {

        if (!esReagendable()) {
            throw new IllegalStateException(
                    "La cita no se puede reagendar.");
        }

        historial.add(new CambioAgenda(
                fechaHora,
                fecha,
                trabajadorAsignado,
                trabajador,
                motivo));

        fechaHora = fecha;
        trabajadorAsignado = trabajador;
        estado = EstadoCita.Reagendada;
    }

    @Override
    public String toString() {
        return idCita
                + " | " + nombrePaciente
                + " | " + trabajadorAsignado.getIdEmpleado()
                + " | " + fechaHora
                + " | " + duracionMinutos + " min"
                + " | " + tipo
                + " | " + estado;
    }

    @Override
    public boolean equals(Object otro) {
        return otro instanceof CitaMedica
                && idCita.equals(((CitaMedica) otro).idCita);
    }

    @Override
    public int hashCode() {
        return idCita.hashCode();
    }
}