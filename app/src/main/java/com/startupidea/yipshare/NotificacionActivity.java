package com.startupidea.yipshare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.condominium.SqliteCondominiumRepository;
import com.startupidea.yipshare.adapters.JoinRequestAdapter;
import com.startupidea.yipshare.adapters.NotificacionAdapter;
import com.startupidea.yipshare.condominium.CondominiumService;
import com.startupidea.yipshare.condominium.JoinRequest;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.loan.LoanRequest;
import com.startupidea.yipshare.loan.LoanService;
import com.startupidea.yipshare.loan.SqliteLoanRepository;
import com.startupidea.yipshare.user.SessionRepository;

import java.util.List;

public class NotificacionActivity extends AppCompatActivity {

    private LoanService loanService;
    private CondominiumService condominiumService;
    private String userId;
    private String condominiumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notificacion);

        Guideline guidelineHeader = findViewById(R.id.guideline_header);
        Guideline guidelineStatus = findViewById(R.id.guideline_status);
        View bottomNav = findViewById(R.id.bottom_nav);
        int headerBase = (int) (48 * getResources().getDisplayMetrics().density);
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



        findViewById(R.id.btn_regresar).setOnClickListener(v -> finish());
        findViewById(R.id.btn_regresar).setOnClickListener(v -> finish());
        findViewById(R.id.nav_inicio).setOnClickListener(v ->
                startActivity(new Intent(NotificacionActivity.this, PantallaPrincipalActivity.class)));

        findViewById(R.id.nav_reportes).setOnClickListener(v ->
                startActivity(new Intent(NotificacionActivity.this, ReporteActivity.class)));

        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(NotificacionActivity.this, PerfilActivity.class)));
        findViewById(R.id.nav_prestamos).setOnClickListener(v -> {
            startActivity(new Intent(NotificacionActivity.this, PrestamoActivity.class));

        });
        findViewById(R.id.nav_agregar).setOnClickListener(v ->
                startActivity(new Intent(NotificacionActivity.this, FormularioArticuloActivity.class)));

        loanService = new LoanService(
                new SqliteLoanRepository(new DatabaseDriver(this), this),
                new SqliteCondominiumRepository(new DatabaseDriver(this), this));
        condominiumService = new CondominiumService(new SqliteCondominiumRepository(new DatabaseDriver(this), this));
        SessionRepository session = new SessionRepository(this);
        userId = session.getUserId();
        condominiumId = session.getCondominiumId();
        showLoanRequests();
    }

    // Only the selected condominium's notifications: join requests if the user is its admin, loan requests for their items.
    private void showLoanRequests() {
        List<LoanRequest> requests;
        List<JoinRequest> joinRequests;
        try {
            requests = loanService.getAllLoanRequests(userId, condominiumId);
            joinRequests = condominiumService.getAllJoinRequests(userId, condominiumId);
        } catch (AppException e) {
            Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean notificationsExist = !requests.isEmpty() || !joinRequests.isEmpty();
        findViewById(R.id.tv_sin_notificaciones).setVisibility(notificationsExist ? View.GONE : View.VISIBLE);
        RecyclerView notificationList = findViewById(R.id.rv_notificaciones);
        notificationList.setVisibility(notificationsExist ? View.VISIBLE : View.GONE);
        notificationList.setAdapter(new ConcatAdapter(joinAdapter(joinRequests), new NotificacionAdapter(requests, new NotificacionAdapter.OnLoanRequestAction() {
            @Override
            public void onAccept(LoanRequest request) {
                confirm(getString(R.string.confirmar_aceptar_solicitud, request.getRequesterUsername(), request.getItemName()),
                        () -> loanService.acceptLoanRequest(userId, request.getLoanId()), R.string.solicitud_aceptada);
            }

            @Override
            public void onReject(LoanRequest request) {
                confirm(getString(R.string.confirmar_denegar_solicitud, request.getRequesterUsername(), request.getItemName()),
                        () -> loanService.rejectLoanRequest(userId, request.getLoanId()), R.string.solicitud_denegada);
            }

            @Override
            public void onRequesterClick(LoanRequest request) {
                startActivity(UserFeedbackActivity.intentFor(NotificacionActivity.this, request.getRequesterId()));
            }
        })));
    }

    private JoinRequestAdapter joinAdapter(List<JoinRequest> joinRequests) {
        return new JoinRequestAdapter(joinRequests, new JoinRequestAdapter.OnJoinRequestAction() {
            @Override
            public void onAccept(JoinRequest request) {
                confirm(getString(R.string.confirmar_aceptar_union, request.getUsername(), request.getCondominiumName()),
                        () -> condominiumService.acceptJoinRequest(userId, request.getUserId(), request.getCondominiumId()),
                        R.string.union_aceptada);
            }

            @Override
            public void onReject(JoinRequest request) {
                confirm(getString(R.string.confirmar_denegar_union, request.getUsername(), request.getCondominiumName()),
                        () -> condominiumService.rejectJoinRequest(userId, request.getUserId(), request.getCondominiumId()),
                        R.string.union_denegada);
            }

            @Override
            public void onRequesterClick(JoinRequest request) {
                startActivity(UserFeedbackActivity.intentFor(NotificacionActivity.this, request.getUserId()));
            }
        });
    }

    private void confirm(String message, Runnable answer, int doneMessage) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.opcion_si, (dialog, which) -> {
                    try {
                        answer.run();
                        Toast.makeText(this, doneMessage, Toast.LENGTH_SHORT).show();
                    } catch (AppException e) {
                        Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
                    }
                    showLoanRequests();
                })
                .setNegativeButton(R.string.opcion_cancelar, null)
                .show();
    }
}