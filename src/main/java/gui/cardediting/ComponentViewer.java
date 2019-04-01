package gui.cardediting;

import core.Block;
import core.Card;
import core.HashIdentifiedSpeechComponent;
import core.Speech;
import gui.blockediting.BlockEditor;
import gui.speechtools.SpeechEditor;
import gui.speechtools.SpeechViewer;
import io.iocontrollers.IOController;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.List;

public class ComponentViewer {
    private CardEditor cardEditor;
    private CardCutter cardCutter;
    private SpeechEditor speechEditor;
    private SpeechViewer speechViewer;
    private BorderPane viewerPane;
    private BlockEditor blockEditor;
    private SimpleBooleanProperty editMode = new SimpleBooleanProperty(true);
    private enum ViewType {BLOCK, CARD, SPEECH};
    private ViewType currentViewMode = ViewType.CARD;

    public void bindEditMode(Property<Boolean> property){
        property.bindBidirectional(editMode);
    }

    public void open(HashIdentifiedSpeechComponent component){
        save();

        if (component.getClass().isAssignableFrom(Card.class)){
            currentViewMode = ViewType.CARD;
            if (editMode.get()){
                cardEditor.open((Card) component);
            }else{
                cardCutter.open((Card) component);
            }
        }else if (component.getClass().isAssignableFrom(Block.class)){
            currentViewMode = ViewType.BLOCK;
            blockEditor.open((Block) component);
        }else if (component.getClass().isAssignableFrom(Speech.class)){
            currentViewMode = ViewType.SPEECH;
            if (editMode.get()) {
                speechEditor.open((Speech) component);
            }else{
                speechViewer.open((Speech) component);
            }
        }else{
            throw new IllegalArgumentException("Unrecognized type: " + component.getClass());
        }
        updateViewerPane();

    }
    public void init(BorderPane viewerPane){
        this.viewerPane = viewerPane;
        // load the card viewers
        try {
            FXMLLoader editorLoader = new FXMLLoader(getClass().getClassLoader().getResource("card_editor.fxml"));
            editorLoader.load();
            cardEditor = editorLoader.getController();
            cardEditor.init();

            FXMLLoader cutterLoader = new FXMLLoader(getClass().getClassLoader().getResource("card_cutter.fxml"));
            cutterLoader.load();
            cardCutter = cutterLoader.getController();
            cardCutter.init();

            FXMLLoader blockLoader = new FXMLLoader(getClass().getClassLoader().getResource("block_editor.fxml"));
            blockLoader.load();
            blockEditor = blockLoader.getController();
            blockEditor.init();

            FXMLLoader speechEditLoader = new FXMLLoader(getClass().getClassLoader().getResource("speech_editor.fxml"));
            speechEditLoader.load();
            speechEditor = speechEditLoader.getController();
            speechEditor.init();

            FXMLLoader speechViewLoader = new FXMLLoader(getClass().getClassLoader().getResource("speech_viewer.fxml"));
            speechViewLoader.load();
            speechViewer = speechViewLoader.getController();

            editMode.set(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Pane getPane() {
        switch (currentViewMode) {
            case BLOCK:
                return blockEditor.getPane();
            case CARD:
                if (editMode.get()) {
                    return cardEditor.getPane();
                } else {
                    return cardCutter.getPane();
                }
            case SPEECH:
                if (editMode.get()) {
                    return speechEditor.getPane();
                } else {
                    return speechViewer.getPane();
                }

            default:
                throw new IllegalStateException("Invalid View Mode");
        }
    }

    public void save(){
        save(null);
    }

    public void save(List<String> path){
        Task task = new Task<>() {
            @Override
            protected Object call() throws Exception {
                MainGui.getActiveGUI().getScene().getRoot().setCursor(Cursor.WAIT);
                switch (currentViewMode) {
                    case BLOCK:
                        blockEditor.save();
                        break;

                    case CARD:
                        if (editMode.get()) {
                            cardEditor.save(path);
                        } else {
                            cardCutter.save(path);
                        }
                        break;

                    case SPEECH:
                        speechEditor.save();
                        break;

                    default:
                        throw new IllegalStateException("Invalid View Mode");
                }
                MainGui.getActiveGUI().getScene().getRoot().setCursor(Cursor.DEFAULT);
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void clear() {
    }

    public void refresh(){
        switch (currentViewMode) {
            case BLOCK:
                blockEditor.refresh();
                break;

            case CARD:
                if (editMode.get()) {
                    cardEditor.refresh();
                } else {
                    cardCutter.refresh();
                }
                break;

            case SPEECH:
                if (editMode.get()) {
                    speechEditor.refresh();
                } else {
                    speechEditor.refresh();
                }

                break;

            default:
                throw new IllegalStateException("Invalid View Mode");
        }
    }

    public void updateViewerPane(){
        switch (currentViewMode) {
            case BLOCK:
                viewerPane.setCenter(blockEditor.getPane());
                break;

            case CARD:
                if (editMode.get()) {
                    viewerPane.setCenter(cardEditor.getPane());
                } else {
                    viewerPane.setCenter(cardCutter.getPane());
                }
                break;

            case SPEECH:
                if (editMode.get()) {
                    viewerPane.setCenter(speechEditor.getPane());
                } else {
                    viewerPane.setCenter(speechViewer.getPane());
                }

                break;

            default:
                throw new IllegalStateException("Invalid View Mode");
        }

    }

    public void togglePanes() {
        editMode.set(!editMode.get());
        updateEdit();
    }

    public void updateEdit(){
        if (currentViewMode==ViewType.CARD) {
            if (editMode.get()) {
                cardCutter.swapTo(cardEditor);
            } else {
                cardEditor.swapTo(cardCutter);
            }
        }else if (currentViewMode==ViewType.SPEECH) {
            if (editMode.get()){
                speechEditor.open(speechViewer.getSpeech());
            }else{
                speechViewer.open(speechEditor.getSpeech());
            }
        }
        updateViewerPane();
    }

    public void newCard() {
        editMode.set(true);
        currentViewMode = ViewType.CARD;
        updateViewerPane();
    }
}
