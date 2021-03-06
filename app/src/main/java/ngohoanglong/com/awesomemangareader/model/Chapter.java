package ngohoanglong.com.awesomemangareader.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Admin on 10/03/2017.
 */

public class Chapter implements Serializable {
    private String title;
    private List<Image> imageList;

    public Chapter() {
    }

    public Chapter(String title, List<Image> imageList) {
        this.title = title;
        this.imageList = imageList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Image> getImageList() {
        return imageList;
    }

    public void setImageList(List<Image> imageList) {
        this.imageList = imageList;
    }

    @Override
    public String toString() {
        return "ChapterAdapter{" +
                "title='" + title + '\'' +
                ", imageList.size=" + imageList.size() +
                '}';
    }
}
