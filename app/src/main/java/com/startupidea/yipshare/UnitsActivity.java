package com.startupidea.yipshare;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.adapters.UnitAdapter;
import com.startupidea.yipshare.animation_yipshare.LoadingOverlay;
import com.startupidea.yipshare.condominium.SqliteCondominiumRepository;
import com.startupidea.yipshare.condominium.CondominiumService;
import com.startupidea.yipshare.condominium.Unit;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.utils.ConfirmDialogUtil;
import com.startupidea.yipshare.utils.TaskUtil;

public class UnitsActivity extends AppCompatActivity {

    private CondominiumService condominiumService;
    private String condominiumId;
    private RecyclerView unitList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_units);

        Guideline guidelineHeader = findViewById(R.id.guideline_header);
        Guideline guidelineStatus = findViewById(R.id.guideline_status);
        int headerBase = (int) (48 * getResources().getDisplayMetrics().density);
        unitList = findViewById(R.id.rv_unidades);
        int listPadding = unitList.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            guidelineStatus.setGuidelineBegin(bars.top);
            guidelineHeader.setGuidelineBegin(headerBase + bars.top);
            unitList.setPadding(unitList.getPaddingLeft(), unitList.getPaddingTop(),
                    unitList.getPaddingRight(), listPadding + bars.bottom);

            return insets;
        });
        findViewById(R.id.btn_regresar).setOnClickListener(v -> finish());
        findViewById(R.id.btn_agregar_unidad).setOnClickListener(v -> askUnitName(null));

        condominiumService = new CondominiumService(new SqliteCondominiumRepository(this));
        condominiumId = new SessionRepository(this).getCondominiumId();
        showUnits();
    }

    private void showUnits() {
        TaskUtil.run(this, () -> condominiumService.getAllUnits(condominiumId),
                units -> unitList.setAdapter(new UnitAdapter(units, this::askUnitName)),
                e -> {
                    showError(e);
                    finish();
                });
    }

    private void askUnitName(Unit unit) {
        EditText nameField = new EditText(this);
        nameField.setInputType(InputType.TYPE_CLASS_TEXT);
        nameField.setHint(R.string.nombre_unidad);
        if (unit != null) nameField.setText(unit.name());

        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(unit == null ? R.string.agregar_unidad : R.string.editar_unidad)
                .setView(nameField)
                .setPositiveButton(R.string.btn_guardar_cambios, (d, which) -> {
                    String name = nameField.getText().toString();
                    save(unit == null
                            ? () -> condominiumService.createUnit(condominiumId, name)
                            : () -> condominiumService.modifyUnit(condominiumId, unit, name), R.string.unidad_guardada);
                })
                .setNegativeButton(R.string.opcion_cancelar, null);
        if (unit != null) {
            dialog.setNeutralButton(R.string.eliminar_unidad, (d, which) -> ConfirmDialogUtil.show(this,
                    getString(R.string.confirmar_eliminar_unidad, unit.name()),
                    () -> save(() -> condominiumService.deleteUnit(condominiumId, unit), R.string.unidad_eliminada)));
        }
        dialog.show();
    }

    private void save(Runnable work, int doneMessage) {
        new LoadingOverlay(this).run(work, () -> {
            Toast.makeText(this, doneMessage, Toast.LENGTH_SHORT).show();
            showUnits();
        }, this::showError);
    }

    private void showError(AppException e) {
        Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
    }
}
