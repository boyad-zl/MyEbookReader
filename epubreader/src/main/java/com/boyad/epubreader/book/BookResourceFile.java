package com.boyad.epubreader.book;

/**
 * Created by Boyad on 2017/11/1.
 */

public class BookResourceFile {
    public String id;
    public String href;
    public String mediaType;
    public String inFilePath;//在epub中的位置

    public BookResourceFile(String id, String href, String mediaType) {
        this.id = id;
        this.href = href;
        this.mediaType = mediaType;
    }

    public BookResourceFile(String id, String href, String mediaType, String inFilePath) {
        this.id = id;
        this.href = href;
        this.mediaType = mediaType;
        this.inFilePath = inFilePath;
    }
}
