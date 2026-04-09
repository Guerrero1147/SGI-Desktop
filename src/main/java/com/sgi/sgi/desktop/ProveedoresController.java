package com.sgi.sgi.desktop;

import com.sgi.conexion.Conexion;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ProveedoresController implements Initializable {

    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, Integer> colId;
    @FXML private TableColumn<Proveedor, String>  colNombre;
    @FXML private TableColumn<Proveedor, String>  colTelefono;
    @FXML private TableColumn<Proveedor, String>  colEmail;
    @FXML private TableColumn<Proveedor, String>  colDireccion;
    @FXML private TableColumn<Proveedor, String>  colProductos;
    @FXML private TableColumn<Proveedor, Void>    colAcciones;

    @FXML private TextField txtBuscar;
    @FXML private Label     lblMensaje;
    @FXML private VBox      panelFormulario;
    @FXML private Label     lblTituloForm;
    @FXML private Label     lblErrorForm;

    @FXML private TextField          txtNombre;
    @FXML private TextField          txtTelefono;
    @FXML private TextField          txtEmail;
    @FXML private TextField          txtDireccion;
    @FXML private ComboBox<ProductoItem> cmbProducto;
    @FXML private VBox               vboxProductosSeleccionados;

    private final List<ProductoItem>      productosSeleccionados = new ArrayList<>();
    private ObservableList<Proveedor>     listaProveedores       = FXCollections.observableArrayList();
    private int idProveedorEditando = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarProveedores();
        cargarProductosComboBox();
    }

    // ── Columnas ────────────────────────────────────────────────────────────

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProveedor"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colProductos.setCellValueFactory(new PropertyValueFactory<>("productosSurtidos"));

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar   = new Button("✏️ Editar");
            private final Button btnEliminar = new Button("🗑️ Eliminar");
            private final HBox   hbox        = new HBox(5, btnEditar, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; " +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11px;");
                btnEliminar.setStyle("-fx-background-color: #b71c1c; -fx-text-fill: white; " +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11px;");
                btnEditar.setOnAction(e -> abrirFormularioEditar(
                        getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> confirmarEliminar(
                        getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    // ── Carga de datos ──────────────────────────────────────────────────────

    @FXML
    public void cargarProveedores() {
        try {
            Connection con = Conexion.getConexion();
            listaProveedores.clear();
            String sql =
                "SELECT p.id_proveedor, p.nombre, p.telefono, p.email, p.direccion, " +
                "GROUP_CONCAT(pr.nombre ORDER BY pr.nombre SEPARATOR ', ') AS productos_surtidos " +
                "FROM proveedores p " +
                "LEFT JOIN productos pr ON pr.id_proveedor = p.id_proveedor AND pr.activo = TRUE " +
                "WHERE p.activo = TRUE " +
                "GROUP BY p.id_proveedor, p.nombre, p.telefono, p.email, p.direccion " +
                "ORDER BY p.nombre";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                String prod = rs.getString("productos_surtidos");
                listaProveedores.add(new Proveedor(
                    rs.getInt("id_proveedor"),
                    rs.getString("nombre"),
                    rs.getString("telefono")  != null ? rs.getString("telefono")  : "",
                    rs.getString("email")     != null ? rs.getString("email")     : "",
                    rs.getString("direccion") != null ? rs.getString("direccion") : "",
                    prod != null ? prod : "Sin productos"
                ));
            }
            tablaProveedores.setItems(listaProveedores);
            rs.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarProductosComboBox() {
        try {
            Connection con = Conexion.getConexion();
            ObservableList<ProductoItem> productos = FXCollections.observableArrayList();
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT id_producto, nombre FROM productos WHERE activo = TRUE ORDER BY nombre");
            while (rs.next())
                productos.add(new ProductoItem(rs.getInt("id_producto"), rs.getString("nombre")));
            cmbProducto.setItems(productos);
            rs.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Búsqueda ────────────────────────────────────────────────────────────

    @FXML
    private void buscarProveedor() {
        String filtro = txtBuscar.getText().toLowerCase();
        if (filtro.isEmpty()) { tablaProveedores.setItems(listaProveedores); return; }
        ObservableList<Proveedor> filtrados = FXCollections.observableArrayList();
        for (Proveedor p : listaProveedores)
            if (p.getNombre().toLowerCase().contains(filtro)   ||
                p.getEmail().toLowerCase().contains(filtro)    ||
                p.getTelefono().toLowerCase().contains(filtro))
                filtrados.add(p);
        tablaProveedores.setItems(filtrados);
    }

    // ── Formulario ──────────────────────────────────────────────────────────

    @FXML
    private void abrirFormularioAgregar() {
        idProveedorEditando = -1;
        limpiarFormulario();
        lblTituloForm.setText("Agregar Proveedor");
        mostrarFormulario(true);
    }

    private void abrirFormularioEditar(Proveedor p) {
        idProveedorEditando = p.getIdProveedor();
        lblTituloForm.setText("Editar Proveedor");
        txtNombre.setText(p.getNombre());
        txtTelefono.setText(p.getTelefono());
        txtEmail.setText(p.getEmail());
        txtDireccion.setText(p.getDireccion());
        lblErrorForm.setText("");

        // Cargar productos ya asignados
        productosSeleccionados.clear();
        try {
            Connection con = Conexion.getConexion();
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT id_producto, nombre FROM productos " +
                "WHERE id_proveedor = " + idProveedorEditando + " AND activo = TRUE");
            while (rs.next())
                productosSeleccionados.add(
                    new ProductoItem(rs.getInt("id_producto"), rs.getString("nombre")));
            rs.close();
        } catch (Exception e) { e.printStackTrace(); }

        refrescarVboxProductos();
        mostrarFormulario(true);
    }

    // ── Productos seleccionados ─────────────────────────────────────────────

    @FXML
    private void agregarProductoLista() {
        ProductoItem seleccionado = cmbProducto.getValue();
        if (seleccionado == null) return;

        boolean yaExiste = productosSeleccionados.stream()
            .anyMatch(p -> p.getIdProducto() == seleccionado.getIdProducto());
        if (yaExiste) {
            lblErrorForm.setText("⚠️ Ese producto ya está en la lista.");
            return;
        }

        productosSeleccionados.add(seleccionado);
        cmbProducto.setValue(null);
        lblErrorForm.setText("");
        refrescarVboxProductos();
    }

    private void refrescarVboxProductos() {
        vboxProductosSeleccionados.getChildren().clear();
        if (productosSeleccionados.isEmpty()) return;

        Label titulo = new Label("📦 Surte estos productos:");
        titulo.setStyle("-fx-text-fill: #90caf9; -fx-font-size: 12px; -fx-font-weight: bold;");
        vboxProductosSeleccionados.getChildren().add(titulo);

        for (ProductoItem item : new ArrayList<>(productosSeleccionados)) {
            HBox fila = new HBox(8);
            fila.setAlignment(Pos.CENTER_LEFT);

            Label lblNombre = new Label("• " + item.getNombre());
            lblNombre.setStyle("-fx-text-fill: #cfd8dc; -fx-font-size: 12px;");
            HBox.setHgrow(lblNombre, Priority.ALWAYS);

            Button btnQuitar = new Button("✖");
            btnQuitar.setStyle("-fx-background-color: #b71c1c; -fx-text-fill: white; " +
                               "-fx-background-radius: 4; -fx-cursor: hand; " +
                               "-fx-font-size: 10px; -fx-padding: 2 6 2 6;");
            btnQuitar.setOnAction(e -> {
                productosSeleccionados.remove(item);
                refrescarVboxProductos();
            });

            fila.getChildren().addAll(lblNombre, btnQuitar);
            vboxProductosSeleccionados.getChildren().add(fila);
        }
    }

    // ── Guardar ─────────────────────────────────────────────────────────────

    @FXML
    private void guardarProveedor() {
        if (!validarFormulario()) return;
        try {
            Connection con = Conexion.getConexion();
            int idProveedor;

            if (idProveedorEditando == -1) {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO proveedores (nombre, telefono, email, direccion) VALUES (?,?,?,?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, txtNombre.getText().trim());
                ps.setString(2, txtTelefono.getText().trim());
                ps.setString(3, txtEmail.getText().trim());
                ps.setString(4, txtDireccion.getText().trim());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                idProveedor = keys.getInt(1);
                ps.close();
                mostrarMensaje("✅ Proveedor agregado correctamente.", true);
            } else {
                idProveedor = idProveedorEditando;
                PreparedStatement ps = con.prepareStatement(
                    "UPDATE proveedores SET nombre=?, telefono=?, email=?, direccion=? WHERE id_proveedor=?");
                ps.setString(1, txtNombre.getText().trim());
                ps.setString(2, txtTelefono.getText().trim());
                ps.setString(3, txtEmail.getText().trim());
                ps.setString(4, txtDireccion.getText().trim());
                ps.setInt(5, idProveedor);
                ps.executeUpdate();
                ps.close();
                mostrarMensaje("✅ Proveedor actualizado correctamente.", true);
            }

            // Desasignar productos anteriores
            PreparedStatement psReset = con.prepareStatement(
                "UPDATE productos SET id_proveedor = NULL WHERE id_proveedor = ?");
            psReset.setInt(1, idProveedor);
            psReset.executeUpdate();
            psReset.close();

            // Asignar productos seleccionados
            if (!productosSeleccionados.isEmpty()) {
                PreparedStatement psAsignar = con.prepareStatement(
                    "UPDATE productos SET id_proveedor = ? WHERE id_producto = ?");
                for (ProductoItem item : productosSeleccionados) {
                    psAsignar.setInt(1, idProveedor);
                    psAsignar.setInt(2, item.getIdProducto());
                    psAsignar.addBatch();
                }
                psAsignar.executeBatch();
                psAsignar.close();
            }

            cerrarFormulario();
            cargarProveedores();

        } catch (Exception e) {
            lblErrorForm.setText("❌ Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Eliminar ────────────────────────────────────────────────────────────

    private void confirmarEliminar(Proveedor p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar proveedor?");
        alert.setContentText("¿Estás seguro de eliminar \"" + p.getNombre() + "\"?\n" +
                             "Los productos vinculados quedarán sin proveedor asignado.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
            eliminarProveedor(p);
    }

    private void eliminarProveedor(Proveedor p) {
        try {
            Connection con = Conexion.getConexion();
            PreparedStatement psReset = con.prepareStatement(
                "UPDATE productos SET id_proveedor = NULL WHERE id_proveedor = ?");
            psReset.setInt(1, p.getIdProveedor());
            psReset.executeUpdate();
            psReset.close();

            PreparedStatement ps = con.prepareStatement(
                "UPDATE proveedores SET activo = FALSE WHERE id_proveedor = ?");
            ps.setInt(1, p.getIdProveedor());
            ps.executeUpdate();
            ps.close();

            mostrarMensaje("✅ Proveedor eliminado correctamente.", true);
            cargarProveedores();
        } catch (Exception e) {
            mostrarMensaje("❌ Error al eliminar: " + e.getMessage(), false);
        }
    }

    // ── Utilidades ──────────────────────────────────────────────────────────

    private boolean validarFormulario() {
        if (txtNombre.getText().trim().isEmpty()) {
            lblErrorForm.setText("❌ El nombre es obligatorio.");
            return false;
        }
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.contains("@")) {
            lblErrorForm.setText("❌ El email no tiene un formato válido.");
            return false;
        }
        lblErrorForm.setText("");
        return true;
    }

    @FXML
    private void cerrarFormulario() {
        mostrarFormulario(false);
        limpiarFormulario();
    }

    private void mostrarFormulario(boolean visible) {
        panelFormulario.setVisible(visible);
        panelFormulario.setManaged(visible);
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
        cmbProducto.setValue(null);
        productosSeleccionados.clear();
        refrescarVboxProductos();
        lblErrorForm.setText("");
        idProveedorEditando = -1;
    }

    private void mostrarMensaje(String mensaje, boolean exito) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle(exito
            ? "-fx-text-fill: #66bb6a; -fx-font-size: 12px;"
            : "-fx-text-fill: #ef5350; -fx-font-size: 12px;");
    }

    // ── Modelos ─────────────────────────────────────────────────────────────

    public static class Proveedor {
        private int    idProveedor;
        private String nombre, telefono, email, direccion, productosSurtidos;

        public Proveedor(int idProveedor, String nombre, String telefono,
                         String email, String direccion, String productosSurtidos) {
            this.idProveedor       = idProveedor;
            this.nombre            = nombre;
            this.telefono          = telefono;
            this.email             = email;
            this.direccion         = direccion;
            this.productosSurtidos = productosSurtidos;
        }

        public int    getIdProveedor()       { return idProveedor; }
        public String getNombre()            { return nombre; }
        public String getTelefono()          { return telefono; }
        public String getEmail()             { return email; }
        public String getDireccion()         { return direccion; }
        public String getProductosSurtidos() { return productosSurtidos; }
    }

    public static class ProductoItem {
        private int    idProducto;
        private String nombre;

        public ProductoItem(int idProducto, String nombre) {
            this.idProducto = idProducto;
            this.nombre     = nombre;
        }

        public int    getIdProducto() { return idProducto; }
        public String getNombre()     { return nombre; }

        @Override
        public String toString() { return nombre; } // el ComboBox muestra este valor
    }
}   