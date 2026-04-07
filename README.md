# SGI-Desktop 🖥️

Aplicación de escritorio para gestionar inventario, desarrollada 
en Java con JavaFX y base de datos MySQL.

## ⚙️ Requisitos
- Java JDK 23
- Apache NetBeans IDE 20
- MySQL Server 8.0
- MySQL Workbench (opcional, para ver la BD)

## 🚀 Cómo configurar el proyecto

### 1. Clonar el repositorio
En NetBeans: Team → Git → Clone
URL: https://github.com/Guerrero1147/SGI-Desktop.git

### 2. Configurar la base de datos
- Abre MySQL Workbench
- Ve a Server → Data Import
- Selecciona el archivo: database/database.sql
- Ejecuta la importación

### 3. Configurar la conexión
- Ve a la clase Conexion.java
- Cambia el usuario y contraseña por los tuyos de MySQL

### 4. Ejecutar el proyecto
- Abre el proyecto en NetBeans
- Click derecho en proyecto y Run maven
- Click en Goals
- En Goals poner (javafx:run) y apretar go

## 📁 Estructura del proyecto
- src/ → Código fuente Java y archivos FXML
- database/ → Script SQL para crear la base de datos

## ⚠️ Notas
- El proyecto aún está en desarrollo
- Si hay cambios en la BD, se actualizará el archivo database.sql
