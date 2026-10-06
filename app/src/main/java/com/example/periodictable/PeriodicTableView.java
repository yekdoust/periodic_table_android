package com.example.periodictable;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PeriodicTableView extends LinearLayout {
    public interface Listener { void onElement(int z); }

    private final List<Element> elements;
    private Listener listener;
    private int selectedZ = 1;

    private final float density;
    private final int cellW, cellH, gap, labelW;
    private final Map<Integer, TextView> cells = new HashMap<>();

    private static final Map<String, Integer> COLORS = new HashMap<>();
    static {
        COLORS.put("alkali metal", Color.parseColor("#F7B7B7"));
        COLORS.put("alkaline earth metal", Color.parseColor("#F7D49B"));
        COLORS.put("transition metal", Color.parseColor("#F6C98D"));
        COLORS.put("post-transition metal", Color.parseColor("#FFF0A8"));
        COLORS.put("metalloid", Color.parseColor("#C5E6A6"));
        COLORS.put("nonmetal", Color.parseColor("#AEE4E8"));
        COLORS.put("halogen", Color.parseColor("#BFD7FF"));
        COLORS.put("noble gas", Color.parseColor("#D9C2F0"));
        COLORS.put("lanthanide", Color.parseColor("#F2B7D8"));
        COLORS.put("actinide", Color.parseColor("#D8B6A4"));
    }

    public PeriodicTableView(Context context, List<Element> elements) {
        super(context);
        this.elements = elements;
        density = getResources().getDisplayMetrics().density;
        cellW = dp(86);
        cellH = dp(72);
        gap = dp(4);
        labelW = dp(42);
        setOrientation(VERTICAL);
        setGravity(Gravity.TOP | Gravity.START);
        setBackgroundColor(Color.parseColor("#F4F6F8"));
        setPadding(0, 0, 0, dp(8));
        buildTable();
    }

    private int dp(int v) { return Math.round(v * density); }

    public int tableWidth() { return labelW + 18 * cellW + 17 * gap; }
    public int tableHeight() { return dp(30) + 7 * cellH + 6 * gap + dp(18) + 2 * cellH + gap + dp(14); }

    private Element getElement(int z) { return elements.get(z - 1); }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(getContext());
        r.setOrientation(HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        r.setPadding(0, 0, 0, 0);
        return r;
    }

    private View spacer() {
        TextView v = new TextView(getContext());
        v.setBackgroundColor(Color.TRANSPARENT);
        return v;
    }

    private void addHeader() {
        LinearLayout r = row();
        TextView label = headerText("");
        r.addView(label, lp(labelW, dp(28)));

        for (int g = 1; g <= 18; g++) {
            TextView h = headerText("G" + g);
            LinearLayout.LayoutParams p = lp(cellW, dp(28));
            p.setMargins(g == 1 ? 0 : gap, 0, 0, 0);
            r.addView(h, p);
        }
        addView(r);
    }

    private TextView headerText(String s) {
        TextView t = new TextView(getContext());
        t.setText(s);
        t.setTextSize(9);
        t.setTextColor(Color.parseColor("#17324D"));
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private void addMainRow(int period) {
        LinearLayout r = row();

        TextView pl = headerText("P" + period);
        r.addView(pl, lp(labelW, cellH));

        Map<Integer, Element> byGroup = new HashMap<>();
        for (Element e : elements) {
            if (e.period == period && e.group >= 1 && e.group <= 18 && e.z != 58 && e.z != 59) {
                byGroup.put(e.group, e);
            }
        }

        for (int g = 1; g <= 18; g++) {
            Element e = byGroup.get(g);
            View child = e == null ? spacer() : makeCell(e);
            LinearLayout.LayoutParams p = lp(cellW, cellH);
            p.setMargins(g == 1 ? 0 : gap, 0, 0, 0);
            r.addView(child, p);
        }
        addView(r);
    }

    private void addFRow(String label, int startZ) {
        LinearLayout r = row();
        TextView pl = headerText(label);
        r.addView(pl, lp(labelW, cellH));

        for (int i = 0; i < 18; i++) {
            Element e = null;
            if (i >= 3) {
                int z = startZ + (i - 3);
                if (z <= startZ + 13) e = getElement(z);
            }
            View child = e == null ? spacer() : makeCell(e);
            LinearLayout.LayoutParams p = lp(cellW, cellH);
            p.setMargins(i == 0 ? 0 : gap, 0, 0, 0);
            r.addView(child, p);
        }
        addView(r);
    }

    private LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w, h);
    }

    private TextView makeCell(Element e) {
        TextView cell = new TextView(getContext());
        cell.setText(e.z + "\n" + e.symbol + "\nA=" + e.mass + "\nG" + e.groupText + " P" + e.period);
        cell.setTextSize(10);
        cell.setTextColor(Color.parseColor("#24566A"));
        cell.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        cell.setGravity(Gravity.CENTER);
        cell.setLineSpacing(0, 0.85f);
        cell.setPadding(dp(2), dp(2), dp(2), dp(2));
        cell.setBackground(cellBackground(e, e.z == selectedZ));
        cell.setContentDescription(e.nameFa + " (" + e.symbol + ")");
        cell.setOnClickListener(v -> {
            selectedZ = e.z;
            refreshSelection();
            if (listener != null) listener.onElement(e.z);
        });
        cells.put(e.z, cell);
        return cell;
    }

    private GradientDrawable cellBackground(Element e, boolean selected) {
        int base = COLORS.containsKey(e.category) ? COLORS.get(e.category) : Color.LTGRAY;
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(lighten(base, 0.30f));
        bg.setCornerRadius(dp(9));
        bg.setStroke(selected ? dp(3) : dp(1), selected ? Color.WHITE : Color.parseColor("#4BAFCC"));
        return bg;
    }

    private int lighten(int color, float amount) {
        return Color.rgb(
                Math.round(Color.red(color) + (255 - Color.red(color)) * amount),
                Math.round(Color.green(color) + (255 - Color.green(color)) * amount),
                Math.round(Color.blue(color) + (255 - Color.blue(color)) * amount)
        );
    }

    private void refreshSelection() {
        for (Element e : elements) {
            TextView c = cells.get(e.z);
            if (c != null) c.setBackground(cellBackground(e, e.z == selectedZ));
        }
    }

    public void setOnElementClickListener(Listener listener) {
        this.listener = listener;
    }

    public void setSelectedZ(int z) {
        selectedZ = z;
        refreshSelection();
    }

    private void buildTable() {
        addHeader();
        for (int p = 1; p <= 7; p++) addMainRow(p);
        addFRow("Ln", 58);
        addFRow("An", 90);
    }
}
