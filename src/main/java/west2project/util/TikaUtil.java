package west2project.util;

import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TikaUtil {
    public static String isFileValid(MultipartFile file, String type,Long size) {
        if (file == null || file.isEmpty()) {
            return "文件是空的";
        }
        Tika tika = new Tika();
        String fileType;
        try (TikaInputStream stream = TikaInputStream.get(file.getInputStream())) {
            fileType = tika.detect(stream, new Metadata());
        } catch (IOException e) {
            return "文件类型解析失败";
        }
        if (!fileType.startsWith(type)) {
            return type+"文件类型不符合";
        }
        if(file.getSize()>size){
            return type.substring(0, type.length() - 1)+"文件过大: 最大为"+size/1048576+"MB ,您的文件大小为:"+file.getSize()/1048576;
        }
        return "true";
    }

    public static String isVideoValid(MultipartFile file,String title) {
        String isFileValid = isFileValid(file, "video/",1048576*512L);
        if(!isFileValid.equals("true")){
            return isFileValid;
        }
        if(title==null ||title.isEmpty()){
            return "标题为空";
        }
        return "true";
    }

    public static String isImageValid(MultipartFile file) {
        return isFileValid(file, "image/",1048576*10L);
    }

    public static boolean isEmailValid(String email){
        Pattern pattern= Pattern.compile( "[a-zA-z0-9+-]+@[a-z]+");
        Matcher matcher= pattern.matcher(email);
        return matcher.matches();
    }
}
