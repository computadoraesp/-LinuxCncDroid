# PRIVACY POLICY / POLÍTICA DE PRIVACIDAD

**Last Updated / Última actualización:** August 26, 2026 / 26 de agosto de 2026

---

## 🇺🇸 ENGLISH VERSION

### 1. Introduction
This Privacy Policy applies to the **LinuxCncDroid** mobile application (referred to as "the Application", "we", "our", or "us"). LinuxCncDroid is an industrial Human-Machine Interface (HMI) designed for real-time monitoring, telemetric acquisition, toolpath preview, and control of CNC machine tools powered by LinuxCNC.

We are committed to respecting and protecting user privacy. This Application is built on an **offline-first and local-network industrial privacy model**.

---

### 2. Information We Handle and Its Purpose

The Application only processes data strictly necessary for industrial machine tool monitoring and operation:

1. **CNC Operational and Telemetry Data:**
   * Axis position coordinates (Digital Readout - DRO), limit switch triggers, servo drive torque telemetry, spindle rotational speed (RPM), commanded and actual feedrates, coolant states, and Emergency Stop (E-STOP) hardware/software statuses.
   * **Purpose:** Real-time machine supervision, trajectory simulation, safe machine commanding, and synchronization with the local LinuxCNC controller.

2. **Machining Programs (G-Code Files):**
   * Files selected by the operator (`.ngc`, `.gcode`, `.nc`, `.tap`) for simulation, 3D toolpath rendering, line-by-line execution, and MDI command history.
   * **Purpose:** Rendering interactive 3D toolpaths, tool engagement calculation, bounding box analysis, and sending motion blocks to the controller.

3. **Industrial Camera and Optical Feeds:**
   * Live camera preview used for visual alignment, edge finding ($90^\circ$ optical crosshair), concentric circle hole centering, and metrology grid measurement.
   * **Purpose:** On-screen optical centering and tool zeroing assistance. **No video or camera feed is uploaded, streamed, or stored on external cloud servers.**

---

### 3. Data Storage and Local Persistence

* **Local Storage Only:** All configuration settings (target LinuxCNC IP address, TCP socket ports), machine profiles, Room database tool offset tables ($G43\ H$), ISO 230-2 calibration tables (`comp.tbl`), and event alarm logs are persisted **exclusively on the user's local device storage**.
* **Zero Tracking or Advertising:** LinuxCncDroid contains **no advertising SDKs, no behavioral tracking frameworks, and no third-party telemetry collectors**.

---

### 4. Device Permissions and Usage Justification

| Permission | Technical Justification |
| :--- | :--- |
| `INTERNET` / `ACCESS_NETWORK_STATE` | Required to communicate over local area networks (Wi-Fi, Ethernet, Industrial VLAN) via TCP/IP sockets to the host LinuxCNC machine server. |
| `CAMERA` | Required for real-time optical centering reticles, HUD alignment aids, and optional manual inspection snapshots. |
| `READ_EXTERNAL_STORAGE` / Storage Access | Required to import G-Code files, CAM programs, and save exported compensation tables (`comp.tbl`). |
| `VIBRATE` | Provides high-precision haptic feedback for E-STOP actuation, jog ticks, and Virtual MPG electronic handwheel increments. |

---

### 5. Third-Party Sharing and Network Security

* **No Data Transfer to Third Parties:** We do not sell, rent, monetize, or transmit any machine data, personal details, or operational logs to external third parties.
* **Direct Host Connection:** Network packets are exchanged strictly between the Android device and the user-specified LinuxCNC server on the local industrial subnet or VPN.

---

### 6. User Rights and Data Deletion

Users maintain 100% control over all data stored by the Application:
* You may clear MDI command history, event logs, and calibration data directly within the application menus.
* Uninstalling the Application or clearing its storage in Android Settings permanently removes all stored profiles, databases, and cached data.

---

### 7. Contact Us

If you have questions, concerns, or feedback regarding this Privacy Policy, contact:
* **Email:** `computadoraesp@gmail.com`

---
---

## 🇪🇸 VERSIÓN EN ESPAÑOL

### 1. Introducción
Esta Política de Privacidad describe cómo **LinuxCncDroid** (en adelante, "la Aplicación", "nosotros" o "nuestro") gestiona la información y la privacidad del usuario durante el uso de la interfaz de control y monitoreo para máquinas CNC operadas con LinuxCNC.

Diseñamos esta aplicación bajo el principio de **privacidad absoluta y operación local/industrial**.

---

### 2. Información Procesada y su Finalidad

La Aplicación gestiona únicamente los datos técnicos necesarios para la supervisión y operación de la máquina herramienta:

1. **Datos de Operación y Telemetría CNC:**
   * Coordenadas de posición de ejes (DRO), estado de finales de carrera, par motor de servoaccionamientos, velocidad de husillo (RPM), avances reales y de comando (*feedrate*), estados de refrigerante y pulsadores de parada de emergencia (E-STOP).
   * **Finalidad:** Supervisión en tiempo real, cálculo de trayectorias, control seguro de movimientos y sincronización con el controlador LinuxCNC.

2. **Programas de Mecanizado (Archivos G-Code):**
   * Archivos de corte cargados por el operador (`.ngc`, `.gcode`, `.nc`, `.tap`) para simulación, inspección 3D y ejecución bloque a bloque.
   * **Finalidad:** Renderizado 3D de la trayectoria, verificación de cotas límites de pieza y transmisión de bloques al controlador.

3. **Cámara Industrial y Visión Óptica:**
   * Flujo de video en vivo de la cámara del dispositivo para alineación óptica mediante retículas milimétricas, círculos concéntricos y buscador de esquinas a $90^\circ$.
   * **Finalidad:** Asistencia visual durante la toma de ceros y centrado de material. **No se transmite ni almacena video en servidores externos o en la nube.**

---

### 3. Almacenamiento y Persistencia Local

* **Almacenamiento Local Aislado:** Las configuraciones de conexión (IP y puerto TCP del servidor LinuxCNC), perfiles de máquinas, tabla de herramientas en base de datos Room SQLite, tablas de calibración ISO 230-2 (`comp.tbl`) e historial de alarmas se guardan **exclusivamente en la memoria local del dispositivo Android**.
* **Sin Publicidad ni Rastreo:** La Aplicación **no incluye SDKs de publicidad, analíticas invasivas ni seguimiento de comportamiento de usuario**.

---

### 4. Permisos del Dispositivo y Justificación

| Permiso | Justificación Técnica |
| :--- | :--- |
| `INTERNET` / `ACCESS_NETWORK_STATE` | Comunicación mediante sockets TCP/IP a través de la red local (Wi-Fi, Ethernet o VLAN industrial) con el host LinuxCNC. |
| `CAMERA` | Visualización en tiempo real para las retículas de centrado óptico y palpado visual. |
| `READ_EXTERNAL_STORAGE` / Acceso a Almacenamiento | Carga de programas G-Code y guardado de archivos de compensación de paso (`comp.tbl`). |
| `VIBRATE` | Respuesta háptica para confirmación táctil en pulsadores de parada de emergencia, jog y volante electrónico MPG. |

---

### 5. Transferencia a Terceros y Seguridad de Red

* **Cero Transferencia a Terceros:** No vendemos, transferimos ni compartimos ningún dato técnico, archivo ni información del usuario con terceros.
* **Comunicación Directa:** El tráfico de red viaja exclusivamente de forma punto a punto entre la tableta/móvil Android y el host LinuxCNC configurado por el usuario en su red local.

---

### 6. Control de Datos y Eliminación

El usuario conserva el control absoluto de sus datos:
* Puede borrar el historial de comandos MDI, registros de alarmas y tablas de calibración en cualquier momento desde la interfaz.
* Desinstalar la aplicación o limpiar los datos desde los Ajustes de Android elimina permanentemente todas las configuraciones y bases de datos locales.

---

### 7. Contacto

Si tiene preguntas, comentarios o inquietudes sobre esta Política de Privacidad, puede contactarnos en:
* **Correo electrónico:** `computadoraesp@gmail.com`
