package com.example.epubreader.book;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.SparseArray;

import com.example.epubreader.ReaderApplication;
import com.example.epubreader.book.css.BookCSSAttributeSet;
import com.example.epubreader.util.EpubPullParserUtil;
import com.example.epubreader.book.toc.TocElement;
import com.example.epubreader.util.BookSettings;
import com.example.epubreader.util.BookStingUtil;
import com.example.epubreader.util.MyReadLog;
import com.example.epubreader.view.book.BookCoverPage;
import com.example.epubreader.view.book.BookReadPosition;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * BookModel 实体类
 * 在其中保存了分好类的各种资源文件
 * 以及meta信息（epub的名称，作者，以及其他）、顺序信息、逻辑目录的信息、开头位置的记录
 * Created by zjb on 2017/11/2.
 */

public class BookModel {
    public static final String META_INF_CONTAINER_XML = "META-INF/container.xml";

    private String epubPath; // epub文件的位置
    public ArrayMap<String, BookHtmlResourceFile> textFileArrayMap = new ArrayMap<>(); //存放html资源文件
    public ArrayMap<String, BookResourceFile> imageFileArrayMap = new ArrayMap<>(); // 存放图片资源文件
    public ArrayMap<String, BookResourceFile> ncxFileArrayMap = new ArrayMap<>(); // 存放目录文件
    public ArrayMap<String, BookResourceFile> cssFileArrayMap = new ArrayMap<>(); // 存放CSS资源文件

    public ArrayList<String> spinContentList = new ArrayList<>();
    public Book book;
    public String bookCover;
    private String opfDir;
    private ZipFile zipFile;
    public BookCSSAttributeSet cssAttributeSet;
    private BookCoverPage coverPage;
    public TocElement tocElement;

    public BookModel(String epubPath) {
        this.epubPath = epubPath;
    }

    public synchronized void decodeEpubMeta(String path) {
        try {
            zipFile = new ZipFile(new File(path));
            ZipEntry rootEntry = zipFile.getEntry(META_INF_CONTAINER_XML);
            String contentOpfFileName = getContentOpfName(zipFile, rootEntry);
            if (contentOpfFileName.contains("/")) {
                opfDir = contentOpfFileName.substring(0, contentOpfFileName.lastIndexOf("/"));
                MyReadLog.i("opfDir : " + opfDir);
            } else {
                opfDir = "";
            }

            ZipEntry metaFile = zipFile.getEntry(contentOpfFileName);
//            long startCategoryTime = System.currentTimeMillis();
            categoryBook(zipFile, metaFile);
//            MyReadLog.i("category file cost time is " + (System.currentTimeMillis() - startCategoryTime));
//            MyReadLog.i("----开始解析逐个读取html文件");

            ReaderApplication.getInstance().getDummyView().preparePage(getReadPosition());
            ReaderApplication.getInstance().getMyWidget().reset();
//            ReaderApplication.getInstance().getMyWidget().repaint();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ReaderApplication.getInstance().getDummyView().calculateTotalPages();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 将epub里面的文件按照content.opf文件里面的注册信息，将里面用到的文件分类
     *
     * @param zipFile
     * @param metaFile
     */
    private void categoryBook(ZipFile zipFile, ZipEntry metaFile) throws IOException {
        InputStream inputStream = zipFile.getInputStream(metaFile);
//        long start = System.currentTimeMillis();
        EpubPullParserUtil.parseMetaFile(inputStream, this);

        if (!TextUtils.isEmpty(bookCover)) {
            createCoverPage();
//            MyReadLog.i("createCoverPage");
            if (coverPage != null) {
                ReaderApplication.getInstance().getDummyView().setCoverPage(coverPage);
            }
        }
//        MyReadLog.i("metadata file cost time is " + (System.currentTimeMillis() - start));
        if (cssFileArrayMap.size() > 0) {
            loadCSSAttributes(); // 加载CSS文件
        }
        if (ncxFileArrayMap.size() > 0) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
            loadTOCFile(); //加载目录文件
//                }
//            }).start();
        }
    }

    /**
     * 创建封面页
     */
    private void createCoverPage() {
        int[] coverSize = new int[2];
        InputStream inputStream;
        try {
            BookResourceFile coverFile = imageFileArrayMap.get(bookCover);
            inputStream = zipFile.getInputStream(zipFile.getEntry(coverFile.inFilePath));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Rect outPaddingRect = new Rect();
            BitmapFactory.decodeStream(inputStream, outPaddingRect, options);
            coverSize[0] = options.outWidth;
            coverSize[1] = options.outHeight;
//            MyReadLog.d("width = %d, height = %d", coverSize[0], coverSize[1]);
        } catch (IOException e) {
            e.printStackTrace();
            coverSize[0] = 0;
            coverSize[1] = 0;
        }
        BookResourceFile coverFile = imageFileArrayMap.get(bookCover);
        if (coverFile != null && coverSize[0] > 0 && coverSize[1] > 0) {
            coverPage = BookCoverPage.createBookCoverPage(coverFile.inFilePath, coverSize);
        }
    }

    public BookCoverPage getCoverPage() {
        return coverPage;
    }

    /**
     * TODO TEST
     * 加载目录文件
     */
    private void loadTOCFile() {
        long start = System.currentTimeMillis();
        try {
            tocElement = EpubPullParserUtil.parseTocFile(zipFile.getInputStream(zipFile.getEntry(ncxFileArrayMap.valueAt(0).inFilePath)), this);
//            MyReadLog.i("first Element " + (tocElement.tocElements.get(0).getName()));
        } catch (IOException e) {
            tocElement = null;
            e.printStackTrace();
        }
        MyReadLog.i("load toc cost " + (System.currentTimeMillis() - start));
    }

    /**
     * TODO TEST
     * 加载css属性
     */
    private void loadCSSAttributes() throws IOException {
        long startTime = System.currentTimeMillis();
        cssAttributeSet = new BookCSSAttributeSet(zipFile, cssFileArrayMap);
        MyReadLog.i("解析CSS文件 cost time is" + (System.currentTimeMillis() - startTime));
    }

    /**
     * 按照spin的顺序依次将id传给spinContent;
     *
     * @param line
     */
    private int getReadSort(String line, int index) {
        if (line.contains("idref")) {
            spinContentList.add(BookStingUtil.getDataValue(line, "\"", "\"", line.indexOf("idref")));
//            spinContent[index] = BookStingUtil.getDataValue(line, "\"", "\"", line.indexOf("idref"));
            return index + 1;
        } else {
            return index;
        }
    }

    /**
     * TODO TEST 部分属性没有添加进（例如字体（application/x-font-truetype））
     * <p>
     * 获取电子书里面的资源文件（html文件，和图片文件，目录文件）
     *
     * @param line
     */
    private void getEpubResourceFile(String line) {
        String href, id, mediaType, filePath;
        if (line.startsWith("<item") && line.endsWith("/>")) {
            href = BookStingUtil.getDataValue(line, "\"", "\"", line.indexOf("href"));
            id = BookStingUtil.getDataValue(line, "\"", "\"", line.indexOf("id"));
            mediaType = BookStingUtil.getDataValue(line, "\"", "\"", line.indexOf("media-type"));
            if (!TextUtils.isEmpty(opfDir)) {
                filePath = opfDir + "/" + href;
            } else {
                filePath = href;
            }
            filePath = getRealPath(filePath);
            if (mediaType.equals("application/xhtml+xml")) {
                textFileArrayMap.put(id, new BookHtmlResourceFile(id, href, mediaType, filePath));
            } else if (mediaType.equals("image/png") || mediaType.equals("image/jpeg")) {
                imageFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
            } else if (mediaType.equals("application/x-dtbncx+xml")) {
                MyReadLog.d("id = %s, href = %s, mediaType = %s, filePath = %s", id, href, mediaType, filePath);
                ncxFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
            } else if (mediaType.equals("text/css")) {
                cssFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
            }
        }
    }

    /**
     * 处理epub获取到的地址
     * 由于获取到的可能是网址url的网址导致将符号转移（例如 空格转义成%20，）
     *
     * @param filePath
     * @return
     */
    private String getRealPath(String filePath) {
        try {
            filePath = URLDecoder.decode(filePath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return filePath;
    }


    /**
     * 获取Content.Opf文件的名称（例如：OEBPS/content.opf）
     *
     * @param rootEntry
     * @return
     */
    private static String getContentOpfName(ZipFile zipFile, ZipEntry rootEntry) {
        String opfName = "";
        try {
            InputStream in = zipFile.getInputStream(rootEntry);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("full-path")) {
                    int firstIndex = line.indexOf("\"", line.indexOf("full-path"));
                    int secondIndex = line.indexOf("\"", firstIndex + 1);
                    opfName = line.substring(firstIndex + 1, secondIndex);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return opfName;
    }

    /**
     * 获取图片资源
     *
     * @param imgPath
     * @return
     */
    public Bitmap getBitmap(String imgPath) {
        if (imageFileArrayMap.containsKey(imgPath)) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(zipFile.getInputStream(zipFile.getEntry(imageFileArrayMap.get(imgPath).inFilePath)));
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    //todo test : 获取内容
    public synchronized InputStream getTextContent() {
        String contentId = spinContentList.get(75);
//        String contentId = spinContent[75];
        if (textFileArrayMap.containsKey(contentId)) {
            try {
                return zipFile.getInputStream(zipFile.getEntry(textFileArrayMap.get(contentId).inFilePath));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 获取指定位置的HTML内容
     *
     * @param htmlIndex
     * @return
     */
    public synchronized InputStream getTextContent(int htmlIndex) {
        String contentId = spinContentList.get(htmlIndex);
//        String contentId = spinContent[htmlIndex];
        if (textFileArrayMap.containsKey(contentId)) {
            try {
                return zipFile.getInputStream(zipFile.getEntry(textFileArrayMap.get(contentId).inFilePath));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 获取epub内的根目录名称
     *
     * @return
     */
    public String getOpfDir() {
        return opfDir;
    }

    /**
     * 获取图片相关的流数据
     *
     * @param imagePathStr
     * @return
     */
    public synchronized InputStream getImageInputStream(String imagePathStr) {
//        MyReadLog.i("getImageInputStream");
        if (!imagePathStr.startsWith(opfDir)) {
            if (imagePathStr.startsWith("../")) {
                imagePathStr = opfDir + imagePathStr.substring(2);
            } else {
                imagePathStr = opfDir + "/" + imagePathStr;
            }
        }
        try {
//            MyReadLog.i("image path = " + imagePathStr);
            ZipEntry imageEntry = zipFile.getEntry(imagePathStr);
            if (imageEntry != null) {
                return zipFile.getInputStream(imageEntry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取电子书路径
     *
     * @return
     */
    public String getEpubPath() {
        return epubPath;
    }

    public BookReadPosition getReadPosition() {
        BookReadPosition bookReadPosition = new BookReadPosition(0, "0", 0);
        String positionStr = BookSettings.getReadBookPosition();
        if (!TextUtils.isEmpty(positionStr)) {
            int linkIndex = positionStr.indexOf("-");
            if (linkIndex > -1) {
                bookReadPosition.setPagePosition(Integer.valueOf(positionStr.substring(0, linkIndex)));
                int slantIndex = positionStr.indexOf("/", linkIndex);
                if (slantIndex > -1) {
                    String contentElement = positionStr.substring(linkIndex + 1, slantIndex);
                    int elementIndex = Integer.valueOf(positionStr.substring(slantIndex + 1));
                    bookReadPosition.setContentIndex(contentElement);
                    bookReadPosition.setElementIndex(elementIndex);
//                    bookReadPosition = new BookReadPosition(pagePosition, contentElement, slantIndex);
                }

            }
        }
        return bookReadPosition;
    }

    public void saveReadPosition(String position) {
        if (!TextUtils.isEmpty(position)) {
            BookSettings.setReadPosition(position);
        }
    }

    public int getSpinSize() {
        return spinContentList.size();
//        return spinContent.length;
    }

    /**
     * 根据路径推算html在spine中的顺序
     *
     * @param path
     * @return
     */
    public int getSpinePageIndex(String path) {
        int pageIndex = -1;
        if (!path.startsWith(getOpfDir())) {
            if (path.startsWith("../")) {
                path = getOpfDir() + path.substring(2);
            } else {
                path = getOpfDir() + "/" + path;
            }
        }
//        String resourceId = "";
        for (int i = 0; i < textFileArrayMap.size(); i++) {
            BookHtmlResourceFile file = textFileArrayMap.valueAt(i);
            if (file.inFilePath.equals(path)) {
//                resourceId = textFileArrayMap.keyAt(i);
                pageIndex = file.getSpinIndex();
                break;
            }
        }
//        if (TextUtils.isEmpty(resourceId)) return pageIndex;
//        for (int i = 0; i < spinContentList.size(); i++) {
//            String spineId = spinContentList.get(i);
//            if (spineId.equals(resourceId)) {
//                pageIndex = i;
//                break;
//            }
//        }
        return pageIndex;
    }

    /**
     * 获取 inHtmlId 为键 在tocElement中的位置 为value 的集合
     *
     * @param htmlIndex
     * @return
     */
    public ArrayMap<String, Integer> getHtmlTocElement(int htmlIndex) {
        ArrayMap<String, Integer> arrayMap = null;
        for (int i = 0; i < tocElement.getCount(true); i++) {
            TocElement childElement = tocElement.getElementAt(i, true);
            if (htmlIndex == childElement.getHtmlSpinIndex()) {
                if (arrayMap == null) {
                    arrayMap = new ArrayMap<>();
                }
                arrayMap.put(TextUtils.isEmpty(childElement.getInHtmlId()) ? " " : childElement.getInHtmlId(), i);
            }
        }
        return arrayMap;
    }
}
