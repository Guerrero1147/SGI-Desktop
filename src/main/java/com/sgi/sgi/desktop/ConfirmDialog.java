package com.sgi.sgi.desktop;

import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

/**
 * Diálogos de confirmación con estilo consistente al tema claro de SGI-Desktop.
 */
public class ConfirmDialog {

    private static final String PANEL_BG    = "#ffffff";
    private static final String DARK_BG     = "#f5f5f4";
    private static final String BORDER      = "#e5e5e3";
    private static final String TEXT_PRI    = "#1c1c1c";
    private static final String TEXT_SEC    = "#6b7280";
    private static final String AMBER       = "#d97706";
    private static final String RED         = "#dc2626";
    private static final String GREEN       = "#16a34a";
    private static final String ORANGE      = "#ea580c";

    public enum Tipo { ELIMINAR, GUARDAR, CERRAR_SESION, CORTAR_CAJA }

    /**
     * Muestra un diálogo de confirmación estilizado.
     *
     * @param titulo   Título principal del diálogo.
     * @param mensaje  Texto descriptivo secundario.
     * @param tipo     Tipo de acción (determina icono y color del botón de confirmar).
     * @return {@code true} si el usuario confirmó, {@code false} si canceló.
     */
    public static boolean mostrar(String titulo, String mensaje, Tipo tipo) {

        // ── Configuración según tipo ────────────────────────────────────────
        String labelOK, colorOK, icono;
        switch (tipo) {
            case ELIMINAR:
                labelOK = "Sí, eliminar";   colorOK = "#7c3aed";  icono = "🗑"; break;
            case GUARDAR:
                labelOK = "Sí, guardar";    colorOK = GREEN;      icono = "💾"; break;
            case CERRAR_SESION:
                labelOK = "Cerrar sesión";  colorOK = ORANGE;     icono = "🚪"; break;
            case CORTAR_CAJA:
                labelOK = "Generar corte";  colorOK = AMBER;      icono = "📊"; break;
            default:
                labelOK = "Confirmar";      colorOK = AMBER;      icono = "❓";
        }

        ButtonType btnOK     = new ButtonType(labelOK,    ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, btnCancel);

        // ── Icono ───────────────────────────────────────────────────────────
        Label lblIcono = new Label(icono);
        lblIcono.setStyle("-fx-font-size: 38px;");

        // ── Título ──────────────────────────────────────────────────────────
        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle(
            "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRI + ";");
        lblTitulo.setTextAlignment(TextAlignment.CENTER);
        lblTitulo.setWrapText(true);
        lblTitulo.setMaxWidth(310);

        // ── Mensaje ─────────────────────────────────────────────────────────
        Label lblMsg = new Label(mensaje);
        lblMsg.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT_SEC + ";");
        lblMsg.setTextAlignment(TextAlignment.CENTER);
        lblMsg.setWrapText(true);
        lblMsg.setMaxWidth(310);

        // ── Layout ──────────────────────────────────────────────────────────
        VBox content = new VBox(12, lblIcono, lblTitulo, lblMsg);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30, 32, 16, 32));
        content.setMinWidth(370);
        content.setStyle(
            "-fx-background-color: " + PANEL_BG + "; -fx-background-radius: 12;");

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle(
            "-fx-background-color: " + PANEL_BG + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 20, 0, 0, 4);");

        // ── Estilo de botones ───────────────────────────────────────────────
        javafx.application.Platform.runLater(() -> {
    // ── Botón confirmar ─────────────────────────────────────────────
    javafx.scene.Node nodeOK = dialog.getDialogPane().lookupButton(btnOK);
    if (nodeOK != null) {
        String estiloOK =
            "-fx-background-color: " + colorOK + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-font-size: 12px;" +
            "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;" +
            "-fx-min-width: 130;";
        nodeOK.setStyle(estiloOK);
    }

    // ── Botón cancelar ───────────────────────────────────────────────
    javafx.scene.Node nodeCancel = dialog.getDialogPane().lookupButton(btnCancel);
    if (nodeCancel != null) {
        String estiloNormal =
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-text-fill: " + TEXT_SEC + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 6; -fx-font-size: 12px;" +
            "-fx-padding: 8 20; -fx-cursor: hand;" +
            "-fx-min-width: 100;";
        String estiloHover =
            "-fx-background-color: rgba(220,38,38,0.9);" +
            "-fx-text-fill: white;" +
            "-fx-border-color: transparent;" +
            "-fx-border-radius: 6; -fx-font-size: 12px;" +
            "-fx-padding: 8 20; -fx-cursor: hand;" +
            "-fx-min-width: 100;";
        nodeCancel.setStyle(estiloNormal);
        nodeCancel.setOnMouseEntered(e -> nodeCancel.setStyle(estiloHover));
        nodeCancel.setOnMouseExited(e  -> nodeCancel.setStyle(estiloNormal));
    }
});

        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == btnOK;
    }
}