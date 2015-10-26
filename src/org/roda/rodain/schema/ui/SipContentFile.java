package rodain.schema.ui;

import java.io.File;
import java.nio.file.Path;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import rodain.source.ui.items.SourceTreeItem;
import rodain.source.ui.items.SourceTreeItemState;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SipContentFile extends TreeItem<Object> implements SourceTreeItem{
    public static final Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("icons/file.png"));

    //this stores the full path to the file
    private String fullPath;

    public SipContentFile(Path file) {
        super(file.toString());
        this.fullPath = file.toString();
        this.setGraphic(new ImageView(fileImage));

        //set the value
        if (!fullPath.endsWith(File.separator)) {
            //set the value (which is what is displayed in the tree)
            String value = file.toString();
            int indexOf = value.lastIndexOf(File.separator);
            if (indexOf > 0) {
                this.setValue(value.substring(indexOf + 1));
            } else {
                this.setValue(value);
            }
        }
    }

    @Override
    public String getPath() {
        return this.fullPath;
    }

    @Override
    public SourceTreeItemState getState(){
        return SourceTreeItemState.NORMAL;
    }

    @Override
    public void ignore(){
    }

    @Override
    public void map(String s){
    }

    @Override
    public void unignore(){
    }

    @Override
    public void unmap(String s){

    }
}
