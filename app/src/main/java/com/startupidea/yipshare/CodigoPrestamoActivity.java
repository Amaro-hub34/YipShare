package com.startupidea.yipshare;

import com.startupidea.yipshare.animation_yipshare.LoadingOverlay;
import com.startupidea.yipshare.error.BadRequestException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.loan.ApiLoanRepository;
import com.startupidea.yipshare.loan.Loan;
import com.startupidea.yipshare.loan.LoanService;
import com.startupidea.yipshare.loan.LoanStatus;
import com.startupidea.yipshare.user.SessionRepository;

public class CodigoPrestamoActivity extends AppCompatActivity {

    private LoanService loanService;
    private Loan loan;
    private EditText codeField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_codigo_prestamo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, bars.bottom);
            return insets;
        });

        loanService = new LoanService(new ApiLoanRepository(new ApiClient(this), this));
        loan = Loan.fromIntent(getIntent());
        TextView itemLabel = findViewById(R.id.tv_articulo_codigo);
        TextView codeLabel = findViewById(R.id.tv_codigo_generado);
        codeField = findViewById(R.id.et_codigo);

        itemLabel.setText(loan.itemName());

        boolean lender = loan.lentByMe();
        int lenderOnly = lender ? View.VISIBLE : View.GONE;
        int borrowerOnly = lender ? View.GONE : View.VISIBLE;
        findViewById(R.id.btn_generar_codigo).setVisibility(lenderOnly);
        findViewById(R.id.tv_separador_codigo).setVisibility(View.GONE);
        codeField.setVisibility(borrowerOnly);
        findViewById(R.id.btn_confirmar_codigo).setVisibility(borrowerOnly);

        findViewById(R.id.btn_generar_codigo).setOnClickListener(v -> new LoadingOverlay(this).run(
                () -> loanService.createCode(loan), code -> {
                    codeLabel.setText(code);
                    codeLabel.setVisibility(View.VISIBLE);
                }, e -> Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show()));

        findViewById(R.id.btn_confirmar_codigo).setOnClickListener(v -> tryConfirmCode());
    }

    private void tryConfirmCode() {
        String userId = new SessionRepository(this).getUserId();
        String code = codeField.getText().toString();
        new LoadingOverlay(this).run(() -> loanService.confirmCode(userId, loan, code), this::showConfirmed, this::showCodeError);
    }

    private void showConfirmed(Loan updated) {
        Toast.makeText(this, R.string.codigo_confirmado, Toast.LENGTH_SHORT).show();
        if (updated.status() == LoanStatus.FINISHED) {
            Intent review = new Intent(this, CalificarUsuarioActivity.class);
            updated.putInto(review);
            startActivity(review);
        }

        Intent result = new Intent();
        updated.putInto(result);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    private void showCodeError(AppException e) {
        if (e instanceof BadRequestException) {
            codeField.setError(getString(e.getMessageRes()));
        } else {
            Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
        }
    }
}
