/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgi.sgi.desktop;

import com.sgi.conexion.Conexion;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ButtonBar;
import javafx.geometry.Insets;
/**
 *
 * @author erice
 */
public class MovimientosController implements Initializable {

// Tabla principal
    @FXML private TableView<Movimiento> tablaMovimientos;
    @FXML private TableColumn<Movimiento, String>  colFecha;
    @FXML private TableColumn<Movimiento, String>  colTipo;
    @FXML private TableColumn<Movimiento, String>  colUsuario;
    @FXML private TableColumn<Movimiento, Integer> colProductos;
    @FXML private TableColumn<Movimiento, Double>  colValor;
    @FXML private TableColumn<Movimiento, String>  colObservacion;
    @FXML private TableColumn<Movimiento, Void>    colAcciones;
 
    // Filtros
    @FXML private ComboBox<String> cmbFiltroTipo;
    @FXML private TextField txtBuscar;
    @FXML private Label lblMensaje;
 
    // Formulario
    @FXML private VBox panelFormulario;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtObservacion;
    @FXML private ComboBox<String> cmbProducto;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtPrecioUnitario;
    @FXML private Label lblErrorForm;
 
    // Tabla detalle del formulario
    @FXML private TableView<DetalleMovimiento> tablaDetalle;
    @FXML private TableColumn<DetalleMovimiento, String>  colDetProd;
    @FXML private TableColumn<DetalleMovimiento, Integer> colDetCant;
    @FXML private TableColumn<DetalleMovimiento, Double>  colDetPrecio;
    @FXML private TableColumn<DetalleMovimiento, Void>    colDetQuitar;
 
    private ObservableList<Movimiento> listaMovimientos = FXCollections.observableArrayList();
    private ObservableList<DetalleMovimiento> listaDetalle = FXCollections.observableArrayList();
 
    // Mapa nombre → id de productos
    private java.util.Map<String, Integer> mapaProductos    = new java.util.HashMap<>();
    // Mapa nombre → precios del producto
    private java.util.Map<String, Double>  mapaPrecioCompra = new java.util.HashMap<>();
    private java.util.Map<String, Double>  mapaPrecioVenta  = new java.util.HashMap<>();
    private java.util.Map<String, Integer> mapaStock        = new java.util.HashMap<>();
 
    // Rol del usuario en sesión — lo inyecta DashboardController
    private String rolUsuario = "";

    /** DashboardController llama este método justo después de cargar el FXML */
    public void setRol(String rol) {
        this.rolUsuario = rol;
        tablaMovimientos.refresh(); // re-renderiza las celdas para mostrar/ocultar Eliminar
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTablas();
        configurarFormulario();
        cargarMovimientos();
    }
 
    private void configurarTablas() {
        // Tabla principal
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colProductos.setCellValueFactory(new PropertyValueFactory<>("totalProductos"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("totalValor"));
        colObservacion.setCellValueFactory(new PropertyValueFactory<>("observacion"));
 
        // Colorear tipo ENTRADA/SALIDA
        colTipo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(item.equals("ENTRADA")
                        ? "-fx-text-fill: #66bb6a; -fx-font-weight: bold;"
                        : "-fx-text-fill: #ef5350; -fx-font-weight: bold;");
                }
            }
        });
 
        // Botones Ver + Eliminar (Eliminar solo visible para ADMIN)
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer      = new Button("👁 Ver");
            private final Button btnEliminar = new Button("🗑 Eliminar");
            private final HBox   hbox        = new HBox(6, btnVer, btnEliminar);
            {
                btnVer.setStyle("-fx-background-color: rgba(16,185,129,0.12); -fx-text-fill: #10b981; " +
                        "-fx-font-size: 11px; -fx-background-radius: 4; -fx-cursor: hand; " +
                        "-fx-padding: 4 10 4 10; -fx-border-color: rgba(16,185,129,0.25); -fx-border-radius: 4;");
                btnEliminar.setStyle("-fx-background-color: rgba(239,68,68,0.1); -fx-text-fill: #ef4444; " +
                        "-fx-font-size: 11px; -fx-background-radius: 4; -fx-cursor: hand; " +
                        "-fx-padding: 4 10 4 10; -fx-border-color: rgba(239,68,68,0.25); -fx-border-radius: 4;");
                btnVer.setOnAction(e -> {
                    Movimiento m = getTableView().getItems().get(getIndex());
                    verDetalle(m);
                });
                btnEliminar.setOnAction(e -> {
                    Movimiento m = getTableView().getItems().get(getIndex());
                    confirmarEliminarMovimiento(m);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    boolean esAdmin = "ADMIN".equalsIgnoreCase(rolUsuario);
                    btnEliminar.setVisible(esAdmin);
                    btnEliminar.setManaged(esAdmin);
                    setGraphic(hbox);
                }
            }
        });
 
        // Tabla detalle formulario
        colDetProd.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colDetCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colDetPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
 
        colDetQuitar.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✖");
            {
                btn.setStyle("-fx-background-color: rgba(239,68,68,0.1); -fx-text-fill: #ef4444; " +
                        "-fx-font-size: 11px; -fx-background-radius: 4; -fx-cursor: hand; " +
                        "-fx-padding: 4 10 4 10; -fx-border-color: rgba(239,68,68,0.25); -fx-border-radius: 4;");
                btn.setOnAction(e -> {
                    DetalleMovimiento d = getTableView().getItems().get(getIndex());
                    listaDetalle.remove(d);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
 
        tablaDetalle.setItems(listaDetalle);
    }
 
    private void configurarFormulario() {
        // Tipos de movimiento
        cmbTipo.setItems(FXCollections.observableArrayList("ENTRADA", "SALIDA"));
        cmbFiltroTipo.setItems(FXCollections.observableArrayList("Todos", "ENTRADA", "SALIDA"));
        cmbFiltroTipo.setValue("Todos");
 
        // Cargar productos en el combo
        try {
            Connection con = Conexion.getConexion();
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT id_producto, nombre, precio_compra, precio_venta, stock_actual " +
                "FROM productos WHERE activo = TRUE ORDER BY nombre");
            while (rs.next()) {
                String nombre = rs.getString("nombre");
                mapaProductos.put(nombre,    rs.getInt("id_producto"));
                mapaPrecioCompra.put(nombre, rs.getDouble("precio_compra"));
                mapaPrecioVenta.put(nombre,  rs.getDouble("precio_venta"));
                mapaStock.put(nombre,        rs.getInt("stock_actual"));
            }
            cmbProducto.setItems(FXCollections.observableArrayList(mapaProductos.keySet()));
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Autollenar precio al seleccionar producto o cambiar tipo de movimiento
        cmbProducto.valueProperty().addListener((obs, anterior, nuevo) -> autorellenarPrecio());
        cmbTipo.valueProperty().addListener((obs, anterior, nuevo) -> autorellenarPrecio());
    }

    private void autorellenarPrecio() {
        String producto = cmbProducto.getValue();
        String tipo     = cmbTipo.getValue();
        if (producto == null || tipo == null) return;

        double precio = tipo.equals("ENTRADA")
            ? mapaPrecioCompra.getOrDefault(producto, 0.0)
            : mapaPrecioVenta.getOrDefault(producto, 0.0);

        txtPrecioUnitario.setText(String.format("%.2f", precio));
    }
 
    @FXML
    public void cargarMovimientos() {
        try {
            Connection con = Conexion.getConexion();
            listaMovimientos.clear();
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT id_movimiento, fecha, tipo, usuario, " +
                "total_productos, total_valor, observacion " +
                "FROM vista_movimientos ORDER BY fecha DESC");
            while (rs.next()) {
                listaMovimientos.add(new Movimiento(
                    rs.getInt("id_movimiento"),
                    rs.getString("fecha"),
                    rs.getString("tipo"),
                    rs.getString("usuario"),
                    rs.getInt("total_productos"),
                    rs.getDouble("total_valor"),
                    rs.getString("observacion") != null ? rs.getString("observacion") : ""
                ));
            }
            tablaMovimientos.setItems(listaMovimientos);
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    @FXML
    private void filtrarMovimientos() {
        String tipo = cmbFiltroTipo.getValue();
        String buscar = txtBuscar.getText().toLowerCase();
 
        ObservableList<Movimiento> filtrados = FXCollections.observableArrayList();
        for (Movimiento m : listaMovimientos) {
            boolean coincideTipo = tipo == null || tipo.equals("Todos") || m.getTipo().equals(tipo);
            boolean coincideBuscar = buscar.isEmpty() ||
                m.getObservacion().toLowerCase().contains(buscar) ||
                m.getUsuario().toLowerCase().contains(buscar);
            if (coincideTipo && coincideBuscar) filtrados.add(m);
        }
        tablaMovimientos.setItems(filtrados);
    }
 
    @FXML
    private void abrirFormulario() {
        listaDetalle.clear();
        cmbTipo.setValue(null);
        txtObservacion.clear();
        cmbProducto.setValue(null);
        txtCantidad.clear();
        txtPrecioUnitario.setText("0.00");
        lblErrorForm.setText("");
        // Recargar stock actualizado desde la BD antes de abrir
        recargarStock();
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    private void recargarStock() {
        try {
            Connection con = Conexion.getConexion();
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT nombre, stock_actual FROM productos WHERE activo = TRUE");
            while (rs.next()) {
                mapaStock.put(rs.getString("nombre"), rs.getInt("stock_actual"));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    @FXML
    private void agregarProductoLista() {
        if (cmbProducto.getValue() == null) {
            lblErrorForm.setText("❌ Selecciona un producto.");
            return;
        }
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            double precio = Double.parseDouble(txtPrecioUnitario.getText().trim());
            if (cantidad <= 0 || precio <= 0) {
                lblErrorForm.setText("❌ Cantidad y precio deben ser mayores a 0.");
                return;
            }
            String nombreProd = cmbProducto.getValue();
            int idProd = mapaProductos.get(nombreProd);

            // Validar stock disponible en salidas
            if ("SALIDA".equals(cmbTipo.getValue())) {
                int stockDisponible = mapaStock.getOrDefault(nombreProd, 0);
                if (cantidad > stockDisponible) {
                    lblErrorForm.setText("❌ Stock insuficiente. Disponible: " + stockDisponible);
                    return;
                }
            }

            // Verificar si ya está en la lista
            for (DetalleMovimiento d : listaDetalle) {
                if (d.getIdProducto() == idProd) {
                    lblErrorForm.setText("❌ Ese producto ya está en la lista.");
                    return;
                }
            }
 
            listaDetalle.add(new DetalleMovimiento(idProd, nombreProd, cantidad, precio));
            cmbProducto.setValue(null);
            txtCantidad.clear();
            txtPrecioUnitario.setText("0.00");
            lblErrorForm.setText("");
        } catch (NumberFormatException e) {
            lblErrorForm.setText("❌ Cantidad y precio deben ser números válidos.");
        }
    }
 
    @FXML
    private void guardarMovimiento() {
        if (cmbTipo.getValue() == null) {
            lblErrorForm.setText("❌ Selecciona el tipo de movimiento.");
            return;
        }
        if (listaDetalle.isEmpty()) {
            lblErrorForm.setText("❌ Agrega al menos un producto.");
            return;
        }

        String tipo = cmbTipo.getValue();
        int numProductos = listaDetalle.size();
        boolean confirmado = ConfirmDialog.mostrar(
            "¿Registrar movimiento?",
            "Se registrará una " + tipo + " con " + numProductos +
                " producto(s).\nEsta acción actualizará el stock.",
            ConfirmDialog.Tipo.GUARDAR
        );
        if (!confirmado) return;

        try {
            Connection con = Conexion.getConexion();
 
            // Obtener id del usuario admin (por ahora fijo, después usará sesión)
            int idUsuario = 1;
            ResultSet rsU = con.createStatement().executeQuery(
                "SELECT id_usuario FROM usuarios WHERE username = 'admin' LIMIT 1");
            if (rsU.next()) idUsuario = rsU.getInt("id_usuario");
            rsU.close();
 
            // Insertar movimiento
            String obs = txtObservacion.getText().trim();
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO movimientos (tipo, id_usuario, observacion) VALUES (?, ?, ?)",
                java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, cmbTipo.getValue());
            ps.setInt(2, idUsuario);
            ps.setString(3, obs.isEmpty() ? null : obs);
            ps.executeUpdate();
 
            ResultSet rsKey = ps.getGeneratedKeys();
            int idMovimiento = 0;
            if (rsKey.next()) idMovimiento = rsKey.getInt(1);
            rsKey.close();
            ps.close();
 
            // Insertar detalle y actualizar stock
            for (DetalleMovimiento d : listaDetalle) {
                PreparedStatement psDet = con.prepareStatement(
                    "INSERT INTO detalle_movimientos (id_movimiento, id_producto, cantidad, precio_unitario) " +
                    "VALUES (?, ?, ?, ?)");
                psDet.setInt(1, idMovimiento);
                psDet.setInt(2, d.getIdProducto());
                psDet.setInt(3, d.getCantidad());
                psDet.setDouble(4, d.getPrecioUnitario());
                psDet.executeUpdate();
                psDet.close();
 
                // Actualizar stock
                String sqlStock = cmbTipo.getValue().equals("ENTRADA")
                    ? "UPDATE productos SET stock_actual = stock_actual + ? WHERE id_producto = ?"
                    : "UPDATE productos SET stock_actual = stock_actual - ? WHERE id_producto = ?";
                PreparedStatement psStock = con.prepareStatement(sqlStock);
                psStock.setInt(1, d.getCantidad());
                psStock.setInt(2, d.getIdProducto());
                psStock.executeUpdate();
                psStock.close();
            }
 
            mostrarMensaje("✅ Movimiento registrado correctamente.", true);
            cerrarFormulario();
            cargarMovimientos();
 
        } catch (Exception e) {
            lblErrorForm.setText("❌ Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }
 
    private void verDetalle(Movimiento m) {
        try {
            Connection con = Conexion.getConexion();
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT p.nombre, dm.cantidad, dm.precio_unitario " +
                "FROM detalle_movimientos dm " +
                "JOIN productos p ON dm.id_producto = p.id_producto " +
                "WHERE dm.id_movimiento = " + m.getIdMovimiento());
 
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("• ").append(rs.getString("nombre"))
                  .append(" | Cant: ").append(rs.getInt("cantidad"))
                  .append(" | Precio: $").append(rs.getDouble("precio_unitario"))
                  .append("\n");
            }
            rs.close();
 
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Detalle del Movimiento");
            alert.setHeaderText("Movimiento #" + m.getIdMovimiento() +
                " | " + m.getTipo() + " | " + m.getFecha());
            alert.setContentText(sb.length() > 0 ? sb.toString() : "Sin detalle disponible.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    // ── Eliminar movimiento (solo admin) ────────────────────────────────────

    private void confirmarEliminarMovimiento(Movimiento m) {
        boolean confirmado = ConfirmDialog.mostrar(
            "¿Eliminar movimiento #" + m.getIdMovimiento() + "?",
            "Tipo: " + m.getTipo() + "  |  Fecha: " + m.getFecha() +
            "\n⚠ Se revertirá el stock de todos los productos involucrados.",
            ConfirmDialog.Tipo.ELIMINAR
        );
        if (confirmado) eliminarMovimiento(m);
    }

    private void eliminarMovimiento(Movimiento m) {
        try {
            Connection con = Conexion.getConexion();

            // 1. Leer el detalle ANTES de borrar para poder revertir el stock
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT id_producto, cantidad FROM detalle_movimientos " +
                "WHERE id_movimiento = " + m.getIdMovimiento());

            // Si era ENTRADA restamos stock; si era SALIDA lo devolvemos
            String sqlStock = "ENTRADA".equals(m.getTipo())
                ? "UPDATE productos SET stock_actual = stock_actual - ? WHERE id_producto = ?"
                : "UPDATE productos SET stock_actual = stock_actual + ? WHERE id_producto = ?";

            while (rs.next()) {
                PreparedStatement psStock = con.prepareStatement(sqlStock);
                psStock.setInt(1, rs.getInt("cantidad"));
                psStock.setInt(2, rs.getInt("id_producto"));
                psStock.executeUpdate();
                psStock.close();
            }
            rs.close();

            // 2. Borrar el movimiento; la FK ON DELETE CASCADE elimina
            //    automáticamente sus filas en detalle_movimientos
            PreparedStatement ps = con.prepareStatement(
                "DELETE FROM movimientos WHERE id_movimiento = ?");
            ps.setInt(1, m.getIdMovimiento());
            ps.executeUpdate();
            ps.close();

            mostrarMensaje("✅ Movimiento eliminado y stock revertido.", true);
            cargarMovimientos();

        } catch (Exception e) {
            mostrarMensaje("❌ Error al eliminar: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    // ── Corte de Caja ────────────────────────────────────────────────────────

    @FXML
    private void mostrarCorteDeCaja() {
        // --- Diálogo para elegir la fecha ---
        Dialog<java.time.LocalDate> dialogFecha = new Dialog<>();
        dialogFecha.setTitle("Corte de Caja");
        dialogFecha.setHeaderText("Selecciona la fecha del corte");

        ButtonType btnGenerar = new ButtonType("Generar Corte", ButtonBar.ButtonData.OK_DONE);
        dialogFecha.getDialogPane().getButtonTypes().addAll(btnGenerar, ButtonType.CANCEL);

        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker(java.time.LocalDate.now());
        datePicker.setStyle("-fx-font-size: 13px;");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10,
            new Label("Fecha:"), datePicker);
        content.setPadding(new javafx.geometry.Insets(10));
        dialogFecha.getDialogPane().setContent(content);

        dialogFecha.setResultConverter(btn ->
            btn == btnGenerar ? datePicker.getValue() : null);

        java.util.Optional<java.time.LocalDate> resultFecha = dialogFecha.showAndWait();
        if (resultFecha.isEmpty() || resultFecha.get() == null) return;

        java.time.LocalDate fecha = resultFecha.get();

        boolean confirmado = ConfirmDialog.mostrar(
            "¿Generar corte de caja?",
            "Se generará el corte de caja\npara la fecha: " + fecha + ".",
            ConfirmDialog.Tipo.CORTAR_CAJA
        );
        if (!confirmado) return;

        generarCorteDeCaja(fecha);
    }

    private void generarCorteDeCaja(java.time.LocalDate fecha) {
        try {
            Connection con = Conexion.getConexion();
            String fechaStr = fecha.toString(); // yyyy-MM-dd

            // Totales de ENTRADA del día
            PreparedStatement psEnt = con.prepareStatement(
                "SELECT COUNT(*) AS num_mov, " +
                "       COALESCE(SUM(dm.cantidad), 0)              AS total_piezas, " +
                "       COALESCE(SUM(dm.cantidad * dm.precio_unitario), 0) AS total_valor " +
                "FROM movimientos m " +
                "JOIN detalle_movimientos dm ON m.id_movimiento = dm.id_movimiento " +
                "WHERE m.tipo = 'ENTRADA' AND DATE(m.fecha) = ?");
            psEnt.setString(1, fechaStr);
            ResultSet rsEnt = psEnt.executeQuery();
            int    entMovimientos = 0;
            int    entPiezas      = 0;
            double entValor       = 0;
            if (rsEnt.next()) {
                entMovimientos = rsEnt.getInt("num_mov");
                entPiezas      = rsEnt.getInt("total_piezas");
                entValor       = rsEnt.getDouble("total_valor");
            }
            rsEnt.close(); psEnt.close();

            // Totales de SALIDA del día
            PreparedStatement psSal = con.prepareStatement(
                "SELECT COUNT(*) AS num_mov, " +
                "       COALESCE(SUM(dm.cantidad), 0)              AS total_piezas, " +
                "       COALESCE(SUM(dm.cantidad * dm.precio_unitario), 0) AS total_valor " +
                "FROM movimientos m " +
                "JOIN detalle_movimientos dm ON m.id_movimiento = dm.id_movimiento " +
                "WHERE m.tipo = 'SALIDA' AND DATE(m.fecha) = ?");
            psSal.setString(1, fechaStr);
            ResultSet rsSal = psSal.executeQuery();
            int    salMovimientos = 0;
            int    salPiezas      = 0;
            double salValor       = 0;
            if (rsSal.next()) {
                salMovimientos = rsSal.getInt("num_mov");
                salPiezas      = rsSal.getInt("total_piezas");
                salValor       = rsSal.getDouble("total_valor");
            }
            rsSal.close(); psSal.close();

            // Top 5 productos más movidos del día
            PreparedStatement psTop = con.prepareStatement(
                "SELECT p.nombre, m.tipo, SUM(dm.cantidad) AS total_cant " +
                "FROM movimientos m " +
                "JOIN detalle_movimientos dm ON m.id_movimiento = dm.id_movimiento " +
                "JOIN productos p ON dm.id_producto = p.id_producto " +
                "WHERE DATE(m.fecha) = ? " +
                "GROUP BY p.nombre, m.tipo " +
                "ORDER BY total_cant DESC " +
                "LIMIT 5");
            psTop.setString(1, fechaStr);
            ResultSet rsTop = psTop.executeQuery();
            StringBuilder sbTop = new StringBuilder();
            while (rsTop.next()) {
                sbTop.append(String.format("  • %-25s %-8s %d pzs%n",
                    rsTop.getString("nombre"),
                    rsTop.getString("tipo"),
                    rsTop.getInt("total_cant")));
            }
            rsTop.close(); psTop.close();

            // Construir el resumen
            double balance = salValor - entValor;
            String linea = "─".repeat(44);
            String resumen = String.format(
                "%s%n" +
                "  📅 CORTE DE CAJA — %s%n" +
                "%s%n%n" +

                "  📥 ENTRADAS (Compras)%n" +
                "     Movimientos : %d%n" +
                "     Piezas      : %d%n" +
                "     Total       : $%,.2f%n%n" +

                "  📤 SALIDAS (Ventas)%n" +
                "     Movimientos : %d%n" +
                "     Piezas      : %d%n" +
                "     Total       : $%,.2f%n%n" +

                "%s%n" +
                "  💰 BALANCE NETO (Ventas − Compras)%n" +
                "     $%,.2f%n" +
                "%s%n%n" +

                "  🏆 PRODUCTOS MÁS MOVIDOS DEL DÍA%n" +
                "%s",
                linea, fecha, linea,
                entMovimientos, entPiezas, entValor,
                salMovimientos, salPiezas, salValor,
                linea,
                balance,
                linea,
                sbTop.length() > 0 ? sbTop.toString() : "  Sin movimientos en esta fecha.\n"
            );

            // Mostrar en un diálogo estilizado con área de texto
            Dialog<Void> dialogCorte = new Dialog<>();
            dialogCorte.setTitle("Corte de Caja — " + fecha);
            dialogCorte.setHeaderText(null);
            dialogCorte.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            javafx.scene.control.TextArea txtResumen = new javafx.scene.control.TextArea(resumen);
            txtResumen.setEditable(false);
            txtResumen.setStyle(
                "-fx-font-family: 'Courier New', monospace; -fx-font-size: 13px;" +
                "-fx-background-color: #141a0e; -fx-text-fill: #d4e8c2;" +
                "-fx-control-inner-background: #1a1a2e;");
            txtResumen.setPrefSize(480, 370);
            txtResumen.setWrapText(false);

            dialogCorte.getDialogPane().setContent(txtResumen);
            dialogCorte.getDialogPane().setStyle("-fx-background-color: #141a0e;");
            dialogCorte.showAndWait();

        } catch (Exception e) {
            mostrarMensaje("❌ Error al generar el corte: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    @FXML
    private void cerrarFormulario() {
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
        listaDetalle.clear();
    }
 
    private void mostrarMensaje(String mensaje, boolean exito) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle(exito
            ? "-fx-text-fill: #66bb6a; -fx-font-size: 12px;"
            : "-fx-text-fill: #ef5350; -fx-font-size: 12px;");
    }
 
    // ── Modelos ─────────────────────────────────────────────────────────────
 
    public static class Movimiento {
        private int idMovimiento, totalProductos;
        private String fecha, tipo, usuario, observacion;
        private double totalValor;
 
        public Movimiento(int idMovimiento, String fecha, String tipo, String usuario,
                          int totalProductos, double totalValor, String observacion) {
            this.idMovimiento = idMovimiento; this.fecha = fecha; this.tipo = tipo;
            this.usuario = usuario; this.totalProductos = totalProductos;
            this.totalValor = totalValor; this.observacion = observacion;
        }
 
        public int    getIdMovimiento()  { return idMovimiento; }
        public String getFecha()         { return fecha; }
        public String getTipo()          { return tipo; }
        public String getUsuario()       { return usuario; }
        public int    getTotalProductos(){ return totalProductos; }
        public double getTotalValor()    { return totalValor; }
        public String getObservacion()   { return observacion; }
    }
 
    public static class DetalleMovimiento {
        private int idProducto, cantidad;
        private String nombreProducto;
        private double precioUnitario;
 
        public DetalleMovimiento(int idProducto, String nombreProducto,
                                  int cantidad, double precioUnitario) {
            this.idProducto = idProducto; this.nombreProducto = nombreProducto;
            this.cantidad = cantidad; this.precioUnitario = precioUnitario;
        }
 
        public int    getIdProducto()    { return idProducto; }
        public String getNombreProducto(){ return nombreProducto; }
        public int    getCantidad()      { return cantidad; }
        public double getPrecioUnitario(){ return precioUnitario; }
    }
}