import java.time.YearMonth;
import java.util.List;

public class Radiologo extends TrabajadorMedico {

    private final List<String> equiposCertificados;
    private final double tarifaEstudio;

    public Radiologo(
            String id,
            String nombre,
            String departamento,
            int experiencia,
            double base,
            List<String> equipos,
            double tarifa) {

        super(id, nombre, departamento, experiencia, base);

        if (equipos == null || equipos.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe indicar los equipos certificados.");
        }

        equipos.forEach(TrabajadorMedico::texto);
        equiposCertificados = List.copyOf(equipos);
        tarifaEstudio = monto(tarifa);
    }

    public List<String> getEquiposCertificados() {
        return equiposCertificados;
    }

    public double getTarifaEstudio() {
        return tarifaEstudio;
    }

    public int getEstudiosRealizados(YearMonth mes) {
        return (int) getActividad(mes);
    }

    @Override
    public double calcularSalario(YearMonth mes) {
        return getSalarioBase()
                + getEstudiosRealizados(mes) * tarifaEstudio;
    }

    @Override
    public boolean puedeAtender(TipoCita tipo) {
        return tipo == TipoCita.Diagnostico;
    }

    @Override
    public String toString() {
        return super.toString() + " | " + equiposCertificados;
    }
}