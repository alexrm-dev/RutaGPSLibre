# Guía de uso

[← Volver al README](../README.md)

## Antes de comenzar

En **Opciones de desarrollador** de Android, elige **Ruta GPS Libre** como aplicación de ubicación simulada y concede los permisos de ubicación precisos solicitados. Para ver el mapa se necesita internet. Para calcular por carreteras también.

## Modo Fija

1. Selecciona **📍 Fija**.
2. Toca una ubicación del mapa. Alternativamente, usa **+ Añadir coordenadas manualmente**, introduciendo latitud y longitud decimales.
3. Pulsa **Iniciar** para emitir la ubicación de prueba. El servicio mantiene una notificación de ejecución.
4. Utiliza **Detener** cuando ya no quieras simular la posición.

Tocar un nuevo punto en modo Fija reemplaza el anterior, siempre que no haya una simulación en ejecución.

## Modo Ruta

1. Selecciona **🛣 Ruta**.
2. Toca el mapa para añadir puntos en orden: **inicio → paradas → destino**. Máximo 15 puntos.
3. Usa **Deshacer** para quitar el último punto o **Borrar puntos** para recomenzar.
4. Si hay tres o más puntos, elige **⏱ Configurar paradas intermedias**, selecciona una parada e indica una espera en segundos (0 a 3600).
5. Pulsa **Calcular por calles** para consultar un trayecto de conducción al servidor de demostración OSRM. Como alternativa, pulsa **Línea recta** para unir directamente los puntos.
6. Ajusta el control **Velocidad** entre 1 y 120 km/h.
7. Pulsa **Iniciar** para comenzar. Puedes arrastrar la velocidad mientras avanza.
8. Utiliza **Pausar**, **Reanudar** y **Detener** según necesites.

Las pausas temporizadas se ejecutan en **puntos intermedios**, no en el origen ni en el destino. La ruta no cambia automáticamente por restricciones de tráfico ni reprograma el trayecto al mover el teléfono.

## Diferencia entre calles y línea recta

- **Calcular por calles**: requiere conexión y utiliza OSRM para obtener coordenadas de un trayecto de conducción. No es un navegador con tráfico en tiempo real.
- **Línea recta**: une las coordenadas elegidas sin consultar al servidor de rutas. Puede atravesar edificios, terrenos y zonas sin caminos, porque no sigue la red vial.

## Estado y datos guardados

En pantalla aparece el estado del servicio, incluyendo progreso aproximado, velocidad y cuenta regresiva al esperar en una parada. El proyecto guarda localmente el **borrador de puntos, esperas, velocidad y modo**, pero no mantiene una colección de rutas guardadas ni recuerda la geometría vial calculada cuando vuelves a abrirlo.

## Al terminar

Detén el servicio de simulación antes de utilizar navegación real, servicios de emergencia u otras apps que dependan de tu posición verdadera. El soporte y comportamiento exacto de la ubicación de prueba dependen de Android y de cada aplicación que la consuma.
