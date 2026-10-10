import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HospitalManager {

    private final Map<String, TrabajadorMedico> trabajadores =
            new LinkedHashMap<>();

    private final Map<String, CitaMedica> citas =
            new LinkedHashMap<>();

    private final Consumer<String> notificar;

    public HospitalManager(Consumer<String> notificar) {
        this.notificar = Objects.requireNonNull(notificar);
    }

    public void registrarTrabajador(TrabajadorMedico trabajador) {
        Objects.requireNonNull(trabajador);

        if (trabajadores.putIfAbsent(
                trabajador.getIdEmpleado(), trabajador) != null) {

            throw new IllegalArgumentException(
                    "Id de empleado duplicado.");
        }
    }

    public TrabajadorMedico obtenerTrabajador(String id) {
        TrabajadorMedico trabajador = trabajadores.get(id);

        if (trabajador == null) {
            throw new IllegalArgumentException(
                    "Trabajador no encontrado.");
        }

        return trabajador;
    }

    public CitaMedica obtenerCita(String id) {
        CitaMedica cita = citas.get(id);

        if (cita == null) {
            throw new IllegalArgumentException(
                    "Cita no encontrada.");
        }

        return cita;
    }

    public List<TrabajadorMedico> listarPersonal() {
        return List.copyOf(trabajadores.values());
    }

    public List<CitaMedica> listarCitas() {
        return List.copyOf(citas.values());
    }

    public List<CitaMedica> listarCitas(EstadoCita estado) {
        return citas.values().stream()
                .filter(c -> c.getEstado() == estado)
                .collect(Collectors.toList());
    }

    public List<CitaMedica> listarCitas(
            TrabajadorMedico trabajador) {

        return citas.values().stream()
                .filter(c -> c.getTrabajadorAsignado()
                        .equals(trabajador))
                .collect(Collectors.toList());
    }

    private boolean seCruza(
            CitaMedica cita,
            LocalDateTime inicio,
            LocalDateTime fin) {

        return inicio.isBefore(cita.obtenerFin())
                && cita.getFechaHora().isBefore(fin);
    }

    public boolean hayConflicto(
            TrabajadorMedico trabajador,
            LocalDateTime inicio,
            int minutos) {

        if (minutos <= 0) {
            throw new IllegalArgumentException(
                    "Duracion invalida.");
        }

        return citas.values().stream()
                .anyMatch(c ->
                        c.getTrabajadorAsignado().equals(trabajador)
                        && c.getEstado() != EstadoCita.Cancelada
                        && seCruza(
                                c,
                                inicio,
                                inicio.plusMinutes(minutos)));
    }

    private boolean puedeAsignar(
            TrabajadorMedico trabajador,
            TipoCita tipo,
            LocalDateTime inicio,
            int minutos,
            CitaMedica ignorar) {

        if (trabajador == null
                || trabajadores.get(trabajador.getIdEmpleado())
                    != trabajador
                || minutos <= 0
                || minutos > 1440
                || !trabajador.puedeAtender(tipo)) {

            return false;
        }

        LocalDateTime fin = inicio.plusMinutes(minutos);

        if (!inicio.toLocalDate()
                .equals(fin.minusNanos(1).toLocalDate())
                || !trabajador.estaDisponible(inicio, fin)) {

            return false;
        }

        List<CitaMedica> delDia = new ArrayList<>();

        for (CitaMedica cita : citas.values()) {
            if (cita == ignorar
                    || cita.getEstado() == EstadoCita.Cancelada) {
                continue;
            }

            if (cita.getTrabajadorAsignado().equals(trabajador)) {
                if (seCruza(cita, inicio, fin)) {
                    return false;
                }

                if (cita.getFechaHora().toLocalDate()
                        .equals(inicio.toLocalDate())) {

                    delDia.add(cita);
                }
            }
        }

        if (trabajador instanceof DoctorGeneral) {
            DoctorGeneral doctor = (DoctorGeneral) trabajador;

            if (delDia.size() >= doctor.getCapacidadDiaria()) {
                return false;
            }
        }

        if (trabajador instanceof Cirujano) {
            Cirujano cirujano = (Cirujano) trabajador;

            int ocupados = delDia.stream()
                    .mapToInt(CitaMedica::getDuracionMinutos)
                    .sum();

            if (ocupados + minutos
                    > cirujano.getHorasDisponibles() * 60) {

                return false;
            }
        }

        return true;
    }

    public TrabajadorMedico buscarDisponible(
            TipoCita tipo,
            LocalDateTime inicio,
            int minutos) {

        for (TrabajadorMedico trabajador : trabajadores.values()) {
            if (puedeAsignar(
                    trabajador, tipo, inicio, minutos, null)) {

                return trabajador;
            }
        }

        return null;
    }

    public boolean programarCita(CitaMedica cita) {
        Objects.requireNonNull(cita);

        if (citas.containsKey(cita.getIdCita())) {
            throw new IllegalArgumentException(
                    "Id de cita duplicado.");
        }

        if (cita.getEstado() != EstadoCita.Programada
                || !puedeAsignar(
                        cita.getTrabajadorAsignado(),
                        cita.getTipo(),
                        cita.getFechaHora(),
                        cita.getDuracionMinutos(),
                        null)) {

            return false;
        }

        citas.put(cita.getIdCita(), cita);
        notificar.accept("Cita programada: " + cita);

        return true;
    }

    public boolean reagendarCita(
            String id,
            LocalDateTime fecha) {

        return reagendarCita(
                id,
                fecha,
                obtenerCita(id).getTrabajadorAsignado());
    }

    public boolean reagendarCita(
            String id,
            LocalDateTime fecha,
            TrabajadorMedico trabajador) {

        return mover(
                obtenerCita(id),
                fecha,
                trabajador,
                "Cambio solicitado por el administrador");
    }

    private boolean mover(
            CitaMedica cita,
            LocalDateTime fecha,
            TrabajadorMedico trabajador,
            String motivo) {

        if (!cita.esReagendable()) {
            return false;
        }

        if (cita.getFechaHora().equals(fecha)
                && cita.getTrabajadorAsignado()
                        .equals(trabajador)) {

            return false;
        }

        if (!puedeAsignar(
                trabajador,
                cita.getTipo(),
                fecha,
                cita.getDuracionMinutos(),
                cita)) {

            return false;
        }

        cita.reagendar(fecha, trabajador, motivo);
        notificar.accept("Cita reagendada: " + cita);

        return true;
    }

    public boolean reagendarAutomaticamente(String id) {
        return buscarAlternativa(
                obtenerCita(id),
                "Reagendamiento automatico");
    }

    private boolean buscarAlternativa(
            CitaMedica cita,
            String motivo) {

        if (!cita.esReagendable()) {
            return false;
        }

        LocalDateTime inicio = cita.getFechaHora();

        for (LocalDateTime fecha = inicio;
                fecha.isBefore(inicio.plusDays(30));
                fecha = fecha.plusMinutes(15)) {

            if (mover(
                    cita,
                    fecha,
                    cita.getTrabajadorAsignado(),
                    motivo)) {

                return true;
            }

            for (TrabajadorMedico trabajador
                    : trabajadores.values()) {

                if (trabajador != cita.getTrabajadorAsignado()
                        && mover(cita, fecha, trabajador, motivo)) {

                    return true;
                }
            }
        }

        notificar.accept(
                "Sin alternativa para " + cita.getIdCita()
                + "; se conserva la cita para revision.");

        return false;
    }

    public List<CitaMedica> registrarAusencia(
            String id,
            LocalDateTime inicio,
            LocalDateTime fin) {

        TrabajadorMedico trabajador = obtenerTrabajador(id);

        Disponibilidad ausencia =
                new Disponibilidad(inicio, fin, false);

        boolean iniciada = listarCitas(trabajador).stream()
                .anyMatch(c ->
                        (c.getEstado() == EstadoCita.EnProgreso
                        || c.getEstado() == EstadoCita.Completada)
                        && seCruza(c, inicio, fin));

        if (iniciada) {
            throw new IllegalArgumentException(
                    "La ausencia afecta una atencion "
                    + "ya iniciada o completada.");
        }

        trabajador.agregarDisponibilidad(ausencia);

        List<CitaMedica> pendientes = new ArrayList<>();

        for (CitaMedica cita : listarCitas(trabajador)) {
            if (cita.esReagendable()
                    && seCruza(cita, inicio, fin)
                    && !buscarAlternativa(
                            cita, "Ausencia de " + id)) {

                pendientes.add(cita);
            }
        }

        return pendientes;
    }

    public void cambiarEstado(
            String id,
            EstadoCita estado) {

        CitaMedica cita = obtenerCita(id);

        if (estado == EstadoCita.EnProgreso
                && !puedeAsignar(
                        cita.getTrabajadorAsignado(),
                        cita.getTipo(),
                        cita.getFechaHora(),
                        cita.getDuracionMinutos(),
                        cita)) {

            throw new IllegalStateException(
                    "La cita tiene un conflicto pendiente.");
        }

        cita.cambiarEstado(estado);

        if (estado == EstadoCita.Completada) {
            cita.getTrabajadorAsignado().registrarAtencion(cita);
        }

        notificar.accept("Nuevo estado: " + cita);
    }

    public double calcularNomina(String departamento) {
        return calcularNomina(departamento, YearMonth.now());
    }

    public double calcularNomina(
            String departamento,
            YearMonth mes) {

        return trabajadores.values().stream()
                .filter(t -> t.getDepartamento()
                        .equalsIgnoreCase(departamento))
                .mapToDouble(t -> t.calcularSalario(mes))
                .sum();
    }

    public Map<String, Double> nominaPorDepartamento(
            YearMonth mes) {

        Map<String, Double> resultado =
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (TrabajadorMedico trabajador : trabajadores.values()) {
            resultado.merge(
                    trabajador.getDepartamento(),
                    trabajador.calcularSalario(mes),
                    Double::sum);
        }

        return resultado;
    }

    public double calcularUtilizacion(
            TrabajadorMedico trabajador,
            LocalDate fecha) {

        long disponibles =
                trabajador.minutosDisponibles(fecha);

        if (disponibles == 0) {
            return 0;
        }

        long ocupados = 0;
        List<CitaMedica> agenda = listarCitas(trabajador);

        for (int i = 0; i < 1440; i++) {
            LocalDateTime minuto =
                    fecha.atStartOfDay().plusMinutes(i);

            if (!trabajador.estaDisponible(
                    minuto, minuto.plusMinutes(1))) {
                continue;
            }

            for (CitaMedica cita : agenda) {
                if (cita.getEstado() != EstadoCita.Cancelada
                        && seCruza(
                                cita,
                                minuto,
                                minuto.plusMinutes(1))) {

                    ocupados++;
                    break;
                }
            }
        }

        return ocupados * 100.0 / disponibles;
    }

    public List<CambioAgenda> consultarHistorial(String idCita) {
        return obtenerCita(idCita).getHistorial();
    }
}