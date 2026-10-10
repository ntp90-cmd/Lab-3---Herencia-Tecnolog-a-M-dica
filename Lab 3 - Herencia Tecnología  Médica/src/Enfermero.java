import java.time.YearMonth;

public class Enfermero extends TrabajadorMedico {

    private final String turno;
    private final String certificacion;
    private final double bonificacionNocturna;

    public Enfermero(
            String id,
            String nombre,
            String departamento,
            int experiencia,
            double base,
            String turno,
            String certificacion,
            double bono) {

        super(id, nombre, departamento, experiencia, base);

        String valor = texto(turno);

        if (valor.equalsIgnoreCase("Diurno")) {
            this.turno = "Diurno";
        } else if (valor.equalsIgnoreCase("Nocturno")) {
            this.turno = "Nocturno";
        } else {
            throw new IllegalArgumentException(
                    "El turno debe ser Diurno o Nocturno.");
        }

        this.certificacion = texto(certificacion);
        bonificacionNocturna = monto(bono);
    }

    public String getTurno() {
        return turno;
    }

    public String getCertificacion() {
        return certificacion;
    }

    public double getBonificacionNocturna() {
        return bonificacionNocturna;
    }

    @Override
    public double calcularSalario(YearMonth mes) {
        double bono = turno.equals("Nocturno")
                ? bonificacionNocturna : 0;

        return getSalarioBase() + bono;
    }

    @Override
    public boolean puedeAtender(TipoCita tipo) {
        return false;
    }

    @Override
    public String toString() {
        return super.toString()
                + " | " + turno
                + " | " + certificacion;
    }
}