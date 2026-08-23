# 📱 Google Play Store Listing — LinuxCncDroid

Este documento contiene todos los textos oficiales, metadatos, textos promocionales y especificaciones de capturas de pantalla listos para copiar y pegar en **Google Play Console**.

---

## 📌 1. Ficha Principal de la Tienda (Español - Principal)

### 🏷️ Título de la Aplicación (Máx. 30 caracteres)
```text
LinuxCncDroid
```

---

### 📝 Descripción Corta (Máx. 80 caracteres)
```text
HMI y control industrial para LinuxCNC con EtherCAT, visor 3D, cámara y palpador.
```

---

### 📄 Descripción Completa (Máx. 4000 caracteres)
```text
LinuxCncDroid es la interfaz hombre-máquina (HMI) industrial definitiva para el control, monitorización, centrado óptico y calibración de centros de mecanizado, fresadoras, tornos y routers gobernados por LinuxCNC.

Diseñada con un estándar ergonómico de alto contraste para entornos de taller y pantallas táctiles industriales, LinuxCncDroid proporciona conectividad directa y en tiempo real con controladores LinuxCNC mediante arquitecturas EtherCAT (CiA 402), tarjetas FPGA Mesa Electronics (Hostmot2) y generadores de pulsos por puerto paralelo.

═══════════════════════════════════════
CARACTERÍSTICAS Y MÓDULOS PRINCIPALES
═══════════════════════════════════════

🎛️ PANEL DE CONTROL & DRO MULTIEJE EN TIEMPO REAL
• Lectura digital precisa (DRO) con resolución micrométrica para ejes X, Y, Z, A, B y C.
• Gestión integral de Sistemas de Coordenadas de Trabajo (WCS): G54, G55, G56, G57, G58, G59, G59.1, G59.2, G59.3 con puesta a cero independiente.
• Modulación en caliente de Spindle Override (50%-150%) y Feed Override (0%-200%).
• Control de refrigerante por niebla (M7), chorro continuo (M8) y soplado de virutas.
• Botón de Parada de Emergencia (E-STOP) con protocolo de rearme y corte seguro.

🎯 VOLANTE ELECTRÓNICO (MPG) CON RESPUESTA HÁPTICA
• Selector de incrementos micrométricos continuos y por pasos: ×1 (0.001 mm), ×10 (0.01 mm) y ×100 (0.1 mm).
• Dial táctil rotativo virtual con detención vibratoria háptica sincronizada para un tacto analógico de precisión.

👁️ SISTEMA DE VISIÓN Y CENTRADO ÓPTICO (CNC CAM)
• Alineación óptica de cero de pieza mediante la cámara del dispositivo móvil o tablet.
• Retículas industriales intercambiables:
  - Cruciforme de precisión micrométrica con escalas graduadas.
  - Círculos concéntricos para centrado rápido de orificios y piezas cilíndricas.
  - Cuadrícula metrológica para verificación de escuadrado y paralelismo de mordazas.
  - Buscador de esquinas a 90° (Edge Finder) para cantos de material.
• Telemetría DRO superpuesta en pantalla (HUD) y botones de micro-paso (±0.05 mm) con puesta a cero directa.

📐 ASISTENTE DE CALIBRACIÓN METROLÓGICA (ISO 230-2)
• Procedimiento estandarizado de calibración por sectores equidistantes de carrera.
• Integración de incertidumbre de instrumentos patrón (relojes comparadores, reglas de cristal, micrómetros e interferómetros láser).
• Cálculo estadístico automático de sesgo, repetibilidad e incertidumbre expandida (k=2, 95.45% de confianza).
• Exportación directa de tablas de compensación de paso de husillo (`comp.tbl`) para el módulo `linear_comp` del HAL de LinuxCNC.

🌐 VISUALIZADOR 3D INTERACTIVO DE G-CODE
• Motor de renderizado tridimensional isométrico con rotación orbital 360°, paneo y zoom multitáctil.
• Distinción cromática de trayectorias: movimientos rápidos (G0), avances de corte lineal (G1) y arcos circulares (G2/G3).
• Seguimiento de herramienta en tiempo real sincronizado con el avance físico de la máquina.

📍 PALPADOR 3D (PROBING) & TABLA DE HERRAMIENTAS (ATC)
• Ciclos automáticos de palpado: centrado de agujeros en 4 puntos, búsqueda de esquinas internas/externas y cota Z cero con bloque patrón.
• Base de datos local persistente (Room DB) para offsets de longitud de herramienta (G43 H), radio y geometría de fresa.

⚡ DIAGNÓSTICO DE BUSES ETHERCAT & MESA FPGA
• Supervisión de frecuencia de servo-hilo (1 kHz), monitorización de jitter en microsegundos y telemetría de estados CiA 402 en tiempo real.

Convierte tu tablet o smartphone en la consola industrial más potente para tu máquina CNC con LinuxCncDroid.
```

---

## 🌎 2. Secondary Store Listing (English Translation)

### 🏷️ Title
```text
LinuxCncDroid
```

### 📝 Short Description
```text
Industrial HMI & CNC controller for LinuxCNC with EtherCAT, 3D toolpath & vision.
```

### 📄 Full Description
```text
LinuxCncDroid is a high-performance industrial Human-Machine Interface (HMI) designed for real-time control, 3D visualization, optical alignment, and metrological calibration of LinuxCNC-powered machine tools.

Engineered with high-contrast Material 3 ergonomics for shop-floor touchscreens and tablets, LinuxCncDroid offers seamless connectivity with LinuxCNC controllers across EtherCAT fieldbuses (CiA 402), Mesa Electronics FPGA hardware (Hostmot2), and Parallel Port interfaces.

KEY CAPABILITIES:
• Real-Time Multi-Axis DRO: High-precision digital readout for X, Y, Z, A, B, C axes with G54-G59.3 work coordinate systems.
• Haptic Electronic Handwheel (MPG): Rotary dial jog control with vibration feedback and ×1 / ×10 / ×100 step multipliers.
• Optical Camera Alignment: Center workpieces using live camera overlays (crosshairs, concentric rings, 90° edge finders) with DRO HUD.
• ISO 230-2 Metrology Calibration: Step-by-step axis pitch calibration with expanded uncertainty calculations and direct HAL `comp.tbl` export.
• 3D G-Code Toolpath Viewer: Real-time 3D rendered toolpaths with live tool tracking and feed/spindle overrides.
• Automated 3D Probing & Tool Table: 4-point hole center finders, edge probing, and tool length offsets (G43 H).
• Fieldbus Diagnostics: Servo-thread jitter monitoring, 1 kHz packet tracking, and CiA 402 drive status.
```

---

## 📸 3. Guía y Especificaciones de Capturas de Pantalla (Screenshots)

Para cumplir con los requisitos de Google Play Store (mínimo 2 capturas de teléfono y 1 de tablet), se recomienda capturar las siguientes pantallas de la aplicación:

### 📱 Capturas para Teléfono (Smartphone 9:16 o 1080x1920 / 1080x2400)
1. **Captura 1 — Panel Principal (DRO & Volante MPG)**:
   - *Pestaña*: **CONTROL**
   - *Elemento visual*: DRO con coordenadas digitales iluminadas, selectores de WCS (G54), dial rotativo MPG y barra de estado de husillo y avance.
   - *Texto promocional recomendado sobre la imagen*: *"DRO en Tiempo Real & Volante MPG con Respuesta Háptica"*.

2. **Captura 2 — Visor de Cámara Industrial**:
   - *Pestaña*: **CÁMARA**
   - *Elemento visual*: Visor de cámara activo con retícula cruciforme micrométrica en color Cyan, telemetría DRO HUD en la esquina y botones de micro-alineación.
   - *Texto promocional recomendado sobre la imagen*: *"Centrado Óptico de Pieza & Retículas de Precisión"*.

3. **Captura 3 — Visualizador 3D de G-Code**:
   - *Pestaña*: **TOOLPATH**
   - *Elemento visual*: Trayectoria 3D de mecanizado renderizada con líneas de corte G1 en cian y G0 en amarillo, con herramienta activa en trayectoria.
   - *Texto promocional recomendado sobre la imagen*: *"Visualizador 3D de Trayectorias G-Code en Vivo"*.

---

### 🖥️ Capturas para Tablet (7" y 10" / Formato 16:10 o 4:3 - Ej: 1920x1200 o 2560x1600)
1. **Captura Tablet 1 — Centro de Control Total & Calibración Metrológica**:
   - *Pestaña*: Diálogo de **Calibración Metrológica ISO 230-2** abierto sobre el panel de control.
   - *Elemento visual*: Gráfico de curva de error de paso con bandas de tolerancia $\pm U$, tabla de datos nominales/medidos y botón de exportación `comp.tbl`.
   - *Texto promocional recomendado sobre la imagen*: *"Calibración Metrológica de Ejes ISO 230-2 & Compensación HAL"*.

2. **Captura Tablet 2 — Ciclos de Palpado 3D & Diagnóstico EtherCAT**:
   - *Pestaña*: **PROBING / ETHERCAT**
   - *Elemento visual*: Asistente visual de centrado de orificios por 4 puntos y telemetría de servo-hilo a 1 kHz con jitter mínimo.
   - *Texto promocional recomendado sobre la imagen*: *"Palpado 3D de Piezas & Telemetría Industrial EtherCAT"*.

---

## 🔒 4. Declaración de Seguridad de Datos (Data Safety)

| Campo | Selección en Play Console | Justificación |
| :--- | :--- | :--- |
| **¿La app recopila datos?** | **NO** | No se transmiten datos de usuario ni telemetría fuera de la red local. |
| **Permiso de Cámara (`CAMERA`)** | **Uso de funciones de la app** | Utilizada exclusivamente para el centrado óptico de piezas e inspección en pantalla en tiempo real. No se almacenan fotos en servidores remotos. |
| **Permiso de Internet (`INTERNET`)** | **Conectividad de red local** | Utilizada para comunicar la aplicación con el socket TCP de LinuxCNC en la red local del taller. |
| **Permiso de Vibración (`VIBRATE`)** | **Respuesta háptica** | Retroalimentación física para los clics del volante electrónico (MPG). |
