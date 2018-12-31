package gui;

import core.Card;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class CardEditor extends CardViewer{
    @FXML protected BorderPane mainPane;
    @FXML protected TextField authorField;
    @FXML protected TextField dateField;
    @FXML protected TextField additionalField;
    @FXML protected TextArea cardTextArea;

    public void init(){
        // verify only legal characters are used in card text
        cardTextArea.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    ((StringProperty)observable).setValue(Card.cleanForCard(newValue));
                }
        );
    }

    @Override
    public void setAuthor(String author) {
        authorField.setText(author);
    }

    @Override
    public void setDate(String date) {
        dateField.setText(date);
    }

    @Override
    public void setAdditionalInfo(String additionalInfo) {
        additionalField.setText(additionalInfo);
    }

    @Override
    public void setText(String text) {
        cardTextArea.setText(text);
    }

    @Override
    public String getAuthor() {
        return authorField.getText();
    }

    @Override
    public String getDate() {
        return dateField.getText();
    }

    @Override
    public String getAdditionalInfo() {
        return additionalField.getText();
    }

    @Override
    public String getText() {
        return cardTextArea.getText();
    }

    @Override
    public Pane getPane() {
        return mainPane;
    }

}