import java.time.YearMonth;

public class Terapeuta extends TrabajadorMedico {

    private final String tipoTerapia;
    private final int duracionPromedio;
    private final double comisionPorSesion;

    public Terapeuta(
            String id,
            String nombre,
            String departamento,
            int experiencia,
            double base,
            String terapia,
            int duracion,
            double comision) {

        super(id, nombre, departamento, experiencia, base);

        tipoTerapia = texto(terapia);

        if (duracion <= 0) {
            throw new IllegalArgumentException(
                    "La duracion debe ser mayor que cero.");
        }

        duracionPromedio = duracion;
        comisionPorSesion = monto(comision);
    }

    public String getTipoTerapia() {
        return tipoTerapia;
    }

    public int getDuracionPromedio() {
        return duracionPromedio;
    }

    public double getComisionPorSesion() {
        return comisionPorSesion;
    }

    public int getSesionesRealizadas(YearMonth mes) {
        return (int) getActividad(mes);
    }

    @Override
    public double calcularSalario(YearMonth mes) {
        return getSalarioBase()
                + getSesionesRealizadas(mes) * comisionPorSesion;
    }

    @Override
    public boolean puedeAtender(TipoCita tipo) {
        return tipo == TipoCita.Terapia;
    }

    @Override
    public String toString() {
        return super.toString() + " | " + tipoTerapia;
    }
}