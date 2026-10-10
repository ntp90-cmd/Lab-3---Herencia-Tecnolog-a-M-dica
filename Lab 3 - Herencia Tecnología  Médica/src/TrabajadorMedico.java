import java.time.*;
import java.util.*;

public abstract class TrabajadorMedico {

    private final String idEmpleado;
    private String nombre;
    private String departamento;
    private final int aniosExperiencia;
    private final double salarioBase;

    private final List<Disponibilidad> disponibilidad =
            new ArrayList<>();

    private final Map<YearMonth, Double> actividad =
            new HashMap<>();

    protected TrabajadorMedico(
            String id,
            String nombre,
            String departamento,
            int experiencia,
            double salarioBase) {

        this.idEmpleado = texto(id);
        setNombre(nombre);
        setDepartamento(departamento);

        if (experiencia < 0) {
            throw new IllegalArgumentException(
                    "La experiencia no puede ser negativa.");
        }

        this.aniosExperiencia = experiencia;
        this.salarioBase = monto(salarioBase);
    }

    protected static String texto(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El texto no puede estar vacio.");
        }

        return valor.trim();
    }

    protected static double monto(double valor) {
        if (!Double.isFinite(valor) || valor < 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser finito y no negativo.");
        }

        return valor;
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDepartamento() {
        return departamento;
    }

    public int getAniosExperiencia() {
        return aniosExperiencia;
    }

    public double getSalarioBase() {
        return salarioBase;
    }

    public void setNombre(String nombre) {
        this.nombre = texto(nombre);
    }

    public void setDepartamento(String departamento) {
        this.departamento = texto(departamento);
    }

    public List<Disponibilidad> getDisponibilidad() {
        return List.copyOf(disponibilidad);
    }

    public void agregarDisponibilidad(Disponibilidad intervalo) {
        disponibilidad.add(Objects.requireNonNull(intervalo));
    }

    public boolean estaDisponible(
            LocalDateTime inicio,
            LocalDateTime fin) {

        boolean jornada = disponibilidad.stream()
                .anyMatch(d -> d.isDisponible()
                        && d.contiene(inicio, fin));

        boolean ausencia = disponibilidad.stream()
                .anyMatch(d -> !d.isDisponible()
                        && d.seCruza(inicio, fin));

        return jornada && !ausencia;
    }

    public long minutosDisponibles(LocalDate fecha) {
        long total = 0;

        for (int i = 0; i < 1440; i++) {
            LocalDateTime minuto =
                    fecha.atStartOfDay().plusMinutes(i);

            if (estaDisponible(minuto, minuto.plusMinutes(1))) {
                total++;
            }
        }

        return total;
    }

    public double getActividad(YearMonth mes) {
        return actividad.getOrDefault(mes, 0.0);
    }

    void registrarAtencion(CitaMedica cita) {
        double unidades;

        if (cita.getTipo() == TipoCita.Cirugia) {
            unidades = cita.getDuracionMinutos() / 60.0;
        } else {
            unidades = 1.0;
        }

        YearMonth mes = YearMonth.from(cita.getFechaHora());
        actividad.merge(mes, unidades, Double::sum);
    }

    public double calcularSalario() {
        return calcularSalario(YearMonth.now());
    }

    public abstract double calcularSalario(YearMonth mes);

    public abstract boolean puedeAtender(TipoCita tipo);

    @Override
    public String toString() {
        return idEmpleado
                + " | " + nombre
                + " | " + getClass().getSimpleName()
                + " | " + departamento;
    }

    @Override
    public final boolean equals(Object otro) {
        return otro instanceof TrabajadorMedico
                && idEmpleado.equals(
                        ((TrabajadorMedico) otro).idEmpleado);
    }

    @Override
    public final int hashCode() {
        return idEmpleado.hashCode();
    }
}