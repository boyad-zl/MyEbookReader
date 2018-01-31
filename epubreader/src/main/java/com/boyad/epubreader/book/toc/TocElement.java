package com.boyad.epubreader.book.toc;

import java.util.ArrayList;

/**
 * Created by Boyad on 2017/11/29.
 */

public class TocElement {
    public TocElement parent;
    public ArrayList<TocElement> tocElements;
    private String name;
    private String path;
    private int depth;
    private int pageIndex = -1; // 页码
    private int internalPageIndex; //在当前html中的页码位置
    private String position;
    private int htmlSpinIndex; // 在spin文件中的位置索引
    private String inHtmlId; // 在html中的id位置

    private boolean isOpened = false;

    public TocElement() {
        parent = null;
    }

    public TocElement(TocElement parent) {
        this.parent = parent;
    }

    public void addTocElement(TocElement element) {
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

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }

    public TocElement getParent() {
        return parent;
    }

    public int getElementSize() {
        if (tocElements == null) return 0;
        return tocElements.size();
    }

    public int getCount(boolean isForce) {
        if (tocElements != null && !tocElements.isEmpty()) {
            if (isForce || isOpened) {
                int childSum = 0;
                for (int i = 0; i < tocElements.size(); i++) {
                    TocElement childElement = tocElements.get(i);
                    childSum = childSum + childElement.getCount(isForce);
                }
                return tocElements.size() + childSum;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public int getCount() {
        return getCount(false);
    }

    public TocElement getElementAt(int position, boolean isForce) {
        if (tocElements == null || tocElements.isEmpty()) return this;
        int index = 0;
        TocElement resultElement = null;
        for (int i = 0; i < tocElements.size(); i++) {
            TocElement childElement = tocElements.get(i);
            if (position == index) {
                resultElement = childElement;
                break;
            }
            int childCount = childElement.getCount(isForce);
            if (childCount > 0 && (position <= index + childCount)) {
                resultElement = childElement.getElementAt(position - index - 1, isForce);
                break;
            } else {
                index = index + childCount;
            }
            index = index + 1;
        }
        return resultElement;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * 是否能展开
     * @return
     */
    public boolean canOpen(){
        if (tocElements == null || tocElements.isEmpty()) return false;
        return true;
    }

    /**
     * 打开所有子元素
     */
    public void openAllElements() {
        if (tocElements == null || tocElements.isEmpty()) return;
        isOpened = true;
        for (int i = 0; i < tocElements.size(); i++) {
            TocElement childTocElement = tocElements.get(i);
            childTocElement.openAllElements();
        }
    }

    /**
     * 折叠所有子元素
     */
    public void closeAllElement() {
        if (tocElements == null || tocElements.isEmpty()) return;
        for (int i = 0; i < tocElements.size(); i++) {
            TocElement childTocElement = tocElements.get(i);
            childTocElement.openAllElements();
        }
        isOpened = false;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getHtmlSpinIndex() {
        return htmlSpinIndex;
    }

    public void setHtmlSpinIndex(int htmlSpinIndex) {
        this.htmlSpinIndex = htmlSpinIndex;
    }

    public String getInHtmlId() {
        return inHtmlId;
    }

    public void setInHtmlId(String inHtmlId) {
        this.inHtmlId = inHtmlId;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageIndex() {
        return pageIndex;
    }
}
