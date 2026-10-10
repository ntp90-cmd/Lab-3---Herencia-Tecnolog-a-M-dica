import java.time.*;
import java.util.*;

public class HospitalControlador {

    private final HospitalManager modelo;
    private final HospitalVista vista;

    public HospitalControlador(
            HospitalManager modelo,
            HospitalVista vista) {

        this.modelo = modelo;
        this.vista = vista;
    }

    private LocalDateTime leerFecha(String mensaje) {
        return LocalDateTime.parse(
                vista.leerTexto(
                        mensaje + " (ejemplo: 2026-10-12T08:00)"));
    }

    private TrabajadorMedico leerTrabajador() {
        return modelo.obtenerTrabajador(
                vista.leerTexto("Id trabajador"));
    }

    public void iniciar() {
        while (true) {
            try {
                int opcion = vista.mostrarMenu();

                switch (opcion) {
                    case 0:
                        return;

                    case 1:
                        gestionarPersonal();
                        break;

                    case 2:
                        modelo.listarPersonal().forEach(
                                t -> vista.mostrarMensaje(t.toString()));
                        break;

                    case 3:
                        TrabajadorMedico trabajador = leerTrabajador();

                        trabajador.agregarDisponibilidad(
                                new Disponibilidad(
                                        leerFecha("Inicio"),
                                        leerFecha("Fin"),
                                        true));

                        vista.mostrarMensaje("Jornada agregada.");
                        break;

                    case 4:
                        gestionarCitas();
                        break;

                    case 5:
                        listarCitas();
                        break;

                    case 6:
                        String id = vista.leerTexto("Id cita");

                        vista.mostrarMensaje(
                                "Estados: "
                                + Arrays.toString(EstadoCita.values()));

                        EstadoCita estado = EstadoCita.desdeTexto(
                                vista.leerTexto("Estado"));

                        modelo.cambiarEstado(id, estado);
                        break;

                    case 7:
                        reagendar();
                        break;

                    case 8:
                        String empleado =
                                vista.leerTexto("Id trabajador ausente");

                        List<CitaMedica> pendientes =
                                modelo.registrarAusencia(
                                        empleado,
                                        leerFecha("Inicio"),
                                        leerFecha("Fin"));

                        vista.mostrarMensaje(
                                "Citas pendientes de resolver: "
                                + pendientes.size());
                        break;

                    case 9:
                        generarReportes();
                        break;

                    case 10:
                        LocalDate dia = LocalDate.parse(
                                vista.leerTexto(
                                        "Fecha (ejemplo: 2026-10-12)"));

                        for (TrabajadorMedico t
                                : modelo.listarPersonal()) {

                            vista.mostrarMensaje(String.format(
                                    Locale.ROOT,
                                    "%s: %.2f%%",
                                    t.getNombre(),
                                    modelo.calcularUtilizacion(t, dia)));
                        }
                        break;

                    case 11:
                        List<CambioAgenda> cambios =
                                modelo.consultarHistorial(
                                        vista.leerTexto("Id cita"));

                        if (cambios.isEmpty()) {
                            vista.mostrarMensaje(
                                    "Sin cambios registrados.");
                        }

                        cambios.forEach(
                                c -> vista.mostrarMensaje(c.toString()));
                        break;

                    default:
                        vista.mostrarMensaje("Opcion no valida.");
                }

            } catch (NoSuchElementException e) {
                return;

            } catch (IllegalArgumentException
                    | IllegalStateException
                    | DateTimeException e) {

                vista.mostrarMensaje(
                        "No se pudo realizar la operacion: "
                        + e.getMessage());
            }
        }
    }

    public void gestionarPersonal() {
        int tipo = vista.leerEntero(
                "1 Doctor, 2 Cirujano, 3 Enfermero, "
                + "4 Radiologo, 5 Terapeuta");

        if (tipo < 1 || tipo > 5) {
            throw new IllegalArgumentException("Tipo invalido.");
        }

        String id = vista.leerTexto("Id");
        String nombre = vista.leerTexto("Nombre");
        String departamento = vista.leerTexto("Departamento");
        int experiencia = vista.leerEntero("Anios de experiencia");
        double base = vista.leerDecimal("Salario base mensual");

        TrabajadorMedico trabajador;

        switch (tipo) {
            case 1:
                trabajador = new DoctorGeneral(
                        id,
                        nombre,
                        departamento,
                        experiencia,
                        base,
                        vista.leerTexto("Especializacion"),
                        vista.leerEntero("Capacidad diaria"),
                        vista.leerDecimal("Tarifa por consulta"));
                break;

            case 2:
                trabajador = new Cirujano(
                        id,
                        nombre,
                        departamento,
                        experiencia,
                        base,
                        Arrays.asList(
                                vista.leerTexto(
                                        "Operaciones separadas por coma")
                                        .split(",")),
                        vista.leerDecimal("Horas diarias disponibles"),
                        vista.leerDecimal("Tarifa por hora"),
                        vista.leerDecimal("Bono mensual de riesgo"));
                break;

            case 3:
                trabajador = new Enfermero(
                        id,
                        nombre,
                        departamento,
                        experiencia,
                        base,
                        vista.leerTexto("Turno Diurno o Nocturno"),
                        vista.leerTexto("Certificacion"),
                        vista.leerDecimal("Bono nocturno mensual"));
                break;

            case 4:
                trabajador = new Radiologo(
                        id,
                        nombre,
                        departamento,
                        experiencia,
                        base,
                        Arrays.asList(
                                vista.leerTexto(
                                        "Equipos separados por coma")
                                        .split(",")),
                        vista.leerDecimal("Comision por estudio"));
                break;

            default:
                trabajador = new Terapeuta(
                        id,
                        nombre,
                        departamento,
                        experiencia,
                        base,
                        vista.leerTexto("Terapia"),
                        vista.leerEntero("Duracion promedio en minutos"),
                        vista.leerDecimal("Comision por sesion"));
        }

        modelo.registrarTrabajador(trabajador);

        vista.mostrarMensaje(
                "Trabajador registrado. "
                + "Agrega su jornada con la opcion 3.");
    }

    public void gestionarCitas() {
        String id = vista.leerTexto("Id cita");
        String paciente = vista.leerTexto("Paciente");

        vista.mostrarMensaje(
                "Tipos: " + Arrays.toString(TipoCita.values()));

        TipoCita tipo = TipoCita.desdeTexto(
                vista.leerTexto("Tipo"));

        LocalDateTime fecha = leerFecha("Fecha de la cita");
        int minutos = vista.leerEntero("Duracion en minutos");

        String empleado = vista.leerTexto(
                "Id trabajador (Enter para asignacion automatica)");

        TrabajadorMedico trabajador;

        if (empleado.isEmpty()) {
            trabajador = modelo.buscarDisponible(
                    tipo, fecha, minutos);
        } else {
            trabajador = modelo.obtenerTrabajador(empleado);
        }

        if (trabajador == null) {
            vista.mostrarMensaje(
                    "Sin personal disponible en ese horario.");
            return;
        }

        CitaMedica cita = new CitaMedica(
                id,
                paciente,
                trabajador,
                fecha,
                minutos,
                tipo);

        if (!modelo.programarCita(cita)) {
            vista.mostrarMensaje(
                    "No se pudo programar: revisa "
                    + "compatibilidad, jornada y capacidad.");
        }
    }

    private void listarCitas() {
        int filtro = vista.leerEntero(
                "1 Todas, 2 Por estado, 3 Por trabajador");

        List<CitaMedica> citas;

        switch (filtro) {
            case 1:
                citas = modelo.listarCitas();
                break;

            case 2:
                vista.mostrarMensaje(
                        Arrays.toString(EstadoCita.values()));

                EstadoCita estado = EstadoCita.desdeTexto(
                        vista.leerTexto("Estado"));

                citas = modelo.listarCitas(estado);
                break;

            case 3:
                citas = modelo.listarCitas(leerTrabajador());
                break;

            default:
                throw new IllegalArgumentException(
                        "Filtro invalido.");
        }

        if (citas.isEmpty()) {
            vista.mostrarMensaje("No hay citas.");
        }

        citas.forEach(c -> vista.mostrarMensaje(c.toString()));
    }

    private void reagendar() {
        String id = vista.leerTexto("Id cita");

        int modo = vista.leerEntero(
                "1 Automatico, 2 Fecha, 3 Fecha y trabajador");

        boolean resultado;

        switch (modo) {
            case 1:
                resultado = modelo.reagendarAutomaticamente(id);
                break;

            case 2:
                resultado = modelo.reagendarCita(
                        id,
                        leerFecha("Nueva fecha"));
                break;

            case 3:
                resultado = modelo.reagendarCita(
                        id,
                        leerFecha("Nueva fecha"),
                        leerTrabajador());
                break;

            default:
                throw new IllegalArgumentException(
                        "Modo invalido.");
        }

        if (!resultado) {
            vista.mostrarMensaje(
                    "No se realizo ningun cambio.");
        }
    }

    public void generarReportes() {
        YearMonth mes = YearMonth.parse(
                vista.leerTexto(
                        "Mes de nomina (ejemplo: 2026-10)"));

        for (TrabajadorMedico trabajador
                : modelo.listarPersonal()) {

            vista.mostrarReporte(String.format(
                    Locale.ROOT,
                    "%s: Q %.2f",
                    trabajador,
                    trabajador.calcularSalario(mes)));
        }

        Map<String, Double> nomina =
                modelo.nominaPorDepartamento(mes);

        nomina.forEach((departamento, total) ->
                vista.mostrarReporte(String.format(
                        Locale.ROOT,
                        "%s: Q %.2f",
                        departamento,
                        total)));

        double totalGeneral = nomina.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        vista.mostrarReporte(String.format(
                Locale.ROOT,
                "Total: Q %.2f",
                totalGeneral));
    }
}