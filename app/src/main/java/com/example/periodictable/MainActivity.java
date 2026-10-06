package com.example.periodictable;

    import android.app.*;
    import android.os.Bundle;
    import android.graphics.Color;
    import android.graphics.Typeface;
    import android.text.InputType;
    import android.view.*;
    import android.widget.*;
    import android.graphics.drawable.GradientDrawable;
    import android.text.SpannableString;
    import android.text.Spanned;
    import android.text.style.ForegroundColorSpan;
    import java.io.IOException;
    import java.util.*;

    public class MainActivity extends Activity {
        private final int NAVY=Color.parseColor("#17324D");
        private final int BG=Color.parseColor("#F4F6F8");
        private static final int CYAN=Color.parseColor("#00BFFF");
        private static final int RED=Color.RED;
        private EditText search;
        private TextView selectedTitle, compactTv, fullTv, shellTv, ionTv, validTv, groupCalcTv;
        private PeriodicTableView table;
        private List<Element> elements;
        private Map<Integer,String> ions;
        private int selectedZ=1;

        @Override protected void onCreate(Bundle state) {
            super.onCreate(state);
            getWindow().setStatusBarColor(NAVY);
            getWindow().setNavigationBarColor(Color.BLACK);
            try {
                elements=ElementRepository.loadElements(this);
                ions=ElementRepository.loadIons(this);
            } catch(IOException e) { throw new RuntimeException(e); }
            buildUi();
            selectElement(1);
        }

        private int dp(float v) { return Math.round(v*getResources().getDisplayMetrics().density); }
        private TextView tv(String text, float sp, int color, boolean bold) {
            TextView t=new TextView(this); t.setText(text); t.setTextSize(sp); t.setTextColor(color);
            t.setTypeface(Typeface.DEFAULT, bold?Typeface.BOLD:Typeface.NORMAL);
            t.setPadding(dp(8),dp(5),dp(8),dp(5)); t.setTextDirection(View.TEXT_DIRECTION_RTL); t.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END); return t;
        }
        private GradientDrawable bg(int color, float radius) { GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(radius)); return g; }

        private void buildUi() {
            LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG); root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

            TextView header=tv("جدول تناوبی ۱۱۸ عنصر",20,Color.WHITE,true); header.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT); header.setBackgroundColor(NAVY); header.setPadding(dp(16),dp(12),dp(16),dp(12));
            root.addView(header,new LinearLayout.LayoutParams(-1,dp(58)));

            LinearLayout searchRow=new LinearLayout(this); searchRow.setOrientation(LinearLayout.HORIZONTAL); searchRow.setPadding(dp(8),dp(7),dp(8),dp(4));
            search=new EditText(this); search.setHint("نماد، نام، عدد اتمی یا دسته"); search.setTextSize(16); search.setSingleLine(true); search.setInputType(InputType.TYPE_CLASS_TEXT); search.setGravity(Gravity.RIGHT); search.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            searchRow.addView(search,new LinearLayout.LayoutParams(0,dp(52),1));
            Button find=new Button(this); find.setText("پیدا کن"); find.setOnClickListener(v->searchElement()); searchRow.addView(find,new LinearLayout.LayoutParams(dp(95),dp(52)));
            Button next=new Button(this); next.setText("بعدی"); next.setOnClickListener(v->nextElement()); searchRow.addView(next,new LinearLayout.LayoutParams(dp(82),dp(52)));
            Button clear=new Button(this); clear.setText("پاک"); clear.setOnClickListener(v->{search.setText("");selectElement(1);}); searchRow.addView(clear,new LinearLayout.LayoutParams(dp(75),dp(52)));
            root.addView(searchRow);

            TextView hint=tv("روی هر خانه لمس کنید تا اطلاعات عنصر و محاسبهٔ آموزشی گروه نمایش داده شود.",13,Color.parseColor("#557080"),false); hint.setPadding(dp(12),0,dp(12),dp(5)); root.addView(hint,new LinearLayout.LayoutParams(-1,dp(34)));

            HorizontalScrollView hsv=new HorizontalScrollView(this); hsv.setFillViewport(false); hsv.setBackgroundColor(BG); hsv.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            table=new PeriodicTableView(this,elements); table.setOnElementClickListener(this::selectElement);
            hsv.addView(table,new HorizontalScrollView.LayoutParams(table.tableWidth(),-2));
            root.addView(hsv,new LinearLayout.LayoutParams(-1,0,1));

            ScrollView detailsScroll=new ScrollView(this); detailsScroll.setBackgroundColor(Color.WHITE); detailsScroll.setFillViewport(false); detailsScroll.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            LinearLayout details=new LinearLayout(this); details.setOrientation(LinearLayout.VERTICAL); details.setPadding(dp(10),dp(8),dp(10),dp(12));
            selectedTitle=tv("",18,NAVY,true); selectedTitle.setGravity(Gravity.RIGHT); details.addView(selectedTitle);

            LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.HORIZONTAL); info.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            String[] labs={"عدد اتمی","عدد جرمی","نماد","نام لاتین","نام فارسی","دسته","دوره","گروه"};
            TextView[] values=new TextView[8];
            for(int i=0;i<8;i++){ LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setBackground(bg(Color.parseColor("#F8FAFC"),10)); box.setPadding(dp(4),dp(2),dp(4),dp(2)); TextView l=tv(labs[i],11,Color.parseColor("#66717A"),true); l.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END); values[i]=tv("-",14,Color.BLACK,true); values[i].setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END); box.addView(l);box.addView(values[i]); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(68),1); lp.setMargins(dp(2),0,dp(2),0); info.addView(box,lp);} details.addView(info,new LinearLayout.LayoutParams(-1,-2));

            TextView l1=section("آرایش الکترونی فشرده",Color.parseColor("#EEF3F7")); details.addView(l1); compactTv=tv("",18,CYAN,true); compactTv.setTextDirection(View.TEXT_DIRECTION_LTR); compactTv.setGravity(Gravity.LEFT); compactTv.setBackgroundColor(Color.parseColor("#EEF3F7")); details.addView(compactTv,new LinearLayout.LayoutParams(-1,dp(52)));
            TextView l2=section("آرایش کامل — هستهٔ گاز نجیب قرمز، بخش باقی‌مانده آبی",Color.parseColor("#F9F9F9")); details.addView(l2); fullTv=tv("",16,Color.DKGRAY,true); fullTv.setTextDirection(View.TEXT_DIRECTION_LTR); fullTv.setGravity(Gravity.LEFT); fullTv.setBackgroundColor(Color.parseColor("#F9F9F9")); details.addView(fullTv,new LinearLayout.LayoutParams(-1,-2));

            shellTv=card("توزیع الکترون‌ها در لایه‌ها"); details.addView(shellTv); ionTv=card("یون‌های رایج آموزشی"); details.addView(ionTv); validTv=card("کنترل تعداد الکترون‌ها"); details.addView(validTv);
            TextView ghead=section("محاسبهٔ آموزشی گروه از روی آرایش",Color.parseColor("#F7F0FF")); details.addView(ghead); groupCalcTv=tv("",14,Color.parseColor("#66458A"),false); groupCalcTv.setBackgroundColor(Color.parseColor("#F7F0FF")); groupCalcTv.setGravity(Gravity.RIGHT); details.addView(groupCalcTv,new LinearLayout.LayoutParams(-1,-2));

            detailsScroll.addView(details); root.addView(detailsScroll,new LinearLayout.LayoutParams(-1,0,1));
            setContentView(root);
            this.values=values;
        }

        private TextView[] values;
        private TextView section(String s,int bgColor){TextView t=tv(s,11,NAVY,true);t.setBackgroundColor(bgColor);t.setPadding(dp(10),dp(7),dp(10),dp(4));return t;}
        private TextView card(String title){TextView t=tv(title+"\n",11,Color.parseColor("#66717A"),true);t.setBackground(bg(Color.WHITE,8));t.setPadding(dp(10),dp(7),dp(10),dp(7));return t;}

        private Element element(int z){return elements.get(z-1);}
        private void selectElement(int z){ if(z<1||z>118)return; selectedZ=z; Element e=element(z); String full=prettyConfig(e.config); int[] shells=shellDistribution(e.config); int count=electronCount(e.config); boolean ok=count==z;
            selectedTitle.setText(e.symbol+" — "+e.name+" — "+e.nameFa);
            String[] v={""+e.z,""+e.mass,e.symbol,e.name,e.nameFa,e.categoryFa,""+e.period,e.groupText}; for(int i=0;i<v.length;i++) values[i].setText(v[i]);
            compactTv.setText(prettyCompact(e.config)); fullTv.setText(prettyFullColored(e.config)); shellTv.setText("توزیع الکترون‌ها در لایه‌ها\n"+shellText(shells)); ionTv.setText("یون‌های رایج آموزشی\n"+ions.getOrDefault(z,"اطلاعات یون رایج در بانک آموزشی ثبت نشده است.")); validTv.setText("کنترل تعداد الکترون‌ها\n"+(ok?"✓ صحیح — مجموع = ":"⚠ نیاز به بررسی — مجموع = ")+count+" الکترون"); groupCalcTv.setText(groupCalculation(e)); table.setSelectedZ(z); table.invalidate(); }
        private void searchElement(){String q=search.getText().toString().trim().toLowerCase(Locale.ROOT); if(q.isEmpty())return; for(Element e:elements){ if(q.equals(""+e.z)||q.equals(e.symbol.toLowerCase(Locale.ROOT))||e.name.toLowerCase(Locale.ROOT).contains(q)||e.nameFa.contains(q)||e.categoryFa.contains(q)){selectElement(e.z); return;}} new AlertDialog.Builder(this).setTitle("نتیجه جست‌وجو").setMessage("عنصری با این عبارت پیدا نشد.").setPositiveButton("باشه",null).show();}
        private void nextElement(){selectElement(selectedZ>=118?1:selectedZ+1);}

        private static String superscript(int n){return Integer.toString(n).replace("0","⁰").replace("1","¹").replace("2","²").replace("3","³").replace("4","⁴").replace("5","⁵").replace("6","⁶").replace("7","⁷").replace("8","⁸").replace("9","⁹");}
        private static String expand(String cfg){String r=cfg; String[] cores={"[He]","[Ne]","[Ar]","[Kr]","[Xe]","[Rn]"}; String[] exp={"1s2","1s2 2s2 2p6","1s2 2s2 2p6 3s2 3p6","1s2 2s2 2p6 3s2 3p6 4s2 3d10 4p6","1s2 2s2 2p6 3s2 3p6 4s2 3d10 4p6 5s2 4d10 5p6","1s2 2s2 2p6 3s2 3p6 4s2 3d10 4p6 5s2 4d10 5p6 6s2 4f14 5d10 6p6"}; for(int i=0;i<cores.length;i++)r=r.replace(cores[i],exp[i]); return r;}
        private static List<String> tokens(String cfg){return Arrays.asList(expand(cfg).split(" "));}
        private static String prettyConfig(String cfg){StringBuilder s=new StringBuilder();for(String tok:tokens(cfg)){ if(s.length()>0)s.append(' '); String num=tok.replaceAll("[^0-9]",""); String base=tok.substring(0,tok.length()-num.length()); s.append(base).append(superscript(Integer.parseInt(num)));}return s.toString();}
        private static String prettyCompact(String cfg){StringBuilder s=new StringBuilder();for(String tok:cfg.split(" ")){if(s.length()>0)s.append(' '); if(tok.matches("\\[.*\\]"))s.append(tok); else{String num=tok.replaceAll("[^0-9]","");String base=tok.substring(0,tok.length()-num.length());s.append(base).append(superscript(Integer.parseInt(num)));}}return s.toString();}
        private static CharSequence prettyFullColored(String cfg){
            String full=prettyConfig(cfg);
            String core="";
            for(String tok:cfg.split(" ")) if(tok.matches("\\[.*\\]")){ core=prettyConfig(expand(tok)); break; }
            if(core.isEmpty()) return full;
            String[] parts=full.split(" "); int coreCount=core.split(" ").length;
            int chars=0; for(int i=0;i<Math.min(coreCount,parts.length);i++){ if(i>0)chars++; chars+=parts[i].length(); }
            SpannableString ss=new SpannableString(full);
            ss.setSpan(new ForegroundColorSpan(RED),0,Math.min(chars,ss.length()),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if(chars<ss.length()) ss.setSpan(new ForegroundColorSpan(CYAN),chars,ss.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return ss;
        }
        private static int electronCount(String cfg){int c=0;for(String tok:tokens(cfg)){String n=tok.replaceAll("[^0-9]","");c+=Integer.parseInt(n);}return c;}
        private static int[] shellDistribution(String cfg){int[] a=new int[7]; for(String tok:tokens(cfg)){if(tok.length()<3)continue; int n=tok.charAt(0)-'0'; String num=tok.replaceAll("[^0-9]",""); a[n-1]+=Integer.parseInt(num);} return a;}
        private static String shellText(int[] a){StringBuilder s=new StringBuilder();for(int i=0;i<a.length;i++)if(a[i]>0){if(s.length()>0)s.append("   ");s.append("لایه ").append(i+1).append(": ").append(a[i]);}return s.toString();}

        private static String groupCalculation(Element e){
            List<String> p=tokens(e.config);
            if(e.z==2) return "1) آرایش: 1s²\n2) لایهٔ ظرفیت کامل است (2 الکترون).\n3) هلیم استثناست و با وجود 1s² در گروه 18 قرار می‌گیرد.\nنتیجه: گروه 18 (گاز نجیب)";
            int s=0, pp=0, d=0;
            for(String tok:p){
                String digits=tok.replaceAll("[^0-9]","");
                if(digits.isEmpty()) continue;
                int n=tok.charAt(0)-'0'; char o=tok.charAt(1); int c=Integer.parseInt(digits);
                if(n==e.period&&o=='s') s+=c;
                if(n==e.period&&o=='p') pp+=c;
                if(n==e.period-1&&o=='d') d+=c;
            }
            if(e.category.equals("lanthanide")||e.category.equals("actinide"))
                return "1) آرایش مرتبط با بلوک f از دادهٔ عنصر استخراج شد.\n2) این عنصر در بلوک f قرار دارد.\n3) در این چیدمان گروه عددی مستقلی ندارد.\nنتیجه: بلوک f — گروه: —";
            if(pp>0){
                int g=12+pp;
                return "1) آرایش لایهٔ ظرفیت: "+e.period+"s"+s+" "+e.period+"p"+pp+"\n2) الکترون‌های لایهٔ ظرفیت = "+(s+pp)+"\n3) گروه = 12 + "+pp+" = "+g+"\nنتیجه: گروه "+g;
            }
            if(d>0&&e.period>=4){
                int g=d+s;
                return "1) زیرلایه‌های مؤثر: "+(e.period-1)+"d"+d+" "+e.period+"s"+s+"\n2) الکترون‌های d = "+d+" و s = "+s+"\n3) گروه = "+d+" + "+s+" = "+g+"\nنتیجه: گروه "+g;
            }
            if((s==1||s==2)&&pp==0&&d==0)
                return "1) آرایش لایهٔ ظرفیت: "+e.period+"s"+s+"\n2) تعداد الکترون لایهٔ ظرفیت = "+s+"\n3) گروه = "+s+"\nنتیجه: گروه "+s;
            return "آرایش الکترونی این عنصر با قواعد سادهٔ s/p/d تعیین نشد.\nگروه ثبت‌شده در جدول: "+e.groupText;
        }

    }
