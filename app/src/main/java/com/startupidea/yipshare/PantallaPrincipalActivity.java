package com.startupidea.yipshare;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import com.startupidea.yipshare.utils.ConfirmDialogUtil;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.startupidea.yipshare.adapters.CondominioAdapter;

import java.util.ArrayList;
import java.util.List;

import com.startupidea.yipshare.adapters.ArticuloAdapter;
import com.startupidea.yipshare.condominium.SqliteCondominiumRepository;
import com.startupidea.yipshare.condominium.CondominiumMembership;
import com.startupidea.yipshare.condominium.CondominiumService;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.item.SqliteItemRepository;
import com.startupidea.yipshare.item.ItemService;
import com.startupidea.yipshare.notification.SqliteNotificationRepository;
import com.startupidea.yipshare.notification.NotificationService;
import com.startupidea.yipshare.user.SqliteUserRepository;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.user.UserService;
import com.startupidea.yipshare.utils.TaskUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class PantallaPrincipalActivity extends AppCompatActivity {

    private ItemService itemService;
    private CondominiumService condominiumService;
    private NotificationService notificationService;
    private UserService userService;
    private SessionRepository session;
    private String userId;
    private List<CondominiumMembership> memberships = new ArrayList<>();
    private final ArticuloAdapter myItemsAdapter = new ArticuloAdapter();
    private final ArticuloAdapter availableItemsAdapter = new ArticuloAdapter();
    private EditText searchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_principal);

        Guideline guidelineStatus = findViewById(R.id.guideline_status);
        View bottomNav = findViewById(R.id.bottom_nav);
        int navBaseHeight = (int) (64 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            guidelineStatus.setGuidelineBegin(bars.top);

            bottomNav.setPadding(0, 0, 0, bars.bottom);
            ViewGroup.LayoutParams lp = bottomNav.getLayoutParams();
            lp.height = navBaseHeight + bars.bottom;
            bottomNav.setLayoutParams(lp);

            return insets;
        });

        showForOrientation(getResources().getConfiguration());

        boolean articlesExist = true;
        findViewById(R.id.group_empty_state).setVisibility(articlesExist ? View.GONE : View.VISIBLE);
        findViewById(R.id.group_content_state).setVisibility(articlesExist ? View.VISIBLE : View.GONE);

        session = new SessionRepository(this);
        itemService = new ItemService(new SqliteItemRepository(this));
        condominiumService = new CondominiumService(new SqliteCondominiumRepository(this));
        notificationService = new NotificationService(new SqliteNotificationRepository(this));
        userService = new UserService(new SqliteUserRepository(this));
        userId = session.getUserId();
        showGreeting();
        ((RecyclerView) findViewById(R.id.rv_mis_articulos)).setAdapter(myItemsAdapter);
        ((RecyclerView) findViewById(R.id.rv_articulos_disponibles)).setAdapter(availableItemsAdapter);
        searchField = findViewById(R.id.et_buscar);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadItems();
            }
        });

        findViewById(R.id.iv_agregar_top).setOnClickListener(v -> showAddMenu());

        findViewById(R.id.iv_notificaciones).setOnClickListener(v ->
                startActivity(new Intent(PantallaPrincipalActivity.this, NotificacionActivity.class)));

        findViewById(R.id.nav_prestamos).setOnClickListener(v ->
                startActivity(new Intent(PantallaPrincipalActivity.this, PrestamoActivity.class)));

        findViewById(R.id.nav_reportes).setOnClickListener(v ->
                startActivity(new Intent(PantallaPrincipalActivity.this, ReporteActivity.class)));

        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(PantallaPrincipalActivity.this, PerfilActivity.class)));
        findViewById(R.id.logo_yipshare).setOnClickListener(v -> showCondominiumPanel());
        findViewById(R.id.nav_agregar).setOnClickListener(v ->
                startActivity(new Intent(PantallaPrincipalActivity.this, FormularioArticuloActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCondominium();
    }

    private void showCondominium() {
        TaskUtil.run(this, () -> condominiumService.getAllMemberships(userId), loaded -> {
            memberships = loaded;
            showSelectedCondominium();
            loadItems();
            showNotificationDot();
        }, this::showError);
    }

    private void showSelectedCondominium() {
        CondominiumMembership selected = selectedMembership();
        if (selected == null && !memberships.isEmpty()) selected = memberships.get(0);
        session.modifyCondominiumId(selected == null ? null : selected.condominiumId());

        TextView condominiumLabel = findViewById(R.id.tv_condominio);
        if (selected == null) {
            condominiumLabel.setText(R.string.sin_condominio);
            return;
        }
        String role = getString(selected.admin() ? R.string.rol_admin_condominio : R.string.residente);
        condominiumLabel.setText(getString(R.string.condominio_con_rol, selected.name(), role));
    }

    private void showNotificationDot() {
        int pendingRequests = 0;
        for (CondominiumMembership membership : memberships) pendingRequests += membership.pendingNotifications();
        int requests = pendingRequests;
        TaskUtil.run(this, () -> notificationService.countUnread(), unread -> showNotificationDot(requests + unread),
                e -> showNotificationDot(requests));
    }

    private void showNotificationDot(int pending) {
        findViewById(R.id.punto_notificaciones).setVisibility(pending > 0 ? View.VISIBLE : View.GONE);
        findViewById(R.id.iv_notificaciones).setContentDescription(
                getString(pending > 0 ? R.string.notificaciones_pendientes : R.string.notification));
    }

    private CondominiumMembership selectedMembership() {
        String condominiumId = session.getCondominiumId();
        for (CondominiumMembership membership : memberships) {
            if (membership.condominiumId().equals(condominiumId)) return membership;
        }
        return null;
    }

    private void loadItems() {
        String condominiumId = session.getCondominiumId();
        String query = searchField.getText().toString();
        TaskUtil.run(this, () -> itemService.getAllItems(condominiumId, query), loaded -> {
            if (!query.equals(searchField.getText().toString())) return;
            myItemsAdapter.submitList(itemService.filterMyItems(loaded, userId));
            availableItemsAdapter.submitList(itemService.filterAvailableItems(loaded, userId));
        }, this::showError);
    }

    private void showError(AppException e) {
        Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
    }

    private void showGreeting() {
        TaskUtil.run(this, () -> userService.getUserProfile(userId).firstNames(),
                name -> ((TextView) findViewById(R.id.tv_saludo)).setText(getString(R.string.saludo, name)),
                e -> {
                    session.deleteSession();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
    }

    private void showAddMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.boton_agregar_sobreposicion, null);
        dialog.setContentView(sheet);

        ((View) sheet.getParent()).setBackgroundColor(Color.TRANSPARENT);

        sheet.findViewById(R.id.btn_sheet_crear).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(PantallaPrincipalActivity.this, RegistrarCondominioActivity.class));
        });

        sheet.findViewById(R.id.btn_sheet_unirse).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(PantallaPrincipalActivity.this, IngresoCondominioActivity.class));
        });

        dialog.show();
    }
    private void showCondominiumPanel() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View panel = getLayoutInflater().inflate(R.layout.activity_panel_condominios, null);
        dialog.setContentView(panel);

        panel.findViewById(R.id.btn_cerrar_sesion).setOnClickListener(v -> confirmLogout());

        View neighborsButton = panel.findViewById(R.id.btn_ver_vecinos);
        neighborsButton.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, NeighborsActivity.class));
        });
        View unitsButton = panel.findViewById(R.id.btn_ver_unidades);
        unitsButton.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, UnitsActivity.class));
        });
        View reportsButton = panel.findViewById(R.id.btn_ver_reportes_condominio);
        reportsButton.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, CondominiumFeedbackActivity.class));
        });
        View[] adminButtons = {neighborsButton, unitsButton, reportsButton};
        CondominiumMembership selected = selectedMembership();
        TextView codeLabel = panel.findViewById(R.id.tv_codigo_condominio);
        showAdminOptions(adminButtons, codeLabel, selected);

        RecyclerView condominiumList = panel.findViewById(R.id.rv_condominios);
        condominiumList.setAdapter(new CondominioAdapter(memberships, Math.max(0, memberships.indexOf(selected)),
                panel.findViewById(R.id.resaltado_condominio),
                position -> {
                    CondominiumMembership picked = memberships.get(position);
                    session.modifyCondominiumId(picked.condominiumId());
                    showAdminOptions(adminButtons, codeLabel, picked);
                    showSelectedCondominium();
                    loadItems();
                }));

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.START);
            window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }

        dialog.show();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        showForOrientation(newConfig);
    }

    private void showForOrientation(Configuration config) {
        int visibility = config.orientation == Configuration.ORIENTATION_LANDSCAPE ? View.GONE : View.VISIBLE;
        findViewById(R.id.bottom_nav).setVisibility(visibility);
        findViewById(R.id.tv_condominio).setVisibility(visibility);
        findViewById(R.id.tv_saludo).setVisibility(visibility);
    }

    private void showAdminOptions(View[] buttons, TextView codeLabel, CondominiumMembership membership) {
        boolean admin = membership != null && membership.admin();
        for (View button : buttons) button.setVisibility(admin ? View.VISIBLE : View.GONE);
        boolean hasCode = admin && membership.code() != null;
        codeLabel.setVisibility(hasCode ? View.VISIBLE : View.GONE);
        if (hasCode) codeLabel.setText(getString(R.string.codigo_condominio, membership.code()));
    }

    private void confirmLogout() {
        ConfirmDialogUtil.show(this, getString(R.string.confirmar_cerrar_sesion), () -> {
            session.deleteSession();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
