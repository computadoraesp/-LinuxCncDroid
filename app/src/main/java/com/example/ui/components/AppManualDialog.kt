package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DocSectionItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManualDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sections = remember { getManualSections() }
    var selectedSectionId by remember { mutableStateOf(sections.first().id) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSections = remember(searchQuery) {
        if (searchQuery.isBlank()) sections
        else sections.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.summary.contains(searchQuery, ignoreCase = true) ||
            it.detailedContent.contains(searchQuery, ignoreCase = true)
        }
    }

    val activeSection = filteredSections.find { it.id == selectedSectionId } ?: filteredSections.firstOrNull() ?: sections.first()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CncSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CncCyberCyan.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CncCyberCyan.copy(alpha = 0.15f))
                                .border(1.dp, CncCyberCyan, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Manual",
                                tint = CncCyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "MANUAL DE OPERACIÓN Y REFERENCIA TÉCNICA CNC",
                                color = CncCyberCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Guía Completa de Seguridad, DRO, MPG, Palpado, EtherCAT y Calibración Metrológica",
                                color = CncTextSecondary,
                                fontSize = 9.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(CncSurfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CncTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = CncCardBorder,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar en el manual (ej: calibración, alarma, MPG, WCS, E-STOP)...", fontSize = 9.5.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CncTextSecondary, modifier = Modifier.size(16.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = CncTextSecondary, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CncSurfaceVariant,
                        unfocusedContainerColor = CncSurfaceVariant,
                        focusedBorderColor = CncCyberCyan,
                        unfocusedBorderColor = CncCardBorder,
                        focusedTextColor = CncTextPrimary,
                        unfocusedTextColor = CncTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal Section Navigator Tabs
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredSections) { sec ->
                        val isSelected = sec.id == activeSection.id
                        val bg = if (isSelected) CncCyberCyan else CncSurfaceVariant
                        val textColor = if (isSelected) Color.Black else CncTextPrimary

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(bg)
                                .border(1.dp, if (isSelected) CncCyberCyan else CncCardBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedSectionId = sec.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sec.title,
                                color = textColor,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Detail View for Active Section
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CncSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CncCyberCyan.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = activeSection.title.uppercase(),
                                color = CncActiveGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = activeSection.summary,
                                color = CncCyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            HorizontalDivider(color = CncCardBorder, modifier = Modifier.padding(vertical = 4.dp))

                            Text(
                                text = activeSection.detailedContent,
                                color = CncTextPrimary,
                                fontSize = 9.5.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Standard Operating Steps (SOP)
                    if (activeSection.standardSteps.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CncSurfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = CncActiveGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PROCEDIMIENTO ESTÁNDAR PASO A PASO (SOP)",
                                        color = CncActiveGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                activeSection.standardSteps.forEachIndexed { i, step ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "${i + 1}.",
                                            color = CncCyberCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = step,
                                            color = CncTextPrimary,
                                            fontSize = 9.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Safety & Interlock Warnings
                    if (activeSection.safetyTips.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CncEstopRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CncEstopRed.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = CncEstopRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "NORMAS DE SEGURIDAD Y ENCLAVAMIENTO (INTERLOCK)",
                                        color = CncEstopRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                activeSection.safetyTips.forEach { tip ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("•", color = CncEstopRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(tip, color = CncTextPrimary, fontSize = 9.sp, lineHeight = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getManualSections(): List<DocSectionItem> {
    return listOf(
        DocSectionItem(
            id = "safety_interlocks",
            title = "1. Seguridad y Enclavamientos",
            category = "SEGURIDAD",
            iconName = "ic_security",
            summary = "Estados de la máquina, controles permitidos durante el maquinado y recuperación de alarmas.",
            detailedContent = """
                La arquitectura de LinuxCNC Droid HMI implementa enclavamientos industriales conforme a ISO 23125:
                
                • ESTADO RUNNING (Ciclo en ejecución):
                  - Activos: Parada de Emergencia (E-STOP), Feed Hold (Pausa inmediata), Abort/Stop, Overrides de Avance (0-150%) y Husillo (50-120%), Refrigerante.
                  - Bloqueados: Jog manual (botones y dial MPG), Puesta a cero (G92), Referenciado (G28), MDI y cambios de configuración.
                
                • GESTIÓN Y RECUPERACIÓN DE ALARMAS:
                  - Al dispararse un fallo (sobrecarga servo, final de carrera, límite térmico), el planificador de trayectoria se detiene en <1ms y preserva la línea activa de G-Code.
                  - Para reanudar:
                    1. Despeje físico de la causa.
                    2. En la pestaña LOGS presione 'CLEAR LOGS / RESET'.
                    3. Presione 'POWER ON'.
                    4. Presione 'CYCLE START' para continuar desde la línea donde ocurrió el fallo.
            """.trimIndent(),
            standardSteps = listOf(
                "En caso de colisión inminente, pulse el botón rojo E-STOP en la barra superior.",
                "Para corregir vibración (chatter), ajuste el slider Spindle Override hacia arriba o Feed Override hacia abajo sin detener el programa.",
                "Tras un paro por alarma, consulte el código de error en la pestaña LOGS antes de re-energizar."
            ),
            safetyTips = listOf(
                "Nunca intente mover ejes manualmente mientras el ciclo esté en ejecución.",
                "Verifique que la parada de emergencia física externa esté en serie con el relé de seguridad del armario eléctrico."
            )
        ),

        DocSectionItem(
            id = "metrology_calib",
            title = "2. Calibración Metrológica (ISO 230-2)",
            category = "CALIBRACIÓN",
            iconName = "ic_calib",
            summary = "Estándar de calibración cada 10% del eje, cálculo de incertidumbre y compensación de husillo (comp.tbl).",
            detailedContent = """
                El asistente metrológico permite evaluar y corregir el error de paso del husillo de bolas (Pitch Error Mapping) según ISO 230-2:
                
                • MÉTODO POR SECTORES DEL 10%:
                  Se divide la carrera total del eje en 10 intervalos (0%, 10%, 20%, ..., 100%).
                  En cada cota, el operador toma una lectura con su instrumento patrón (micrómetro, comparador, regla óptica).
                
                • CÁLCULO DE INCERTIDUMBRE COMBINADA (ISO/IEC 17025):
                  Se integra la incertidumbre del instrumento del operador (U_inst) con la repetibilidad del eje.
                  Se calcula la Incertidumbre Expandida U con factor de cobertura k=2 (95% de nivel de confianza).
                
                • APLICACIÓN DIRECTA EN LINUXCNC (comp.tbl):
                  Al finalizar, el sistema genera el archivo 'comp.tbl' para el módulo HAL 'linear_comp'. LinuxCNC compensará activamente las desviaciones en tiempo real durante el corte.
            """.trimIndent(),
            standardSteps = listOf(
                "Abra el diálogo de Calibración Metrológica desde la barra superior (icono regla/compás).",
                "Seleccione el eje (X, Y o Z), longitud de carrera y declare la incertidumbre de su instrumento (ej. ±0.003 mm).",
                "Pulse 'INIT SESSION' para generar los 11 puntos de prueba.",
                "Para cada punto, pulse 'DRIVE AXIS' para posicionar el cabezal en la cota nominal.",
                "Lea el instrumento patrón, ingrese el valor medido y pulse 'GUARDAR Y SIGUIENTE'.",
                "Al completar el 100%, revise la curva de error y pulse 'EXPORT comp.tbl' para copiar la tabla de compensación a su configuración de LinuxCNC."
            ),
            safetyTips = listOf(
                "Asegúrese de que el husillo esté a temperatura normal de trabajo (15 minutos de giro previo) para evitar deriva térmica en la medición.",
                "Fije el reloj comparador firmemente a la bancada con base magnética rígida."
            )
        ),

        DocSectionItem(
            id = "dro_wcs",
            title = "3. DRO y Sistemas de Coordenadas",
            category = "OPERACIÓN",
            iconName = "ic_dro",
            summary = "Lectura digital DRO, origen de pieza WCS G54-G59, coordenadas máquina G53 y par de servos.",
            detailedContent = """
                El panel DRO presenta las posiciones en tiempo real con resolución de 3 decimales (0.001 mm):
                
                • MODOS DE COORDENADAS:
                  - WORK POS (G54 a G59): Coordenadas relativas al origen de la pieza de trabajo.
                  - MACHINE ABS (G53): Coordenadas absolutas físicas del bastidor respecto a los finales de carrera de Home.
                  - DISTANCE TO GO (DTG): Distancia remanente hasta el final del bloque actual de G-Code.
                
                • TELEMETRÍA DE SERVOS:
                  Monitorea la carga porcentual del servo drive (% Torque). Valores sostenidos superiores al 85% indican desgaste de guías o avance excesivo.
            """.trimIndent(),
            standardSteps = listOf(
                "Seleccione el sistema de trabajo deseado en el selector de la barra superior (G54 por defecto).",
                "Para poner a cero un eje individual, use el botón ZERO del eje en el panel DRO.",
                "Para referenciar todos los ejes a los finales de carrera físicos, presione 'HOME ALL (G28)'."
            ),
            safetyTips = listOf(
                "Nunca ponga a cero las coordenadas G92 con la fresa dentro del material sin verificar las cotas de seguridad."
            )
        ),

        DocSectionItem(
            id = "virtual_mpg",
            title = "4. Volante Electrónico Virtual MPG",
            category = "JOG",
            iconName = "ic_mpg",
            summary = "Volante industrial táctil de 100 divisiones con multiplicadores de resolución x1, x10, x100, x1000.",
            detailedContent = """
                El volante virtual MPG emula fielmente una manivela electrónica física (Handwheel MPG):
                
                • 100 DIVISIONES POR REVOLUCIÓN (3.6° por detención táctil).
                • MULTIPLICADORES:
                  - x1: 0.001 mm / paso (Alineación micrométrica final).
                  - x10: 0.010 mm / paso (Aproximación de precisión).
                  - x100: 0.100 mm / paso (Posicionamiento rápido de pieza).
                  - x1000: 1.000 mm / paso (Desplazamiento largo de mesa).
                • RETROALIMENTACIÓN HÁPTICA: Emite una vibración y clic acústico en cada muesca de 3.6°.
            """.trimIndent(),
            standardSteps = listOf(
                "En el panel de Jog, seleccione la pestaña 'VIRTUAL MPG WHEEL'.",
                "Elija el eje a mover (X, Y, Z o A) en el selector de ejes.",
                "Seleccione el multiplicador adecuado (empiece con x10 para seguridad).",
                "Gire la manivela con el dedo en sentido horario (+) o antihorario (-) observando la respuesta en el DRO."
            ),
            safetyTips = listOf(
                "En eje Z cerca de la mordaza o pieza, use exclusivamente multiplicadores x1 o x10 para evitar colisiones del husillo."
            )
        ),

        DocSectionItem(
            id = "tool_table",
            title = "5. Tabla de Herramientas y ATC (tool.tbl)",
            category = "HERRAMIENTAS",
            iconName = "ic_tools",
            summary = "Gestión de fresas T1-T99, compensación de longitud H (G43), Touch-Off rápido y vida útil.",
            detailedContent = """
                El gestor de herramientas sincroniza directamente con el archivo 'tool.tbl' de LinuxCNC:
                
                • MONTAR HERRAMIENTA (M6): Carga la herramienta en el husillo y activa la compensación G43 Hx.
                • TOUCH-OFF Z (G43.1): Permite calibrar la longitud de la herramienta actual tomando la cota Z del husillo con un solo toque.
                • MONITOREO DE DESGASTE: Alerta cuando el tiempo acumulado de corte supera el 85% de la vida útil estimada.
            """.trimIndent(),
            standardSteps = listOf(
                "Abra la Tabla de Herramientas desde el icono de llave inglesa en la barra superior.",
                "Para montar una herramienta, localice el carretel deseado y presione 'MOUNT (M6)'.",
                "Para calibrar su longitud en Z, baje la punta hasta tocar el sensor de mesa y pulse 'TOUCH-OFF (G43.1)'."
            ),
            safetyTips = listOf(
                "Verifique que las RPM máximas asignadas a la herramienta no superen las capacidades del collet o portaherramientas."
            )
        ),

        DocSectionItem(
            id = "probing_cycles",
            title = "6. Ciclos de Palpador 3D (WCS Probing)",
            category = "PALPADO",
            iconName = "ic_probe",
            summary = "Rutinas automatizadas para centrado de agujeros, cilindros, esquinas y toma de altura en Z.",
            detailedContent = """
                La pestaña PROBING incluye rutinas cinemáticas con parada por contacto eléctrico (G38.2):
                
                • BORE CENTER: Palpa en 4 cuadrantes (-X, +X, -Y, +Y) y calcula el centro exacto del agujero.
                • BOSS CENTER: Palpa las 4 caras externas de un cilindro para hallar el eje central.
                • CORNER FINDER: Localiza el vértice XY de una esquina para fijar el origen de pieza.
                • SURFACE HEIGHT Z: Desciende hasta contacto para fijar el plano Z=0.
            """.trimIndent(),
            standardSteps = listOf(
                "Monte el palpador 3D y conecte el cable de señal al puerto PROBE.",
                "Posicione el palpador manualmente dentro del agujero o sobre la superficie.",
                "En la pestaña PROBING, presione la rutina deseada (ej. BORE CENTER).",
                "El sistema ejecutará los contactos y actualizará el WCS G54 automáticamente."
            ),
            safetyTips = listOf(
                "Pruebe el LED indicador del palpador tocándolo suavemente con la mano antes de iniciar un ciclo automático."
            )
        ),

        DocSectionItem(
            id = "ethercat_bus",
            title = "7. Telemetría de Bus EtherCAT",
            category = "DIAGNÓSTICO",
            iconName = "ic_ethercat",
            summary = "Monitoreo en tiempo real de esclavos CiA402, jitter de ciclo y tramas perdidas.",
            detailedContent = """
                El monitor EtherCAT reporta la salud de la red de automatización industrial:
                
                • ESTADO DEL MAESTRO: Debe permanecer en OP (Operational).
                • JITTER DE CICLO: Variación del tiempo de reloj en tiempo real. Valores < 5 µs son óptimos.
                • TRAMAS PERDIDAS (Lost Frames): Si este contador sube de 0, revise el cableado apantallado y la toma a tierra.
            """.trimIndent(),
            standardSteps = listOf(
                "Acceda a la pestaña ETHERCAT.",
                "Verifique que todos los servodrives de los ejes X, Y, Z aparezcan en estado 'OP'.",
                "Supervise la temperatura de los variadores durante trabajos pesados de mecanizado."
            ),
            safetyTips = listOf(
                "Si el maestro entra en estado INIT o PRE-OP durante el mecanizado, la máquina ejecutará una parada de seguridad instantánea."
            )
        )
    )
}
