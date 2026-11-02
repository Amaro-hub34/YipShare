# Formulario de artículo Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the post-item form validate its fields, accept several loan durations and preview the chosen image.

**Architecture:** New `item/` package mirroring `user/`: payload, entity, service, repository interface and pending repository. `FormularioArticuloActivity` only reads views, delegates to `ItemService` and shows errors.

**Tech Stack:** Java, Android Views, JUnit 4. No new dependencies. minSdk 24.

**Spec:** `docs/superpowers/specs/2026-11-01-formulario-articulo-design.md`

## Global Constraints

- Keep `Theme.AppCompat`; no Material chips.
- Code in English, user-facing text in `values/strings.xml` and `values-es/strings.xml`, almost no comments, 4 spaces.
- Name, description, image and at least one duration are required. Nothing is persisted.
- Accepted image types: `image/png`, `image/jpeg`. No permissions added to the manifest.
- Do not commit: the user has not asked for commits, and `PasswordToggleUtil.java` holds unrelated uncommitted work.

## Review Focus

- Name or description containing only spaces: rejected like an empty field (test in Task 1).
- Several fields invalid at once: every error is reported, not just the first (test in Task 1).
- Rotating the screen: image and marked durations stay (manual check in Task 2).
- Very large photo: the screen does not crash on an oversized bitmap (manual check in Task 2).
- Picker cancelled, or file unreadable: a cancel keeps the previous image; an unreadable file shows the image error and clears the selection (manual check in Task 2).

---

### Task 1: Item layer

**Files:**
- Create in `app/src/main/java/com/startupidea/yipshare/item/`: `ItemField.java`, `ItemPayload.java`, `Item.java`, `InvalidItem.java`, `ItemRepository.java`, `PendingItemRepository.java`, `ItemService.java`
- Test: `app/src/test/java/com/startupidea/yipshare/item/ItemServiceTest.java`

**Interfaces:**
- Produces:
  - `enum ItemField { NAME, DESCRIPTION, IMAGE, LOAN_DURATIONS }`
  - `final class ItemPayload implements Serializable`: `ItemPayload(String name, String category, String description, String physicalCondition, String imageUri, List<String> loanDurations)`, a getter per field; `imageUri` may be null; the list is copied and unmodifiable, and must remain serializable.
  - `final class Item implements Serializable`: `Item(String id, ItemPayload payload)`, `getId()`, `getPayload()`.
  - `InvalidItem extends AppException`: `InvalidItem(Set<ItemField> fields)` with message res `R.string.error_campo_obligatorio`, `Set<ItemField> getFields()`.
  - `interface ItemRepository { Item createItem(ItemPayload payload); }`
  - `PendingItemRepository` returns a new `Item` with a random UUID, with the same TODO comment as `PendingUserRepository`.
  - `ItemService(ItemRepository)`, `Item createItem(ItemPayload payload)`.

- [ ] **Step 1: Write failing tests**

`ItemServiceTest` uses a helper building a payload from valid defaults (`"Drill"`, `"content://image/1"`, `List.of("1 day")`).

```java
@Test public void createsValidItem()            // id not empty, payload is the same object
@Test public void rejectsBlankName()            // "   " -> fields == {NAME}
@Test public void rejectsBlankDescription()     // "   " -> fields == {DESCRIPTION}
@Test public void rejectsMissingImage()         // null -> fields == {IMAGE}
@Test public void rejectsNoDurations()          // empty list -> fields == {LOAN_DURATIONS}
@Test public void reportsAllInvalidFields()     // all invalid -> all four fields
@Test public void acceptsSeveralDurations()     // List.of("1 day", "1 week") -> size 2
```

- [ ] **Step 2: Run to verify failure**

Run from the project root: `./gradlew testDebugUnitTest --tests '*ItemServiceTest'`
Expected: compilation failure, `ItemService` not found.

- [ ] **Step 3: Implement the classes**

`createItem` collects invalid fields in an `EnumSet`, throws `InvalidItem` when it is not empty, and otherwise delegates to the repository.

- [ ] **Step 4: Run to verify success**

Run: `./gradlew testDebugUnitTest --tests '*ItemServiceTest'`
Expected: BUILD SUCCESSFUL, 7 tests pass.

### Task 2: Form screen

**Files:**
- Create: `app/src/main/java/com/startupidea/yipshare/utils/ImagePreviewUtil.java`
- Modify: `app/src/main/java/com/startupidea/yipshare/FormularioArticuloActivity.java`
- Modify: `app/src/main/res/layout/activity_formulario_articulo.xml`
- Modify: `app/src/main/res/values/strings.xml`, `app/src/main/res/values-es/strings.xml`

**Interfaces:**
- Consumes: `ItemService.createItem`, `ItemPayload`, `InvalidItem.getFields()`, `ItemField`, `PendingItemRepository` from Task 1.
- Produces: `ImagePreviewUtil.tryLoad(Context context, Uri uri, int maxSizePx)` returning a `Bitmap`, or null when unreadable.

- [ ] **Step 1: Strings**

| Name | English | Spanish |
|---|---|---|
| `error_imagen_obligatoria` | Choose an image | Elige una imagen |
| `error_imagen_invalida` | Could not load that image | No se pudo cargar esa imagen |
| `error_duracion_obligatoria` | Choose at least one duration | Elige al menos una duración |
| `articulo_publicado` | Item posted | Artículo publicado |

- [ ] **Step 2: Layout**

- Replace `sp_tiempo_prestamo` with an `android.widget.GridLayout` `grid_tiempo_prestamo`, `android:columnCount="2"`.
- Add `tv_error_duracion` under the grid and `tv_error_imagen` under the `FA_formatos` text: `gone`, `@color/rojo_en_uso`, 12sp.

- [ ] **Step 3: `ImagePreviewUtil.tryLoad`**

Read bounds with `inJustDecodeBounds`, then decode with a power-of-two `inSampleSize` so the longest side is at most `maxSizePx`. Each decode opens its own stream through `ContentResolver.openInputStream`. Catch `IOException` and `SecurityException`, returning null.

- [ ] **Step 4: Activity**

- Fill the grid from `R.array.tiempo_prestamo`, one `CheckBox` per item, with `setId(1000 + index)` so Android restores its state after rotation, and column weight 1.
- Register `ActivityResultContracts.OpenDocument`; launch it from `btn_cambiar_imagen` with `{"image/png", "image/jpeg"}`.
- Picker result: ignore null. Otherwise call `tryLoad` with 1080; on a bitmap, store `imageUri`, show it in `iv_preview_imagen` and hide `tv_error_imagen`; on null, clear `imageUri`, hide the preview and show `error_imagen_invalida`.
- Save `imageUri` in `onSaveInstanceState` under `"image_uri"`; in `onCreate`, restore it and reload the preview the same way.
- Checking a box hides `tv_error_duracion`.
- `btn_accion_principal`: clear errors, build `ItemPayload` from the views and call `ItemService.createItem`. On success show `articulo_publicado` in a `Toast` and `finish()`. On `InvalidItem`, show each field's error: `setError` with `error_campo_obligatorio` for name and description, focusing the first; `tv_error_imagen` with `error_imagen_obligatoria`; `tv_error_duracion` with `error_duracion_obligatoria`.
- Pass trimmed name and description, and the spinner's `getSelectedItem().toString()` for category and physical condition.

- [ ] **Step 5: Verify the build**

Run: `./gradlew testDebugUnitTest assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Manual check on a device or emulator**

Expected:
- Empty form + "Publicar": all four errors appear.
- Name with only spaces: error on the field.
- Image selected: it fills the box, button on top.
- Several durations marked and screen rotated: image and marks stay.
- Large photo: no crash.
- Picker cancelled: previous image stays.
- Complete form: confirmation shown and the screen closes.
