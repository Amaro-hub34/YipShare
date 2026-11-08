package com.startupidea.yipshare;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.startupidea.yipshare.condominium.CondominiumField;
import com.startupidea.yipshare.condominium.CondominiumPayload;
import com.startupidea.yipshare.condominium.CondominiumService;
import com.startupidea.yipshare.condominium.InvalidCondominium;
import com.startupidea.yipshare.condominium.SqliteCondominiumRepository;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.error.AppException;

import java.util.Set;

public class RegistrarCondominioActivity extends AppCompatActivity {

    private static final String PDF_MIME = "application/pdf";
    private static final String KEY_DOCUMENTS = "documents";
    private static final String KEY_PENDING_DOCUMENT = "pending_document";
    private static final int[] DOCUMENT_BUTTONS = {
            R.id.btn_adjuntar_escritura, R.id.btn_adjuntar_acta,
            R.id.btn_adjuntar_rfc, R.id.btn_adjuntar_identificacion};
    private static final int[] DOCUMENT_LABELS = {
            R.id.tv_archivo_escritura, R.id.tv_archivo_acta,
            R.id.tv_archivo_rfc, R.id.tv_archivo_identificacion};
    private static final CondominiumField[] DOCUMENT_FIELDS = {
            CondominiumField.DEED, CondominiumField.MINUTES,
            CondominiumField.TAX_ID, CondominiumField.IDENTIFICATION};

    private final String[] documents = new String[DOCUMENT_FIELDS.length];
    private final TextView[] documentLabels = new TextView[DOCUMENT_FIELDS.length];
    private final ActivityResultLauncher<String[]> pdfPicker = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) attachDocument(this.pendingDocument, uri);
            });

    private CondominiumService condominiumService;
    private EditText nameField;
    private EditText streetField;
    private EditText neighborhoodField;
    private EditText zipCodeField;
    private EditText municipalityField;
    private EditText cityField;
    private EditText unitsField;
    private int pendingDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar_condominio);

        condominiumService = new CondominiumService(new SqliteCondominiumRepository(new DatabaseDriver(this), this));
        nameField = findViewById(R.id.et_nombre_condominio);
        streetField = findViewById(R.id.et_direccion_condominio1);
        neighborhoodField = findViewById(R.id.et_direccion_condominio2);
        zipCodeField = findViewById(R.id.et_direccion_condominio3);
        municipalityField = findViewById(R.id.et_direccion_condominio4);
        cityField = findViewById(R.id.et_direccion_condominio5);
        unitsField = findViewById(R.id.et_cantidad_unidades);

        for (int index = 0; index < DOCUMENT_FIELDS.length; index++) {
            int document = index;
            documentLabels[index] = findViewById(DOCUMENT_LABELS[index]);
            findViewById(DOCUMENT_BUTTONS[index]).setOnClickListener(v -> {
                pendingDocument = document;
                pdfPicker.launch(new String[]{PDF_MIME});
            });
        }

        if (savedInstanceState != null) {
            pendingDocument = savedInstanceState.getInt(KEY_PENDING_DOCUMENT);
            String[] saved = savedInstanceState.getStringArray(KEY_DOCUMENTS);
            for (int index = 0; saved != null && index < saved.length; index++) {
                if (saved[index] != null) attachDocument(index, Uri.parse(saved[index]));
            }
        }

        findViewById(R.id.btn_crear_condominio).setOnClickListener(v -> tryRequestCondominium());
        Guideline guidelineHeader = findViewById(R.id.guideline_header);
        View bottomNav = findViewById(R.id.bottom_nav);
        int headerBase = (int) (70 * getResources().getDisplayMetrics().density);
        int navBase = (int) (64 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            guidelineHeader.setGuidelineBegin(headerBase + bars.top);

            bottomNav.setPadding(0, 0, 0, bars.bottom);
            ViewGroup.LayoutParams lp = bottomNav.getLayoutParams();
            lp.height = navBase + bars.bottom;
            bottomNav.setLayoutParams(lp);

            return insets;
        });
        findViewById(R.id.btn_regresar).setOnClickListener(v -> finish());
        findViewById(R.id.nav_inicio).setOnClickListener(v ->
                startActivity(new Intent(RegistrarCondominioActivity.this, PantallaPrincipalActivity.class)));

        findViewById(R.id.nav_reportes).setOnClickListener(v ->
                startActivity(new Intent(RegistrarCondominioActivity.this, ReporteActivity.class)));

        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(RegistrarCondominioActivity.this, PerfilActivity.class)));
        findViewById(R.id.nav_prestamos).setOnClickListener(v -> {
            startActivity(new Intent(RegistrarCondominioActivity.this, PrestamoActivity.class));

        });
        findViewById(R.id.nav_agregar).setOnClickListener(v ->
                startActivity(new Intent(RegistrarCondominioActivity.this, FormularioArticuloActivity.class)));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PENDING_DOCUMENT, pendingDocument);
        outState.putStringArray(KEY_DOCUMENTS, documents);
    }

    private void attachDocument(int index, Uri uri) {
        TextView label = documentLabels[index];

        // Some providers ignore the MIME filter, so the picked file is checked again.
        if (!PDF_MIME.equals(getContentResolver().getType(uri))) {
            documents[index] = null;
            label.setText(R.string.tv_archivo_identificacion);
            label.setError(getString(R.string.error_documento_pdf));
            Toast.makeText(this, R.string.error_documento_pdf, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // Restored URIs may already hold the permission or not offer a persistable one.
        }
        documents[index] = uri.toString();
        label.setText(displayName(uri));
        label.setError(null);
    }

    private String displayName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(
                uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) return cursor.getString(0);
        }
        return uri.getLastPathSegment();
    }

    private void tryRequestCondominium() {
        try {
            condominiumService.createCondominiumRequest(new SessionRepository(this).getUserId(), readPayload());
            Toast.makeText(this, R.string.solicitud_condominio_enviada, Toast.LENGTH_LONG).show();
            finish();
        } catch (InvalidCondominium e) {
            showErrors(e.getFields(), getString(e.getMessageRes()));
        } catch (AppException e) {
            Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
        }
    }

    private CondominiumPayload readPayload() {
        String units = unitsField.getText().toString().trim();

        return new CondominiumPayload(
                nameField.getText().toString().trim(),
                streetField.getText().toString().trim(),
                neighborhoodField.getText().toString().trim(),
                zipCodeField.getText().toString().trim(),
                municipalityField.getText().toString().trim(),
                cityField.getText().toString().trim(),
                units.isEmpty() ? 0 : Integer.parseInt(units),
                documents[0],
                documents[1],
                documents[2],
                documents[3]);
    }

    private void showErrors(Set<CondominiumField> fields, String requiredMessage) {
        for (int index = 0; index < DOCUMENT_FIELDS.length; index++) {
            if (fields.contains(DOCUMENT_FIELDS[index])) {
                documentLabels[index].setError(getString(R.string.error_documento_obligatorio));
            }
        }
        // Reverse screen order so the topmost invalid input keeps the focus.
        showInputError(fields, CondominiumField.UNITS, unitsField, requiredMessage);
        showInputError(fields, CondominiumField.CITY, cityField, requiredMessage);
        showInputError(fields, CondominiumField.MUNICIPALITY, municipalityField, requiredMessage);
        showInputError(fields, CondominiumField.ZIP_CODE, zipCodeField, getString(R.string.error_cp_formato));
        showInputError(fields, CondominiumField.NEIGHBORHOOD, neighborhoodField, requiredMessage);
        showInputError(fields, CondominiumField.STREET, streetField, requiredMessage);
        showInputError(fields, CondominiumField.NAME, nameField, requiredMessage);
    }

    private void showInputError(Set<CondominiumField> fields, CondominiumField field, EditText input, String message) {
        if (!fields.contains(field)) return;
        input.setError(message);
        input.requestFocus();
    }
}
