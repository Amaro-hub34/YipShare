package com.startupidea.yipshare.utils;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.adapters.ReportAdapter;
import com.startupidea.yipshare.adapters.ReviewAdapter;
import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.feedback.FeedbackService;
import com.startupidea.yipshare.feedback.Report;
import com.startupidea.yipshare.feedback.Review;
import com.startupidea.yipshare.feedback.ApiFeedbackRepository;

import java.util.List;

public final class FeedbackSectionUtil {

    private FeedbackSectionUtil() {
    }

    public static void attach(Activity activity, String userId) {
        FeedbackService feedbackService = new FeedbackService(new ApiFeedbackRepository(new ApiClient(activity), activity));
        TaskUtil.run(activity, () -> feedbackService.getAllReviewsByUserId(userId),
                reviews -> TaskUtil.run(activity, () -> feedbackService.getAllReportsByUserId(userId),
                        reports -> show(activity, reviews, reports, null),
                        e -> Toast.makeText(activity, e.getMessageRes(), Toast.LENGTH_SHORT).show()),
                e -> Toast.makeText(activity, e.getMessageRes(), Toast.LENGTH_SHORT).show());
    }

    public static void show(Activity activity, List<Review> reviews, List<Report> reports,
                            ReportAdapter.OnReportClick onReportClick) {
        Button reviewsButton = activity.findViewById(R.id.btn_ver_calificaciones);
        Button reportsButton = activity.findViewById(R.id.btn_ver_reportes);
        TextView emptyLabel = activity.findViewById(R.id.tv_lista_vacia);
        RecyclerView list = activity.findViewById(R.id.rv_feedback);
        list.setLayoutManager(new LinearLayoutManager(activity));
        boolean showingReports = list.getAdapter() instanceof ReportAdapter;

        reviewsButton.setOnClickListener(v -> {
            select(reviewsButton, reportsButton);
            show(list, emptyLabel, new ReviewAdapter(reviews), reviews.isEmpty(), R.string.sin_calificaciones);
        });
        reportsButton.setOnClickListener(v -> {
            select(reportsButton, reviewsButton);
            show(list, emptyLabel, new ReportAdapter(reports, onReportClick), reports.isEmpty(), R.string.sin_reportes);
        });
        (showingReports ? reportsButton : reviewsButton).performClick();
    }

    private static void select(Button selected, Button other) {
        selected.setBackgroundResource(R.drawable.bg_button_primary);
        other.setBackgroundResource(R.drawable.bg_button_secundary);
    }

    private static void show(RecyclerView list, TextView emptyLabel, RecyclerView.Adapter<?> adapter,
                             boolean empty, int emptyMessage) {
        list.setAdapter(adapter);
        emptyLabel.setText(emptyMessage);
        emptyLabel.setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}
