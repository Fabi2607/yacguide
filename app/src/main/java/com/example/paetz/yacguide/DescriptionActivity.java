package com.example.paetz.yacguide;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paetz.yacguide.database.Comment.RouteComment;
import com.example.paetz.yacguide.database.Route;
import com.example.paetz.yacguide.utils.DateUtils;
import com.example.paetz.yacguide.utils.IntentConstants;
import com.example.paetz.yacguide.utils.WidgetUtils;

public class DescriptionActivity extends TableActivity {

    private Route _route;
    private int _resultUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int routeId = getIntent().getIntExtra(IntentConstants.ROUTE_KEY, db.INVALID_ID);
        super.initialize(R.layout.activity_description);

        _route = db.routeDao().getRoute(routeId);
        final int routeStatusId = _route.getStatusId();
        if (routeStatusId > 1) {
            ((TextView) findViewById(R.id.infoTextView)).setText("Achtung: Der Weg ist " + _route.STATUS.get(routeStatusId));
        }
        _resultUpdated = IntentConstants.RESULT_NO_UPDATE;

        displayContent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Note: Once reset, _resultUpdated may not be set back to RESULT_NO_UPDATE again!
        if (resultCode == IntentConstants.RESULT_UPDATED) {
            _resultUpdated = resultCode;
            _route = db.routeDao().getRoute(_route.getId()); // update route instance
            ImageButton ascendsButton = findViewById(R.id.ascendsButton);
            ascendsButton.setVisibility(_route.getAscendCount() > 0 ? View.VISIBLE : View.INVISIBLE);
            Toast.makeText(this, "Begehungen aktualisiert", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void back(View v) {
        Intent resultIntent = new Intent();
        setResult(_resultUpdated, resultIntent);
        finish();
    }

    public void showComments(View v) {
        final Dialog dialog = prepareCommentDialog();

        LinearLayout layout = dialog.findViewById(R.id.commentLayout);
        for (final RouteComment comment : db.routeCommentDao().getAll(_route.getId())) {
            final int qualityId = comment.getQualityId();
            final int gradeId = comment.getGradeId();
            final int securityId = comment.getSecurityId();
            final int wetnessId = comment.getWetnessId();
            final String text = comment.getText();

            layout.addView(WidgetUtils.createHorizontalLine(this, 5));
            if (RouteComment.QUALITY_MAP.containsKey(qualityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Wegqualität:",
                        RouteComment.QUALITY_MAP.get(qualityId),
                        WidgetUtils.textFontSizeDp,
                        null,
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0));
            }
            if (RouteComment.GRADE_MAP.containsKey(gradeId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Schwierigkeit:",
                        RouteComment.GRADE_MAP.get(gradeId),
                        WidgetUtils.textFontSizeDp,
                        null,
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0));
            }
            if (RouteComment.SECURITY_MAP.containsKey(securityId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Absicherung:",
                        RouteComment.SECURITY_MAP.get(securityId),
                        WidgetUtils.textFontSizeDp,
                        null,
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0));
            }
            if (RouteComment.WETNESS_MAP.containsKey(wetnessId)) {
                layout.addView(WidgetUtils.createCommonRowLayout(this,
                        "Abtrocknung:",
                        RouteComment.WETNESS_MAP.get(wetnessId),
                        WidgetUtils.textFontSizeDp,
                        null,
                        Color.WHITE,
                        Typeface.NORMAL,
                        10, 10, 10, 0));
            }

            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    text,
                    "",
                    WidgetUtils.textFontSizeDp,
                    null,
                    Color.WHITE,
                    Typeface.NORMAL,
                    10, 10, 10, 10));
        }
    }

    public void enterAscend(View v) {
        Intent intent = new Intent(DescriptionActivity.this, AscendActivity.class);
        intent.putExtra(IntentConstants.ROUTE_KEY, _route.getId());
        startActivityForResult(intent, 0);
    }

    public void goToAscends(View v) {
        Intent intent = new Intent(DescriptionActivity.this, TourbookAscendActivity.class);
        intent.putExtra(IntentConstants.ROUTE_KEY, _route.getId());
        startActivityForResult(intent, 0);
    }

    @Override
    protected void displayContent() {
        findViewById(R.id.ascendsButton).setVisibility(_route.getAscendCount() > 0 ? View.VISIBLE : View.INVISIBLE);

        LinearLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();
        this.setTitle(_route.getName() + "   " + _route.getGrade());
        String firstAscendClimbers = _route.getFirstAscendLeader().isEmpty()
                ? "Erstbegeher unbekannt"
                : _route.getFirstAscendLeader();
        firstAscendClimbers += _route.getFirstAscendFollower().isEmpty()
                ? ""
                : ", " + _route.getFirstAscendFollower();
        final String firstAscendDate = _route.getFirstAscendDate().equals(DateUtils.UNKNOWN_DATE)
                ? "Datum unbekannt"
                : DateUtils.formatDate(_route.getFirstAscendDate());
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                firstAscendClimbers,
                firstAscendDate,
                WidgetUtils.infoFontSizeDp,
                null,
                Color.WHITE,
                Typeface.BOLD,
                20, 20, 20, 0));
        final String climbingType = _route.getTypeOfClimbing();
        if (!climbingType.isEmpty()) {
            layout.addView(WidgetUtils.createCommonRowLayout(this,
                    "Kletterei:",
                    climbingType,
                    WidgetUtils.infoFontSizeDp,
                    null,
                    Color.WHITE,
                    Typeface.NORMAL,
                    20, 20, 20, 0));
        }
        layout.addView(WidgetUtils.createCommonRowLayout(this,
                _route.getDescription(),
                "",
                WidgetUtils.tableFontSizeDp,
                null,
                Color.WHITE,
                Typeface.BOLD));
    }

    @Override
    protected void deleteContent() {}
}
