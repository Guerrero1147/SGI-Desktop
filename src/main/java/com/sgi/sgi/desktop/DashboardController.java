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
    @FXML private Button btnUsuarios;
    @FXML private Label lblUsuario;
    @FXML private StackPane contenidoPrincipal;

    private String nombreUsuario = "";
    private String rolUsuario = "";

    private final String ESTILO_ACTIVO =
        "-fx-background-color: #000000; -fx-text-fill: #ffffff; " +
        "-fx-font-size: 13px; -fx-padding: 10 12 10 16; -fx-cursor: hand; " +
        "-fx-background-radius: 6; -fx-alignment: CENTER-LEFT; " +
        "-fx-border-color: transparent transparent transparent #ffffff; " +
        "-fx-border-width: 0 0 0 3;";
    private final String ESTILO_INACTIVO =
        "-fx-background-color: transparent; -fx-text-fill: #6b7280; " +
        "-fx-font-size: 13px; -fx-padding: 10 12 10 16; -fx-cursor: hand; " +
        "-fx-background-radius: 6; -fx-alignment: CENTER-LEFT; " +
        "-fx-border-color: transparent; -fx-border-width: 0;";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mostrarDashboard();
    }

    public void setUsuario(String nombre, String rol) {
    this.nombreUsuario = nombre;
    this.rolUsuario = rol;
    lblUsuario.setText("👤 " + nombre + " (" + rol + ")");

    // Ocultar módulos según rol
    if ("EMPLEADO".equals(rol)) {
        btnProveedores.setVisible(false);
        btnProveedores.setManaged(false);
        btnProductos.setVisible(false);
        btnProductos.setManaged(false);
        btnInicio.setVisible(false);
        btnInicio.setManaged(false);
        btnUsuarios.setVisible(false);
        btnUsuarios.setManaged(false);
        mostrarMovimientos(); // Entra directo a Movimientos
    } else {
        mostrarDashboard(); // ADMIN ve el inicio normal
    }
}

    private void marcarBotonActivo(Button activo) {
        btnInicio.setStyle(ESTILO_INACTIVO);
        btnProductos.setStyle(ESTILO_INACTIVO);
        btnMovimientos.setStyle(ESTILO_INACTIVO);
        btnProveedores.setStyle(ESTILO_INACTIVO);
        btnUsuarios.setStyle(ESTILO_INACTIVO);
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/com/sgi/sgi/desktop/Movimientos.fxml"));
            Parent vista = loader.load();
            MovimientosController ctrl = loader.getController();
            ctrl.setRol(rolUsuario);
            contenidoPrincipal.getChildren().clear();
            contenidoPrincipal.getChildren().add(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void mostrarProveedores() {
        marcarBotonActivo(btnProveedores);
        cargarVista("Proveedores.fxml");
    }

    private void mostrarProximamente(String modulo) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: #ffffff;");
        Label lbl = new Label("🚧 Módulo " + modulo + " próximamente");
        lbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 20px;");
        vbox.getChildren().add(lbl);
        contenidoPrincipal.getChildren().clear();
        contenidoPrincipal.getChildren().add(vbox);
    }

    @FXML
    public void mostrarUsuarios() {
        marcarBotonActivo(btnUsuarios);
        cargarVista("Usuarios.fxml");
    }
    
    private void cargarVistaInicio() {
        try {
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: #ffffff; -fx-background-color: #ffffff;");

            VBox vbox = new VBox(20);
            vbox.setStyle("-fx-padding: 25; -fx-background-color: #ffffff;");
            vbox.setFillWidth(true);

            // Título
            Label titulo = new Label("Inicio");
            titulo.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1c1c1c;");
            vbox.getChildren().add(titulo);

            // Tarjetas
            int totalProductos = contarConsulta("SELECT COUNT(*) FROM productos WHERE activo = TRUE");
            int stockBajo     = contarConsulta("SELECT COUNT(*) FROM vista_stock_bajo");
            int entradasHoy   = contarConsulta("SELECT COUNT(*) FROM movimientos WHERE tipo = 'ENTRADA' AND DATE(fecha) = CURDATE()");
            int salidasHoy    = contarConsulta("SELECT COUNT(*) FROM movimientos WHERE tipo = 'SALIDA' AND DATE(fecha) = CURDATE()");

            HBox tarjetas = new HBox(15);
            tarjetas.setFillHeight(true);
            tarjetas.getChildren().addAll(
                crearTarjeta("📦 Total Productos", String.valueOf(totalProductos), "Productos registrados", "#90caf9",   "#90caf9"),
                crearTarjeta("⚠ Stock Bajo",      String.valueOf(stockBajo),      "Productos por agotarse","#ff7043","#ffb74d"),
                crearTarjeta("📥 Entradas",    String.valueOf(entradasHoy),    "Movimientos de entrada","#66bb6a","#81c784"),
                crearTarjeta("📤 Salidas",     String.valueOf(salidasHoy),     "Movimientos de salida", "#ef5350","#ef9a9a")
            );
            for (javafx.scene.Node n : tarjetas.getChildren())
                HBox.setHgrow(n, Priority.ALWAYS);
            vbox.getChildren().add(tarjetas);

            // ── Fila: Gráfica + Stock Bajo ──────────────────────────────────
            HBox filaGrafica = new HBox(15);
            filaGrafica.setFillHeight(true);

            // Gráfica
            VBox graficaBox = new VBox(10);
            graficaBox.setStyle("-fx-background-color: #1c1c1c; -fx-background-radius: 10; -fx-padding: 20;");
            HBox.setHgrow(graficaBox, Priority.ALWAYS);

            Label lblGrafica = new Label("Gráfica de Inventario");
            lblGrafica.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold;");

            CategoryAxis ejeX = new CategoryAxis();
            ejeX.setLabel("Productos");
            ejeX.setTickLabelFill(javafx.scene.paint.Color.web("#ffffff"));

            NumberAxis ejeY = new NumberAxis();
            ejeY.setLabel("Cantidad");
            ejeY.setTickLabelFill(javafx.scene.paint.Color.web("#ffffff"));

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
            stockBox.setStyle("-fx-background-color: #1c1c1c; -fx-background-radius: 10; -fx-padding: 20;");
            stockBox.setPrefWidth(280);
            stockBox.setMinWidth(280);

            Label lblStockBajo = new Label("⚠ Productos con Stock Bajo");
            lblStockBajo.setStyle("-fx-text-fill: #ffb74d; -fx-font-size: 13px; -fx-font-weight: bold;");

            TableView<ProductosController.Producto> tablaStock = new TableView<>();
            tablaStock.setStyle("-fx-background-color: #1c1c1c; -fx-border-color: #333333;");
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

            // ── Fila inferior: Movimientos (60%) + Cortes de Caja (40%) ────
            HBox filaInferior = new HBox(15);
            filaInferior.setFillHeight(true);

            // ── Últimos Movimientos ─────────────────────────────────────────
            VBox tablaBox = new VBox(10);
            tablaBox.setStyle("-fx-background-color: #1c1c1c; -fx-background-radius: 10; -fx-padding: 20;");
            HBox.setHgrow(tablaBox, Priority.ALWAYS);

            Label lblMovimientos = new Label("Últimos Movimientos");
            lblMovimientos.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold;");

            TableView<MovimientoResumen> tabla = new TableView<>();
            tabla.setPrefHeight(220);
            tabla.setStyle("-fx-background-color: #1c1c1c; -fx-border-color: #333333;");

            TableColumn<MovimientoResumen, String>  cFecha  = new TableColumn<>("Fecha");
            TableColumn<MovimientoResumen, String>  cTipo   = new TableColumn<>("Tipo");
            TableColumn<MovimientoResumen, String>  cUsr    = new TableColumn<>("Usuario");
            TableColumn<MovimientoResumen, Integer> cTot    = new TableColumn<>("Total Prods.");
            TableColumn<MovimientoResumen, String>  cObs    = new TableColumn<>("Observación");
            cFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));          cFecha.setPrefWidth(145);
            cTipo .setCellValueFactory(new PropertyValueFactory<>("tipo"));           cTipo .setPrefWidth(80);
            cUsr  .setCellValueFactory(new PropertyValueFactory<>("usuario"));        cUsr  .setPrefWidth(110);
            cTot  .setCellValueFactory(new PropertyValueFactory<>("totalProductos")); cTot  .setPrefWidth(100);
            cObs  .setCellValueFactory(new PropertyValueFactory<>("observacion"));    cObs  .setPrefWidth(160);

            // Color ENTRADA/SALIDA
            cTipo.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle(""); }
                    else {
                        setText(item);
                        setStyle("ENTRADA".equals(item)
                            ? "-fx-text-fill: #66bb6a; -fx-font-weight: bold;"
                            : "-fx-text-fill: #ef5350; -fx-font-weight: bold;");
                    }
                }
            });

            tabla.getColumns().addAll(cFecha, cTipo, cUsr, cTot, cObs);
            tabla.setPlaceholder(new Label("No hay movimientos registrados aún"));
            cargarMovimientosTabla(tabla);

            tablaBox.getChildren().addAll(lblMovimientos, tabla);

            // ── Cortes de Caja ──────────────────────────────────────────────
            VBox cortesBox = new VBox(10);
            cortesBox.setStyle("-fx-background-color: #1c1c1c; -fx-background-radius: 10; -fx-padding: 20;");
            cortesBox.setPrefWidth(370);
            cortesBox.setMinWidth(340);

            Label lblCortes = new Label("📊 Cortes de Caja");
            lblCortes.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold;");

            TableView<CorteDeCaja> tablaCortes = new TableView<>();
            tablaCortes.setPrefHeight(220);
            tablaCortes.setStyle("-fx-background-color: #1c1c1c; -fx-border-color: #333333;");
            VBox.setVgrow(tablaCortes, Priority.ALWAYS);

            TableColumn<CorteDeCaja, String> ccFecha   = new TableColumn<>("Fecha");
            TableColumn<CorteDeCaja, String> ccBalance = new TableColumn<>("Balance Neto");
            TableColumn<CorteDeCaja, Void>   ccTicket  = new TableColumn<>("Ticket");

            ccFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
            ccFecha.setPrefWidth(100);

            ccBalance.setCellValueFactory(new PropertyValueFactory<>("balanceNetoStr"));
            ccBalance.setPrefWidth(120);
            ccBalance.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle(""); }
                    else {
                        setText(item);
                        boolean positivo = !item.startsWith("-");
                        setStyle(positivo
                            ? "-fx-text-fill: #66bb6a; -fx-font-weight: bold;"
                            : "-fx-text-fill: #ef5350; -fx-font-weight: bold;");
                    }
                }
            });

            ccTicket.setPrefWidth(80);
            ccTicket.setCellFactory(col -> new TableCell<>() {
                private final Button btn = new Button("🎫 Ver");
                { btn.setStyle("-fx-background-color: #1c1c1c; -fx-text-fill: white;" +
                               "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11px;");
                  btn.setOnAction(e -> {
                      CorteDeCaja c = getTableView().getItems().get(getIndex());
                      mostrarTicketCorte(c);
                  });
                }
                @Override protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });

            tablaCortes.getColumns().addAll(ccFecha, ccBalance, ccTicket);
            tablaCortes.setPlaceholder(new Label("Sin cortes de caja registrados"));
            cargarCortesTabla(tablaCortes);

            cortesBox.getChildren().addAll(lblCortes, tablaCortes);

            filaInferior.getChildren().addAll(tablaBox, cortesBox);
            vbox.getChildren().add(filaInferior);

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
        card.setStyle("-fx-background-color: #1c1c1c; -fx-background-radius: 10; -fx-padding: 20;");
        Label t = new Label(titulo);
        t.setStyle("-fx-text-fill: " + colorTitulo + "; -fx-font-size: 12px;");
        Label v = new Label(valor);
        v.setStyle("-fx-text-fill: " + colorValor + "; -fx-font-size: 32px; -fx-font-weight: bold;");
        Label s = new Label(subtitulo);
        s.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
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
        boolean confirmado = ConfirmDialog.mostrar(
            "¿Cerrar sesión?",
            "Se cerrará la sesión actual y regresarás\na la pantalla de inicio de sesión.",
            ConfirmDialog.Tipo.CERRAR_SESION
        );
        if (!confirmado) return;

        try {
            Stage stageActual = (Stage) lblUsuario.getScene().getWindow();
            stageActual.close();
            Parent root = FXMLLoader.load(getClass()
                .getResource("/com/sgi/sgi/desktop/Login.fxml"));
            Stage nuevoStage = new Stage();
            nuevoStage.setTitle("SGI-Desktop | Iniciar Sesión");
            nuevoStage.setScene(new Scene(root, 720, 500));
            nuevoStage.setResizable(false);
            nuevoStage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarCortesTabla(TableView<CorteDeCaja> tabla) {
        try {
            Connection con = Conexion.getConexion();
            ObservableList<CorteDeCaja> lista = FXCollections.observableArrayList();
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT DATE(fecha) AS dia, " +
                "  SUM(CASE WHEN tipo='ENTRADA' THEN total_valor ELSE 0 END) AS entradas, " +
                "  SUM(CASE WHEN tipo='SALIDA'  THEN total_valor ELSE 0 END) AS salidas " +
                "FROM vista_movimientos " +
                "GROUP BY DATE(fecha) " +
                "ORDER BY dia DESC " +
                "LIMIT 10");
            while (rs.next()) {
                double ent = rs.getDouble("entradas");
                double sal = rs.getDouble("salidas");
                lista.add(new CorteDeCaja(rs.getString("dia"), ent, sal));
            }
            tabla.setItems(lista);
            rs.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void mostrarTicketCorte(CorteDeCaja c) {
        try {
            Connection con = Conexion.getConexion();
            // Top productos del día
            java.sql.PreparedStatement psTop = con.prepareStatement(
                "SELECT p.nombre, m.tipo, SUM(dm.cantidad) AS total_cant, " +
                "       SUM(dm.cantidad * dm.precio_unitario) AS subtotal " +
                "FROM movimientos m " +
                "JOIN detalle_movimientos dm ON m.id_movimiento = dm.id_movimiento " +
                "JOIN productos p ON dm.id_producto = p.id_producto " +
                "WHERE DATE(m.fecha) = ? " +
                "GROUP BY p.nombre, m.tipo " +
                "ORDER BY total_cant DESC LIMIT 8");
            psTop.setString(1, c.getFecha());
            ResultSet rsTop = psTop.executeQuery();
            StringBuilder sbTop = new StringBuilder();
            while (rsTop.next())
                sbTop.append(String.format("  %-22s %-8s %4d pzs  $%,.2f%n",
                    rsTop.getString("nombre"), rsTop.getString("tipo"),
                    rsTop.getInt("total_cant"), rsTop.getDouble("subtotal")));
            rsTop.close(); psTop.close();

            String linea = "─".repeat(46);
            double balance = c.getBalanceNeto();
            String ticket = String.format(
                "%s%n  📊 CORTE DE CAJA — %s%n%s%n%n" +
                "  📥 Compras (Entradas)   $%,.2f%n" +
                "  📤 Ventas  (Salidas)    $%,.2f%n" +
                "%s%n" +
                "  💰 Balance neto         $%,.2f%n" +
                "%s%n%n" +
                "  DETALLE POR PRODUCTO%n%s",
                linea, c.getFecha(), linea,
                c.getTotalEntradas(), c.getTotalSalidas(),
                linea, balance, linea,
                sbTop.length() > 0 ? sbTop : "  Sin detalle disponible.\n");

            javafx.scene.control.TextArea txt = new javafx.scene.control.TextArea(ticket);
            txt.setEditable(false);
            txt.setPrefSize(460, 340);
            txt.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px;" +
                         "-fx-background-color: #ffffff; -fx-text-fill: #1c1c1c;" +
                         "-fx-control-inner-background: #ffffff;");

            Dialog<Void> d = new Dialog<>();
            d.setTitle("Ticket — " + c.getFecha());
            d.setHeaderText(null);
            d.getDialogPane().setContent(txt);
            d.getDialogPane().setStyle("-fx-background-color: #ffffff;");
            d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            d.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static class CorteDeCaja {
        private final String fecha;
        private final double totalEntradas, totalSalidas;

        public CorteDeCaja(String fecha, double totalEntradas, double totalSalidas) {
            this.fecha = fecha;
            this.totalEntradas = totalEntradas;
            this.totalSalidas  = totalSalidas;
        }

        public String getFecha()          { return fecha; }
        public double getTotalEntradas()  { return totalEntradas; }
        public double getTotalSalidas()   { return totalSalidas; }
        public double getBalanceNeto()    { return totalSalidas - totalEntradas; }
        public String getBalanceNetoStr() {
            double b = getBalanceNeto();
            return String.format("%s$%,.2f", b < 0 ? "-" : "+", Math.abs(b));
        }
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