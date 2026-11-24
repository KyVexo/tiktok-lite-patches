# TikTok Lite Patches

La versión de desarrollo está dirigida a **TikTok Lite 47.8.2** (código `470802`), paquete `com.zhiliaoapp.musically.go`. Se probó el APK nodpi de APKMirror en un Galaxy A36 con una copia instalada como `.morphe`.

| Parche | Función |
| --- | --- |
| Hide feed ads | Filtra anuncios, muestra un contador de sesión y añade el interruptor opcional para ocultar publicaciones de fotos. |
| Prefer clean source for video downloads | Prefiere la fuente de reproducción cuando la descarga está permitida y la fuente limpia está disponible. |
| Open mod settings from profile | Añade Ajustes del mod a Ajustes y privacidad. Mantener pulsado Perfil también abre el panel. |
| Stop video looping | Añade un interruptor en Reproducción para detener el video al terminar. Viene apagado. |
| Clean share links | Quita parámetros de seguimiento conocidos y deja solo la URL al usar Copiar enlace. Se selecciona al parchear. |
| Show exact counts | Añade un interruptor en Apariencia para ver cifras completas en el feed y los perfiles. El interruptor viene apagado. |

**Ajustes del mod → Apariencia** también permite elegir el color de acento mediante muestras o un HEX personalizado. Se aplica al panel, al botón de seguir del feed y a los contadores y al aviso flotante de Mensajes. El rosa original es el valor inicial.

## Prueba de la versión de desarrollo

1. Descarga el bundle `.mpp` de la versión preliminar en [GitHub Releases](https://github.com/KyVexo/tiktok-lite-patches/releases) y usa el APK original **47.8.2 nodpi**.
2. Aplica los parches al APK con Morphe. Puedes activar **Clone app** para instalar la copia junto a la original. Si actualizas una copia existente, usa el mismo identificador y certificado de firma.
3. En el teléfono, abre **Ajustes y privacidad → Ajustes del mod** o mantén pulsado **Perfil**.

La prueba local se hizo con Morphe Desktop y una instalación por ADB. La instalación completa desde Morphe Manager está pendiente de validación. El parche LIVE no se incluye en 47.8.2 porque ese APK carece del módulo de reproducción usado por la edición anterior.

Este proyecto distribuye código y bundles de parches. Cada usuario aporta su APK original. Proyecto independiente de TikTok y del equipo de Morphe.

## Validación pendiente en Perfil

En TikTok Lite 47.8.2, **Ocultar cuentas recomendadas** ya se probó en Mensajes con el interruptor activado y desactivado, sin reiniciar la app. Las conversaciones y notificaciones quedaron visibles.

La validación visual en **Perfil** sigue pendiente porque las recomendaciones aún no aparecen en la copia `.morphe` del Galaxy. Cuando aparezcan con el interruptor apagado, comprobar que el bloque desaparezca al activarlo y reaparezca al desactivarlo.
