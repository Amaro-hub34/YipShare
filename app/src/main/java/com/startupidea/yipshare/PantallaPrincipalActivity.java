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
import androidx.appcompat.app.AlertDialog;
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

import com.startupidea.yipshare.adapters.CondominioAdapter;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.startupidea.yipshare.adapters.ArticuloAdapter;
import com.startupidea.yipshare.item.ItemService;
import com.startupidea.yipshare.item.LocalItemRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class PantallaPrincipalActivity extends AppCompatActivity {

    private static final String ITEMS_FILE = "items.dat";
    private static final int EXAMPLE_ITEMS = 2;

    private static final List<String> CONDOMINIUMS =
            Arrays.asList("Torres del Valle", "Residencial Los Pinos", "Condominio Reforma");

    private ItemService itemService;
    private RecyclerView myItemsList;
    private RecyclerView availableItemsList;
    private EditText searchField;
    private int selectedCondominium = 0;

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

        // The activity is recreated on rotation, so checking once here is enough.
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomNav.setVisibility(View.GONE);
            findViewById(R.id.tv_condominio).setVisibility(View.GONE);
            findViewById(R.id.tv_saludo).setVisibility(View.GONE);
        }

        boolean articlesExist = true;
        findViewById(R.id.group_empty_state).setVisibility(articlesExist ? View.GONE : View.VISIBLE);
        findViewById(R.id.group_content_state).setVisibility(articlesExist ? View.VISIBLE : View.GONE);

        itemService = new ItemService(new LocalItemRepository(new File(getFilesDir(), ITEMS_FILE)));
        myItemsList = findViewById(R.id.rv_mis_articulos);

        availableItemsList = findViewById(R.id.rv_articulos_disponibles);
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
                showItems();
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
        showItems();
    }

    private void showItems() {
        String query = searchField.getText().toString();
        // Example cards have no real data, so they only show while not searching.
        int examples = query.trim().isEmpty() ? EXAMPLE_ITEMS : 0;
        myItemsList.setAdapter(new ArticuloAdapter(itemService.searchItems(query), examples));
        availableItemsList.setAdapter(new ArticuloAdapter(Collections.emptyList(), examples));
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

        RecyclerView condominiumList = panel.findViewById(R.id.rv_condominios);
        condominiumList.setAdapter(new CondominioAdapter(CONDOMINIUMS, selectedCondominium,
                panel.findViewById(R.id.resaltado_condominio),
                position -> {
                    selectedCondominium = position;
                    ((TextView) findViewById(R.id.tv_condominio)).setText(CONDOMINIUMS.get(position));
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

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.confirmar_cerrar_sesion)
                .setPositiveButton(R.string.opcion_si, (d, which) -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.opcion_cancelar, null)
                .show();
    }
}
