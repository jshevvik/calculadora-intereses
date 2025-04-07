package app.calculadora;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para la calculadora de interés compuesto
 */
public class Controller {
    // Elementos de la interfaz
    @FXML private TextField txtCapital;
    @FXML private TextField txtAportacion;
    @FXML private TextField txtInteres;
    @FXML private TextField txtTiempo;
    @FXML private ComboBox<String> cbFrecuencia;
    @FXML private CheckBox chkAportaciones;
    @FXML private VBox containerAportaciones;
    @FXML private Label lblResultado;
    @FXML private ComboBox<String> cbInflacion;

    
    // Mapa para frecuencias de aportación
    private final Map<String, Integer> frecuenciaMap = new HashMap<>();
    
    // Formateador de números
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        configurarValidaciones();
        configurarFrecuencias();
        configurarListeners();
        configurarInflacion();

    }

    /**
     * Configura validaciones para los campos de texto
     */
    private void configurarValidaciones() {
        // Solo permitir números decimales positivos
        txtCapital.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 0.0, 
            change -> change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null));
        
        txtAportacion.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 0.0, 
            change -> change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null));
        
        txtInteres.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 0.0, 
            change -> change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null));
        
        // Solo permitir números enteros positivos para los años
        txtTiempo.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, 
            change -> change.getControlNewText().matches("\\d*") ? change : null));
    }

    /**
     * Configura las opciones de frecuencia de aportación
     */
    private void configurarFrecuencias() {
        frecuenciaMap.put("Mensual", 12);
        frecuenciaMap.put("Trimestral", 4);
        frecuenciaMap.put("Semestral", 2);
        frecuenciaMap.put("Anual", 1);
        
        cbFrecuencia.getItems().addAll(frecuenciaMap.keySet());
        cbFrecuencia.getSelectionModel().selectFirst();
    }

    /**
     * Configura los listeners para eventos
     */
    private void configurarListeners() {
        // Mostrar/ocultar campos de aportación según el checkbox
        chkAportaciones.selectedProperty().addListener((obs, oldVal, newVal) -> {
            containerAportaciones.setVisible(newVal);
            txtAportacion.setDisable(!newVal);
            cbFrecuencia.setDisable(!newVal);
        });
    }

    /**
     * Método principal que realiza el cálculo
     */
    @FXML
    private void calcularInteres(ActionEvent event) {
        try {
            // Obtener valores de los campos
            double capitalInicial = parseDouble(txtCapital.getText());
            double tasaInteres = parseDouble(txtInteres.getText()) / 100;
            int años = parseInt(txtTiempo.getText());
            
            // Obtener inflación seleccionada
            String inflacionSeleccionada = cbInflacion.getValue().replace("%", "").trim();
            double tasaInflacion = Double.parseDouble(inflacionSeleccionada) / 100;


            
            // Validaciones básicas
            if (capitalInicial <= 0 || tasaInteres <= 0 || años <= 0) {
                mostrarError("Todos los valores deben ser positivos");
                return;
            }
            
            double resultado;
            String detalle;

            if (chkAportaciones.isSelected()) {
                double aportacion = parseDouble(txtAportacion.getText());
                if (aportacion <= 0) {
                    mostrarError("La aportación debe ser positiva");
                    return;
                }
                
                int frecuencia = frecuenciaMap.get(cbFrecuencia.getValue());
                resultado = calcularConAportaciones(capitalInicial, tasaInteres, años, aportacion, frecuencia);
                
                detalle = String.format(
                    "Con %d aportaciones de %s€/%s", 
                    años * frecuencia,
                    df.format(aportacion),
                    cbFrecuencia.getValue().toLowerCase()
                );
            } else {
                resultado = calcularSinAportaciones(capitalInicial, tasaInteres, años);
                detalle = "Sin aportaciones adicionales";
            }

            double resultadoReal = resultado / Math.pow(1 + tasaInflacion, años); // Ajustado por inflación
            
            double totalAportado = 0;

            if (chkAportaciones.isSelected()) {
                int frecuencia = frecuenciaMap.get(cbFrecuencia.getValue());
                totalAportado = parseDouble(txtAportacion.getText()) * frecuencia * años;
            }


            mostrarResultado(resultado, capitalInicial, tasaInteres * 100, años, detalle, resultadoReal, totalAportado);

        } catch (Exception e) {
            mostrarError("Error en los datos: " + e.getMessage());
        }
    }

    /**
     * Calcula el interés compuesto SIN aportaciones periódicas
     */
    private double calcularSinAportaciones(double capital, double tasa, int años) {
        return capital * Math.pow(1 + tasa, años);
    }

    /**
     * Calcula el interés compuesto CON aportaciones periódicas
     */
    private double calcularConAportaciones(double capital, double tasa, int años, double aportacion, int frecuencia) {
        // Convertir tasa anual a tasa por periodo y calcular total de periodos
        double tasaPeriodica = tasa / frecuencia;
        int totalPeriodos = años * frecuencia;
        
        // Valor futuro del capital inicial
        double futuroCapital = capital * Math.pow(1 + tasaPeriodica, totalPeriodos);
        
        // Valor futuro de las aportaciones (fórmula de anualidad)
        double futuroAportaciones = aportacion * 
            (Math.pow(1 + tasaPeriodica, totalPeriodos) - 1) / 
            tasaPeriodica;
        
        return futuroCapital + futuroAportaciones;
    }
    
    /**
     * Muestra resultado con inflacion segun opcion eligida
     */
    private void configurarInflacion() {
        cbInflacion.getItems().addAll("2%", "5%", "8%");
        cbInflacion.getSelectionModel().selectFirst();
    }


    /**
     * Muestra el resultado del cálculo
     */
    private void mostrarResultado(double resultado, double capitalInicial, double tasa, int años, String detalle, double resultadoReal, double totalAportado) {
    	
    	double inversionTotal = capitalInicial + totalAportado;
        double gananciaPura = resultado - capitalInicial;
        
        String mensaje = String.format(
            "💰 Resultado después de %d años al %.2f%% anual:\n\n" +
            "➤ Capital Inicial: %s€\n" +
            		(totalAportado > 0 ? "➤ Aportaciones Totales: %s€\n" : "") +
            "➤ Capital Final: %s€\n" +
            "➤ Ganancia: %s€\n" +
            "➤ Capital Ajustado por inflación: %s€\n\n" +
            "➤ Detalle: %s",
            años, tasa,
            df.format(capitalInicial),
            totalAportado > 0 ? df.format(totalAportado) : "",
            df.format(resultado),
            df.format(gananciaPura),
            df.format(resultadoReal),
            detalle
        );

        lblResultado.setText(mensaje);
        lblResultado.getStyleClass().removeAll("error");
        lblResultado.getStyleClass().add("success");
        lblResultado.setVisible(true);
    }


    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        lblResultado.setText("⚠ Error: " + mensaje);
        lblResultado.getStyleClass().removeAll("success");
        lblResultado.getStyleClass().add("error");
        lblResultado.setVisible(true);
    }

    /**
     * Convierte String a double con manejo de errores
     */
    private double parseDouble(String input) {
        if (input == null || input.trim().isEmpty()) return 0;
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Convierte String a int con manejo de errores
     */
    private int parseInt(String input) {
        if (input == null || input.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}