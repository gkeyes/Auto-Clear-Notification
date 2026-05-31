package com.auto.clear.notification;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(padding, padding, padding, padding);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView title = new TextView(this);
        title.setText(getString(R.string.app_name));
        title.setTextSize(22);

        TextView body = new TextView(this);
        body.setText("Enable this AP101 Xposed module in LSPosed or Vector, then scope it to the apps whose own non-ongoing notifications should be cleared when they regain focus.");
        body.setTextSize(15);
        body.setPadding(0, padding / 2, 0, 0);

        root.addView(title);
        root.addView(body);
        setContentView(root);
    }
}
