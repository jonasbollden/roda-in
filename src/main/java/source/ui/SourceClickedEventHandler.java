package source.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;

import org.slf4j.LoggerFactory;

import source.ui.items.SourceTreeDirectory;
import source.ui.items.SourceTreeFile;
import source.ui.items.SourceTreeLoadMore;
import source.ui.items.SourceTreeLoading;

/**
 * Created by adrapereira on 21-09-2015.
 */
public class SourceClickedEventHandler implements EventHandler<MouseEvent> {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SourceClickedEventHandler.class.getName());
    private TreeView<Object> treeView;
    private FileExplorerPane fep;

    public SourceClickedEventHandler(FileExplorerPane pane){
        this.treeView = pane.getTreeView();
        fep = pane;
    }

    public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            TreeItem<Object> item = treeView.getSelectionModel().getSelectedItem();
            if(item instanceof SourceTreeLoadMore) {
                final SourceTreeDirectory parent = (SourceTreeDirectory)item.getParent();
                parent.getChildren().remove(item);
                SourceTreeLoading loading = new SourceTreeLoading();
                parent.getChildren().add(loading);

                Platform.runLater(new Runnable() {
                    public void run() {
                        parent.loadMore();
                    }
                });
            }else if(item instanceof SourceTreeDirectory){
                SourceTreeDirectory directory = (SourceTreeDirectory)item;
                fep.updateMetadata(directory.getPath());
            }else if(item instanceof SourceTreeFile) {
                SourceTreeFile directory = (SourceTreeFile) item;
                fep.updateMetadata(directory.getPath());
            }
        }
    }
}