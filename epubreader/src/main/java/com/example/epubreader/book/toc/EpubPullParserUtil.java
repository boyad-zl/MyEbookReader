package com.example.epubreader.book.toc;

import android.text.TextUtils;

import com.example.epubreader.book.BookModel;
import com.example.epubreader.book.BookResourceFile;
import com.example.epubreader.util.BookStingUtil;
import com.example.epubreader.util.MyReadLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.zip.ZipEntry;

/**
 * Created by Boyad on 2017/11/29.
 */
public class EpubPullParserUtil {
    // toc 文件解析需要的常量
    private static final String NAVPOINT_STRING = "navPoint";
    private static final String NAVLABLE_STRING = "navLabel";
    private static final String CONTENT_STRING = "content";
    private static final String TEXT_STRING = "text";

    // opf文文件解析需要的常量
    private static final String TAGNAME_METADATA = "metadata";
    private static final String TAGNAME_META = "meta";
    private static final String TAGNAME_DC_IDENTIFIER = "dc:identifier";
    private static final String TAGNAME_DC_TITLE = "dc:title";
    private static final String TAGNAME_DC_CREATOR = "dc:creator";
    private static final String TAGNAME_DC_LANGUAGE = "dc:language";

    private static final String TAGNAME_MANIFEST = "manifest";
    private static final String TAGNAME_ITEM = "item";
    private static final String TAGNAME_SPINE = "spine";
    private static final String TAGNAME_ITEM_REF = "itemref";

    private static final String ATTRIBUTE_CONTENT = "content";
    private static final String ATTRIBUTE_NAME = "name";

    private static final String TAGNAME_GUIDE = "guide";
    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_MEDIA_TYPE = "media-type";

    private static final String ATTRIBUTE_IDREF = "idref";


    public static TocElement parseTocFile(InputStream inputStream) throws  IOException {
        try {

            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(inputStream));
            TocElement tocElement = null;
            int event = parser.getEventType();
            int depth = -1;
            while (event != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equals(NAVPOINT_STRING)) {
                            depth++;
                            if (tocElement == null) {
                                tocElement = new TocElement();
                            } else {
                                TocElement childTocElement = new TocElement(tocElement);
                                tocElement = childTocElement;
                            }
                        } else if (tagName.equals(NAVLABLE_STRING)) {

                        } else if (tagName.equals(CONTENT_STRING)) {
                            String path = parser.getAttributeValue(null, "src");
                            if (!TextUtils.isEmpty(path) && tocElement != null) {
//                            MyReadLog.i("path = " + path);
                                tocElement.setPath(path);
                            }
                        } else if (tagName.equals(TEXT_STRING)) {
                            String title = parser.nextText();
                            if (tocElement != null) {
                                tocElement.setName(title);
//                            MyReadLog.i(tagName + " : " + title);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (tagName.equals(NAVPOINT_STRING)) {
                            TocElement parent = tocElement.getParent();
                            if (parent != null) {
                                parent.addTocElement(tocElement);
                                tocElement = parent;
                            }
                        } else if (tagName.equals(NAVLABLE_STRING)) {

                        } else if (tagName.equals(CONTENT_STRING)) {

                        } else if (tagName.equals(TEXT_STRING)) {

                        }
                        break;
                }
                event = parser.next();
            }

            int size = tocElement.getElementSize();
            MyReadLog.i("size  = " + size);
            return tocElement;
        }catch (XmlPullParserException e) {
            return null;
        }
    }

    public static void parseMetaFile(InputStream inputStream, BookModel model) throws IOException {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(inputStream));
            int eventType = parser.getEventType();
            boolean isReadMetaData = false;
            boolean isReadManifest = false;
            boolean isReadSpine = false;
            boolean isReadGuide = false;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equals(TAGNAME_METADATA) && !isReadMetaData) isReadMetaData = true;
                        if (isReadMetaData) {
                            if (tagName.equals(TAGNAME_DC_TITLE)) {
                                model.bookName = parser.nextText();
                            } else if (tagName.equals(TAGNAME_DC_CREATOR)) {
                                model.bookWriter = parser.nextText();
                            } else if (tagName.equals(TAGNAME_DC_LANGUAGE)) {
                                model.bookLanguage = parser.nextText();
                            } else if (tagName.equals(TAGNAME_META)) {
                                String name = parser.getAttributeValue(null, ATTRIBUTE_NAME);
                                if (name != null && name.equals("cover")){
                                    model.bookCover = parser.getAttributeValue(null, ATTRIBUTE_CONTENT);
                                }
                            }
                        }
                        if (tagName.equals(TAGNAME_MANIFEST) && !isReadManifest) isReadManifest = true;
                        if (isReadManifest) {
                            if (tagName.equals(TAGNAME_ITEM)) {
                                String href = parser.getAttributeValue(null, ATTRIBUTE_HREF);
                                String id = parser.getAttributeValue(null, ATTRIBUTE_ID);
                                String mediaType = parser.getAttributeValue(null, ATTRIBUTE_MEDIA_TYPE);
                                String filePath;
                                if (!TextUtils.isEmpty(model.getOpfDir())) {
                                    filePath = model.getOpfDir() + "/" + href;
                                } else {
                                    filePath = href;
                                }

                                try {
                                    filePath = URLDecoder.decode(filePath, "UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }

                                if (mediaType.equals("application/xhtml+xml")) {
                                    model.textFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
                                } else if (mediaType.equals("image/png") || mediaType.equals("image/jpeg")) {
                                    model.imageFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
                                } else if (mediaType.equals("application/x-dtbncx+xml")) {
//                                    MyReadLog.d("id = %s, href = %s, mediaType = %s, filePath = %s" , id, href, mediaType, filePath);
                                    model.ncxFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
                                } else if (mediaType.equals("text/css")) {
                                    model.cssFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
                                }
                            }
                        }
                        if (tagName.equals(TAGNAME_SPINE) && !isReadSpine) isReadSpine = true;
                        if (isReadSpine) {
                            if (tagName.equals(TAGNAME_ITEM_REF)){
                                String idHref = parser.getAttributeValue(null, ATTRIBUTE_IDREF);
//                                spinContent[spineIndex] = idHref;
//                                spineIndex ++;
                                model.spinContentList.add(idHref);
                            }
                        }
                        if (tagName.equals(TAGNAME_GUIDE) && !isReadGuide) isReadGuide = true;
                        if (isReadGuide) {

                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (isReadMetaData && tagName.equals(TAGNAME_METADATA)) isReadMetaData = false;
                        if (isReadManifest && tagName.equals(TAGNAME_MANIFEST)) {
                            isReadManifest = false;
                            model.spinContentList.clear();
                        }
                        if (isReadSpine && tagName.equals(TAGNAME_SPINE)) isReadSpine = false;
                        if (isReadGuide && tagName.equals(TAGNAME_GUIDE)) isReadGuide = false;
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e){

        }
    }
}
