# Contribuir a Ruta GPS Libre

¡Gracias por querer mejorar el proyecto!

## Antes de proponer cambios

- Revisa el [README](README.md), la [arquitectura](docs/ARQUITECTURA.md) y los [problemas conocidos](docs/SOLUCION_DE_PROBLEMAS.md).
- Para errores, abre un **Issue** con pasos para reproducir, modelo de dispositivo y versión Android, sin incluir ubicaciones privadas.
- Para cambios grandes, abre primero una propuesta para debatir alcance y mantenimiento.

## Desarrollo

1. Crea un fork y una rama descriptiva.
2. Conserva las versiones de bibliotecas del proyecto, salvo que el cambio sea explícitamente una actualización de dependencias.
3. Mantén la interfaz en español y evita introducir cuentas, publicidad o mapas descargables sin justificar un cambio de alcance.
4. Añade pruebas para la lógica de distancia, velocidades y paradas cuando corresponda.
5. Ejecuta `:app:testDebugUnitTest` y `:app:assembleDebug` o el BAT de Windows.
6. Abre un Pull Request explicando qué cambiaste, cómo lo probaste y cualquier limitación conocida.

## Uso responsable

No aceptamos contribuciones orientadas a ocultar la condición simulada del GPS, eludir validaciones de servicios externos, vulnerar reglas de terceros ni comprometer la privacidad de los usuarios.

## Licencia

Al enviar contribuciones al repositorio aceptas que se distribuyan bajo la licencia MIT del proyecto, sin perjuicio de los derechos y licencias de terceros.
