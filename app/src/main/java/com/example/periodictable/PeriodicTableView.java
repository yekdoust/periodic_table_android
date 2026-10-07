package com.example.periodictable;

import android.content.*;
import android.graphics.*;
import android.view.*;
import java.util.*;

public final class PeriodicTableView extends View {
    public interface Listener { void onElement(int z); }
    private final List<Element> elements; private Listener listener; private int selectedZ=1;
    private final float d; private final int cellW, cellH, left, top, gap;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); private final RectF r=new RectF();
    private static final Map<String,Integer> COLORS=new HashMap<>();
    static { COLORS.put("alkali metal",Color.parseColor("#F7B7B7")); COLORS.put("alkaline earth metal",Color.parseColor("#F7D49B")); COLORS.put("transition metal",Color.parseColor("#F6C98D")); COLORS.put("post-transition metal",Color.parseColor("#FFF0A8")); COLORS.put("metalloid",Color.parseColor("#C5E6A6")); COLORS.put("nonmetal",Color.parseColor("#AEE4E8")); COLORS.put("halogen",Color.parseColor("#BFD7FF")); COLORS.put("noble gas",Color.parseColor("#D9C2F0")); COLORS.put("lanthanide",Color.parseColor("#F2B7D8")); COLORS.put("actinide",Color.parseColor("#D8B6A4")); }
    public PeriodicTableView(Context c,List<Element> e){super(c);elements=e;d=getResources().getDisplayMetrics().density;cellW=dp(84);cellH=dp(70);left=dp(42);top=dp(30);gap=dp(3); setLayerType(View.LAYER_TYPE_SOFTWARE,null); setBackgroundColor(Color.parseColor("#F4F6F8"));}
    private int dp(int v){return Math.round(v*d);} public int tableWidth(){return left+18*cellW+dp(12);} @Override protected void onMeasure(int w,int h){setMeasuredDimension(tableWidth(),dp(30+7*70+36+2*70));}
    public void setOnElementClickListener(Listener l){listener=l;} public void setSelectedZ(int z){selectedZ=z;}
    private Element get(int z){return elements.get(z-1);} private boolean f(int z){return (z>=58&&z<=71)||(z>=90&&z<=103);}
    private int group(int z){ if(z==1)return 1;if(z==2)return 18;if(z>=3&&z<=4)return z-2;if(z>=5&&z<=10)return z+8;if(z>=11&&z<=12)return z-10;if(z>=13&&z<=18)return z; if(z>=19&&z<=36)return z-18;if(z>=37&&z<=54)return z-36;if(z>=55&&z<=86){if(z==55)return 1;if(z==56)return 2;if(z==57)return 3;return z-68;} if(z>=87&&z<=118){if(z==87)return 1;if(z==88)return 2;if(z==89)return 3;return z-101;} return 1;}
    private int period(int z){if(z<=2)return 1;if(z<=10)return 2;if(z<=18)return 3;if(z<=36)return 4;if(z<=54)return 5;if(z<=86)return 6;return 7;}
    private float X(int g){return left+(g-1)*(cellW+gap);} private float Y(int per){return top+(per-1)*(cellH+gap);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(9));p.setColor(Color.parseColor("#17324D")); for(int g=1;g<=18;g++)c.drawText("G"+g,X(g)+cellW/2f,dp(18),p); for(int per=1;per<=7;per++)c.drawText("P"+per,dp(18),Y(per)+cellH/2f,p);
        for(Element e:elements){if(f(e.z))continue; drawCell(c,e,X(e.group),Y(e.period));} float fy=Y(7)+cellH+dp(12); c.drawText("Ln",dp(18),fy+cellH/2f,p); c.drawText("An",dp(18),fy+cellH+gap+cellH/2f,p); for(int i=0;i<14;i++){drawCell(c,get(58+i),X(i+4),fy);drawCell(c,get(90+i),X(i+4),fy+cellH+gap);} }
    private void drawCell(Canvas c,Element e,float x,float y){int base=COLORS.getOrDefault(e.category,Color.LTGRAY); boolean selected=e.z==selectedZ;
        p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(x,y,x,y+cellH,new int[]{lighten(base,.64f),lighten(base,.35f),lighten(base,.50f)},null,Shader.TileMode.CLAMP));r.set(x,y,x+cellW,y+cellH);c.drawRoundRect(r,dp(9),dp(9),p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(selected?dp(3):dp(2));p.setColor(selected?Color.WHITE:Color.parseColor("#4BAFCC"));c.drawRoundRect(r,dp(9),dp(9),p);p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);c.drawRoundRect(new RectF(x+dp(10),y+dp(5),x+cellW-dp(10),y+dp(9)),dp(2),dp(2),p);
        p.setColor(Color.parseColor("#245A70"));p.setTextAlign(Paint.Align.LEFT);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(13));c.drawText(""+e.z,x+dp(7),y+dp(16),p); p.setTextAlign(Paint.Align.RIGHT);p.setTextSize(dp(8));p.setColor(Color.parseColor("#3A7185"));c.drawText("A="+e.mass,x+cellW-dp(7),y+dp(15),p);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(dp(17));p.setColor(Color.parseColor("#24566A"));c.drawText(e.symbol,x+cellW/2f,y+dp(38),p);p.setTextSize(dp(8));p.setTypeface(Typeface.DEFAULT);p.setColor(Color.parseColor("#4B7F91"));c.drawText("G"+e.groupText+"  P"+e.period,x+cellW/2f,y+cellH-dp(9),p);
    }
    private int lighten(int color,float amount){return Color.rgb(Math.round(Color.red(color)+(255-Color.red(color))*amount),Math.round(Color.green(color)+(255-Color.green(color))*amount),Math.round(Color.blue(color)+(255-Color.blue(color))*amount));}
    @Override public boolean onTouchEvent(android.view.MotionEvent ev){if(ev.getAction()!=MotionEvent.ACTION_UP)return true;float x=ev.getX(),y=ev.getY(); for(Element e:elements){int gy=e.group;if(f(e.z)){int idx=e.z<72?e.z-58:e.z-90;float fy=Y(7)+cellH+dp(12)+(e.z>=90?cellH+gap:0);float ex=X(idx+4);if(x>=ex&&x<=ex+cellW&&y>=fy&&y<=fy+cellH){selectedZ=e.z;if(listener!=null)listener.onElement(e.z);performClick();return true;}}else{float ex=X(gy),ey=Y(e.period);if(x>=ex&&x<=ex+cellW&&y>=ey&&y<=ey+cellH){selectedZ=e.z;if(listener!=null)listener.onElement(e.z);performClick();return true;}}}return true;}
    @Override public boolean performClick(){super.performClick();return true;}
}
