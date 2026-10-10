public class Main {

    public static void main(String[] args) {
        HospitalVista vista = new HospitalVista();

        HospitalManager modelo =
                new HospitalManager(vista::mostrarMensaje);

        HospitalControlador controlador =
                new HospitalControlador(modelo, vista);

        controlador.iniciar();
    }
}
