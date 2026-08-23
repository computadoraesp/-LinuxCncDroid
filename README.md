# LinuxCncDroid 🎛️

**LinuxCncDroid** es una interfaz hombre-máquina (HMI) industrial moderna, táctil y de alta fidelidad diseñada para el control, supervisión, centrado óptico y calibración metrológica de máquinas CNC que operan bajo **LinuxCNC**.

Construida íntegramente en **Kotlin** con **Jetpack Compose** y arquitectura **Material 3**, LinuxCncDroid ofrece soporte nativo para las principales topologías de hardware industrial (Mesa FPGA, EtherCAT y Puerto Paralelo), integrando telemetría en tiempo real, volante electrónico con respuesta háptica (MPG), visualizador 3D de trayectorias G-code, palpado 3D, visión por cámara y asistente de calibración metrológica según norma **ISO 230-2**.

---

## 🚀 Características Principales

### 1. Panel de Control y Lectura Digital (DRO)
* **Visualización de Ejes Múltiples**: Lectura en tiempo real para ejes lineales $X, Y, Z$ y rotativos $A, B, C$.
* **Sistemas de Coordenadas de Trabajo (WCS)**: Cambio inmediato y puesta a cero para $G54, G55, G56, G57, G58, G59, G59.1, G59.2, G59.3$.
* **Control de Husillo y Avances**:
  * Ajuste de RPM nominales y lecturas de encoder en tiempo real.
  * Modulación de Feed Override ($0\% - 200\%$) y Spindle Override ($50\% - 150\%$).
  * Control de refrigerante por niebla (M7), chorro (M8) y soplado de aire.
* **Seguridad y Parada de Emergencia (E-STOP)**:
  * Botón físico/virtual prioritario de Parada de Emergencia con desenergización segura y rearme secuencial.

### 2. Volante Electrónico (MPG Jogging) con Feedback Háptico
* **Selector de Multiplicador**: Pasos discretos $\times1$ ($0.001\text{ mm}$), $\times10$ ($0.01\text{ mm}$) y $\times100$ ($0.1\text{ mm}$).
* **Rueda Táctil Virtual**: Diales giratorios con detención táctil háptica (vibración física) sincronizada a cada incremento angular.
* **Jogging Continuo y por Pasos**: Desplazamiento fino y rápido para aproximación de herramienta.

### 3. Visualizador 3D de Trayectorias G-Code (Toolpath)
* **Motor de Renderizado 3D Isométrico**: Proyección tridimensional interactiva con soporte de rotación orbital, paneo y zoom multitáctil.
* **Diferenciación de Movimientos**:
  * Movimientos rápidos ($G0$) en línea discontinua de advertencia.
  * Avances de corte lineal ($G1$) y arcos circulares ($G2/G3$) con distinción cromática.
* **Ejecución y Simulación en Vivo**:
  * Marcador de posición de herramienta en tiempo real sincronizado con el DRO.
  * Línea de progreso de ejecución, tiempo estimado y verificación de cotas límite ($X_{\min}/X_{\max}, Y_{\min}/Y_{\max}, Z_{\min}/Z_{\max}$).

### 4. Sistema de Visión y Centrado Óptico (CNC CAM)
* **Cámara Industrial con CameraX**:
  * Retícula Cruciforme de precisión con divisiones micrométricas ($0.01\text{ mm}$).
  * Retícula de Círculos Concéntricos para centrado de orificios y piezas cilíndricas.
  * Cuadrícula Metrológica para alineación y paralelismo de mordazas.
  * Buscador de esquina a $90^\circ$ (Edge Finder) para alineación de cantos de material.
* **Telemetría DRO Superpuesta (HUD)**: Lectura de coordenadas activas en pantalla mientras se ajusta la alineación.
* **Micro-Jogging Integrado**: Pasos finos ($\pm 0.05\text{ mm}$) y botones directos de "CERO X" y "CERO Y" desde la vista de cámara.
* **Captura de Instantáneas**: Registro fotográfico de inspección y control de calidad.

### 5. Asistente de Calibración Metrológica de Ejes (ISO 230-2)
* **Procedimiento Metrológico por Sectores**:
  * División configurable de la carrera del eje al 10% (11 puntos de calibración).
  * Presets para instrumentos patrón: Reloj comparador ($\pm 0.003\text{ mm}$), Regla de cristal ($\pm 0.001\text{ mm}$), Micrómetro ($\pm 0.002\text{ mm}$), Interferómetro láser ($\pm 0.0005\text{ mm}$).
* **Cálculo Estadístico de Incertidumbre**:
  * Cálculo de sesgo medio, error máximo e incertidumbre expandida ($k=2$, $95.45\%$ nivel de confianza).
* **Exportación HAL LinuxCNC (`comp.tbl`)**:
  * Generación automática de la tabla de compensación de paso para el módulo `linear_comp` del HAL de LinuxCNC con copiado directo al portapapeles.

### 6. Palpador 3D y Rutinas de Centrado (Probing)
* **Rutinas Automatizadas**:
  * Palpado de esquina exterior e interior ($X/Y$).
  * Centrado automático de orificios cilíndricos (método de 4 puntos).
  * Toma de cota $Z$ cero de pieza con bloque patrón.
* **Tabla de Herramientas y ATC**:
  * Gestión local con persistencia en **Room Database** de offsets de longitud ($G43\ H$), radio y tipo de fresa.

### 7. Diagnóstico de Bus de Campo y Red
* **Soporte de Topologías de Hardware**:
  * **Mesa Electronics FPGA** (5i25, 6i24, 7i76E, 7i92, 7i96) vía Hostmot2.
  * **EtherCAT Master** (CiA 402 Servo Drives, Beckhoff, Omron, Delta).
  * **Puerto Paralelo (LPT)** con generador de pasos por software.
* **Monitor de Telemetría**:
  * Frecuencia de servo-hilo (1000 Hz / 1 kHz), jitter en microsegundos, paquetes transmitidos y monitor de estados CiA 402.

---

## 🛠️ Arquitectura y Tecnologías

* **Lenguaje**: Kotlin 100%
* **Interfaz de Usuario**: Jetpack Compose con Material Design 3 (M3 Industrial Dark Theme)
* **Cámara & Visión**: Android CameraX (Core, Camera2, Lifecycle, View)
* **Persistencia Local**: Android Jetpack Room Database con SQLite para perfiles de máquina y tabla de herramientas
* **Concurrencia y Reactividad**: Kotlin Coroutines & StateFlow (`collectAsStateWithLifecycle`)
* **Gestión de Ciclo de Vida**: Architecture Components ViewModel & LiveData
* **Comunicación LinuxCNC**: Clientes TCP para protocolo NML / `linuxcncrsh` y emulador en tiempo real integrado

---

## 📂 Estructura del Proyecto

```
LinuxCncDroid/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── data/            # Entidades Room, DAOs y Base de Datos local
│   │   │   ├── model/           # Modelos de dominio (Ejes, Estados, GCode, Metrología)
│   │   │   ├── service/         # Motor de comunicación LinuxCNC y generador de eventos
│   │   │   ├── ui/
│   │   │   │   ├── components/  # Componentes modulares (DRO, MPG, Cámara, 3D, Calibración)
│   │   │   │   ├── screens/     # Pantalla principal y pestañas de navegación
│   │   │   │   └── theme/       # Paleta de colores industriales, Tipografía y Formas M3
│   │   │   ├── viewmodel/       # CncViewModel y gestión de estados
│   │   │   └── MainActivity.kt  # Punto de entrada de la aplicación
│   │   ├── res/                 # Recursos XML (strings, drawables, icono adaptativo)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/libs.versions.toml    # Catálogo de versiones centralizado
├── metadata.json
├── settings.gradle.kts
└── README.md
```

---

## 🔌 Conexión con LinuxCNC

Para conectar LinuxCncDroid a un controlador LinuxCNC en red local:

1. Asegúrese de que el servidor LinuxCNC tenga habilitado el servidor de control remoto (por ejemplo, `linuxcncrsh` o socket TCP HAL).
2. En LinuxCncDroid, vaya a la pestaña **CONFIG**.
3. Ingrese la dirección IP del controlador LinuxCNC (por ejemplo `192.168.1.120`) y el puerto configurado (por defecto `5007`).
4. Seleccione la arquitectura correspondiente (**Mesa FPGA**, **EtherCAT** o **LPT**) y presione **CONECTAR CONTROLADOR**.

---

## 📄 Licencia

Este proyecto está disponible para la comunidad industrial y fabricantes CNC bajo licencia de código abierto. Desarrollado con los más altos estándares de ergonomía industrial y seguridad operativa.
