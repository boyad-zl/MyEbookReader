package com.example.epubreader.book;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.example.epubreader.book.css.BookCSSAttributeSet;
import com.example.epubreader.book.css.BookSingleCSSSet;
import com.example.epubreader.util.BookStingUtil;
import com.example.epubreader.util.MyReadLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
    public ArrayMap<String, BookResourceFile> textFileArrayMap = new ArrayMap<>(); //存放html资源文件
    public ArrayMap<String, BookResourceFile> imageFileArrayMap = new ArrayMap<>(); // 存放图片资源文件
    public ArrayMap<String, BookResourceFile> ncxFileArrayMap = new ArrayMap<>(); // 存放目录文件
    public ArrayMap<String, BookResourceFile> cssFileArrayMap = new ArrayMap<>(); // 存放CSS资源文件

    public String bookName;
    public String bookWriter;
    public String bookLanguage;
    public String bookCover;

    private String opfDir;
    private String[] spinContent;
    private ZipFile zipFile;

    public BookCSSAttributeSet cssAttributeSet;

    public BookModel(String epubPath) {
        this.epubPath = epubPath;
        create(epubPath);
    }

    private void create(String path) {
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
            long startCategoryTime = System.currentTimeMillis();
            categoryBook(zipFile, metaFile);
            MyReadLog.i("category file cost time is " + (System.currentTimeMillis() - startCategoryTime));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将epub里面的文件按照content.opf文件里面的注册信息，将里面用到的文件分类
     * @param zipFile
     * @param metaFile
     */
    private void categoryBook(ZipFile zipFile, ZipEntry metaFile) throws IOException {
        InputStream inputStream = zipFile.getInputStream(metaFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        boolean metaDataReadStart = false;
        boolean manifestReadStart = false;
        boolean spineReadStart = false;
        boolean guideReadStart = false;
        int index = 0;
        long start = System.currentTimeMillis();
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("<metadata")) metaDataReadStart = true;
            if (metaDataReadStart) {
                getBookInfo(line);
                if (line.endsWith("</metadata>")) {
                    metaDataReadStart = false;
                    MyReadLog.i("metadata cost time is " + (System.currentTimeMillis() - start));
                }
            }

            if (line.startsWith("<manifest>")) manifestReadStart = true;
            if (manifestReadStart) {
                getEpubResourceFile(line);
                if (line.endsWith("</manifest>")) {
                    manifestReadStart = false;
                    spinContent = new String[textFileArrayMap.size()];
                }
            }

            if (line.startsWith("<spine")) spineReadStart = true;
            if (spineReadStart) {
                index = getReadSort(line, index);
                if (line.endsWith("</spine>")) spineReadStart = false;
            }

            if (line.startsWith("<guide>")) guideReadStart = true;
            if (guideReadStart) {
                if (line.endsWith("</guide>")) guideReadStart = false;
            }
        }

//        info.append("书名：" + bookName + " , 作者：" + bookWriter + " , 语言：" + bookLanguage + " , cover:" + bookCover);
        MyReadLog.i("text file size is " + textFileArrayMap.size());
        MyReadLog.i("img file size is " + imageFileArrayMap.size());
        MyReadLog.i("ncx file size is " + ncxFileArrayMap.size());
        MyReadLog.i("css file size is " + cssFileArrayMap.size());
        if (cssFileArrayMap.size() > 0) {
            loadCSSAttributes(); // 加载CSS文件
        }
        if (ncxFileArrayMap.size() > 0){
            loadTOCFile(); //加载目录文件
        }
    }

    /**
     * TODO TEST
     * 加载目录文件
     */
    private void loadTOCFile() {

    }

    /**
     * TODO TEST
     * 加载css属性
     */
    private void loadCSSAttributes() throws IOException {
        cssAttributeSet = new BookCSSAttributeSet(zipFile, cssFileArrayMap);
    }

    /**
     * 按照spin的顺序依次将id传给spinContent;
     *
     * @param line
     */
    private int getReadSort(String line, int index) {
        if (line.contains("idref")) {
            spinContent[index] = BookStingUtil.getDataValue(line, "\"", "\"", line.indexOf("idref"));
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
                textFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
            } else if (mediaType.equals("image/png") || mediaType.equals("image/jpeg")) {
                imageFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
            } else if (mediaType.equals("application/x-dtbncx+xml")) {
                MyReadLog.d("id = %s, href = %s, mediaType = %s, filePath = %s" , id, href, mediaType, filePath);
                ncxFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
            } else if (mediaType.equals("text/css")) {
                cssFileArrayMap.put(id, new BookResourceFile(id, href, mediaType, filePath));
            }
        }
    }

    /**
     * TODO TEST: 需要对特殊字符进行处理（例如括号，空格）
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
     * TODO TEST 还有很多属性没有添加识别（UID）
     * 根据字符串的信息，获取电子书的信息（读者，UUID，书名）
     *
     * @param line
     */
    private void getBookInfo(String line) {
        if (line.startsWith("<dc:title") && line.endsWith("</dc:title>")) {
            bookName = BookStingUtil.getDataValue(line, ">", "</", 0);
            MyReadLog.i("bookName ->" + bookName);
        } else if (line.startsWith("<dc:creator") && line.endsWith("</dc:creator>")) {
            bookWriter = BookStingUtil.getDataValue(line, ">", "</", 0);
        } else if (line.startsWith("<dc:language") && line.endsWith("</dc:language>")) {
            bookLanguage = BookStingUtil.getDataValue(line, ">", "</", 0);
        } else if (line.startsWith("<meta nameStr=") && line.endsWith("/>")) {
            bookCover = BookStingUtil.getDataValue(line, "\"", "\"", line.indexOf("content"));
        }
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
     * @param imgPath
     * @return
     */
    public Bitmap getBitmap(String imgPath){
        if (imageFileArrayMap.containsKey(imgPath)){
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

    public InputStream getTextContent() {
        String contentId = spinContent[11];
        if (textFileArrayMap.containsKey(contentId)){
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
     * @return
     */
    public String getOpfDir(){
        return opfDir;
    }
}