package com.example.periodictable;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PeriodicTableView extends FrameLayout {
    public interface Listener { void onElement(int z); }

    private final List<Element> elements;
    private Listener listener;
    private int selectedZ = 1;

    private final float density;
    private final int cellW, cellH, left, top, gap;
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
        left = dp(42);
        top = dp(34);
        gap = dp(4);
        setBackgroundColor(Color.parseColor("#F4F6F8"));
        setWillNotDraw(false);
        buildTable();
    }

    private int dp(int value) {
        return Math.round(value * density);
    }

    public int tableWidth() {
        return left + 18 * cellW + 17 * gap + dp(16);
    }

    public int tableHeight() {
        int fStart = top + 7 * (cellH + gap) + dp(18);
        return fStart + 2 * (cellH + gap) + dp(12);
    }

    private boolean isFBlock(int z) {
        return (z >= 58 && z <= 71) || (z >= 90 && z <= 103);
    }

    private int fIndex(int z) {
        return z < 72 ? z - 58 : z - 90;
    }

    private float xForGroup(int group) {
        return left + (group - 1) * (cellW + gap);
    }

    private float yForPeriod(int period) {
        return top + (period - 1) * (cellH + gap);
    }

    private void buildTable() {
        addHeaderLabels();

        for (Element e : elements) {
            if (isFBlock(e.z)) continue;
            addElementCell(e, xForGroup(e.group), yForPeriod(e.period));
        }

        int fStartY = top + 7 * (cellH + gap) + dp(18);
        addTextLabel("Ln", dp(18), fStartY + dp(27));
        addTextLabel("An", dp(18), fStartY + cellH + gap + dp(27));

        for (int i = 0; i < 14; i++) {
            Element lanthanide = findElement(58 + i);
            Element actinide = findElement(90 + i);
            addElementCell(lanthanide, xForGroup(i + 4), fStartY);
            addElementCell(actinide, xForGroup(i + 4), fStartY + cellH + gap);
        }
    }

    private void addHeaderLabels() {
        for (int g = 1; g <= 18; g++) {
            TextView t = new TextView(getContext());
            t.setText("G" + g);
            t.setTextSize(9);
            t.setTextColor(Color.parseColor("#17324D"));
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            t.setGravity(Gravity.CENTER);
            addView(t, lp(dp((int) xForGroup(g)), dp(1), cellW, dp(25)));
        }

        for (int p = 1; p <= 7; p++) {
            TextView t = new TextView(getContext());
            t.setText("P" + p);
            t.setTextSize(9);
            t.setTextColor(Color.parseColor("#17324D"));
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            t.setGravity(Gravity.CENTER);
            addView(t, lp(dp(1), (int) yForPeriod(p) + dp(12), dp(34), dp(28)));
        }
    }

    private void addTextLabel(String text, int x, int y) {
        TextView t = new TextView(getContext());
        t.setText(text);
        t.setTextSize(9);
        t.setTextColor(Color.parseColor("#17324D"));
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        addView(t, lp(x - dp(5), y - dp(8), dp(34), dp(30)));
    }

    private FrameLayout.LayoutParams lp(int x, int y, int w, int h) {
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(w, h);
        p.leftMargin = x;
        p.topMargin = y;
        return p;
    }

    private Element findElement(int z) {
        return elements.get(z - 1);
    }

    private GradientDrawable cellBackground(Element e, boolean selected) {
        int base = COLORS.containsKey(e.category) ? COLORS.get(e.category) : Color.LTGRAY;
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(lighten(base, 0.32f));
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

    private void addElementCell(Element e, float x, float y) {
        if (e == null) return;

        TextView cell = new TextView(getContext());
        cell.setText(
                e.z + "   A=" + e.mass + "\n" +
                e.symbol + "\n" +
                "G" + e.groupText + "  P" + e.period
        );
        cell.setTextSize(11);
        cell.setTextColor(Color.parseColor("#24566A"));
        cell.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        cell.setGravity(Gravity.CENTER);
        cell.setLineSpacing(0, 0.92f);
        cell.setPadding(dp(2), dp(3), dp(2), dp(3));
        cell.setBackground(cellBackground(e, e.z == selectedZ));
        cell.setContentDescription(e.nameFa + " (" + e.symbol + ")");
        cell.setOnClickListener(v -> {
            selectedZ = e.z;
            refreshSelection();
            if (listener != null) listener.onElement(e.z);
        });

        cells.put(e.z, cell);
        addView(cell, lp(Math.round(x), Math.round(y), cellW, cellH));
    }

    private void refreshSelection() {
        for (Element e : elements) {
            TextView cell = cells.get(e.z);
            if (cell != null) {
                cell.setBackground(cellBackground(e, e.z == selectedZ));
            }
        }
    }

    public void setOnElementClickListener(Listener listener) {
        this.listener = listener;
    }

    public void setSelectedZ(int z) {
        selectedZ = z;
        refreshSelection();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(tableWidth(), tableHeight());
        int childW = MeasureSpec.makeMeasureSpec(cellW, MeasureSpec.EXACTLY);
        int childH = MeasureSpec.makeMeasureSpec(cellH, MeasureSpec.EXACTLY);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getLayoutParams().width == cellW && child.getLayoutParams().height == cellH) {
                child.measure(childW, childH);
            } else {
                child.measure(
                        MeasureSpec.makeMeasureSpec(child.getLayoutParams().width, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, MeasureSpec.EXACTLY)
                );
            }
        }
    }
}
