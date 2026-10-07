package com.example.periodictable;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {

    private final int NAVY = Color.parseColor("#17324D");
    private final int BG = Color.parseColor("#F4F6F8");
    private static final int CYAN = Color.parseColor("#00BFFF");
    private static final int RED = Color.RED;

    private EditText search;
    private TextView selectedTitle, compactTv, fullTv, shellTv, ionTv, validTv, groupCalcTv, countTv;
    private List<Element> elements;
    private Map<Integer, String> ions;
    private TextView[] infoValues;
    private DiagnosticView tableCanvas;
    private final Map<Integer, TextView> tableCells = new HashMap<>();
    private int selectedZ = 1;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        // صفحه تشخیصی مستقل: بدون وابستگی به داده‌ها و Layoutهای قبلی
        setContentView(new DiagnosticView());
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private TextView textView(String text, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);
        t.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        t.setPadding(dp(8), dp(4), dp(8), dp(4));
        return t;
    }

    private GradientDrawable rounded(int color, int strokeColor, int strokeWidth, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp((int) radius));
        if (strokeWidth > 0) g.setStroke(dp(strokeWidth), strokeColor);
        return g;
    }

    private GradientDrawable cellBackground(Element e, boolean selected) {
        int base = categoryColor(e.category);
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{lighten(base, 0.58f), lighten(base, 0.22f), lighten(base, 0.43f)}
        );
        g.setCornerRadius(dp(9));
        g.setStroke(dp(selected ? 3 : 1), selected ? Color.WHITE : Color.parseColor("#4BAFCC"));
        return g;
    }

    private int categoryColor(String category) {
        if ("alkali metal".equals(category)) return Color.parseColor("#F7B7B7");
        if ("alkaline earth metal".equals(category)) return Color.parseColor("#F7D49B");
        if ("transition metal".equals(category)) return Color.parseColor("#F6C98D");
        if ("post-transition metal".equals(category)) return Color.parseColor("#FFF0A8");
        if ("metalloid".equals(category)) return Color.parseColor("#C5E6A6");
        if ("nonmetal".equals(category)) return Color.parseColor("#AEE4E8");
        if ("halogen".equals(category)) return Color.parseColor("#BFD7FF");
        if ("noble gas".equals(category)) return Color.parseColor("#D9C2F0");
        if ("lanthanide".equals(category)) return Color.parseColor("#F2B7D8");
        if ("actinide".equals(category)) return Color.parseColor("#D8B6A4");
        return Color.LTGRAY;
    }

    private int lighten(int color, float amount) {
        return Color.rgb(
                Math.round(Color.red(color) + (255 - Color.red(color)) * amount),
                Math.round(Color.green(color) + (255 - Color.green(color)) * amount),
                Math.round(Color.blue(color) + (255 - Color.blue(color)) * amount)
        );
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView header = textView("جدول تناوبی ۱۱۸ عنصر", 20, Color.WHITE, true);
        header.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        header.setBackgroundColor(NAVY);
        header.setPadding(dp(16), dp(10), dp(16), dp(10));
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(58)));

        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setPadding(dp(8), dp(6), dp(8), dp(3));

        search = new EditText(this);
        search.setHint("نماد، نام، عدد اتمی یا دسته");
        search.setTextSize(16);
        search.setSingleLine(true);
        search.setInputType(InputType.TYPE_CLASS_TEXT);
        search.setGravity(Gravity.RIGHT);
        search.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        searchRow.addView(search, new LinearLayout.LayoutParams(0, dp(50), 1));

        Button find = new Button(this);
        find.setText("پیدا کن");
        find.setOnClickListener(v -> searchElement());
        searchRow.addView(find, new LinearLayout.LayoutParams(dp(92), dp(50)));

        Button next = new Button(this);
        next.setText("بعدی");
        next.setOnClickListener(v -> nextElement());
        searchRow.addView(next, new LinearLayout.LayoutParams(dp(80), dp(50)));

        Button clear = new Button(this);
        clear.setText("پاک");
        clear.setOnClickListener(v -> {
            search.setText("");
            selectElement(1);
        });
        searchRow.addView(clear, new LinearLayout.LayoutParams(dp(72), dp(50)));

        root.addView(searchRow, new LinearLayout.LayoutParams(-1, dp(59)));

        LinearLayout statusRow = new LinearLayout(this);
        statusRow.setPadding(dp(10), 0, dp(10), dp(2));
        countTv = textView("", 12, Color.parseColor("#557080"), true);
        statusRow.addView(countTv, new LinearLayout.LayoutParams(-1, dp(28)));
        root.addView(statusRow);

        HorizontalScrollView tableScrollH = new HorizontalScrollView(this);
        tableScrollH.setFillViewport(false);
        tableScrollH.setBackgroundColor(BG);
        tableScrollH.setHorizontalScrollBarEnabled(true);
        tableScrollH.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        View tableRows = buildTableRows();
        HorizontalScrollView.LayoutParams tableRowsLp =
                new HorizontalScrollView.LayoutParams(dp(1710), dp(730));
        tableRowsLp.gravity = Gravity.TOP | Gravity.LEFT;
        tableScrollH.addView(tableRows, tableRowsLp);

        

        ScrollView detailScroll = new ScrollView(this);
        detailScroll.setFillViewport(true);
        detailScroll.setBackgroundColor(Color.WHITE);
        detailScroll.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(dp(8), dp(5), dp(8), dp(10));

        selectedTitle = textView("", 18, NAVY, true);
        selectedTitle.setGravity(Gravity.RIGHT);
        details.addView(selectedTitle, new LinearLayout.LayoutParams(-1, dp(40)));

        String[] labels = {"عدد اتمی", "عدد جرمی", "نماد", "نام لاتین", "نام فارسی", "دسته", "دوره", "گروه"};
        infoValues = new TextView[labels.length];
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.HORIZONTAL);
        info.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        for (int i = 0; i < labels.length; i++) {
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setGravity(Gravity.RIGHT);
            box.setPadding(dp(3), dp(2), dp(3), dp(2));
            box.setBackground(rounded(Color.parseColor("#F8FAFC"), Color.parseColor("#DDE6EC"), 1, 8));

            TextView lab = textView(labels[i], 9, Color.parseColor("#66717A"), true);
            infoValues[i] = textView("-", 12, Color.BLACK, true);
            box.addView(lab, new LinearLayout.LayoutParams(-1, dp(22)));
            box.addView(infoValues[i], new LinearLayout.LayoutParams(-1, dp(30)));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(55), 1);
            lp.setMargins(dp(2), 0, dp(2), 0);
            info.addView(box, lp);
        }
        details.addView(info, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView c1 = section("آرایش الکترونی فشرده", "#EEF3F7");
        details.addView(c1);
        compactTv = textView("", 17, CYAN, true);
        compactTv.setTextDirection(View.TEXT_DIRECTION_LTR);
        compactTv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        compactTv.setGravity(Gravity.LEFT);
        compactTv.setBackgroundColor(Color.parseColor("#EEF3F7"));
        details.addView(compactTv, new LinearLayout.LayoutParams(-1, dp(42)));

        TextView c2 = section("آرایش کامل — هستهٔ گاز نجیب قرمز، بخش باقی‌مانده آبی", "#F9F9F9");
        details.addView(c2);
        fullTv = textView("", 15, Color.DKGRAY, true);
        fullTv.setTextDirection(View.TEXT_DIRECTION_LTR);
        fullTv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        fullTv.setGravity(Gravity.LEFT);
        fullTv.setBackgroundColor(Color.parseColor("#F9F9F9"));
        details.addView(fullTv, new LinearLayout.LayoutParams(-1, dp(50)));

        shellTv = card("توزیع الکترون‌ها در لایه‌ها");
        details.addView(shellTv);
        ionTv = card("یون‌های رایج آموزشی");
        details.addView(ionTv);
        validTv = card("کنترل تعداد الکترون‌ها");
        details.addView(validTv);

        details.addView(section("محاسبهٔ آموزشی گروه از روی آرایش", "#F7F0FF"));
        groupCalcTv = textView("", 13, Color.parseColor("#66458A"), false);
        groupCalcTv.setBackgroundColor(Color.parseColor("#F7F0FF"));
        details.addView(groupCalcTv, new LinearLayout.LayoutParams(-1, dp(110)));

        detailScroll.addView(details);

        // جدول و جزئیات مستقیماً در ریشه قرار می‌گیرند؛
        // از ScrollView تو‌در‌تو جلوگیری می‌کنیم تا اندازه‌گیری خانه‌ها
        // در شبیه‌سازهای مختلف پایدار باشد.
        LinearLayout.LayoutParams tableLp =
                new LinearLayout.LayoutParams(-1, 0, 0.68f);
        tableLp.setMargins(0, 0, 0, dp(4));
        root.addView(tableScrollH, tableLp);

        LinearLayout.LayoutParams detailLp =
                new LinearLayout.LayoutParams(-1, 0, 0.32f);
        root.addView(detailScroll, detailLp);

        setContentView(root);

        countTv.setText("تعداد عناصر بارگذاری‌شده: " + elements.size() + " از 118");
    }

    private TextView section(String title, String bgColor) {
        TextView t = textView(title, 10, NAVY, true);
        t.setBackgroundColor(Color.parseColor(bgColor));
        t.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        return t;
    }

    private TextView card(String title) {
        TextView t = textView(title, 11, Color.parseColor("#66717A"), true);
        t.setBackground(rounded(Color.WHITE, Color.parseColor("#DDE6EC"), 1, 8));
        t.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        return t;
    }

    private View buildTableRows() {
        tableCanvas = new DiagnosticView();
        return tableCanvas;
    }

    private final class DiagnosticView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint t = new Paint(Paint.ANTI_ALIAS_FLAG);

        DiagnosticView() {
            super(MainActivity.this);
            setBackgroundColor(Color.WHITE);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);

            int w = getWidth();
            int h = getHeight();

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.BLACK);
            c.drawRect(0, 0, w, h, p);

            t.setColor(Color.WHITE);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setTextAlign(Paint.Align.CENTER);
            t.setTextSize(Math.max(24, w * 0.05f));
            c.drawText("PERIODIC TABLE TEST", w / 2f, h * 0.14f, t);

            float gap = Math.max(12, w * 0.02f);
            float boxW = (w - 4 * gap) / 3f;
            float boxH = Math.min(180, h * 0.32f);
            float y = h * 0.28f;

            drawBox(c, gap, y, boxW, boxH, Color.RED, "H");
            drawBox(c, gap * 2 + boxW, y, boxW, boxH, Color.GREEN, "He");
            drawBox(c, gap * 3 + boxW * 2, y, boxW, boxH, Color.BLUE, "Li");

            t.setColor(Color.YELLOW);
            t.setTextSize(Math.max(18, w * 0.035f));
            c.drawText("اگر این ۳ خانه دیده می‌شوند، موتور Canvas سالم است.",
                    w / 2f, y + boxH + h * 0.15f, t);

            t.setTextSize(Math.max(14, w * 0.025f));
            c.drawText("WIDTH=" + w + "   HEIGHT=" + h,
                    w / 2f, h - 30, t);
        }

        private void drawBox(Canvas c, float x, float y, float w, float h, int color, String label) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            c.drawRoundRect(x, y, x + w, y + h, 18, 18, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(6);
            p.setColor(Color.WHITE);
            c.drawRoundRect(x, y, x + w, y + h, 18, 18, p);

            t.setColor(Color.WHITE);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setTextAlign(Paint.Align.CENTER);
            t.setTextSize(Math.max(32, w * 0.28f));
            c.drawText(label, x + w / 2f, y + h / 2f + t.getTextSize() * 0.35f, t);
        }
    }

    private LinearLayout makeRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        return row;
    }

    private TextView labelCell(String text, int width, int height) {
        TextView t = textView(text, 9, NAVY, true);
        t.setGravity(Gravity.CENTER);
        t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        t.setLayoutParams(lp);
        return t;
    }

    private TextView emptyCell() {
        TextView t = new TextView(this);
        t.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(86), dp(72));
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        t.setLayoutParams(lp);
        return t;
    }

    private TextView makeElementCell(Element e) {
        TextView cell = new TextView(this);
        cell.setText(
                e.z + "\n" +
                e.symbol + "\n" +
                "A=" + e.mass + "\n" +
                "G" + e.groupText + "  P" + e.period
        );
        cell.setTextSize(10);
        cell.setTextColor(Color.parseColor("#24566A"));
        cell.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        cell.setGravity(Gravity.CENTER);
        cell.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        cell.setLineSpacing(0, 0.84f);
        cell.setPadding(dp(2), dp(2), dp(2), dp(2));
        cell.setBackground(cellBackground(e, e.z == selectedZ));
        cell.setContentDescription(e.nameFa + " (" + e.symbol + ")");

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(86), dp(72));
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        cell.setLayoutParams(lp);

        tableCells.put(e.z, cell);
        cell.setOnClickListener(v -> selectElement(e.z));
        return cell;
    }

    private void refreshTableSelection() {
        for (Element e : elements) {
            TextView cell = tableCells.get(e.z);
            if (cell != null) cell.setBackground(cellBackground(e, e.z == selectedZ));
        }
        if (tableCanvas != null) tableCanvas.invalidate();
    }

    private Element findByPeriodGroup(int period, int group) {
        for (Element e : elements) {
            if (e.period == period && e.group == group) {
                if ((e.z >= 58 && e.z <= 71) || (e.z >= 90 && e.z <= 103)) continue;
                return e;
            }
        }
        return null;
    }

    private Element element(int z) {
        return elements.get(z - 1);
    }

    private void selectElement(int z) {
        if (z < 1 || z > 118 || elements.size() < 118) return;

        selectedZ = z;
        Element e = element(z);

        selectedTitle.setText(e.symbol + " — " + e.name + " — " + e.nameFa);

        String[] values = {
                String.valueOf(e.z), String.valueOf(e.mass), e.symbol, e.name,
                e.nameFa, e.categoryFa, String.valueOf(e.period), e.groupText
        };
        for (int i = 0; i < values.length; i++) infoValues[i].setText(values[i]);

        compactTv.setText(prettyCompact(e.config));
        fullTv.setText(prettyFullColored(e.config));

        int[] shells = shellDistribution(e.config);
        shellTv.setText("توزیع الکترون‌ها در لایه‌ها\n" + shellText(shells));

        ionTv.setText("یون‌های رایج آموزشی\n" +
                ions.getOrDefault(z, "اطلاعات یون رایج در بانک آموزشی ثبت نشده است."));

        int count = electronCount(e.config);
        validTv.setText("کنترل تعداد الکترون‌ها\n" +
                (count == z ? "✓ صحیح — مجموع = " : "⚠ نیاز به بررسی — مجموع = ") +
                count + " الکترون");

        groupCalcTv.setText(groupCalculation(e));
        refreshTableSelection();
    }

    private void searchElement() {
        String q = search.getText().toString().trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return;

        for (Element e : elements) {
            if (q.equals(String.valueOf(e.z)) ||
                    q.equals(e.symbol.toLowerCase(Locale.ROOT)) ||
                    e.name.toLowerCase(Locale.ROOT).contains(q) ||
                    e.nameFa.contains(q) ||
                    e.categoryFa.contains(q)) {
                selectElement(e.z);
                return;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("نتیجه جست‌وجو")
                .setMessage("عنصری با این عبارت پیدا نشد.")
                .setPositiveButton("باشه", null)
                .show();
    }

    private void nextElement() {
        selectElement(selectedZ >= 118 ? 1 : selectedZ + 1);
    }

    private static String superscript(int n) {
        return Integer.toString(n)
                .replace("0", "⁰").replace("1", "¹").replace("2", "²").replace("3", "³")
                .replace("4", "⁴").replace("5", "⁵").replace("6", "⁶").replace("7", "⁷")
                .replace("8", "⁸").replace("9", "⁹");
    }

    private static String expand(String cfg) {
        String r = cfg;
        String[] cores = {"[He]", "[Ne]", "[Ar]", "[Kr]", "[Xe]", "[Rn]"};
        String[] exp = {
                "1s2",
                "1s2 2s2 2p6",
                "1s2 2s2 2p6 3s2 3p6",
                "1s2 2s2 2p6 3s2 3p6 4s2 3d10 4p6",
                "1s2 2s2 2p6 3s2 3p6 4s2 3d10 4p6 5s2 4d10 5p6",
                "1s2 2s2 2p6 3s2 3p6 4s2 3d10 4p6 5s2 4d10 5p6 6s2 4f14 5d10 6p6"
        };
        for (int i = 0; i < cores.length; i++) r = r.replace(cores[i], exp[i]);
        return r;
    }

    private static List<String> tokens(String cfg) {
        return Arrays.asList(expand(cfg).split(" "));
    }

    private static String prettyConfig(String cfg) {
        StringBuilder s = new StringBuilder();
        for (String tok : tokens(cfg)) {
            if (s.length() > 0) s.append(' ');
            String num = tok.replaceAll("[^0-9]", "");
            String base = tok.substring(0, tok.length() - num.length());
            s.append(base).append(superscript(Integer.parseInt(num)));
        }
        return s.toString();
    }

    private static String prettyCompact(String cfg) {
        StringBuilder s = new StringBuilder();
        for (String tok : cfg.split(" ")) {
            if (s.length() > 0) s.append(' ');
            if (tok.matches("\\[.*\\]")) {
                s.append(tok);
            } else {
                String num = tok.replaceAll("[^0-9]", "");
                String base = tok.substring(0, tok.length() - num.length());
                s.append(base).append(superscript(Integer.parseInt(num)));
            }
        }
        return s.toString();
    }

    private static CharSequence prettyFullColored(String cfg) {
        String full = prettyConfig(cfg);
        String core = "";
        for (String tok : cfg.split(" ")) {
            if (tok.matches("\\[.*\\]")) {
                core = prettyConfig(expand(tok));
                break;
            }
        }
        if (core.isEmpty()) return full;

        String[] parts = full.split(" ");
        int coreCount = core.split(" ").length;
        int chars = 0;
        for (int i = 0; i < Math.min(coreCount, parts.length); i++) {
            if (i > 0) chars++;
            chars += parts[i].length();
        }

        SpannableString ss = new SpannableString(full);
        ss.setSpan(new ForegroundColorSpan(RED), 0, Math.min(chars, ss.length()),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (chars < ss.length()) {
            ss.setSpan(new ForegroundColorSpan(CYAN), chars, ss.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }

    private static int electronCount(String cfg) {
        int c = 0;
        for (String tok : tokens(cfg)) {
            String n = tok.replaceAll("[^0-9]", "");
            c += Integer.parseInt(n);
        }
        return c;
    }

    private static int[] shellDistribution(String cfg) {
        int[] a = new int[7];
        for (String tok : tokens(cfg)) {
            if (tok.length() < 3) continue;
            int n = tok.charAt(0) - '0';
            String num = tok.replaceAll("[^0-9]", "");
            a[n - 1] += Integer.parseInt(num);
        }
        return a;
    }

    private static String shellText(int[] a) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (a[i] > 0) {
                if (s.length() > 0) s.append("   ");
                s.append("لایه ").append(i + 1).append(": ").append(a[i]);
            }
        }
        return s.toString();
    }

    private static String groupCalculation(Element e) {
        List<String> p = tokens(e.config);
        if (e.z == 2) {
            return "1) آرایش: 1s²\n2) لایهٔ ظرفیت کامل است (2 الکترون).\n" +
                    "3) هلیم استثناست و با وجود 1s² در گروه 18 قرار می‌گیرد.\n" +
                    "نتیجه: گروه 18 (گاز نجیب)";
        }

        int s = 0, pp = 0, d = 0;
        for (String tok : p) {
            String digits = tok.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) continue;
            int n = tok.charAt(0) - '0';
            char o = tok.charAt(1);
            int c = Integer.parseInt(digits);
            if (n == e.period && o == 's') s += c;
            if (n == e.period && o == 'p') pp += c;
            if (n == e.period - 1 && o == 'd') d += c;
        }

        if (e.category.equals("lanthanide") || e.category.equals("actinide"))
            return "1) آرایش مرتبط با بلوک f از دادهٔ عنصر استخراج شد.\n" +
                    "2) این عنصر در بلوک f قرار دارد.\n" +
                    "3) در این چیدمان گروه عددی مستقلی ندارد.\nنتیجه: بلوک f — گروه: —";

        if (pp > 0) {
            int g = 12 + pp;
            return "1) آرایش لایهٔ ظرفیت: " + e.period + "s" + s + " " + e.period + "p" + pp + "\n" +
                    "2) الکترون‌های لایهٔ ظرفیت = " + (s + pp) + "\n" +
                    "3) گروه = 12 + " + pp + " = " + g + "\nنتیجه: گروه " + g;
        }

        if (d > 0 && e.period >= 4) {
            int g = d + s;
            return "1) زیرلایه‌های مؤثر: " + (e.period - 1) + "d" + d + " " + e.period + "s" + s + "\n" +
                    "2) الکترون‌های d = " + d + " و s = " + s + "\n" +
                    "3) گروه = " + d + " + " + s + " = " + g + "\nنتیجه: گروه " + g;
        }

        if ((s == 1 || s == 2) && pp == 0 && d == 0)
            return "1) آرایش لایهٔ ظرفیت: " + e.period + "s" + s + "\n" +
                    "2) تعداد الکترون لایهٔ ظرفیت = " + s + "\n" +
                    "3) گروه = " + s + "\nنتیجه: گروه " + s;

        return "آرایش الکترونی این عنصر با قواعد سادهٔ s/p/d تعیین نشد.\n" +
                "گروه ثبت‌شده در جدول: " + e.groupText;
    }
}
