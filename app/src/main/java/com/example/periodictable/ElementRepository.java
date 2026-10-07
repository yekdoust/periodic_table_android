package com.example.periodictable;

import android.content.Context;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ElementRepository {
    private ElementRepository() {}

    public static List<Element> loadElements(Context context) throws IOException {
        ArrayList<Element> out = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                context.getAssets().open("elements.tsv"), StandardCharsets.UTF_8))) {
            String line = br.readLine();
            while ((line=br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p=line.split("\t", -1);
                if (p.length < 11) continue;
                out.add(new Element(Integer.parseInt(p[0]),p[1],p[2],p[3],p[4],p[5],
                        Integer.parseInt(p[6]),Integer.parseInt(p[7]),p[8],p[9],Integer.parseInt(p[10])));
            }
        }
        return out;
    }

    public static Map<Integer,String> loadIons(Context context) throws IOException {
        HashMap<Integer,String> out = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                context.getAssets().open("ions.tsv"), StandardCharsets.UTF_8))) {
            String line;
            while ((line=br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p=line.split("\t", 2);
                if (p.length==2) out.put(Integer.parseInt(p[0]),p[1]);
            }
        }
        return out;
    }
}
