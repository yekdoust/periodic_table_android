package com.example.periodictable;

public final class Element {
    public final int z, period, group, mass;
    public final String symbol, name, nameFa, category, categoryFa, groupText, config;
    public Element(int z, String symbol, String name, String nameFa, String category, String categoryFa,
                   int period, int group, String groupText, String config, int mass) {
        this.z=z; this.symbol=symbol; this.name=name; this.nameFa=nameFa; this.category=category;
        this.categoryFa=categoryFa; this.period=period; this.group=group; this.groupText=groupText;
        this.config=config; this.mass=mass;
    }
}
