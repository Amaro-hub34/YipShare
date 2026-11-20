package com.startupidea.yipshare;

import com.startupidea.yipshare.animation_yipshare.LoadingOverlay;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import com.startupidea.yipshare.utils.ConfirmDialogUtil;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AlertDialog;

import com.startupidea.yipshare.adapters.InboxAdapter;
import com.startupidea.yipshare.adapters.JoinRequestAdapter;
import com.startupidea.yipshare.adapters.NotificacionAdapter;
import com.startupidea.yipshare.condominium.SqliteCondominiumRepository;
import com.startupidea.yipshare.condominium.CondominiumService;
import com.startupidea.yipshare.condominium.JoinRequest;
import com.startupidea.yipshare.condominium.Unit;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.loan.SqliteLoanRepository;
import com.startupidea.yipshare.loan.LoanRequest;
import com.startupidea.yipshare.loan.LoanService;
import com.startupidea.yipshare.notification.SqliteNotificationRepository;
import com.startupidea.yipshare.notification.Notification;
import com.startupidea.yipshare.notification.NotificationService;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.utils.TaskUtil;

import java.util.List;

public class NotificacionActivity extends AppCompatActivity {

    private LoanService loanService;
    private CondominiumService condominiumService;
    private NotificationService notificationService;
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

        loanService = new LoanService(new SqliteLoanRepository(this));
        condominiumService = new CondominiumService(new SqliteCondominiumRepository(this));
        notificationService = new NotificationService(new SqliteNotificationRepository(this));
        SessionRepository session = new SessionRepository(this);
        userId = session.getUserId();
        condominiumId = session.getCondominiumId();
        showLoanRequests();
    }

    private void showLoanRequests() {
        TaskUtil.run(this, () -> new Inbox(
                        loanService.getAllLoanRequests(userId, condominiumId),
                        condominiumService.getAllJoinRequests(userId, condominiumId),
                        notificationService.getAllNotifications()),
                this::showInbox, this::showError);
    }

    private void showInbox(Inbox inbox) {
        List<LoanRequest> requests = inbox.loanRequests;
        List<JoinRequest> joinRequests = inbox.joinRequests;
        boolean notificationsExist = !requests.isEmpty() || !joinRequests.isEmpty() || !inbox.notifications.isEmpty();
        findViewById(R.id.tv_sin_notificaciones).setVisibility(notificationsExist ? View.GONE : View.VISIBLE);
        RecyclerView notificationList = findViewById(R.id.rv_notificaciones);
        notificationList.setVisibility(notificationsExist ? View.VISIBLE : View.GONE);
        notificationList.setAdapter(new ConcatAdapter(joinAdapter(joinRequests), new NotificacionAdapter(requests, new NotificacionAdapter.OnLoanRequestAction() {
            @Override
            public void onAccept(LoanRequest request) {
                confirm(getString(R.string.confirmar_aceptar_solicitud, request.requesterUsername(), request.itemName()),
                        () -> loanService.acceptLoanRequest(userId, request.loanId()), R.string.solicitud_aceptada);
            }

            @Override
            public void onReject(LoanRequest request) {
                confirm(getString(R.string.confirmar_denegar_solicitud, request.requesterUsername(), request.itemName()),
                        () -> loanService.rejectLoanRequest(userId, request.loanId()), R.string.solicitud_denegada);
            }

            @Override
            public void onRequesterClick(LoanRequest request) {
                startActivity(UserFeedbackActivity.intentFor(NotificacionActivity.this, request.requesterId()));
            }
        }), inboxAdapter(inbox.notifications)));
    }

    private InboxAdapter inboxAdapter(List<Notification> notifications) {
        return new InboxAdapter(notifications, new InboxAdapter.OnNotificationAction() {
            @Override
            public void onRead(Notification notification) {
                TaskUtil.run(NotificacionActivity.this, () -> notificationService.markAsRead(notification),
                        NotificacionActivity.this::showLoanRequests, NotificacionActivity.this::showError);
            }

            @Override
            public void onDelete(Notification notification) {
                confirm(getString(R.string.confirmar_eliminar_aviso, notification.header()),
                        () -> notificationService.deleteNotification(notification), R.string.aviso_eliminado);
            }
        });
    }

    private JoinRequestAdapter joinAdapter(List<JoinRequest> joinRequests) {
        return new JoinRequestAdapter(joinRequests, new JoinRequestAdapter.OnJoinRequestAction() {
            @Override
            public void onAccept(JoinRequest request) {
                if (request.suggestedUnitId() != null) {
                    confirm(getString(R.string.confirmar_aceptar_union, request.username(), request.condominiumName()),
                            () -> condominiumService.acceptJoinRequest(userId, request, null), R.string.union_aceptada);
                    return;
                }
                TaskUtil.run(NotificacionActivity.this, () -> condominiumService.getAllUnits(request.condominiumId()),
                        units -> pickUnit(request, units), NotificacionActivity.this::showError);
            }

            @Override
            public void onReject(JoinRequest request) {
                confirm(getString(R.string.confirmar_denegar_union, request.username(), request.condominiumName()),
                        () -> condominiumService.rejectJoinRequest(userId, request.userId(), request.condominiumId()),
                        R.string.union_denegada);
            }

            @Override
            public void onRequesterClick(JoinRequest request) {
                startActivity(UserFeedbackActivity.intentFor(NotificacionActivity.this, request.userId()));
            }
        });
    }

    private void pickUnit(JoinRequest request, List<Unit> units) {
        if (units.isEmpty()) {
            Toast.makeText(this, R.string.sin_unidades, Toast.LENGTH_LONG).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.elegir_unidad, request.username(), request.unit()))
                .setItems(units.stream().map(Unit::name).toArray(String[]::new), (dialog, which) ->
                        answer(() -> condominiumService.acceptJoinRequest(userId, request, units.get(which).id()),
                                R.string.union_aceptada))
                .setNegativeButton(R.string.opcion_cancelar, null)
                .show();
    }

    private void confirm(String message, Runnable work, int doneMessage) {
        ConfirmDialogUtil.show(this, message, () -> answer(work, doneMessage));
    }

    private void answer(Runnable work, int doneMessage) {
        new LoadingOverlay(this).run(work, () -> {
            Toast.makeText(this, doneMessage, Toast.LENGTH_SHORT).show();
            showLoanRequests();
        }, e -> {
            showError(e);
            showLoanRequests();
        });
    }

    private void showError(AppException e) {
        Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
    }

    private static final class Inbox {
        final List<LoanRequest> loanRequests;
        final List<JoinRequest> joinRequests;
        final List<Notification> notifications;

        Inbox(List<LoanRequest> loanRequests, List<JoinRequest> joinRequests, List<Notification> notifications) {
            this.loanRequests = loanRequests;
            this.joinRequests = joinRequests;
            this.notifications = notifications;
        }
    }
}