package com.example.periodictable;

import android.content.Context;
import android.graphics.Color;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PeriodicTableView extends WebView {
    public interface Listener { void onElement(int z); }

    private final List<Element> elements;
    private Listener listener;
    private int selectedZ = 1;

    private static final Map<String, String> COLORS = new HashMap<>();
    static {
        COLORS.put("alkali metal", "#F7B7B7");
        COLORS.put("alkaline earth metal", "#F7D49B");
        COLORS.put("transition metal", "#F6C98D");
        COLORS.put("post-transition metal", "#FFF0A8");
        COLORS.put("metalloid", "#C5E6A6");
        COLORS.put("nonmetal", "#AEE4E8");
        COLORS.put("halogen", "#BFD7FF");
        COLORS.put("noble gas", "#D9C2F0");
        COLORS.put("lanthanide", "#F2B7D8");
        COLORS.put("actinide", "#D8B6A4");
    }

    public PeriodicTableView(Context context, List<Element> elements) {
        super(context);
        this.elements = elements;
        setBackgroundColor(Color.parseColor("#F4F6F8"));
        setHorizontalScrollBarEnabled(true);
        setVerticalScrollBarEnabled(false);
        WebSettings s = getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        addJavascriptInterface(new AndroidBridge(), "Android");
        setOverScrollMode(OVER_SCROLL_IF_CONTENT_SCROLLS);
        loadDataWithBaseURL(null, buildHtml(), "text/html", "UTF-8", null);
    }

    public int tableWidth() {
        return dp(1710);
    }

    public int tableHeight() {
        return dp(700);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    public void setOnElementClickListener(Listener listener) {
        this.listener = listener;
    }

    public void setSelectedZ(int z) {
        selectedZ = z;
        post(() -> evaluateJavascript(
                "window.selectElement && window.selectElement(" + z + ");", null
        ));
    }

    private final class AndroidBridge {
        @JavascriptInterface
        public void select(int z) {
            selectedZ = z;
            if (listener != null) {
                post(() -> listener.onElement(z));
            }
        }
    }

    private Element getElement(int z) {
        return elements.get(z - 1);
    }

    private boolean isF(int z) {
        return (z >= 58 && z <= 71) || (z >= 90 && z <= 103);
    }

    private String cell(int z) {
        Element e = getElement(z);
        String base = COLORS.containsKey(e.category) ? COLORS.get(e.category) : "#DDDDDD";
        String selected = z == selectedZ ? " selected" : "";
        return "<td class='cell" + selected + "' data-z='" + z + "' " +
                "style='--base:" + base + "' onclick='pick(" + z + ")'>" +
                "<div class='z'>" + e.z + "</div>" +
                "<div class='mass'>A=" + e.mass + "</div>" +
                "<div class='sym'>" + esc(e.symbol) + "</div>" +
                "<div class='gp'>G" + esc(e.groupText) + " P" + e.period + "</div>" +
                "</td>";
    }

    private String empty() {
        return "<td class='empty'></td>";
    }

    private String headerCell(String text) {
        return "<td class='head'>" + esc(text) + "</td>";
    }

    private String buildHtml() {
        StringBuilder h = new StringBuilder(30000);
        h.append("<!doctype html><html><head><meta charset='utf-8'>");
        h.append("<meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=yes'>");
        h.append("<style>");
        h.append("html,body{margin:0;padding:0;background:#F4F6F8;font-family:Arial,'Tahoma',sans-serif;}");
        h.append("body{min-width:1680px;padding:16px 10px 20px 10px;box-sizing:border-box;}");
        h.append("table{border-collapse:separate;border-spacing:4px;table-layout:fixed;width:1660px;}");
        h.append("td{box-sizing:border-box;}");
        h.append(".head{width:86px;height:28px;text-align:center;font-weight:700;color:#17324D;font-size:13px;}");
        h.append(".cell{width:86px;height:72px;border-radius:10px;border:2px solid #4BAFCC;");
        h.append("background:linear-gradient(160deg,#fff 0%,var(--base) 38%,#E7F6FA 100%);");
        h.append("position:relative;text-align:center;color:#24566A;font-weight:700;overflow:hidden;");
        h.append("box-shadow:0 3px 5px rgba(60,100,120,.20);}");
        h.append(".cell.selected{border:3px solid #17324D;box-shadow:0 0 0 3px #FFFFFF inset,0 3px 7px rgba(30,70,90,.32);}");
        h.append(".empty{width:86px;height:72px;border:0;background:transparent;}");
        h.append(".z{position:absolute;left:7px;top:4px;font-size:12px;color:#245A70;}");
        h.append(".mass{position:absolute;right:6px;top:5px;font-size:8px;color:#3A7185;}");
        h.append(".sym{margin-top:20px;font-size:19px;line-height:22px;color:#24566A;}");
        h.append(".gp{position:absolute;bottom:5px;left:0;right:0;font-size:8px;color:#4B7F91;}");
        h.append(".title{font-size:17px;font-weight:700;color:#17324D;padding:4px 2px 6px;text-align:right;}");
        h.append(".fblank{height:8px;}");
        h.append("</style></head><body>");

        h.append("<div class='title'>جدول تناوبی ۱۱۸ عنصر — برای مشاهدهٔ همهٔ ستون‌ها صفحه را افقی حرکت دهید</div>");
        h.append("<table><tr>");
        h.append(headerCell(""));
        for (int g = 1; g <= 18; g++) h.append(headerCell("G" + g));
        h.append("</tr>");

        for (int p = 1; p <= 7; p++) {
            h.append("<tr>");
            h.append(headerCell("P" + p));
            for (int g = 1; g <= 18; g++) {
                Element e = findMain(p, g);
                h.append(e == null ? empty() : cell(e.z));
            }
            h.append("</tr>");
        }

        h.append("<tr><td class='head'>Ln</td>");
        for (int g = 1; g <= 18; g++) {
            if (g < 4) h.append(empty());
            else h.append(cell(58 + (g - 4)));
        }
        h.append("</tr>");

        h.append("<tr><td class='head'>An</td>");
        for (int g = 1; g <= 18; g++) {
            if (g < 4) h.append(empty());
            else h.append(cell(90 + (g - 4)));
        }
        h.append("</tr></table>");

        h.append("<script>");
        h.append("function pick(z){");
        h.append("document.querySelectorAll('.cell.selected').forEach(function(x){x.classList.remove('selected');});");
        h.append("var e=document.querySelector(\'.cell[data-z="\'+z+\'"]\'); if(e)e.classList.add(\'selected\');");
        h.append("if(window.Android) Android.select(z);");
        h.append("}");
        h.append("function selectElement(z){pick(z);}");
        h.append("</script></body></html>");
        return h.toString();
    }

    private Element findMain(int period, int group) {
        for (Element e : elements) {
            if (isF(e.z)) continue;
            if (e.period == period && e.group == group) return e;
        }
        return null;
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
