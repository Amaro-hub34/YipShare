# Formulario para publicar un artículo

## Intención

Que el formulario de `FormularioArticuloActivity` funcione: permitir ofrecer un artículo con varias duraciones de préstamo, impedir publicaciones incompletas y mostrar la imagen elegida en el cuadro.

## Objetivos

- Marcar una o más duraciones de la lista actual (1 día, 2 días, 3 días, 1 semana, 2 semanas, 1 mes).
- Exigir nombre, descripción, imagen y al menos una duración para publicar.
- "Cambiar imagen" abre un selector y la imagen elegida aparece en el cuadro.

## Fuera de alcance

- Fechas de calendario, edición de artículos y el botón "Borrar objeto".

## Enfoques

1. **Casillas (`CheckBox`) — elegido (decisión de Claude, recomendada sin objeción del usuario):** no cambia el tema y `CheckBox` ya se usa en el perfil.
2. Chips de Material: requieren un tema Material en ese bloque, porque la app usa `Theme.AppCompat`.
3. Diálogo de selección múltiple: requiere un toque más y oculta lo marcado.

## Comportamiento

- **Duraciones:** el `Spinner` `sp_tiempo_prestamo` se reemplaza por seis casillas en dos columnas, generadas desde el arreglo `tiempo_prestamo`. Ninguna viene marcada.
- **Nombre y descripción:** se recortan los espacios; si quedan vacíos, el error aparece en el campo.
- **Imagen (obligatoria):** se elige con el selector de fotos (`PickVisualMedia`), y solo se aceptan PNG y JPEG, para respetar el texto "Formatos: PNG, JPG, JPEG" sin pedir permisos. La imagen se muestra en `iv_preview_imagen` con el botón encima. La URI se conserva al rotar la pantalla.
- **Errores de imagen y duración:** texto rojo bajo cada sección, que desaparece al corregirlo.
- **"Publicar":** valida todos los campos y muestra todos los errores a la vez. Si todo es válido, guarda el artículo en un archivo local, copia la imagen a la app, muestra un mensaje de confirmación y cierra la pantalla. El artículo aparece primero en "Mis Artículos".
- **Textos:** se agregan en `values` y `values-es`.

## A criterio de quien implemente

- Nombres exactos de las clases.

## Estructura

Sigue el patrón de `user/`: un paquete `item/` con un payload inmutable y un servicio que valida y lanza errores con nombre. La Activity solo delega y convierte esos errores en mensajes. `LocalItemRepository` guarda en un archivo local hasta que exista la base de datos.
