package ngohoanglong.com.awesomemangareader;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ngohoanglong.com.awesomemangareader.model.Chapter;
import ngohoanglong.com.awesomemangareader.model.Image;

/**
 * Created by Admin on 12/03/2017.
 */

public class AppState {


    public static List<Chapter> chapeters = new ArrayList<>();;

    public static List<Chapter> getFile(Context context) {

        List<Chapter> chapeters = new ArrayList<>();
        try {
            InputStream in = context.getAssets().open("JSONfiles.zip");
            ZipInputStream zipInputStream = new ZipInputStream(in);
            ZipEntry ze = null;

            while ((ze = zipInputStream.getNextEntry()) != null) {
                if (!ze.isDirectory() && !ze.getName().contains("_")) {

                    List<Image> imageList = new ArrayList<>();

                    File file = new File(ze.getName());
                    int size = (int) file.length();
                    byte[] bytes = new byte[size];
                    if (zipInputStream.read(bytes, 0, bytes.length) == bytes.length) {
                        InputStreamReader isr = new InputStreamReader(zipInputStream);
                        BufferedReader input = new BufferedReader(isr);
                        String line = "";
                        while ((line = input.readLine()) != null) {
                            line = line.replaceAll("\\[|\\]|\"|,", "");
                            if (line.length() > 0) imageList.add(new Image(line,null));
                        }
                    }
                    chapeters.add(new Chapter(ze.getName(), imageList));
                }
                zipInputStream.closeEntry();
            }
            zipInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chapeters;

    }
}
