package com.startupidea.yipshare;

import com.startupidea.yipshare.animation_yipshare.LoadingOverlay;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.startupidea.yipshare.utils.ConfirmDialogUtil;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.item.Item;
import com.startupidea.yipshare.item.ItemAvailability;
import com.startupidea.yipshare.item.ItemPayload;
import com.startupidea.yipshare.item.ItemService;
import com.startupidea.yipshare.item.LoanDuration;
import com.startupidea.yipshare.item.ApiItemRepository;
import com.startupidea.yipshare.condominium.CondominiumService;
import com.startupidea.yipshare.condominium.ApiCondominiumRepository;
import com.startupidea.yipshare.loan.LoanService;
import com.startupidea.yipshare.loan.ApiLoanRepository;
import com.startupidea.yipshare.utils.PhoneUtil;
import com.startupidea.yipshare.loan.Loan;
import com.startupidea.yipshare.loan.LoanStatus;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.user.ApiUserRepository;
import com.startupidea.yipshare.user.UserService;
import com.startupidea.yipshare.utils.ImagePreviewUtil;
import com.startupidea.yipshare.utils.TaskUtil;

import java.util.Arrays;
import java.util.Locale;

public class DetalleArticuloActivity extends AppCompatActivity {

    private static final int IMAGE_MAX_SIZE_PX = 1080;

    private final ActivityResultLauncher<Intent> codeScreen = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    showLoan(Loan.fromIntent(result.getData()));
                }
            });

    private final ActivityResultLauncher<Intent> editScreen = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    showItem(Item.fromIntent(result.getData()));
                }
            });

    private ItemService itemService;
    private CondominiumService condominiumService;
    private LoanService loanService;
    private UserService userService;
    private String userId;
    private Item item;
    private Loan loan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_articulo);

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
        ApiClient apiClient = new ApiClient(this);
        SessionRepository session = new SessionRepository(this);
        itemService = new ItemService(new ApiItemRepository(apiClient, this));
        condominiumService = new CondominiumService(new ApiCondominiumRepository(apiClient, this));
        loanService = new LoanService(new ApiLoanRepository(apiClient, this));
        userService = new UserService(new ApiUserRepository(apiClient, session, this));
        userId = session.getUserId();

        Item received = Item.fromIntent(getIntent());
        if (received != null) showItem(received);

        Loan receivedLoan = Loan.fromIntent(getIntent());
        if (receivedLoan != null) showLoan(receivedLoan);

        findViewById(R.id.btn_reportar_vecino).setOnClickListener(v -> {
            Intent intent = new Intent(this, ReporteActivity.class);
            loan.putInto(intent);
            startActivity(intent);
        });
        findViewById(R.id.btn_introducir_codigo).setOnClickListener(v -> {
            boolean finished = loan.getStatus() == LoanStatus.FINISHED;
            Intent intent = new Intent(this, finished ? CalificarUsuarioActivity.class : CodigoPrestamoActivity.class);
            loan.putInto(intent);
            codeScreen.launch(intent);
        });

        findViewById(R.id.btn_editar_articulo).setOnClickListener(v -> {
            Intent intent = new Intent(this, FormularioArticuloActivity.class);
            item.putInto(intent);
            editScreen.launch(intent);
        });
        findViewById(R.id.btn_eliminar_articulo).setOnClickListener(v -> confirmDeleteItem());

        findViewById(R.id.btn_accion_articulo).setOnClickListener(v -> confirmRequestLoan());

        findViewById(R.id.btn_regresar).setOnClickListener(v -> finish());
        findViewById(R.id.nav_inicio).setOnClickListener(v ->
                startActivity(new Intent(DetalleArticuloActivity.this, PantallaPrincipalActivity.class)));

        findViewById(R.id.nav_reportes).setOnClickListener(v ->
                startActivity(new Intent(DetalleArticuloActivity.this, ReporteActivity.class)));

        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(DetalleArticuloActivity.this, PerfilActivity.class)));
        findViewById(R.id.nav_prestamos).setOnClickListener(v -> {
            startActivity(new Intent(DetalleArticuloActivity.this, PrestamoActivity.class));

        });
        findViewById(R.id.nav_agregar).setOnClickListener(v ->
                startActivity(new Intent(DetalleArticuloActivity.this, FormularioArticuloActivity.class)));

    }

    private void showItem(Item item) {
        this.item = item;
        item.putInto(getIntent());
        ItemPayload payload = item.getPayload();

        TextView statusLabel = findViewById(R.id.tv_estado_articulo);
        statusLabel.setText(item.getAvailability().getMessageRes());
        statusLabel.setBackgroundResource(item.getAvailability().getBadgeRes());
        showOwner(item.getOwnerId());
        findViewById(R.id.layout_prestamista).setOnClickListener(v ->
                startActivity(UserFeedbackActivity.intentFor(this, item.getOwnerId())));

        boolean own = item.isOwnedBy(userId);
        findViewById(R.id.btn_accion_articulo).setVisibility(own ? View.GONE : View.VISIBLE);
        showOwnerActions(item, false);
        if (!own) {
            TaskUtil.run(this, () -> condominiumService.isAdmin(userId, item.getCondominiumId()),
                    admin -> showOwnerActions(item, admin), e -> showOwnerActions(item, false));
        }

        if (payload.getImageUri() != null) {
            ((ImageView) findViewById(R.id.iv_imagen_articulo)).setImageBitmap(
                    ImagePreviewUtil.tryLoad(this, Uri.parse(payload.getImageUri()), IMAGE_MAX_SIZE_PX));
        }
        ((TextView) findViewById(R.id.tv_nombre_articulo)).setText(payload.getName());
        ((TextView) findViewById(R.id.tv_categoria)).setText(
                getString(R.string.DA_categoria_estado, payload.getCategory(), payload.getPhysicalCondition()));
        ((TextView) findViewById(R.id.tv_descripcion)).setText(payload.getDescription());
        ((Spinner) findViewById(R.id.sp_estado_fisico)).setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, payload.getLoanDurations()));
    }

    private void showOwnerActions(Item item, boolean admin) {
        boolean own = item.isOwnedBy(userId);
        boolean editable = own && item.getAvailability() == ItemAvailability.AVAILABLE;
        Button deleteButton = findViewById(R.id.btn_eliminar_articulo);
        findViewById(R.id.layout_acciones_dueno).setVisibility(own || admin ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_editar_articulo).setVisibility(own ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_editar_articulo).setEnabled(editable);
        deleteButton.setEnabled(editable || admin);
        deleteButton.setText(admin ? R.string.eliminar_publicacion : R.string.eliminar_articulo);
        findViewById(R.id.tv_aviso_no_editable).setVisibility(own && !editable ? View.VISIBLE : View.GONE);
    }

    private void showOwner(String ownerId) {
        TaskUtil.run(this, () -> userService.getUserProfile(ownerId), owner -> {
            ((TextView) findViewById(R.id.tv_prestamista)).setText(owner.getUsername());
            ((RatingBar) findViewById(R.id.rb_calificacion)).setRating(owner.getRating());
            ((TextView) findViewById(R.id.tv_calificacion_numero))
                    .setText(String.format(Locale.getDefault(), "%.1f", owner.getRating()));
            PhoneUtil.attach(findViewById(R.id.tv_telefono_prestamista), owner.getPhoneNumber());
        }, e -> findViewById(R.id.layout_prestamista).setVisibility(View.GONE));
    }

    private void confirmRequestLoan() {
        Spinner durationSpinner = findViewById(R.id.sp_estado_fisico);
        Object picked = durationSpinner.getSelectedItem();
        if (picked == null) return;
        String duration = picked.toString();
        int days = LoanDuration.daysOf(Arrays.asList(getResources().getStringArray(R.array.tiempo_prestamo)).indexOf(duration));

        ConfirmDialogUtil.show(this,
                getString(R.string.confirmar_solicitar_articulo, item.getPayload().getName(), duration),
                () -> new LoadingOverlay(this).run(
                        () -> loanService.requestLoan(userId, item, getString(R.string.texto_solicitud, duration), days),
                        () -> Toast.makeText(this, R.string.solicitud_enviada, Toast.LENGTH_SHORT).show(),
                        e -> Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_LONG).show()));
    }

    private void confirmDeleteItem() {
        ConfirmDialogUtil.show(this, getString(R.string.confirmar_eliminar_articulo), this::tryDeleteItem);
    }

    private void tryDeleteItem() {
        new LoadingOverlay(this).run(() -> itemService.deleteItem(userId, item), () -> {
            Toast.makeText(this, R.string.articulo_eliminado, Toast.LENGTH_SHORT).show();
            finish();
        }, e -> Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show());
    }

    private void showLoan(Loan loan) {
        this.loan = loan;
        loan.putInto(getIntent());

        if (loan.getImageUri() != null) {
            ((ImageView) findViewById(R.id.iv_imagen_articulo)).setImageBitmap(
                    ImagePreviewUtil.tryLoad(this, Uri.parse(loan.getImageUri()), IMAGE_MAX_SIZE_PX));
        }
        ((TextView) findViewById(R.id.tv_titulo_pantalla)).setText(R.string.DP_tv_titulo_pantalla);
        TextView statusLabel = findViewById(R.id.tv_estado_articulo);
        statusLabel.setText(loan.getStatus().getMessageRes());
        statusLabel.setBackgroundResource(loan.getStatus().getBadgeRes());
        ((TextView) findViewById(R.id.tv_nombre_articulo)).setText(loan.getItemName());
        ((TextView) findViewById(R.id.tv_descripcion)).setText(loan.getDescription());
        showOwner(loan.getOtherUserId());
        findViewById(R.id.layout_prestamista).setOnClickListener(v ->
                startActivity(UserFeedbackActivity.intentFor(this, loan.getOtherUserId())));

        String startDate = loan.getStartDate() == null ? getString(R.string.sin_iniciar) : loan.getStartDate();
        TextView datesLabel = findViewById(R.id.tv_fechas_prestamo);
        datesLabel.setText(getString(R.string.fechas_prestamo, startDate, loan.getEndDate()));
        datesLabel.setVisibility(View.VISIBLE);

        findViewById(R.id.tv_categoria).setVisibility(View.GONE);
        findViewById(R.id.tv_titulo_tiempo).setVisibility(View.GONE);
        findViewById(R.id.sp_estado_fisico).setVisibility(View.GONE);
        findViewById(R.id.btn_accion_articulo).setVisibility(View.GONE);
        findViewById(R.id.layout_acciones_prestamo).setVisibility(View.VISIBLE);
        Button codeButton = findViewById(R.id.btn_introducir_codigo);
        boolean finished = loan.getStatus() == LoanStatus.FINISHED;
        codeButton.setText(finished ? R.string.btn_calificar_vecino : R.string.btn_introducir_codigo);
        codeButton.setVisibility(finished || loan.getStatus().acceptsCode() ? View.VISIBLE : View.GONE);
    }
}
