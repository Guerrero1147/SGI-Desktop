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

public class UsuariosController implements Initializable {

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String>  colNombre;
    @FXML private TableColumn<Usuario, String>  colUsername;
    @FXML private TableColumn<Usuario, String>  colRol;
    @FXML private TableColumn<Usuario, String>  colFecha;
    @FXML private TableColumn<Usuario, Void>    colAcciones;

    @FXML private TextField txtBuscar;
    @FXML private Label     lblMensaje;
    @FXML private VBox      panelFormulario;
    @FXML private Label     lblTituloForm;
    @FXML private Label     lblErrorForm;

    @FXML private TextField       txtNombre;
    @FXML private TextField       txtUsername;
    @FXML private PasswordField   txtPassword;
    @FXML private PasswordField   txtConfirmarPassword;
    @FXML private ComboBox<String> cmbRol;

    private ObservableList<Usuario> listaUsuarios = FXCollections.observableArrayList();
    private int idUsuarioEditando = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbRol.setItems(FXCollections.observableArrayList("ADMIN", "EMPLEADO"));
        configurarColumnas();
        cargarUsuarios();
    }

    // ── Columnas ────────────────────────────────────────────────────────────

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));

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
    public void cargarUsuarios() {
        try {
            Connection con = Conexion.getConexion();
            listaUsuarios.clear();
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT id_usuario, nombre, username, rol, fecha_creacion " +
                "FROM usuarios WHERE activo = TRUE ORDER BY nombre");
            while (rs.next()) {
                listaUsuarios.add(new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre"),
                    rs.getString("username"),
                    rs.getString("rol"),
                    rs.getString("fecha_creacion")
                ));
            }
            tablaUsuarios.setItems(listaUsuarios);
            rs.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Búsqueda ────────────────────────────────────────────────────────────

    @FXML
    private void buscarUsuario() {
        String filtro = txtBuscar.getText().toLowerCase();
        if (filtro.isEmpty()) { tablaUsuarios.setItems(listaUsuarios); return; }
        ObservableList<Usuario> filtrados = FXCollections.observableArrayList();
        for (Usuario u : listaUsuarios)
            if (u.getNombre().toLowerCase().contains(filtro) ||
                u.getUsername().toLowerCase().contains(filtro) ||
                u.getRol().toLowerCase().contains(filtro))
                filtrados.add(u);
        tablaUsuarios.setItems(filtrados);
    }

    // ── Formulario ──────────────────────────────────────────────────────────

    @FXML
    private void abrirFormularioAgregar() {
        idUsuarioEditando = -1;
        limpiarFormulario();
        lblTituloForm.setText("Agregar Usuario");
        txtPassword.setPromptText("Contraseña");
        txtConfirmarPassword.setPromptText("Confirmar contraseña");
        mostrarFormulario(true);
    }

    private void abrirFormularioEditar(Usuario u) {
        idUsuarioEditando = u.getIdUsuario();
        lblTituloForm.setText("Editar Usuario");
        txtNombre.setText(u.getNombre());
        txtUsername.setText(u.getUsername());
        txtPassword.clear();
        txtConfirmarPassword.clear();
        txtPassword.setPromptText("Nueva contraseña (dejar vacío para no cambiar)");
        txtConfirmarPassword.setPromptText("Confirmar nueva contraseña");
        cmbRol.setValue(u.getRol());
        lblErrorForm.setText("");
        mostrarFormulario(true);
    }

    // ── Guardar ─────────────────────────────────────────────────────────────

    @FXML
    private void guardarUsuario() {
        if (!validarFormulario()) return;

        try {
            Connection con = Conexion.getConexion();

            if (idUsuarioEditando == -1) {
                // INSERT
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO usuarios (nombre, username, password, rol) VALUES (?,?,?,?)");
                ps.setString(1, txtNombre.getText().trim());
                ps.setString(2, txtUsername.getText().trim());
                ps.setString(3, txtPassword.getText().trim());
                ps.setString(4, cmbRol.getValue());
                ps.executeUpdate();
                ps.close();
                mostrarMensaje("✅ Usuario agregado correctamente.", true);
            } else {
                // UPDATE — si dejó la contraseña vacía no la cambia
                String nuevaPassword = txtPassword.getText().trim();
                if (nuevaPassword.isEmpty()) {
                    PreparedStatement ps = con.prepareStatement(
                        "UPDATE usuarios SET nombre=?, username=?, rol=? WHERE id_usuario=?");
                    ps.setString(1, txtNombre.getText().trim());
                    ps.setString(2, txtUsername.getText().trim());
                    ps.setString(3, cmbRol.getValue());
                    ps.setInt(4, idUsuarioEditando);
                    ps.executeUpdate();
                    ps.close();
                } else {
                    PreparedStatement ps = con.prepareStatement(
                        "UPDATE usuarios SET nombre=?, username=?, password=?, rol=? WHERE id_usuario=?");
                    ps.setString(1, txtNombre.getText().trim());
                    ps.setString(2, txtUsername.getText().trim());
                    ps.setString(3, nuevaPassword);
                    ps.setString(4, cmbRol.getValue());
                    ps.setInt(5, idUsuarioEditando);
                    ps.executeUpdate();
                    ps.close();
                }
                mostrarMensaje("✅ Usuario actualizado correctamente.", true);
            }

            cerrarFormulario();
            cargarUsuarios();

        } catch (Exception e) {
            lblErrorForm.setText("❌ Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Eliminar ────────────────────────────────────────────────────────────

    private void confirmarEliminar(Usuario u) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar usuario?");
        alert.setContentText("¿Estás seguro de eliminar a \"" + u.getNombre() + "\"?\n" +
                             "Esta acción no se puede deshacer.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
            eliminarUsuario(u);
    }

    private void eliminarUsuario(Usuario u) {
        try {
            Connection con = Conexion.getConexion();
            PreparedStatement ps = con.prepareStatement(
                "UPDATE usuarios SET activo = FALSE WHERE id_usuario = ?");
            ps.setInt(1, u.getIdUsuario());
            ps.executeUpdate();
            ps.close();
            mostrarMensaje("✅ Usuario eliminado correctamente.", true);
            cargarUsuarios();
        } catch (Exception e) {
            mostrarMensaje("❌ Error al eliminar: " + e.getMessage(), false);
        }
    }

    // ── Validación ──────────────────────────────────────────────────────────

    private boolean validarFormulario() {
        if (txtNombre.getText().trim().isEmpty()) {
            lblErrorForm.setText("❌ El nombre es obligatorio.");
            return false;
        }
        if (txtUsername.getText().trim().isEmpty()) {
            lblErrorForm.setText("❌ El username es obligatorio.");
            return false;
        }
        if (cmbRol.getValue() == null) {
            lblErrorForm.setText("❌ Selecciona un rol.");
            return false;
        }
        // Al agregar, la contraseña es obligatoria
        if (idUsuarioEditando == -1 && txtPassword.getText().trim().isEmpty()) {
            lblErrorForm.setText("❌ La contraseña es obligatoria.");
            return false;
        }
        // Si escribió contraseña, verificar que coincida
        if (!txtPassword.getText().trim().isEmpty()) {
            if (!txtPassword.getText().equals(txtConfirmarPassword.getText())) {
                lblErrorForm.setText("❌ Las contraseñas no coinciden.");
                return false;
            }
            if (txtPassword.getText().trim().length() < 4) {
                lblErrorForm.setText("❌ La contraseña debe tener al menos 4 caracteres.");
                return false;
            }
        }
        lblErrorForm.setText("");
        return true;
    }

    // ── Utilidades ──────────────────────────────────────────────────────────

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
        txtUsername.clear();
        txtPassword.clear();
        txtConfirmarPassword.clear();
        cmbRol.setValue(null);
        lblErrorForm.setText("");
        idUsuarioEditando = -1;
    }

    private void mostrarMensaje(String mensaje, boolean exito) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle(exito
            ? "-fx-text-fill: #66bb6a; -fx-font-size: 12px;"
            : "-fx-text-fill: #ef5350; -fx-font-size: 12px;");
    }

    // ── Modelo ──────────────────────────────────────────────────────────────

    public static class Usuario {
        private int    idUsuario;
        private String nombre, username, rol, fechaCreacion;

        public Usuario(int idUsuario, String nombre, String username,
                       String rol, String fechaCreacion) {
            this.idUsuario     = idUsuario;
            this.nombre        = nombre;
            this.username      = username;
            this.rol           = rol;
            this.fechaCreacion = fechaCreacion;
        }

        public int    getIdUsuario()     { return idUsuario; }
        public String getNombre()        { return nombre; }
        public String getUsername()      { return username; }
        public String getRol()           { return rol; }
        public String getFechaCreacion() { return fechaCreacion; }
    }
}