package dev.cqb13.crosswordgenerator;

import dev.cqb13.crosswordgenerator.generator.Generator;
import dev.cqb13.crosswordgenerator.generator.setup.GridSetup;
import dev.cqb13.crosswordgenerator.generator.setup.Word;
import dev.cqb13.crosswordgenerator.generator.setup.WordLoader;
import javafx.application.Application;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.awt.Dimension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class CrosswordGenerator extends Application {
    private int width = 5;
    private GridPane gridpane;
    private double screenWidth;
    private double screenHeight;
    private ArrayList<ArrayList<TextField>> displayGrid;
    private ArrayList<ArrayList<Character>> crosswordGrid;
    private final String WHITE_STYLE = "-fx-control-inner-background: #FFFFFF";
    private final String BLACK_STYLE = "-fx-control-inner-background: #000000";

    @Override
    public void start(Stage stage) {
        crosswordGrid = new ArrayList<>();
        this.gridpane = new GridPane();
        HBox vbox = new HBox();
        vbox.setAlignment(Pos.CENTER);
        HBox horizontalCenterContainer = new HBox();
        horizontalCenterContainer.setAlignment(Pos.CENTER);
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screenSize.getWidth();
        screenHeight = screenSize.getHeight();
        Scene scene = new Scene(horizontalCenterContainer, screenWidth, screenHeight);
        displayGrid = new ArrayList<>();
        for (int y = 0; y < this.width; y++) {
            displayGrid.add(new ArrayList<>());
            for (int x = 0; x < this.width; x++) {
                TextField button = createButton(screenHeight < screenWidth ? screenHeight / this.width - 30 : screenWidth / this.width);
                gridpane.add(button, x, y);
                displayGrid.get(y).add(button);
            }
        }

        // Width and Height options
        VBox widthAndHeightContainer = new VBox();
        widthAndHeightContainer.setAlignment(Pos.CENTER);
        HBox sizeContainer = new HBox();
        Label sizeLabel = new Label("Size: ");
        TextField widthInput = new TextField(String.valueOf(this.width));
        widthInput.setMinWidth(30);
        widthInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                widthInput.setText(newValue.replaceAll("\\D", ""));
            } else if(newValue.length() > 5) {
                widthInput.setText(newValue.substring(0, 5));
            }
        });
        Button resizeButton = new Button("Resize");
        resizeButton.setOnAction(actionEvent -> {
            this.resize(Integer.parseInt(widthInput.getText()));
        });
        sizeContainer.getChildren().addAll(sizeLabel, widthInput, resizeButton);
        sizeContainer.setAlignment(Pos.CENTER_LEFT);
        sizeContainer.setSpacing(5);
        HBox frequencyContainer = new HBox();
        Label frequencyLabel = new Label("Minimum word frequency (0-9): ");
        TextField frequencyInput = new TextField(String.valueOf(8));
        frequencyInput.setMinWidth(30);
        frequencyInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                frequencyInput.setText(newValue.replaceAll("\\D", ""));
            } else if(newValue.length() > 1) {
                frequencyInput.setText(newValue.substring(0, 1));
            }
        });
        frequencyContainer.getChildren().addAll(frequencyLabel, frequencyInput);
        frequencyContainer.setAlignment(Pos.CENTER_LEFT);
        frequencyContainer.setSpacing(5);
        HBox buttonContainer = new HBox();
        Button generateButton = new Button("Generate Grid");
        generateButton.setOnAction(actionEvent -> {
            try {
                this.generate(Integer.parseInt(frequencyInput.getText()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Button clearWordsButton = new Button("Clear Words");
        clearWordsButton.setOnAction(actionEvent -> {
            gridpane.getChildren().forEach(node -> {
                TextField textField = (TextField) node;
                textField.textProperty().setValue("");
            });
        });
        Button clearGridButton = new Button("Clear Grid");
        clearGridButton.setOnAction(actionEvent -> {
            gridpane.getChildren().forEach(node -> {
                TextField textField = (TextField) node;
                textField.textProperty().setValue("");
                textField.setStyle(WHITE_STYLE);
            });
        });
        buttonContainer.setSpacing(5);
        buttonContainer.getChildren().addAll(generateButton, clearWordsButton, clearGridButton);
        widthAndHeightContainer.setPadding(new Insets(10));
        widthAndHeightContainer.setSpacing(5);
        widthAndHeightContainer.getChildren().addAll(sizeContainer, frequencyContainer, buttonContainer);
        vbox.getChildren().addAll(gridpane, widthAndHeightContainer);
        horizontalCenterContainer.getChildren().add(vbox);

        //TODO: add field to set the amount of grids to generate
        //TODO: add buttons to switch between grids, should looop when at end
        //TODO: add indicator with current grid and total amount of girds

        stage.setTitle("Crossword Generator");
        stage.setScene(scene);
        stage.show();
    }

    private TextField createButton(double size) {
        TextField textField = new TextField();
        textField.setMinWidth(size);
        textField.setMinHeight(size);
        textField.setMaxWidth(size);
        textField.setMaxHeight(size);
        textField.setStyle(WHITE_STYLE);
        textField.textProperty().addListener((ov, oldValue, newValue) -> {
            if (textField.getText().length() > 1) {
                String s = textField.getText().substring(0, 1);
                textField.setText(s);
            }

            if (textField.getText().matches("[^a-zA-Z]+")) {
                textField.setText("");
            }
            textField.setText(textField.getText().toUpperCase());
        });
        textField.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        textField.setAlignment(Pos.CENTER);
        textField.setFont(Font.font("arial", FontWeight.BOLD, size / 2));
        textField.setOnMouseClicked(actionEvent -> {
            if(actionEvent.getButton() == MouseButton.SECONDARY) {
                if (textField.getStyle().equals(WHITE_STYLE)) {
                    textField.setStyle(BLACK_STYLE);
                    textField.setEditable(false);
                } else {
                    textField.setStyle(WHITE_STYLE);
                    textField.setEditable(true);
                }
            }
        });

        return textField;
    }

    private void resize(int width) {
        int MAX_SIZE = 20;
        if(width > MAX_SIZE) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Too big!");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                return;
            }
        }
        int MIN_SIZE = 5;
        if(width < MIN_SIZE) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Too small!");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                return;
            }
        }
        if (width < this.width) {
            this.gridpane.getChildren().removeIf(node -> GridPane.getColumnIndex(node) >= width);
            this.displayGrid.subList(width, this.width).clear();
            this.gridpane.getChildren().removeIf(node -> GridPane.getRowIndex(node) >= width);
            for (ArrayList<TextField> col : this.displayGrid) {
                col.subList(width, this.width).clear();
            }
        }
        if(width > this.width) {
            for(int x = this.width; x < width; x++){
                ArrayList<TextField> column = new ArrayList<>();
                for(int y = 0; y < this.width; y++) {
                    TextField button = createButton(screenHeight < screenWidth ? screenHeight / width - 30 : screenWidth / width);
                    gridpane.add(button, x, y);
                    column.add(button);
                }
                displayGrid.add(column);
            }
            for(int x = 0; x < width; x++){
                for(int y = this.width; y < width; y++){
                    TextField button = createButton(screenHeight < screenWidth ? screenHeight / width - 30 : screenWidth / width);
                    gridpane.add(button, x, y);
                    displayGrid.get(x).add(button);
                }
            }
        }
        for (ArrayList<TextField> col : displayGrid) {
            for(TextField button : col) {
                button.setMinWidth(screenHeight < screenWidth ? screenHeight / width - 30 : screenWidth / width);
                button.setMinHeight(screenHeight < screenWidth ? screenHeight / width - 30 : screenWidth / width);
                button.setMaxWidth(screenHeight < screenWidth ? screenHeight / width - 30 : screenWidth / width);
                button.setMaxHeight(screenHeight < screenWidth ? screenHeight / width - 30 : screenWidth / width);
                button.setFont(Font.font("arial", FontWeight.BOLD, (screenHeight < screenWidth ? screenHeight / width - 30 : screenWidth / width) / 2));
            }
        }
        this.width = width;
    }

    private void generate(int frequency) throws IOException {
        this.crosswordGrid.clear();
        for (ArrayList<TextField> row : displayGrid) {
            ArrayList<Character> newRow = new ArrayList<>();
            for(TextField text : row) {
                String value = text.getText();
                if(text.getStyle().equals(BLACK_STYLE))
                    newRow.add('#');
                else if(value.isEmpty())
                    newRow.add('-');
                else
                    newRow.add((char) (text.getText().toCharArray()[0] + 32));
            }
            this.crosswordGrid.add(newRow);
        }

        GridSetup gridSetup = WordLoader.findLimits(this.crosswordGrid);

        ArrayList<Word> words = WordLoader.loadWordList(gridSetup, frequency);
        System.out.println(words.size());

        Generator gen = new Generator(words, gridSetup, this.crosswordGrid);
        ArrayList<ArrayList<ArrayList<Character>>> output = gen.generate(1);
        System.out.println(output.getFirst());
        if(output.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR, "No possible solution");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                return;
            }
        }

        gridpane.getChildren().forEach(node -> {
            TextField textField = (TextField) node;
            textField.textProperty().setValue(output.getFirst().get(GridPane.getRowIndex(node)).get(GridPane.getColumnIndex(node)).toString());
        });

    }

    public static void main(String[] args) {
        launch();
    }
}