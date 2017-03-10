package ngohoanglong.com.awesomemangareader;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Admin on 10/03/2017.
 */

public class MangaPage implements Serializable {
    private String title;
    private List<String> imageList;

    public MangaPage() {
    }

    public MangaPage(String title, List<String> imageList) {
        this.title = title;
        this.imageList = imageList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }

    @Override
    public String toString() {
        return "MangaPage{" +
                "title='" + title + '\'' +
                ", imageList.size=" + imageList.size() +
                '}';
    }
}
