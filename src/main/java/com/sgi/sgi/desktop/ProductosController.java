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
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author erice
 */
public class ProductosController implements Initializable {
    
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Double> colPrecioCompra;
    @FXML private TableColumn<Producto, Double> colPrecioVenta;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Integer> colStockMin;
    @FXML private TableColumn<Producto, Void> colAcciones;

    @FXML private TextField txtBuscar;
    @FXML private Label lblMensaje;
    @FXML private VBox panelFormulario;
    @FXML private Label lblTituloForm;
    @FXML private Label lblErrorForm;

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtStock;
    @FXML private TextField txtStockMinimo;

    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private int idProductoEditando = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarCategorias();
        cargarProductos();
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecioCompra.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        // Columna de acciones con botones Editar y Eliminar
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️ Editar");
            private final Button btnEliminar = new Button("🗑️ Eliminar");
            private final HBox hbox = new HBox(5, btnEditar, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; " +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11px;");
                btnEliminar.setStyle("-fx-background-color: #b71c1c; -fx-text-fill: white; " +
                        "-fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11px;");

                btnEditar.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    abrirFormularioEditar(p);
                });

                btnEliminar.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    confirmarEliminar(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    private void cargarCategorias() {
        try {
            Connection con = Conexion.getConexion();
            ResultSet rs = con.createStatement()
                .executeQuery("SELECT nombre FROM categorias ORDER BY nombre");
            ObservableList<String> categorias = FXCollections.observableArrayList();
            while (rs.next()) categorias.add(rs.getString("nombre"));
            cmbCategoria.setItems(categorias);
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void cargarProductos() {
        try {
            Connection con = Conexion.getConexion();
            listaProductos.clear();
            String sql = "SELECT p.id_producto, p.codigo, p.nombre, p.descripcion, " +
                         "p.precio_compra, p.precio_venta, p.stock_actual, p.stock_minimo, " +
                         "IFNULL(c.nombre, 'Sin categoría') AS categoria " +
                         "FROM productos p LEFT JOIN categorias c ON p.id_categoria = c.id_categoria " +
                         "WHERE p.activo = TRUE ORDER BY p.nombre";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                listaProductos.add(new Producto(
                    rs.getInt("id_producto"),
                    rs.getString("codigo"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getDouble("precio_compra"),
                    rs.getDouble("precio_venta"),
                    rs.getInt("stock_actual"),
                    rs.getInt("stock_minimo"),
                    rs.getString("categoria")
                ));
            }
            tablaProductos.setItems(listaProductos);
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void buscarProducto() {
        String filtro = txtBuscar.getText().toLowerCase();
        if (filtro.isEmpty()) {
            tablaProductos.setItems(listaProductos);
            return;
        }
        ObservableList<Producto> filtrados = FXCollections.observableArrayList();
        for (Producto p : listaProductos) {
            if (p.getNombre().toLowerCase().contains(filtro) ||
                p.getCodigo().toLowerCase().contains(filtro)) {
                filtrados.add(p);
            }
        }
        tablaProductos.setItems(filtrados);
    }

    @FXML
    private void abrirFormularioAgregar() {
        idProductoEditando = -1;
        limpiarFormulario();
        lblTituloForm.setText("Agregar Producto");
        mostrarFormulario(true);
    }

    private void abrirFormularioEditar(Producto p) {
        idProductoEditando = p.getIdProducto();
        lblTituloForm.setText("Editar Producto");
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        cmbCategoria.setValue(p.getCategoria());
        txtPrecioCompra.setText(String.valueOf(p.getPrecioCompra()));
        txtPrecioVenta.setText(String.valueOf(p.getPrecioVenta()));
        txtStock.setText(String.valueOf(p.getStockActual()));
        txtStockMinimo.setText(String.valueOf(p.getStockMinimo()));
        lblErrorForm.setText("");
        mostrarFormulario(true);
    }

    @FXML
    private void guardarProducto() {
        if (!validarFormulario()) return;

        try {
            Connection con = Conexion.getConexion();
            String categoria = cmbCategoria.getValue();
            int idCategoria = -1;

            ResultSet rscat = con.prepareStatement(
                "SELECT id_categoria FROM categorias WHERE nombre = '" + categoria + "'")
                .executeQuery();
            if (rscat.next()) idCategoria = rscat.getInt("id_categoria");
            rscat.close();

            if (idProductoEditando == -1) {
                // INSERT
                String sql = "INSERT INTO productos (codigo, nombre, descripcion, precio_compra, " +
                             "precio_venta, stock_actual, stock_minimo, id_categoria) VALUES (?,?,?,?,?,?,?,?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, txtCodigo.getText().trim());
                ps.setString(2, txtNombre.getText().trim());
                ps.setString(3, txtDescripcion.getText().trim());
                ps.setDouble(4, Double.parseDouble(txtPrecioCompra.getText().trim()));
                ps.setDouble(5, Double.parseDouble(txtPrecioVenta.getText().trim()));
                ps.setInt(6, Integer.parseInt(txtStock.getText().trim()));
                ps.setInt(7, Integer.parseInt(txtStockMinimo.getText().trim()));
                ps.setInt(8, idCategoria);
                ps.executeUpdate();
                ps.close();
                mostrarMensaje("✅ Producto agregado correctamente.", true);
            } else {
                // UPDATE
                String sql = "UPDATE productos SET codigo=?, nombre=?, descripcion=?, " +
                             "precio_compra=?, precio_venta=?, stock_actual=?, stock_minimo=?, " +
                             "id_categoria=? WHERE id_producto=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, txtCodigo.getText().trim());
                ps.setString(2, txtNombre.getText().trim());
                ps.setString(3, txtDescripcion.getText().trim());
                ps.setDouble(4, Double.parseDouble(txtPrecioCompra.getText().trim()));
                ps.setDouble(5, Double.parseDouble(txtPrecioVenta.getText().trim()));
                ps.setInt(6, Integer.parseInt(txtStock.getText().trim()));
                ps.setInt(7, Integer.parseInt(txtStockMinimo.getText().trim()));
                ps.setInt(8, idCategoria);
                ps.setInt(9, idProductoEditando);
                ps.executeUpdate();
                ps.close();
                mostrarMensaje("✅ Producto actualizado correctamente.", true);
            }

            cerrarFormulario();
            cargarProductos();

        } catch (Exception e) {
            lblErrorForm.setText("❌ Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void confirmarEliminar(Producto p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar producto?");
        alert.setContentText("¿Estás seguro de eliminar \"" + p.getNombre() + "\"?\nEsta acción no se puede deshacer.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            eliminarProducto(p);
        }
    }

    private void eliminarProducto(Producto p) {
        try {
            Connection con = Conexion.getConexion();
            PreparedStatement ps = con.prepareStatement(
                "DELETE FROM productos WHERE id_producto = ?");
            ps.setInt(1, p.getIdProducto());
            ps.executeUpdate();
            ps.close();
            mostrarMensaje("✅ Producto eliminado correctamente.", true);
            cargarProductos();
        } catch (Exception e) {
            mostrarMensaje("❌ Error al eliminar: " + e.getMessage(), false);
        }
    }

    private boolean validarFormulario() {
        if (txtCodigo.getText().trim().isEmpty()) {
            lblErrorForm.setText("❌ El código es obligatorio.");
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            lblErrorForm.setText("❌ El nombre es obligatorio.");
            return false;
        }
        if (cmbCategoria.getValue() == null) {
            lblErrorForm.setText("❌ Selecciona una categoría.");
            return false;
        }
        try {
            Double.parseDouble(txtPrecioCompra.getText().trim());
            Double.parseDouble(txtPrecioVenta.getText().trim());
            Integer.parseInt(txtStock.getText().trim());
            Integer.parseInt(txtStockMinimo.getText().trim());
        } catch (NumberFormatException e) {
            lblErrorForm.setText("❌ Los precios y stock deben ser números válidos.");
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
        txtCodigo.clear();
        txtNombre.clear();
        txtDescripcion.clear();
        cmbCategoria.setValue(null);
        txtPrecioCompra.clear();
        txtPrecioVenta.clear();
        txtStock.clear();
        txtStockMinimo.clear();
        lblErrorForm.setText("");
        idProductoEditando = -1;
    }

    private void mostrarMensaje(String mensaje, boolean exito) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle(exito
            ? "-fx-text-fill: #66bb6a; -fx-font-size: 12px;"
            : "-fx-text-fill: #ef5350; -fx-font-size: 12px;");
    }

    // Clase modelo Producto
    public static class Producto {
        private int idProducto;
        private String codigo, nombre, descripcion, categoria;
        private double precioCompra, precioVenta;
        private int stockActual, stockMinimo;

        public Producto(int idProducto, String codigo, String nombre, String descripcion,
                        double precioCompra, double precioVenta, int stockActual,
                        int stockMinimo, String categoria) {
            this.idProducto = idProducto;
            this.codigo = codigo;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.precioCompra = precioCompra;
            this.precioVenta = precioVenta;
            this.stockActual = stockActual;
            this.stockMinimo = stockMinimo;
            this.categoria = categoria;
        }

        public int getIdProducto() { return idProducto; }
        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public String getDescripcion() { return descripcion; }
        public String getCategoria() { return categoria; }
        public double getPrecioCompra() { return precioCompra; }
        public double getPrecioVenta() { return precioVenta; }
        public int getStockActual() { return stockActual; }
        public int getStockMinimo() { return stockMinimo; }
    }
}
