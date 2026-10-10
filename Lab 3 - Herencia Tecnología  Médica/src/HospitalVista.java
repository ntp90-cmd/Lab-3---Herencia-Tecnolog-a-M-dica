import java.util.NoSuchElementException;
import java.util.Scanner;

public class HospitalVista {

    private final Scanner entrada = new Scanner(System.in);

    public String leerTexto(String mensaje) {
        System.out.print(mensaje + ": ");

        if (!entrada.hasNextLine()) {
            throw new NoSuchElementException("Fin de entrada.");
        }

        return entrada.nextLine().trim();
    }

    public int leerEntero(String mensaje) {
        return Integer.parseInt(leerTexto(mensaje));
    }

    public double leerDecimal(String mensaje) {
        return Double.parseDouble(leerTexto(mensaje));
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    public void mostrarReporte(String reporte) {
        System.out.println(reporte);
    }

    public int mostrarMenu() {
        mostrarMensaje(
                "\nHospital"
                + "\n1. Registrar trabajador"
                + "\n2. Listar personal"
                + "\n3. Agregar jornada"
                + "\n4. Programar cita"
                + "\n5. Listar citas"
                + "\n6. Cambiar estado"
                + "\n7. Reagendar"
                + "\n8. Registrar ausencia"
                + "\n9. Nomina mensual"
                + "\n10. Utilizacion"
                + "\n11. Historial"
                + "\n0. Salir");

        return leerEntero("Opcion");
    }
}