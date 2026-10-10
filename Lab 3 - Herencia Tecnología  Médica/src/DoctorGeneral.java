import java.time.YearMonth;

public class DoctorGeneral extends TrabajadorMedico {

    private final String especializacion;
    private final int capacidadDiaria;
    private final double tarifaConsulta;

    public DoctorGeneral(
            String id,
            String nombre,
            String departamento,
            int experiencia,
            double base,
            String especializacion,
            int capacidad,
            double tarifa) {

        super(id, nombre, departamento, experiencia, base);

        this.especializacion = texto(especializacion);

        if (capacidad <= 0) {
            throw new IllegalArgumentException(
                    "La capacidad debe ser mayor que cero.");
        }

        capacidadDiaria = capacidad;
        tarifaConsulta = monto(tarifa);
    }

    public String getEspecializacion() {
        return especializacion;
    }

    public int getCapacidadDiaria() {
        return capacidadDiaria;
    }

    public double getTarifaConsulta() {
        return tarifaConsulta;
    }

    public int getConsultasRealizadas(YearMonth mes) {
        return (int) getActividad(mes);
    }

    @Override
    public double calcularSalario(YearMonth mes) {
        return getSalarioBase()
                + getConsultasRealizadas(mes) * tarifaConsulta;
    }

    @Override
    public boolean puedeAtender(TipoCita tipo) {
        return tipo == TipoCita.ConsultaGeneral;
    }

    @Override
    public String toString() {
        return super.toString() + " | " + especializacion;
    }
}