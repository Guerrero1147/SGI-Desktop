# SGI-Desktop 🖥️

Aplicación de escritorio para la gestión de inventario, desarrollada en Java con JavaFX y base de datos MySQL.

## 📋 Requisitos

- **Java JDK 21 o superior** — [Descargar aquí](https://www.oracle.com/java/technologies/downloads/)
- **Apache Maven 3.8+** — [Descargar aquí](https://maven.apache.org/download.cgi)
- **MySQL Server 8.0+** — [Descargar aquí](https://dev.mysql.com/downloads/mysql/)
- **Apache NetBeans IDE 20** (recomendado) — [Descargar aquí](https://netbeans.apache.org/front/main/download/)
- MySQL Workbench *(opcional, para visualizar la BD)*

> ⚠️ Si usas una versión de JDK distinta, cambia las líneas `<source>` y `<target>` en el `pom.xml` para que coincidan con la tuya.

---

## 🚀 Instalación y configuración

### 1. Clonar el repositorio

**Desde NetBeans:**
```
Team → Git → Clone
URL: https://github.com/Guerrero1147/SGI-Desktop.git
```

**Desde la terminal:**
```bash
git clone https://github.com/Guerrero1147/SGI-Desktop.git
cd SGI-Desktop
```

---

### 2. Configurar la base de datos

1. Abre **MySQL Workbench** (o cualquier cliente MySQL).
2. Importa el script de la base de datos:
   ```sql
   source database/database.sql
   ```
   O en MySQL Workbench: **Server → Data Import → Import from Self-Contained File** y selecciona `database/database.sql`.
3. Esto creará automáticamente la base de datos `sgi_desktop` con todas las tablas necesarias.

---

### 3. Configurar la conexión a la base de datos

1. Copia el archivo de ejemplo de configuración:
   ```bash
   cp config.properties.example src/main/resources/config.properties
   ```
2. Edita `src/main/resources/config.properties` con los datos de tu servidor MySQL:
   ```properties
   db.host=localhost
   db.port=3306
   db.name=sgi_desktop
   db.user=tu_usuario
   db.password=tu_contraseña
   ```

> 🔒 El archivo `config.properties` **no se subío al repositorio** (está en `.gitignore`) para proteger las credenciales.

---

### 4. Ejecutar el proyecto

**Desde NetBeans:**
1. Abre el proyecto en NetBeans.
2. Click derecho en el proyecto → **Run Maven** → **Goals**.
3. En el campo *Goals* escribe: `javafx:run` y presiona **Go**.

**Desde la terminal:**
```bash
mvn javafx:run
```

---

## 📁 Estructura del proyecto

```
SGI-Desktop/
├── src/
│   └── main/
│       ├── java/com/sgi/
│       │   ├── conexion/               # Clase de conexión a la BD
│       │   └── sgi/desktop/            # Controladores y lógica de la app
│       │       ├── ConfirmDialog.java  # Diálogos de confirmación estilizados
│       │       └── *Controller.java    # Controladores de cada módulo
│       └── resources/com/sgi/sgi/desktop/
│           ├── *.fxml                  # Vistas de cada módulo
│           └── estilo.css              # Design system (tema oscuro)
├── database/
│   └── database.sql                    # Script para crear la base de datos
├── config.properties.example           # Plantilla de configuración
└── pom.xml                             # Dependencias y build de Maven
```

---

## 🧩 Módulos de la aplicación

| Módulo | Descripción |
|---|---|
| **Login** | Autenticación de usuarios |
| **Dashboard** | Resumen general y corte de caja |
| **Productos** | Alta, edición y consulta de productos |
| **Proveedores** | Gestión de proveedores y sus productos |
| **Movimientos** | Registro de entradas y salidas de inventario |
| **Usuarios** | Administración de usuarios del sistema |

---

## 🎨 Diseño

La interfaz utiliza un **tema claro** con una paleta neutra (blanco, gris y negro) para una experiencia más limpia y amigable. Todos los estilos están centralizados en `estilo.css`, que actúa como design system de la aplicación e incluye estilos para tablas, inputs, botones, sidebar, combo boxes, tooltips y scroll panes.

En esta versión se corrigieron errores visuales y se mejoró el diseño general para hacer la aplicación más intuitiva y agradable para el usuario.

Los diálogos de confirmación (guardar, eliminar, cerrar sesión y corte de caja) están implementados en `ConfirmDialog.java`, con estilos y colores consistentes al tema de la aplicación.

---

## 🛠️ Tecnologías utilizadas

- **Java 21** — Lenguaje principal
- **JavaFX 21** — Interfaz gráfica
- **CSS** — Design system y estilos de la interfaz
- **MySQL 8.0** — Base de datos
- **Maven** — Gestión de dependencias y build
- **MySQL Connector/J 8.0.33** — Driver JDBC para MySQL
