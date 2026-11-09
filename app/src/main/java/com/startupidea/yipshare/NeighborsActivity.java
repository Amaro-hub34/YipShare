package com.startupidea.yipshare;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.adapters.NeighborAdapter;
import com.startupidea.yipshare.condominium.CondominiumService;
import com.startupidea.yipshare.condominium.Neighbor;
import com.startupidea.yipshare.condominium.SqliteCondominiumRepository;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.user.SessionRepository;

public class NeighborsActivity extends AppCompatActivity {

    private CondominiumService condominiumService;
    private String userId;
    private String condominiumId;
    private RecyclerView neighborList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_neighbors);

        Guideline guidelineHeader = findViewById(R.id.guideline_header);
        Guideline guidelineStatus = findViewById(R.id.guideline_status);
        int headerBase = (int) (48 * getResources().getDisplayMetrics().density);
        neighborList = findViewById(R.id.rv_vecinos);
        int listPadding = neighborList.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            guidelineStatus.setGuidelineBegin(bars.top);
            guidelineHeader.setGuidelineBegin(headerBase + bars.top);
            neighborList.setPadding(neighborList.getPaddingLeft(), neighborList.getPaddingTop(),
                    neighborList.getPaddingRight(), listPadding + bars.bottom);

            return insets;
        });
        findViewById(R.id.btn_regresar).setOnClickListener(v -> finish());

        condominiumService = new CondominiumService(new SqliteCondominiumRepository(new DatabaseDriver(this), this));
        SessionRepository session = new SessionRepository(this);
        userId = session.getUserId();
        condominiumId = session.getCondominiumId();
        showNeighbors();
    }

    private void showNeighbors() {
        try {
            neighborList.setAdapter(new NeighborAdapter(condominiumService.getAllNeighbors(userId, condominiumId),
                    new NeighborAdapter.OnNeighborAction() {
                        @Override
                        public void onNeighborClick(Neighbor neighbor) {
                            startActivity(UserFeedbackActivity.intentFor(NeighborsActivity.this, neighbor.getUserId()));
                        }

                        @Override
                        public void onLoanAccessChange(Neighbor neighbor, boolean allowed) {
                            confirmLoanAccess(neighbor, allowed);
                        }
                    }));
        } catch (AppException e) {
            Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void confirmLoanAccess(Neighbor neighbor, boolean allowed) {
        new AlertDialog.Builder(this)
                .setMessage(getString(allowed ? R.string.confirmar_dar_acceso : R.string.confirmar_quitar_acceso,
                        neighbor.getUsername()))
                .setPositiveButton(R.string.opcion_si, (dialog, which) -> {
                    try {
                        condominiumService.modifyLoanAccess(userId, neighbor.getUserId(), condominiumId, allowed);
                    } catch (AppException e) {
                        Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
                    }
                    showNeighbors();
                })
                .setNegativeButton(R.string.opcion_cancelar, (dialog, which) -> showNeighbors())
                .setOnCancelListener(dialog -> showNeighbors())
                .show();
    }
}
