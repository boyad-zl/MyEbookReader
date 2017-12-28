package com.example.epubreader.book;

/**
 * Created by Boyad on 2017/12/26.
 */

public class BookHtmlResourceFile extends BookResourceFile {
    private int spinIndex ;

    public BookHtmlResourceFile(String id, String href, String mediaType) {
        super(id, href, mediaType);
    }

    public BookHtmlResourceFile(String id, String href, String mediaType, String inFilePath) {
        super(id, href, mediaType, inFilePath);
    }

    public int getSpinIndex() {
        return spinIndex;
    }

    public void setSpinIndex(int spinIndex) {
        this.spinIndex = spinIndex;
    }
}
