package west2project.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class SaveUtil {

    public boolean areAllNonEmpty(Object... objects) {
        for (Object o : objects) {
            if (o == null) {
                return false;
            }
        }
        return true;
    }
    public boolean areAllFieldsNonNullOrEmpty(Object obj) {
        if (obj == null) {
            return false;
        }
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true); // 设置私有属性也可以访问
            try {
                Object value = field.get(obj);
                if (value == null) {
                    return false;
                }
                if (value instanceof String && ((String) value).isEmpty()) {
                    return false; // 属性值为空字符串
                }
                //TODO 数组检查


            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public String saveFileWithName(MultipartFile file,String path,String fileName){
        try {
            file.transferTo(new File(path+"\\"+fileName));
            return path+"\\"+fileName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String changeFileName(MultipartFile file){
        String originalFileName = file.getOriginalFilename();
        String newFileName;
        String uuid = System.currentTimeMillis()+String.valueOf(UUID.randomUUID());
        if (originalFileName != null) {
            newFileName =  uuid+originalFileName.substring(originalFileName.lastIndexOf("."));
        }else {
            newFileName = uuid;
        }
        return newFileName;
    }

    public String imageToBase64(String filePath) {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] fileContent = baos.toByteArray();
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            log.info("图片读取出错");
            return null;
        }
    }


}