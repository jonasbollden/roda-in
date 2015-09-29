package source.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import org.slf4j.LoggerFactory;

import source.representation.SourceDirectory;
import source.ui.items.SourceTreeDirectory;
import source.ui.items.SourceTreeItem;
import utils.Utils;

/**
 * Created by adrapereira on 24-09-2015.
 */
public class FileExplorerPane extends BorderPane {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FileExplorerPane.class.getName());
    private HBox openfolder;
    private StackPane fileExplorer;
    private TreeView<Object> treeView;
    private Stage stage;

    private GridPane metadata;
    private Label l_title, l_type, l_content, l_path, l_metadata;

    //This thread is used to walk a directory's file tree and update the UI periodically with the size and file count
    private ComputeDirectorySize computeThread;

    public FileExplorerPane(Stage stage){
        super();

        this.stage = stage;

        createOpenFolder();
        createFileExplorer();
        createMetadata();

        SplitPane split  = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);
        split.getItems().addAll(fileExplorer, metadata);

        this.setTop(openfolder);
        this.setCenter(split);
        this.minWidthProperty().bind(stage.widthProperty().multiply(0.2));
    }

    private void createOpenFolder(){
        Button btn = new Button("Open Folder");
        Label title = new Label("Source File Explorer");
        title.setFont(Font.font("System",FontWeight.BOLD ,14));

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        openfolder = new HBox();
        openfolder.setPadding(new Insets(10, 10, 10, 10));
        openfolder.setSpacing(10);
        openfolder.setAlignment(Pos.TOP_RIGHT);
        openfolder.getChildren().addAll(title, space, btn);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Please choose a folder");
                File selectedDirectory = chooser.showDialog(stage);
                if (selectedDirectory == null) return;
                Path path = selectedDirectory.toPath();
                setFileExplorerRoot(path);
            }
        });
    }

    private void createFileExplorer(){
        //create tree pane
        VBox treeBox=new VBox();
        treeBox.setPadding(new Insets(10, 10, 10, 10));
        treeBox.setSpacing(10);

        treeView = new TreeView<Object>();
        // add everything to the tree pane
        treeBox.getChildren().addAll(treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        fileExplorer = new StackPane();
        fileExplorer.getChildren().add(treeBox);

        treeView.setOnMouseClicked(new SourceClickedEventHandler(this));
    }

    public void setFileExplorerRoot(Path rootPath){
        SourceTreeDirectory rootNode = new SourceTreeDirectory(rootPath, new SourceDirectory(rootPath));
        rootNode.setExpanded(true);
        treeView.setRoot(rootNode);
        updateMetadata(rootPath);
    }

    private void createMetadata(){
        metadata = new GridPane();
        metadata.setAlignment(Pos.TOP_LEFT);
        metadata.setHgap(10);
        metadata.setVgap(10);
        metadata.setPadding(new Insets(25, 25, 25, 25));

        Label title = new Label("Title:");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        metadata.add(title, 0, 1);
        l_title = new Label();
        l_title.setWrapText(true);
        metadata.add(l_title, 1, 1);

        Label type = new Label("Type:");
        type.setFont(Font.font("System", FontWeight.BOLD, 14));
        metadata.add(type, 0, 2);
        l_type = new Label();
        l_type.setWrapText(true);
        metadata.add(l_type, 1, 2);

        Label content = new Label("Content:");
        content.setFont(Font.font("System", FontWeight.BOLD, 14));
        metadata.add(content, 0, 3);
        l_content = new Label();
        l_content.setWrapText(true);
        metadata.add(l_content, 1, 3);

        Label path = new Label("Path:");
        path.setFont(Font.font("System", FontWeight.BOLD, 14));
        metadata.add(path, 0, 4);
        l_path = new Label();
        l_path.setWrapText(true);
        metadata.add(l_path, 1, 4);

        Label metadataLabel = new Label("Metadata:");
        metadataLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        metadataLabel.setMinWidth(100); //don't allow the label to minimize when the pane is shrunk
        metadata.add(metadataLabel, 0, 5);
        l_metadata = new Label();
        l_metadata.setWrapText(true);
        metadata.add(l_metadata, 1, 5);
    }

    public void updateMetadata(Path path){
        //we need to stop the directory size compute thread to avoid more than one thread updating the ui at the same time
        stopComputeThread();
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

            String title;
            if(path.getFileName() != null) title = path.getFileName().toString();
            else title = path.toString();
            l_title.setText(title);
            l_path.setText(path.toString());
            l_metadata.setText("dummy.xml");

            if(attr.isDirectory()){
                l_type.setText("Directory");
                l_content.setText("");
                computeThread = new ComputeDirectorySize(this, path.toString());
                computeThread.start();
            }
            else{
                l_type.setText("File");
                l_content.setText(Utils.formatSize(attr.size()));
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void updateMetadata(String pathString){
        Path path = Paths.get(pathString);
        updateMetadata(path);
    }

    public TreeView<Object> getTreeView() {
        return treeView;
    }

    private void stopComputeThread(){
        if(computeThread != null) computeThread.interrupt();
    }

    public void updateSize(long count, long size){
        final long countF = count, sizeF = size;
        Platform.runLater(new Runnable() {
            public void run() {
                String result = countF + " items, ";
                result += Utils.formatSize(sizeF);
                l_content.setText(result);
            }
        });
    }

    public SourceTreeItem getSelectedItem(){
        if(treeView == null) return null;
        int selIndex = treeView.getSelectionModel().getSelectedIndex();
        if(selIndex == -1) return null;
        SourceTreeItem item = (SourceTreeItem)treeView.getTreeItem(selIndex);
        return item;
    }
}