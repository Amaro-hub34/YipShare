package com.startupidea.yipshare;

import com.startupidea.yipshare.animation_yipshare.LoadingOverlay;
import com.startupidea.yipshare.error.BadRequestException;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.item.SqliteItemRepository;
import com.startupidea.yipshare.item.InvalidItem;
import com.startupidea.yipshare.item.Item;
import com.startupidea.yipshare.item.ItemField;
import com.startupidea.yipshare.item.ItemPayload;
import com.startupidea.yipshare.item.ItemService;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.utils.ImageFileUtil;
import com.startupidea.yipshare.utils.ImagePreviewUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FormularioArticuloActivity extends AppCompatActivity {

    private static final String KEY_IMAGE_URI = "image_uri";
    private static final String KEY_CAMERA_FILE = "camera_file";
    private static final int DURATION_ID_BASE = 1000;
    private static final int PREVIEW_MAX_SIZE_PX = 1080;

    private final List<CheckBox> durationBoxes = new ArrayList<>();
    private final ActivityResultLauncher<PickVisualMediaRequest> imagePicker = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) showImage(uri);
            });
    private final ActivityResultLauncher<Uri> camera = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), this::onPictureTaken);

    private ItemService itemService;
    private String userId;
    private String condominiumId;
    private Item editingItem;
    private EditText nameField;
    private EditText descriptionField;
    private Spinner categorySpinner;
    private Spinner physicalConditionSpinner;
    private ImageView previewImage;
    private TextView durationErrorLabel;
    private TextView imageErrorLabel;
    private Uri imageUri;
    private File cameraFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario_articulo);

        itemService = new ItemService(new SqliteItemRepository(this));
        nameField = findViewById(R.id.et_nombre_articulo);
        descriptionField = findViewById(R.id.et_descripcion);
        categorySpinner = findViewById(R.id.spinner_categoria);
        physicalConditionSpinner = findViewById(R.id.sp_estado_fisico);
        previewImage = findViewById(R.id.iv_preview_imagen);
        durationErrorLabel = findViewById(R.id.tv_error_duracion);
        imageErrorLabel = findViewById(R.id.tv_error_imagen);

        SessionRepository session = new SessionRepository(this);
        userId = session.getUserId();
        condominiumId = session.getCondominiumId();
        editingItem = Item.fromIntent(getIntent());

        addDurationBoxes();
        findViewById(R.id.btn_cambiar_imagen).setOnClickListener(v -> chooseImageSource());
        Button mainButton = findViewById(R.id.btn_accion_principal);
        mainButton.setOnClickListener(v -> tryPostItem());
        if (editingItem != null) mainButton.setText(R.string.btn_guardar_cambios);

        if (savedInstanceState != null) {
            String savedCameraFile = savedInstanceState.getString(KEY_CAMERA_FILE);
            if (savedCameraFile != null) cameraFile = new File(savedCameraFile);
            String savedUri = savedInstanceState.getString(KEY_IMAGE_URI);
            if (savedUri != null) showImage(Uri.parse(savedUri));
        } else if (editingItem != null) {
            showEditingItem();
        }

        Guideline guidelineHeader = findViewById(R.id.guideline_header);
        Guideline guidelineStatus = findViewById(R.id.guideline_status);
        View bottomNav = findViewById(R.id.bottom_nav);
        int headerBase = (int) (70 * getResources().getDisplayMetrics().density);
        int navBase = (int) (64 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            guidelineStatus.setGuidelineBegin(bars.top);
            guidelineHeader.setGuidelineBegin(headerBase + bars.top);

            bottomNav.setPadding(0, 0, 0, bars.bottom);
            ViewGroup.LayoutParams lp = bottomNav.getLayoutParams();
            lp.height = navBase + bars.bottom;
            bottomNav.setLayoutParams(lp);

            return insets;
        });
        findViewById(R.id.nav_inicio).setOnClickListener(v ->
                startActivity(new Intent(FormularioArticuloActivity.this, PantallaPrincipalActivity.class)));
        findViewById(R.id.nav_reportes).setOnClickListener(v ->
                startActivity(new Intent(FormularioArticuloActivity.this, ReporteActivity.class)));
        findViewById(R.id.nav_agregar).setOnClickListener(v ->
                startActivity(new Intent(FormularioArticuloActivity.this, FormularioArticuloActivity.class)));
        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(FormularioArticuloActivity.this, PerfilActivity.class)));
        findViewById(R.id.nav_prestamos).setOnClickListener(v -> {
            startActivity(new Intent(FormularioArticuloActivity.this, PrestamoActivity.class));
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageUri != null) outState.putString(KEY_IMAGE_URI, imageUri.toString());
        if (cameraFile != null) outState.putString(KEY_CAMERA_FILE, cameraFile.getPath());
    }

    private void addDurationBoxes() {
        GridLayout grid = findViewById(R.id.grid_tiempo_prestamo);
        String[] durations = getResources().getStringArray(R.array.tiempo_prestamo);

        for (int index = 0; index < durations.length; index++) {
            CheckBox box = new CheckBox(this);
            box.setId(DURATION_ID_BASE + index);
            box.setText(durations[index]);
            box.setOnCheckedChangeListener((view, checked) -> durationErrorLabel.setVisibility(View.GONE));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            grid.addView(box, params);
            durationBoxes.add(box);
        }
    }

    private void chooseImageSource() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.elegir_origen_imagen)
                .setItems(new CharSequence[]{getString(R.string.opcion_galeria), getString(R.string.opcion_camara)},
                        (dialog, which) -> {
                            if (which == 0) openGallery();
                            else openCamera();
                        })
                .setNegativeButton(R.string.opcion_cancelar, null)
                .show();
    }

    private void openCamera() {
        try {
            cameraFile = ImageFileUtil.createCameraFile(this);
            camera.launch(FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", cameraFile));
        } catch (BadRequestException e) {
            showImageError(e.getMessageRes());
        } catch (ActivityNotFoundException e) {
            cameraFile.delete();
            Toast.makeText(this, R.string.error_sin_camara, Toast.LENGTH_SHORT).show();
        }
    }

    private void onPictureTaken(boolean taken) {
        if (taken) showImage(Uri.fromFile(cameraFile));
        else cameraFile.delete();
    }

    private void openGallery() {
        imagePicker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void showImage(Uri uri) {
        Bitmap preview = ImagePreviewUtil.tryLoad(this, uri, PREVIEW_MAX_SIZE_PX);

        if (preview == null) {
            imageUri = null;
            previewImage.setVisibility(View.GONE);
            showImageError(R.string.error_imagen_invalida);
            return;
        }

        imageUri = uri;
        previewImage.setImageBitmap(preview);
        previewImage.setVisibility(View.VISIBLE);
        imageErrorLabel.setVisibility(View.GONE);
    }

    private void tryPostItem() {
        cleanGUI();

        ItemPayload payload = readPayload(imageUri == null ? null : imageUri.toString());
        if (editingItem == null) {
            new LoadingOverlay(this).run(() -> itemService.createItem(condominiumId, payload), created -> {
                Toast.makeText(this, R.string.articulo_publicado, Toast.LENGTH_SHORT).show();
                finish();
            }, this::showPostError);
        } else {
            new LoadingOverlay(this).run(() -> itemService.modifyItem(userId, editingItem, payload), modified -> {
                Intent result = new Intent();
                modified.putInto(result);
                setResult(Activity.RESULT_OK, result);
                Toast.makeText(this, R.string.articulo_actualizado, Toast.LENGTH_SHORT).show();
                finish();
            }, this::showPostError);
        }
    }

    private void showPostError(AppException e) {
        if (e instanceof InvalidItem invalidItem) {
            showErrors(invalidItem.getFields(), e.getMessageRes());
        } else if (e instanceof BadRequestException) {
            showImageError(e.getMessageRes());
        } else {
            Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditingItem() {
        ItemPayload payload = editingItem.payload();
        nameField.setText(payload.getName());
        descriptionField.setText(payload.getDescription());
        selectValue(categorySpinner, payload.getCategory());
        selectValue(physicalConditionSpinner, payload.getPhysicalCondition());
        for (CheckBox box : durationBoxes) {
            box.setChecked(payload.getLoanDurations().contains(box.getText().toString()));
        }
        if (payload.getImageUri() != null) showImage(Uri.parse(payload.getImageUri()));
    }

    @SuppressWarnings("unchecked")
    private void selectValue(Spinner spinner, String value) {
        int position = ((ArrayAdapter<CharSequence>) spinner.getAdapter()).getPosition(value);
        if (position >= 0) spinner.setSelection(position);
    }

    private void cleanGUI() {
        nameField.setError(null);
        descriptionField.setError(null);
    }

    private void showImageError(@StringRes int message) {
        imageErrorLabel.setText(message);
        imageErrorLabel.setVisibility(View.VISIBLE);
    }

    private ItemPayload readPayload(String image) {
        List<String> durations = new ArrayList<>();
        for (CheckBox box : durationBoxes) {
            if (box.isChecked()) durations.add(box.getText().toString());
        }

        return new ItemPayload(
                nameField.getText().toString().trim(),
                categorySpinner.getSelectedItem().toString(),
                descriptionField.getText().toString().trim(),
                physicalConditionSpinner.getSelectedItem().toString(),
                image,
                durations);
    }

    private void showErrors(Set<ItemField> fields, @StringRes int requiredMessage) {
        if (fields.contains(ItemField.DESCRIPTION)) {
            boolean empty = descriptionField.getText().toString().trim().isEmpty();
            descriptionField.setError(empty ? getString(requiredMessage)
                    : getString(R.string.error_descripcion_corta, ItemService.DESCRIPTION_MIN_LENGTH));
            descriptionField.requestFocus();
        }
        if (fields.contains(ItemField.NAME)) {
            nameField.setError(getString(requiredMessage));
            nameField.requestFocus();
        }
        if (fields.contains(ItemField.IMAGE)) {
            showImageError(R.string.error_imagen_obligatoria);
        }
        if (fields.contains(ItemField.LOAN_DURATIONS)) {
            durationErrorLabel.setVisibility(View.VISIBLE);
        }
    }
}
