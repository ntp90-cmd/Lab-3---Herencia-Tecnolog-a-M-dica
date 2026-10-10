import java.time.YearMonth;
import java.util.List;

public class Cirujano extends TrabajadorMedico {

    private final List<String> tiposOperaciones;
    private final double horasDisponibles;
    private final double tarifaPorHora;
    private final double bonoRiesgo;

    public Cirujano(
            String id,
            String nombre,
            String departamento,
            int experiencia,
            double base,
            List<String> operaciones,
            double horas,
            double tarifa,
            double bono) {

        super(id, nombre, departamento, experiencia, base);

        if (operaciones == null || operaciones.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe indicar las operaciones.");
        }

        operaciones.forEach(TrabajadorMedico::texto);
        tiposOperaciones = List.copyOf(operaciones);

        if (!Double.isFinite(horas) || horas <= 0 || horas > 24) {
            throw new IllegalArgumentException(
                    "Las horas diarias deben ser mayores que cero "
                    + "y no superar 24.");
        }

        horasDisponibles = horas;
        tarifaPorHora = monto(tarifa);
        bonoRiesgo = monto(bono);
    }

    public List<String> getTiposOperaciones() {
        return tiposOperaciones;
    }

    public double getHorasDisponibles() {
        return horasDisponibles;
    }

    public double getTarifaPorHora() {
        return tarifaPorHora;
    }

    public double getBonoRiesgo() {
        return bonoRiesgo;
    }

    public double getHorasRealizadas(YearMonth mes) {
        return getActividad(mes);
    }

    @Override
    public double calcularSalario(YearMonth mes) {
        return getSalarioBase()
                + getHorasRealizadas(mes) * tarifaPorHora
                + bonoRiesgo;
    }

    @Override
    public boolean puedeAtender(TipoCita tipo) {
        return tipo == TipoCita.Cirugia;
    }

    @Override
    public String toString() {
        return super.toString() + " | " + tiposOperaciones;
    }
}