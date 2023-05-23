package com.example.simulinkviewer;
import java.util.ArrayList;
import java.util.List;
public class Arrow {
    private int scrId;
    private int srcPlace;
    private double x;

    private List<myPair> Dests = new ArrayList<myPair>();

    public Arrow(int scrId, int srcPlace, double x) {
        this.scrId = scrId;
        this.srcPlace = srcPlace;
        this.x = x;
    }

    public void addDest(int destId, double y) {
        myPair p = new myPair(destId, y);
        Dests.add(p);
    }

    public int getScrId() {
        return scrId;
    }

    public int getSrcPlace() {
        return srcPlace;
    }

    public double getX() {
        return x;
    }

    public double getY(int idx) {
        return Dests.get(idx).getY();
    }

    public int getDestId(int idx) {
        return Dests.get(idx).getDestId();
    }

    public int getDestsSize() {
        return Dests.size();
    }
}

class myPair {
    private int destId;
    private double y;

    public myPair(int destId, double y) {
        this.destId = destId;
        this.y = y;
    }

    public int getDestId() {
        return destId;
    }

    public double getY() {
        return y;
    }
}

