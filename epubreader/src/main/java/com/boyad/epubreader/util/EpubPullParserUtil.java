package com.boyad.epubreader.util;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.SparseArray;

import com.boyad.epubreader.db.Book;
import com.boyad.epubreader.book.BookHtmlResourceFile;
import com.boyad.epubreader.book.BookModel;
import com.boyad.epubreader.book.BookResourceFile;
import com.boyad.epubreader.book.toc.TocElement;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Boyad on 2017/11/29.
 */
public class EpubPullParserUtil {
    // toc 文件解析需要的常量
    private static final String NAVMAP_STRING = "navMap";
    private static final String NAVPOINT_STRING = "navPoint";
    private static final String NAVLABLE_STRING = "navLabel";
    private static final String CONTENT_STRING = "content";
    private static final String TEXT_STRING = "text";

    // opf文文件解析需要的常量
    private static final String TAGNAME_METADATA = "metadata";
    private static final String TAGNAME_META = "meta";
    private static final String TAGNAME_DC_IDENTIFIER = "dc:identifier"; //识别符
    private static final String TAGNAME_DC_TITLE = "dc:title";  //电子书的标题
    private static final String TAGNAME_DC_CREATOR = "dc:creator";
    private static final String TAGNAME_DC_LANGUAGE = "dc:language";
    private static final String TAGNAME_DC_RIGHTS = "dc:rights";  //版权描叙
    private static final String TAGNAME_DC_DESCRIPTION = "dc:description";  //电子书的内容介绍

    private static final String TAGNAME_MANIFEST = "manifest";
    private static final String TAGNAME_ITEM = "item";
    private static final String TAGNAME_SPINE = "spine";
    private static final String TAGNAME_ITEM_REF = "itemref";
    private static final String TAGNAME_GUIDE = "guide";

    private static final String ATTRIBUTE_VALUE_MAKER = "maker"; // 创建者
    private static final String ATTRIBUTE_VALUE_AUT = "aut"; // 作家
    private static final String ATTRIBUTE_VALUE_AUTHOR = "author"; // 作家
    private static final String ATTRIBUTE_VALUE_ISBN = "ISBN"; // ISBN

    private static final String ATTRIBUTE_OPF_ROLE = "opf:role";
    private static final String ATTRIBUTE_CONTENT = "content";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_MEDIA_TYPE = "media-type";

    private static final String ATTRIBUTE_IDREF = "idref";
    private static final String APPLICATION_RESOURCE_FILE_TYPE_XML = "application/xhtml+xml";
    private static final String APPLICATION_RESOURCE_FILE_TYPE_IMAGE_PNG = "image/png";
    private static final String APPLICATION_RESOURCE_FILE_TYPE_IMAGE_JPEG = "image/jpeg";
    private static final String APPLICATION_RESOURCE_FILE_TYPE_NCX = "application/x-dtbncx+xml";
    private static final String APPLICATION_RESOURCE_FILE_TYPE_CSS = "text/css";
    private static final String APPLICATION_RESOURCE_FILE_TYPE_OEBPS = "application/oebps-package+xml";
    public static final String META_INF_CONTAINER_XML = "META-INF/container.xml";


    public static TocElement parseTocFile(InputStream inputStream, BookModel model) throws IOException {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(inputStream));
            TocElement tocElement = null;
            int event = parser.getEventType();
            int depth = 0;
            if (model.htmlIndexTocMapsSparseArray == null) {
                model.htmlIndexTocMapsSparseArray = new SparseArray<>();
            }
            model.htmlIndexTocMapsSparseArray.clear();
            int tocIndex = -1;
            while (event != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equals(NAVMAP_STRING)) {
                            if (tocElement == null) {
                                tocElement = new TocElement();
                                tocElement.setDepth(depth);
                            }
                        } else if (tagName.equals(NAVPOINT_STRING)) {
                            depth++;
                            tocIndex++;
                            TocElement childTocElement = new TocElement(tocElement);
                            tocElement = childTocElement;
                            tocElement.setDepth(depth);
                        } else if (tagName.equals(NAVLABLE_STRING)) {

                        } else if (tagName.equals(CONTENT_STRING)) {
                            String path = parser.getAttributeValue(null, "src");
                            if (!TextUtils.isEmpty(path) && tocElement != null) {
//                            MyReadLog.i("path = " + path);
                                tocElement.setPath(path);
                                String[] pathInfos = path.split("#");
                                int htmlSpinIndex = model.getSpinePageIndex(pathInfos[0].trim());
                                tocElement.setHtmlSpinIndex(htmlSpinIndex);

                                ArrayMap<String, Integer> array = model.htmlIndexTocMapsSparseArray.get(htmlSpinIndex);
                                if (array == null) {
                                    array = new ArrayMap<>();
                                }
                                if (pathInfos.length > 1) {
                                    array.put(TextUtils.isEmpty(pathInfos[1].trim()) ? " " : pathInfos[1].trim(), tocIndex);
                                    tocElement.setInHtmlId(pathInfos[1].trim());
//                                    MyReadLog.i("htmlSpinIndex = " + htmlSpinIndex+ ", tocIndex =" + tocIndex + ", pathInfo = " + pathInfos[1]);
                                } else {
                                    array.put(" ", tocIndex);
                                }
                                model.htmlIndexTocMapsSparseArray.put(htmlSpinIndex, array);
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
                            depth--;
                        } else if (tagName.equals(NAVLABLE_STRING)) {

                        } else if (tagName.equals(CONTENT_STRING)) {

                        } else if (tagName.equals(TEXT_STRING)) {

                        }
                        break;
                }
                event = parser.next();
            }
            return tocElement;
        } catch (XmlPullParserException e) {
            return null;
        }
    }

    public static void parseMetaFile(InputStream inputStream, BookModel model, Book book) throws IOException {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(inputStream));
            int eventType = parser.getEventType();
            boolean isReadMetaData = false;
            boolean isReadManifest = false;
            boolean isReadSpine = false;
            boolean isReadGuide = false;
//            Book book = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equals(TAGNAME_METADATA) && !isReadMetaData) {
                            isReadMetaData = true;
//                            book = new Book();
                        }
                        if (isReadMetaData) {
                            if (tagName.equals(TAGNAME_DC_TITLE)) {
                                book.title = parser.nextText();
                            } else if (tagName.equals(TAGNAME_DC_CREATOR)) {
//                                model.bookWriter = parser.nextText();
                                String roleValue = parser.getAttributeValue(null, ATTRIBUTE_OPF_ROLE);
                                if (TextUtils.isEmpty(roleValue)) {
//                                    book.addAuthor(parser.nextText());
                                } else {
                                    if (roleValue.equals(ATTRIBUTE_VALUE_AUT) || roleValue.equals(ATTRIBUTE_VALUE_AUTHOR)) {
//                                        book.addAuthor(parser.nextText());
//                                    model.bookName = parser.nextText();
                                    } else if (roleValue.equals(ATTRIBUTE_VALUE_MAKER)) {
//                                        book.maker = parser.nextText();
                                    }
                                }
                            } else if (tagName.equals(TAGNAME_DC_LANGUAGE)) {
//                                book.language = parser.nextText();
//                                model.bookLanguage = parser.nextText();
                            } else if (tagName.equals(TAGNAME_META)) {
                                String name = parser.getAttributeValue(null, ATTRIBUTE_NAME);
                                if (name != null && name.equals("cover")) {
                                    model.bookCover = parser.getAttributeValue(null, ATTRIBUTE_CONTENT);
                                }
                            }
                        }
                        if (tagName.equals(TAGNAME_MANIFEST) && !isReadManifest)
                            isReadManifest = true;
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

                                if (mediaType.equals(APPLICATION_RESOURCE_FILE_TYPE_XML)) {
                                    model.textFileArrayMap.put(id, new BookHtmlResourceFile(id, href, mediaType, filePath));
                                } else if (mediaType.equals(APPLICATION_RESOURCE_FILE_TYPE_IMAGE_PNG) || mediaType.equals(APPLICATION_RESOURCE_FILE_TYPE_IMAGE_JPEG)) {
                                    model.imageFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
                                } else if (mediaType.equals(APPLICATION_RESOURCE_FILE_TYPE_NCX)) {
//                                    MyReadLog.d("id = %s, href = %s, mediaType = %s, filePath = %s" , id, href, mediaType, filePath);
                                    model.ncxFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
                                } else if (mediaType.equals(APPLICATION_RESOURCE_FILE_TYPE_CSS)) {
                                    model.cssFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
                                }
                            }
                        }
                        if (tagName.equals(TAGNAME_SPINE) && !isReadSpine) isReadSpine = true;
                        if (isReadSpine) {
                            if (tagName.equals(TAGNAME_ITEM_REF)) {
                                String idHref = parser.getAttributeValue(null, ATTRIBUTE_IDREF);
//                                spinContent[spineIndex] = idHref;
//                                spineIndex ++;
                                model.textFileArrayMap.get(idHref).setSpinIndex(model.spinContentList.size());
                                model.spinContentList.add(idHref);
                            }
                        }
                        if (tagName.equals(TAGNAME_GUIDE) && !isReadGuide) isReadGuide = true;
                        if (isReadGuide) {

                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (isReadMetaData && tagName.equals(TAGNAME_METADATA))
                            isReadMetaData = false;
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
            model.book = book;
        } catch (XmlPullParserException e) {

        }
    }

    public static void parseOnlyMetaFile(final ZipFile zipFile, Book book) throws IOException {
        try {
            ZipEntry containerEntry = zipFile.getEntry(META_INF_CONTAINER_XML);
            String internalDir = "";
            String internalOpfPath = "";
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(new InputStreamReader(zipFile.getInputStream(containerEntry)));
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equals("rootfile")) {
                            if (APPLICATION_RESOURCE_FILE_TYPE_OEBPS.equals(parser.getAttributeValue(null, ATTRIBUTE_MEDIA_TYPE))) {
                                internalOpfPath = parser.getAttributeValue(null, "full-path");
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }

            if (!TextUtils.isEmpty(internalOpfPath) && internalOpfPath.length() > 11) {
                internalDir = internalOpfPath.substring(0, internalOpfPath.length() - 12);
                MyReadLog.i(internalDir);
            }

            MyReadLog.i("internalOpfPath -->" + internalOpfPath);
            ZipEntry opfEntry = zipFile.getEntry(internalOpfPath);
            parser.setInput(new InputStreamReader(zipFile.getInputStream(opfEntry)));
//                int eventType = parser.getEventType();
            boolean isReadMetaData = false;
            boolean isReadManifest = false;
            boolean needBreak = false;
            String coverId = "";
            String internalCoverPath = "";
            eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (needBreak) break;
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equals(TAGNAME_METADATA) && !isReadMetaData) {
                            isReadMetaData = true;
                        }
                        if (isReadMetaData) {
                            if (tagName.equals(TAGNAME_DC_TITLE)) {
                                book.title = parser.nextText();
                            } else if (tagName.equals(TAGNAME_DC_CREATOR)) {
                                String roleValue = parser.getAttributeValue(null, ATTRIBUTE_OPF_ROLE);
                                if (TextUtils.isEmpty(roleValue)) {
//                                    book.addAuthor(parser.nextText());
                                } else {
                                    if (roleValue.equals(ATTRIBUTE_VALUE_AUT) || roleValue.equals(ATTRIBUTE_VALUE_AUTHOR)) {
                                        book.addAuthor(parser.nextText());
//                                    model.bookName = parser.nextText();
                                    } else if (roleValue.equals(ATTRIBUTE_VALUE_MAKER)) {
//                                        book.maker = parser.nextText();
                                    }
                                }
                            } else if (tagName.equals(TAGNAME_DC_LANGUAGE)) {
//                                book.language = parser.nextText();
//                                model.bookLanguage = parser.nextText();
                            } else if (tagName.equals(TAGNAME_META)) {
                                String name = parser.getAttributeValue(null, ATTRIBUTE_NAME);
                                if (name != null && name.equals("cover")) {
                                    coverId = parser.getAttributeValue(null, ATTRIBUTE_CONTENT);
                                }
                            }
                        }
                        if (tagName.equals(TAGNAME_MANIFEST) && !isReadManifest)
                            isReadManifest = true;
                        if (isReadManifest) {
                            if (tagName.equals(TAGNAME_ITEM)) {
                                String href = parser.getAttributeValue(null, ATTRIBUTE_HREF);
                                String id = parser.getAttributeValue(null, ATTRIBUTE_ID);
                                String mediaType = parser.getAttributeValue(null, ATTRIBUTE_MEDIA_TYPE);
                                if (id.equals(coverId)) {
                                    if (mediaType.equals(APPLICATION_RESOURCE_FILE_TYPE_IMAGE_PNG)
                                            || mediaType.equals(APPLICATION_RESOURCE_FILE_TYPE_IMAGE_JPEG)) {
                                        internalCoverPath = href;
//                                        MyReadLog.i("cover path in zip = " + href);
                                    }
                                    needBreak = true;
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (isReadMetaData && tagName.equals(TAGNAME_METADATA)) {
                            if (TextUtils.isEmpty(coverId)) {
                                needBreak = true;
                            }
                            isReadMetaData = false;
                        }
                        break;
                }
                eventType = parser.next();
            }

            if (!TextUtils.isEmpty(internalCoverPath)) {
                if (!TextUtils.isEmpty(internalDir)) {
                    internalCoverPath = internalDir + "/" + internalCoverPath;
                }
                String fileType = internalCoverPath.substring(internalCoverPath.lastIndexOf(".") + 1);
                final String coverRealPath = BookFileHelper.PATH_COVER + "/" + book.title + "." + fileType;
                MyReadLog.i("coverRealPath = " + coverRealPath);
                book.coverFile = coverRealPath;
                File coverFile = new File(coverRealPath);
                if (!coverFile.exists()) {
                    final ZipEntry coverEntry = zipFile.getEntry(internalCoverPath);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                File file = new File(BookFileHelper.PATH_COVER);
                                if (!file.exists()) {
                                    file.mkdirs();
                                }
//                                FileOutputStream outputStream = new FileOutputStream(coverRealPath);
                                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(coverRealPath));
                                InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(coverEntry));
                                int readLen = 0;
                                byte[] bytes = new byte[1024];
                                while ((readLen = inputStream.read(bytes, 0, 1024)) != -1) {
                                    outputStream.write(bytes, 0, readLen);
                                }
                                inputStream.close();
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {

                            }
                        }
                    }).start();
                }
            }

        } catch (XmlPullParserException e) {

        }
    }

}
