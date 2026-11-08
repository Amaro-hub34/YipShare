package com.startupidea.yipshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.startupidea.yipshare.condominium.SqliteCondominiumRepository;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.loan.InvalidCode;
import com.startupidea.yipshare.loan.Loan;
import com.startupidea.yipshare.loan.LoanService;
import com.startupidea.yipshare.loan.LoanStatus;
import com.startupidea.yipshare.loan.SqliteLoanRepository;
import com.startupidea.yipshare.user.SessionRepository;

import java.util.Random;

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

        loanService = new LoanService(
                new SqliteLoanRepository(new DatabaseDriver(this), this),
                new SqliteCondominiumRepository(new DatabaseDriver(this), this));
        loan = Loan.fromIntent(getIntent());
        TextView itemLabel = findViewById(R.id.tv_articulo_codigo);
        TextView codeLabel = findViewById(R.id.tv_codigo_generado);
        codeField = findViewById(R.id.et_codigo);

        itemLabel.setText(loan.getItemName());

        findViewById(R.id.btn_generar_codigo).setOnClickListener(v -> {
            String code = String.format("%06d", new Random().nextInt(1000000));
            codeLabel.setText(code);
            codeLabel.setVisibility(TextView.VISIBLE);
        });

        findViewById(R.id.btn_confirmar_codigo).setOnClickListener(v -> tryConfirmCode());
    }

    private void tryConfirmCode() {
        try {
            Loan updated = loanService.confirmCode(
                    new SessionRepository(this).getUserId(), loan, codeField.getText().toString().trim());
            Toast.makeText(this, R.string.codigo_confirmado, Toast.LENGTH_SHORT).show();
            if (updated.getStatus() == LoanStatus.FINISHED) {
                Intent review = new Intent(this, CalificarUsuarioActivity.class);
                updated.putInto(review);
                startActivity(review);
            }

            Intent result = new Intent();
            updated.putInto(result);
            setResult(Activity.RESULT_OK, result);
            finish();
        } catch (InvalidCode e) {
            codeField.setError(getString(e.getMessageRes()));
        } catch (AppException e) {
            Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
        }
    }
}
