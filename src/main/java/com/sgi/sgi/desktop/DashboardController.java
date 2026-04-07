/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgi.sgi.desktop;

import com.sgi.conexion.Conexion;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;


/**
 *
 * @author erice
 */
public class DashboardController implements Initializable{
    
    @FXML private Button btnInicio;
    @FXML private Button btnProductos;
    @FXML private Button btnMovimientos;
    @FXML private Button btnProveedores;
    @FXML private Label lblUsuario;
    @FXML private StackPane contenidoPrincipal;

    private String nombreUsuario = "";
    private String rolUsuario = "";

    private final String ESTILO_ACTIVO =
        "-fx-background-color: #1565c0; -fx-text-fill: white; " +
        "-fx-font-size: 13px; -fx-padding: 10; -fx-cursor: hand; " +
        "-fx-background-radius: 6; -fx-alignment: CENTER-LEFT;";
    private final String ESTILO_INACTIVO =
        "-fx-background-color: transparent; -fx-text-fill: #90caf9; " +
        "-fx-font-size: 13px; -fx-padding: 10; -fx-cursor: hand; " +
        "-fx-background-radius: 6; -fx-alignment: CENTER-LEFT;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mostrarDashboard();
    }

    public void setUsuario(String nombre, String rol) {
        this.nombreUsuario = nombre;
        this.rolUsuario = rol;
        lblUsuario.setText("👤 " + nombre + " (" + rol + ")");
    }

    private void marcarBotonActivo(Button activo) {
        btnInicio.setStyle(ESTILO_INACTIVO);
        btnProductos.setStyle(ESTILO_INACTIVO);
        btnMovimientos.setStyle(ESTILO_INACTIVO);
        btnProveedores.setStyle(ESTILO_INACTIVO);
        activo.setStyle(ESTILO_ACTIVO);
    }

    private void cargarVista(String fxml) {
        try {
            Parent vista = FXMLLoader.load(getClass()
                .getResource("/com/sgi/sgi/desktop/" + fxml));
            contenidoPrincipal.getChildren().clear();
            contenidoPrincipal.getChildren().add(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void mostrarDashboard() {
        marcarBotonActivo(btnInicio);
        cargarVistaInicio();
    }

    @FXML
    public void mostrarProductos() {
        marcarBotonActivo(btnProductos);
        cargarVista("Productos.fxml");
    }

    @FXML
    public void mostrarMovimientos() {
        marcarBotonActivo(btnMovimientos);
        cargarVista("Movimientos.fxml");
    }

    @FXML
    public void mostrarProveedores() {
        marcarBotonActivo(btnProveedores);
        mostrarProximamente("Proveedores");
    }

    private void mostrarProximamente(String modulo) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: #1a1a2e;");
        Label lbl = new Label("🚧 Módulo " + modulo + " próximamente");
        lbl.setStyle("-fx-text-fill: #546e7a; -fx-font-size: 20px;");
        vbox.getChildren().add(lbl);
        contenidoPrincipal.getChildren().clear();
        contenidoPrincipal.getChildren().add(vbox);
    }

    private void cargarVistaInicio() {
        try {
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");

            VBox vbox = new VBox(20);
            vbox.setStyle("-fx-padding: 25; -fx-background-color: #1a1a2e;");
            vbox.setFillWidth(true);

            // Título
            Label titulo = new Label("Inicio");
            titulo.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");
            vbox.getChildren().add(titulo);

            // Tarjetas
            int totalProductos = contarConsulta("SELECT COUNT(*) FROM productos WHERE activo = TRUE");
            int stockBajo     = contarConsulta("SELECT COUNT(*) FROM vista_stock_bajo");
            int entradasHoy   = contarConsulta("SELECT COUNT(*) FROM movimientos WHERE tipo = 'ENTRADA' AND DATE(fecha) = CURDATE()");
            int salidasHoy    = contarConsulta("SELECT COUNT(*) FROM movimientos WHERE tipo = 'SALIDA' AND DATE(fecha) = CURDATE()");

            HBox tarjetas = new HBox(15);
            tarjetas.setFillHeight(true);
            tarjetas.getChildren().addAll(
                crearTarjeta("📦 Total Productos", String.valueOf(totalProductos), "productos registrados", "white",   "#90caf9"),
                crearTarjeta("⚠️Stock Bajo",      String.valueOf(stockBajo),      "productos por agotarse","#ff7043","#ffb74d"),
                crearTarjeta("📥 Entradas Hoy",    String.valueOf(entradasHoy),    "movimientos de entrada","#66bb6a","#81c784"),
                crearTarjeta("📤 Salidas Hoy",     String.valueOf(salidasHoy),     "movimientos de salida", "#ef5350","#ef9a9a")
            );
            for (javafx.scene.Node n : tarjetas.getChildren())
                HBox.setHgrow(n, Priority.ALWAYS);
            vbox.getChildren().add(tarjetas);

            // ── Fila: Gráfica + Stock Bajo ──────────────────────────────────
            HBox filaGrafica = new HBox(15);
            filaGrafica.setFillHeight(true);

            // Gráfica
            VBox graficaBox = new VBox(10);
            graficaBox.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10; -fx-padding: 20;");
            HBox.setHgrow(graficaBox, Priority.ALWAYS);

            Label lblGrafica = new Label("Gráfica de Inventario");
            lblGrafica.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

            CategoryAxis ejeX = new CategoryAxis();
            ejeX.setLabel("Productos");
            ejeX.setTickLabelFill(javafx.scene.paint.Color.web("#90caf9"));

            NumberAxis ejeY = new NumberAxis();
            ejeY.setLabel("Cantidad");
            ejeY.setTickLabelFill(javafx.scene.paint.Color.web("#90caf9"));

            BarChart<String, Number> barChart = new BarChart<>(ejeX, ejeY);
            barChart.setStyle("-fx-background-color: transparent;");
            barChart.setLegendVisible(true);
            barChart.setPrefHeight(280);
            barChart.setAnimated(false);

            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            serie.setName("Stock actual");
            try {
                Connection con = Conexion.getConexion();
                ResultSet rs = con.createStatement().executeQuery(
                    "SELECT nombre, stock_actual FROM productos WHERE activo = TRUE " +
                    "ORDER BY stock_actual DESC LIMIT 8");
                while (rs.next())
                    serie.getData().add(new XYChart.Data<>(rs.getString("nombre"), rs.getInt("stock_actual")));
                rs.close();
            } catch (Exception e) { e.printStackTrace(); }

            barChart.getData().add(serie);
            graficaBox.getChildren().addAll(lblGrafica, barChart);

            // Stock bajo
            VBox stockBox = new VBox(10);
            stockBox.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10; -fx-padding: 20;");
            stockBox.setPrefWidth(280);
            stockBox.setMinWidth(280);

            Label lblStockBajo = new Label("⚠️ Productos con Stock Bajo");
            lblStockBajo.setStyle("-fx-text-fill: #ffb74d; -fx-font-size: 13px; -fx-font-weight: bold;");

            TableView<ProductosController.Producto> tablaStock = new TableView<>();
            tablaStock.setStyle("-fx-background-color: #0f3460; -fx-border-color: #1565c0;");
            VBox.setVgrow(tablaStock, Priority.ALWAYS);

            TableColumn<ProductosController.Producto, String>  cNom  = new TableColumn<>("Producto");
            TableColumn<ProductosController.Producto, Integer> cSto  = new TableColumn<>("Stock");
            TableColumn<ProductosController.Producto, Integer> cMin  = new TableColumn<>("Mínimo");
            cNom.setCellValueFactory(new PropertyValueFactory<>("nombre"));       cNom.setPrefWidth(110);
            cSto.setCellValueFactory(new PropertyValueFactory<>("stockActual"));  cSto.setPrefWidth(60);
            cMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));  cMin.setPrefWidth(65);
            tablaStock.getColumns().addAll(cNom, cSto, cMin);
            tablaStock.setPlaceholder(new Label("Sin productos con stock bajo ✓"));

            try {
                Connection con = Conexion.getConexion();
                ObservableList<ProductosController.Producto> lista = FXCollections.observableArrayList();
                ResultSet rs = con.createStatement().executeQuery(
                    "SELECT p.id_producto, p.codigo, p.nombre, p.descripcion, " +
                    "p.precio_compra, p.precio_venta, p.stock_actual, p.stock_minimo, " +
                    "IFNULL(c.nombre,'Sin categoría') AS categoria " +
                    "FROM vista_stock_bajo vsb " +
                    "JOIN productos p ON vsb.id_producto = p.id_producto " +
                    "LEFT JOIN categorias c ON p.id_categoria = c.id_categoria");
                while (rs.next())
                    lista.add(new ProductosController.Producto(
                        rs.getInt("id_producto"), rs.getString("codigo"),
                        rs.getString("nombre"),   rs.getString("descripcion"),
                        rs.getDouble("precio_compra"), rs.getDouble("precio_venta"),
                        rs.getInt("stock_actual"),     rs.getInt("stock_minimo"),
                        rs.getString("categoria")));
                tablaStock.setItems(lista);
                rs.close();
            } catch (Exception e) { e.printStackTrace(); }

            stockBox.getChildren().addAll(lblStockBajo, tablaStock);
            filaGrafica.getChildren().addAll(graficaBox, stockBox);
            vbox.getChildren().add(filaGrafica);

            // ── Tabla movimientos ───────────────────────────────────────────
            VBox tablaBox = new VBox(10);
            tablaBox.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10; -fx-padding: 20;");

            Label lblMovimientos = new Label("Últimos Movimientos");
            lblMovimientos.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

            TableView<MovimientoResumen> tabla = new TableView<>();
            tabla.setPrefHeight(200);
            tabla.setStyle("-fx-background-color: #0f3460; -fx-border-color: #1565c0;");

            TableColumn<MovimientoResumen, String>  cFecha  = new TableColumn<>("Fecha");
            TableColumn<MovimientoResumen, String>  cTipo   = new TableColumn<>("Tipo");
            TableColumn<MovimientoResumen, String>  cUsr    = new TableColumn<>("Usuario");
            TableColumn<MovimientoResumen, Integer> cTot    = new TableColumn<>("Total Productos");
            TableColumn<MovimientoResumen, String>  cObs    = new TableColumn<>("Observación");
            cFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));          cFecha.setPrefWidth(160);
            cTipo .setCellValueFactory(new PropertyValueFactory<>("tipo"));           cTipo .setPrefWidth(90);
            cUsr  .setCellValueFactory(new PropertyValueFactory<>("usuario"));        cUsr  .setPrefWidth(130);
            cTot  .setCellValueFactory(new PropertyValueFactory<>("totalProductos")); cTot  .setPrefWidth(130);
            cObs  .setCellValueFactory(new PropertyValueFactory<>("observacion"));    cObs  .setPrefWidth(200);
            tabla.getColumns().addAll(cFecha, cTipo, cUsr, cTot, cObs);
            tabla.setPlaceholder(new Label("No hay movimientos registrados aún"));
            cargarMovimientosTabla(tabla);

            tablaBox.getChildren().addAll(lblMovimientos, tabla);
            vbox.getChildren().add(tablaBox);

            scrollPane.setContent(vbox);
            contenidoPrincipal.getChildren().clear();
            contenidoPrincipal.getChildren().add(scrollPane);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox crearTarjeta(String titulo, String valor, String subtitulo,
                               String colorValor, String colorTitulo) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10; -fx-padding: 20;");
        Label t = new Label(titulo);
        t.setStyle("-fx-text-fill: " + colorTitulo + "; -fx-font-size: 12px;");
        Label v = new Label(valor);
        v.setStyle("-fx-text-fill: " + colorValor + "; -fx-font-size: 32px; -fx-font-weight: bold;");
        Label s = new Label(subtitulo);
        s.setStyle("-fx-text-fill: #546e7a; -fx-font-size: 11px;");
        card.getChildren().addAll(t, v, s);
        return card;
    }

    private int contarConsulta(String sql) {
        try {
            Connection con = Conexion.getConexion();
            ResultSet rs = con.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private void cargarMovimientosTabla(TableView<MovimientoResumen> tabla) {
        try {
            Connection con = Conexion.getConexion();
            ObservableList<MovimientoResumen> lista = FXCollections.observableArrayList();
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT fecha, tipo, usuario, total_productos, observacion " +
                "FROM vista_movimientos ORDER BY fecha DESC LIMIT 10");
            while (rs.next())
                lista.add(new MovimientoResumen(
                    rs.getString("fecha"), rs.getString("tipo"),
                    rs.getString("usuario"), rs.getInt("total_productos"),
                    rs.getString("observacion") != null ? rs.getString("observacion") : ""));
            tabla.setItems(lista);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void cerrarSesion() {
        try {
            Stage stageActual = (Stage) lblUsuario.getScene().getWindow();
            stageActual.close();
            Parent root = FXMLLoader.load(getClass()
                .getResource("/com/sgi/sgi/desktop/Login.fxml"));
            Stage nuevoStage = new Stage();
            nuevoStage.setTitle("SGI-Desktop | Iniciar Sesión");
            nuevoStage.setScene(new Scene(root, 420, 520));
            nuevoStage.setResizable(false);
            nuevoStage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static class MovimientoResumen {
        private String fecha, tipo, usuario, observacion;
        private int totalProductos;

        public MovimientoResumen(String fecha, String tipo, String usuario,
                                  int totalProductos, String observacion) {
            this.fecha = fecha; this.tipo = tipo; this.usuario = usuario;
            this.totalProductos = totalProductos; this.observacion = observacion;
        }

        public String getFecha()        { return fecha; }
        public String getTipo()         { return tipo; }
        public String getUsuario()      { return usuario; }
        public int    getTotalProductos(){ return totalProductos; }
        public String getObservacion()  { return observacion; }
    }
}