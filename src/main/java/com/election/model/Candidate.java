package com.election.model;

public class Candidate {
    private final String name;
    private final String list;
    private final int index;
    private int selectionOrder;

    public Candidate(String name, String list, int index) {
        this.name = name;
        this.list = list;
        this.index = index;
        this.selectionOrder = 0; // 0 means not selected
    }

    public String getName() {
        return name;
    }

    public String getList() {
        return list;
    }

    public int getIndex() {
        return index;
    }

    public int getSelectionOrder() {
        return selectionOrder;
    }

    public void setSelectionOrder(int selectionOrder) {
        this.selectionOrder = selectionOrder;
    }

    @Override
    public String toString() {
        return name;
    }
} 