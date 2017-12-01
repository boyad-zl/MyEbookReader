package com.example.epubreader.book.toc;

import java.util.ArrayList;

/**
 * Created by Boyad on 2017/11/29.
 */

public class TocElement {
    public TocElement parent;
    public ArrayList<TocElement> tocElements;

    private String name;
    private String path;

    public TocElement() {
        parent = null;
    }

    public TocElement(TocElement parent) {
        this.parent = parent;
    }

    public void  addTocElement(TocElement element) {
        if (tocElements == null) {
            tocElements = new ArrayList<>();
        }
        tocElements.add(element);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public TocElement getParent() {
        return parent;
    }

    public int getElementSize() {
        if (tocElements == null) return 0;
        return tocElements.size();
    }

}
