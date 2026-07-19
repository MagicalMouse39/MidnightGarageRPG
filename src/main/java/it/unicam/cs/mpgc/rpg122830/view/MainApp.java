package it.unicam.cs.mpgc.rpg122830.view;

import it.unicam.cs.mpgc.rpg122830.core.exception.InsufficientFundsException;
import it.unicam.cs.mpgc.rpg122830.core.model.*;
import it.unicam.cs.mpgc.rpg122830.core.service.*;
import it.unicam.cs.mpgc.rpg122830.persistence.api.SaveManager;
import it.unicam.cs.mpgc.rpg122830.persistence.impl.JsonSaveManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private Player player;
    private final RepairService repairService = new RepairService();
    private final MarketService marketService = new MarketService();
    private final SaveManager<Player> saveManager = new JsonSaveManager();
    private final File saveFile = new File("savegame.json");

    // UI elements to update globally
    private Label cashLabel;
    private Label repLabel;
    private Label levelLabel;

    // Garage View elements
    private ComboBox<Vehicle> vehicleComboBox;
    private Label carInfoLabel;
    private TableView<ComponentRow> componentTable;
    private ListView<Component> inventoryListView;
    private Button repairBtn;
    private Button replaceBtn;
    private Button unequipBtn;

    // Market View elements
    private ListView<Vehicle> marketVehiclesListView;
    private ListView<Component> marketComponentsListView;
    private ListView<Vehicle> playerSellVehiclesListView;
    private ListView<Component> playerSellComponentsListView;

    // Race View elements
    private ComboBox<RaceStrategy> raceSelector;
    private Label raceDescLabel;
    private TextArea raceLogArea;
    private Button startRaceBtn;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        initGameState();

        primaryStage.setTitle("Midnight Garage RPG");

        // Top Status Bar
        HBox statusBar = createStatusBar();

        // Tabs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab garageTab = new Tab("Garage (Officina)", createGarageView());
        Tab marketTab = new Tab("Market (Mercato)", createMarketView());
        Tab raceTab = new Tab("Races (Gare)", createRaceView());

        tabPane.getTabs().addAll(garageTab, marketTab, raceTab);

        // Main Layout
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(statusBar, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 950, 650);
        // Load custom styles
        try {
            String cssPath = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Could not load style.css. Proceeding with defaults.");
        }

        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial selection setup
        refreshUI();
    }

    private void initGameState() {
        if (saveFile.exists()) {
            try {
                player = saveManager.load(saveFile);
                System.out.println("Save game loaded successfully.");
            } catch (IOException e) {
                System.err.println("Failed to load save file. Initializing new game.");
                initializeNewGame();
            }
        } else {
            initializeNewGame();
        }
        marketService.refreshMarket(player.getLevel());
    }

    private void initializeNewGame() {
        player = new Player();
        // Starter car
        Vehicle starterCar = new Vehicle("Toyota AE86 Trueno", 1985, 7500);
        // Install some worn OEM components so the user starts with something to repair
        starterCar.installComponent(new Engine("OEM 4A-GE", 0.65, 42));
        starterCar.installComponent(new Brakes("OEM Discs", 0.50, 15));
        starterCar.installComponent(new Suspension("OEM Springs", 0.60, 15));
        // Turbocharger is missing initially (requires purchase to boost power)

        player.addVehicle(starterCar);

        // Put some starting items in inventory
        player.addComponent(ComponentFactory.createRustyComponent(ComponentType.TURBOCHARGER));
    }

    private void saveGame() {
        try {
            saveManager.save(player, saveFile);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Game Saved Successfully!");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save the game: " + e.getMessage());
        }
    }

    // --- STATUS BAR ---
    private HBox createStatusBar() {
        HBox bar = new HBox(20);
        bar.setPadding(new Insets(10));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 6; -fx-border-color: #2c2c2c; -fx-border-radius: 6;");

        Label nameLabel = new Label("MIDNIGHT GARAGE");
        nameLabel.getStyleClass().add("header-label");

        cashLabel = new Label();
        cashLabel.getStyleClass().addAll("sub-header-label", "status-value-label");
        
        repLabel = new Label();
        repLabel.getStyleClass().add("sub-header-label");

        levelLabel = new Label();
        levelLabel.getStyleClass().addAll("sub-header-label", "status-value-label");
        levelLabel.setStyle("-fx-text-fill: #00e5ff;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button saveBtn = new Button("Save Game");
        saveBtn.setOnAction(e -> saveGame());

        Button resetBtn = new Button("Reset Game");
        resetBtn.setStyle("-fx-background-color: #c62828;");
        resetBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Reset game? Current progress will be lost.", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    if (saveFile.exists()) {
                        saveFile.delete();
                    }
                    initializeNewGame();
                    marketService.refreshMarket(1);
                    refreshUI();
                }
            });
        });

        bar.getChildren().addAll(nameLabel, new Separator(), cashLabel, repLabel, levelLabel, spacer, saveBtn, resetBtn);
        return bar;
    }

    // --- GARAGE VIEW ---
    private Parent createGarageView() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        // Column Constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(65);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(35);
        grid.getColumnConstraints().addAll(col1, col2);

        RowConstraints row1 = new RowConstraints();
        row1.setVgrow(Priority.ALWAYS);
        grid.getRowConstraints().add(row1);

        // Left Side: Active Car Specs and Installed Components
        VBox leftPane = new VBox(10);
        leftPane.getStyleClass().add("card-panel");

        HBox carSelectionHeader = new HBox(10);
        carSelectionHeader.setAlignment(Pos.CENTER_LEFT);
        Label selCarLabel = new Label("Select Car:");
        selCarLabel.getStyleClass().add("sub-header-label");
        
        vehicleComboBox = new ComboBox<>();
        vehicleComboBox.setPrefWidth(250);
        vehicleComboBox.setOnAction(e -> handleVehicleSelection());

        carSelectionHeader.getChildren().addAll(selCarLabel, vehicleComboBox);

        carInfoLabel = new Label("Model: - | Est. Value: -");
        carInfoLabel.getStyleClass().add("sub-header-label");

        // Table for Components
        componentTable = new TableView<>();
        componentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<ComponentRow, String> typeCol = new TableColumn<>("Component");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().getDisplayName()));

        TableColumn<ComponentRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<ComponentRow, String> condCol = new TableColumn<>("Condition");
        condCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getConditionString()));

        TableColumn<ComponentRow, String> bonusCol = new TableColumn<>("Bonus");
        bonusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBonusString()));

        componentTable.getColumns().addAll(typeCol, nameCol, condCol, bonusCol);
        componentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> updateGarageButtons());

        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        repairBtn = new Button("Repair ($0)");
        repairBtn.setOnAction(e -> handleRepair());

        unequipBtn = new Button("Uninstall / Unequip");
        unequipBtn.setStyle("-fx-background-color: #555555;");
        unequipBtn.setOnAction(e -> handleUnequip());

        actionButtons.getChildren().addAll(repairBtn, unequipBtn);

        leftPane.getChildren().addAll(carSelectionHeader, carInfoLabel, componentTable, actionButtons);
        VBox.setVgrow(componentTable, Priority.ALWAYS);

        // Right Side: Spare Parts Inventory to Install
        VBox rightPane = new VBox(10);
        rightPane.getStyleClass().add("card-panel");

        Label invLabel = new Label("Spare Parts Inventory");
        invLabel.getStyleClass().add("sub-header-label");

        inventoryListView = new ListView<>();
        inventoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> updateGarageButtons());

        replaceBtn = new Button("Install in Car");
        replaceBtn.setMaxWidth(Double.MAX_VALUE);
        replaceBtn.setOnAction(e -> handleInstall());

        rightPane.getChildren().addAll(invLabel, inventoryListView, replaceBtn);
        VBox.setVgrow(inventoryListView, Priority.ALWAYS);

        grid.add(leftPane, 0, 0);
        grid.add(rightPane, 1, 0);

        return grid;
    }

    // --- MARKET VIEW ---
    private Parent createMarketView() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        // Column Constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Left Side: Buy Vehicles and Parts
        VBox leftPane = new VBox(10);
        leftPane.getStyleClass().add("card-panel");

        Label shopCarsLabel = new Label("Used Vehicles for Sale");
        shopCarsLabel.getStyleClass().add("sub-header-label");

        marketVehiclesListView = new ListView<>();
        Button buyCarBtn = new Button("Buy Selected Vehicle");
        buyCarBtn.getStyleClass().add("btn-buy");
        buyCarBtn.setOnAction(e -> handleBuyVehicle());

        Label shopPartsLabel = new Label("Daily Components");
        shopPartsLabel.getStyleClass().add("sub-header-label");

        marketComponentsListView = new ListView<>();
        Button buyPartBtn = new Button("Buy Selected Component");
        buyPartBtn.getStyleClass().add("btn-buy");
        buyPartBtn.setOnAction(e -> handleBuyComponent());

        Button refreshBtn = new Button("Pass Day / Refresh Market ($50)");
        refreshBtn.setStyle("-fx-background-color: #d84315;");
        refreshBtn.setOnAction(e -> handleRefreshMarketPrice());

        leftPane.getChildren().addAll(shopCarsLabel, marketVehiclesListView, buyCarBtn, new Separator(), shopPartsLabel, marketComponentsListView, buyPartBtn, refreshBtn);
        VBox.setVgrow(marketVehiclesListView, Priority.ALWAYS);
        VBox.setVgrow(marketComponentsListView, Priority.ALWAYS);

        // Right Side: Sell Vehicles and Parts
        VBox rightPane = new VBox(10);
        rightPane.getStyleClass().add("card-panel");

        Label sellCarsLabel = new Label("Your Vehicles (Sell for 75% Value)");
        sellCarsLabel.getStyleClass().add("sub-header-label");

        playerSellVehiclesListView = new ListView<>();
        Button sellCarBtn = new Button("Sell Selected Vehicle");
        sellCarBtn.getStyleClass().add("btn-sell");
        sellCarBtn.setOnAction(e -> handleSellVehicle());

        Label sellPartsLabel = new Label("Your Inventory Parts (Sell for 50% Value)");
        sellPartsLabel.getStyleClass().add("sub-header-label");

        playerSellComponentsListView = new ListView<>();
        Button sellPartBtn = new Button("Sell Selected Component");
        sellPartBtn.getStyleClass().add("btn-sell");
        sellPartBtn.setOnAction(e -> handleSellComponent());

        rightPane.getChildren().addAll(sellCarsLabel, playerSellVehiclesListView, sellCarBtn, new Separator(), sellPartsLabel, playerSellComponentsListView, sellPartBtn);
        VBox.setVgrow(playerSellVehiclesListView, Priority.ALWAYS);
        VBox.setVgrow(playerSellComponentsListView, Priority.ALWAYS);

        grid.add(leftPane, 0, 0);
        grid.add(rightPane, 1, 0);

        return grid;
    }

    // --- RACE VIEW ---
    private Parent createRaceView() {
        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(15));
        mainBox.getStyleClass().add("card-panel");

        Label header = new Label("Street Racing League");
        header.getStyleClass().add("header-label");

        HBox selectorRow = new HBox(15);
        selectorRow.setAlignment(Pos.CENTER_LEFT);

        Label selectLabel = new Label("Choose Race Type:");
        selectLabel.getStyleClass().add("sub-header-label");

        raceSelector = new ComboBox<>();
        raceSelector.setPrefWidth(250);
        raceSelector.setOnAction(e -> updateRaceSelection());

        startRaceBtn = new Button("START RACE");
        startRaceBtn.setStyle("-fx-background-color: #2e7d32; -fx-font-size: 14px;");
        startRaceBtn.setOnAction(e -> handleStartRace());

        selectorRow.getChildren().addAll(selectLabel, raceSelector, startRaceBtn);

        raceDescLabel = new Label("Description: Select a race above.");
        raceDescLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-style: italic;");

        Label logHeader = new Label("Live Simulation Ticker:");
        logHeader.getStyleClass().add("sub-header-label");

        raceLogArea = new TextArea();
        raceLogArea.setEditable(false);
        raceLogArea.setPrefHeight(350);
        raceLogArea.setPromptText("Logs will print here during race...");

        mainBox.getChildren().addAll(header, selectorRow, raceDescLabel, new Separator(), logHeader, raceLogArea);
        VBox.setVgrow(raceLogArea, Priority.ALWAYS);

        return mainBox;
    }

    // --- UI UPDATERS AND HANDLERS ---
    private void refreshUI() {
        // Status Bar
        cashLabel.setText(String.format("Cash: $%d", player.getCash()));
        repLabel.setText(String.format("Reputation: %d XP", player.getReputation()));
        levelLabel.setText(String.format("Mechanic Level: %d", player.getLevel()));

        // Active Vehicle ComboBox
        Vehicle selectedCar = vehicleComboBox.getSelectionModel().getSelectedItem();
        ObservableList<Vehicle> ownedCars = FXCollections.observableArrayList(player.getVehicles());
        vehicleComboBox.setItems(ownedCars);
        if (selectedCar != null && ownedCars.contains(selectedCar)) {
            vehicleComboBox.getSelectionModel().select(selectedCar);
        } else if (!ownedCars.isEmpty()) {
            vehicleComboBox.getSelectionModel().select(0);
        } else {
            vehicleComboBox.getSelectionModel().clearSelection();
        }
        handleVehicleSelection();

        // Inventory
        ObservableList<Component> inventory = FXCollections.observableArrayList(player.getInventory());
        inventoryListView.setItems(inventory);
        playerSellComponentsListView.setItems(inventory);

        // Shop / Sell lists
        marketVehiclesListView.setItems(FXCollections.observableArrayList(marketService.getAvailableVehicles()));
        
        // Show components for sale with prices
        ObservableList<Component> shopComps = FXCollections.observableArrayList(marketService.getAvailableComponents());
        marketComponentsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Component item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - Price: $%d", item.toString(), marketService.getComponentPrice(item)));
                }
            }
        });
        marketComponentsListView.setItems(shopComps);

        // Player owned list for selling
        playerSellVehiclesListView.setItems(ownedCars);

        // Races setup
        if (raceSelector.getItems().isEmpty()) {
            List<RaceStrategy> strategies = new ArrayList<>();
            strategies.add(new DragRaceStrategy());
            strategies.add(new DriftRaceStrategy(player.getLevel()));
            raceSelector.setItems(FXCollections.observableArrayList(strategies));
            raceSelector.getSelectionModel().select(0);
        } else {
            // Update strategies with new player level
            RaceStrategy selectedStrategy = raceSelector.getSelectionModel().getSelectedItem();
            List<RaceStrategy> strategies = new ArrayList<>();
            strategies.add(new DragRaceStrategy());
            strategies.add(new DriftRaceStrategy(player.getLevel()));
            raceSelector.setItems(FXCollections.observableArrayList(strategies));
            
            // Re-select
            if (selectedStrategy instanceof DragRaceStrategy) {
                raceSelector.getSelectionModel().select(0);
            } else {
                raceSelector.getSelectionModel().select(1);
            }
        }
        updateRaceSelection();
        updateGarageButtons();
    }

    private void handleVehicleSelection() {
        Vehicle car = vehicleComboBox.getSelectionModel().getSelectedItem();
        if (car == null) {
            carInfoLabel.setText("No active vehicle. Purchase one at the market!");
            componentTable.setItems(FXCollections.observableArrayList());
            updateGarageButtons();
            return;
        }

        carInfoLabel.setText(String.format("Car: %d %s | Base Value: $%d | Est. Value: $%d | Performance Score: %d", 
                car.getYear(), car.getModelName(), car.getBaseValue(), car.getCurrentValue(), car.getPerformanceScore()));

        // Popola la tabella dei componenti installati
        ObservableList<ComponentRow> rows = FXCollections.observableArrayList();
        for (ComponentType type : ComponentType.values()) {
            Component comp = car.getComponent(type);
            rows.add(new ComponentRow(type, comp));
        }
        componentTable.setItems(rows);
        updateGarageButtons();
    }

    private void updateGarageButtons() {
        Vehicle car = vehicleComboBox.getSelectionModel().getSelectedItem();
        ComponentRow selectedRow = componentTable.getSelectionModel().getSelectedItem();
        Component selectedInvComponent = inventoryListView.getSelectionModel().getSelectedItem();

        // 1. Repair Button
        if (selectedRow != null && selectedRow.getComponent() != null && selectedRow.getComponent().getCondition() < 1.0) {
            Component comp = selectedRow.getComponent();
            int cost = repairService.calculateRepairCost(comp);
            repairBtn.setText(String.format("Repair %s ($%d)", comp.getType().getDisplayName(), cost));
            repairBtn.setDisable(player.getCash() < cost);
        } else {
            repairBtn.setText("Repair Component");
            repairBtn.setDisable(true);
        }

        // 2. Unequip Button
        if (selectedRow != null && selectedRow.getComponent() != null) {
            unequipBtn.setDisable(false);
        } else {
            unequipBtn.setDisable(true);
        }

        // 3. Install Button
        if (car != null && selectedInvComponent != null) {
            replaceBtn.setDisable(false);
            replaceBtn.setText(String.format("Install '%s' in Car", selectedInvComponent.getName()));
        } else {
            replaceBtn.setDisable(true);
            replaceBtn.setText("Install in Car");
        }
    }

    private void handleRepair() {
        ComponentRow selectedRow = componentTable.getSelectionModel().getSelectedItem();
        if (selectedRow == null || selectedRow.getComponent() == null) return;

        Component comp = selectedRow.getComponent();
        try {
            repairService.repairComponent(player, comp);
            refreshUI();
        } catch (InsufficientFundsException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void handleUnequip() {
        Vehicle car = vehicleComboBox.getSelectionModel().getSelectedItem();
        ComponentRow selectedRow = componentTable.getSelectionModel().getSelectedItem();
        if (car == null || selectedRow == null || selectedRow.getComponent() == null) return;

        Component comp = selectedRow.getComponent();
        car.getInstalledComponents().remove(comp.getType());
        player.addComponent(comp);

        refreshUI();
    }

    private void handleInstall() {
        Vehicle car = vehicleComboBox.getSelectionModel().getSelectedItem();
        Component selectedInvComp = inventoryListView.getSelectionModel().getSelectedItem();
        if (car == null || selectedInvComp == null) return;

        // Install
        Component oldComp = car.installComponent(selectedInvComp);
        player.removeComponent(selectedInvComp);
        
        if (oldComp != null) {
            player.addComponent(oldComp); // Old component goes to spare parts inventory
        }

        refreshUI();
    }

    // --- MARKET HANDLERS ---
    private void handleBuyVehicle() {
        Vehicle car = marketVehiclesListView.getSelectionModel().getSelectedItem();
        if (car == null) return;

        try {
            marketService.buyVehicle(player, car);
            refreshUI();
        } catch (InsufficientFundsException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void handleBuyComponent() {
        Component comp = marketComponentsListView.getSelectionModel().getSelectedItem();
        if (comp == null) return;

        try {
            marketService.buyComponent(player, comp);
            refreshUI();
        } catch (InsufficientFundsException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void handleSellVehicle() {
        Vehicle car = playerSellVehiclesListView.getSelectionModel().getSelectedItem();
        if (car == null) return;

        if (player.getVehicles().size() <= 1) {
            showAlert(Alert.AlertType.WARNING, "Warning", "You cannot sell your only vehicle! You need at least one car to play.");
            return;
        }

        marketService.sellVehicle(player, car);
        refreshUI();
    }

    private void handleSellComponent() {
        Component comp = playerSellComponentsListView.getSelectionModel().getSelectedItem();
        if (comp == null) return;

        marketService.sellComponent(player, comp);
        refreshUI();
    }

    private void handleRefreshMarketPrice() {
        try {
            player.deductCash(50);
            marketService.refreshMarket(player.getLevel());
            refreshUI();
        } catch (InsufficientFundsException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Not enough cash to refresh market!");
        }
    }

    // --- RACE HANDLERS ---
    private void updateRaceSelection() {
        RaceStrategy strategy = raceSelector.getSelectionModel().getSelectedItem();
        if (strategy == null) {
            raceDescLabel.setText("No strategy selected.");
            return;
        }
        raceDescLabel.setText("Description: " + strategy.getDescription());
    }

    private void handleStartRace() {
        Vehicle car = vehicleComboBox.getSelectionModel().getSelectedItem();
        RaceStrategy strategy = raceSelector.getSelectionModel().getSelectedItem();

        if (car == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "You need to select a vehicle to race!");
            return;
        }

        if (strategy == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No race selected.");
            return;
        }

        // Validate car can race (requires at least an Engine)
        if (car.getComponent(ComponentType.ENGINE) == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Your car cannot race without an ENGINE installed!");
            return;
        }

        startRaceBtn.setDisable(true);
        raceLogArea.clear();

        // Run the simulation strategy
        RaceResult result = strategy.executeRace(car);

        // Apply rewards
        player.addCash(result.getRewardCash());
        player.addReputation(result.getRewardReputation());

        // Refresh market daily components after race
        marketService.refreshMarket(player.getLevel());

        // Display results with a typewriter tick effect
        String[] lines = result.getNarrativeLog().split("\n");
        Timeline timeline = new Timeline();
        
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(350.0 * (i + 1)), event -> {
                    raceLogArea.appendText(line + "\n");
                })
            );
        }

        timeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(350.0 * (lines.length + 1)), event -> {
                raceLogArea.appendText(String.format("\n[RACE OVER] Result: %s! Earned: $%d, +%d XP\n", 
                        result.isWon() ? "VICTORY" : "DEFEAT", result.getRewardCash(), result.getRewardReputation()));
                startRaceBtn.setDisable(false);
                refreshUI();
            })
        );

        timeline.play();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- HELPER WRAPPER CLASS FOR TABLEVIEW ROWS ---
    public static class ComponentRow {
        private final ComponentType type;
        private final Component component;

        public ComponentRow(ComponentType type, Component component) {
            this.type = type;
            this.component = component;
        }

        public ComponentType getType() {
            return type;
        }

        public Component getComponent() {
            return component;
        }

        public String getName() {
            return (component != null) ? component.getName() : "[Not Installed / Empty]";
        }

        public String getConditionString() {
            return (component != null) ? String.format("%.0f%%", component.getCondition() * 100) : "-";
        }

        public String getBonusString() {
            return (component != null) ? String.format("+%d HP/Stats", component.getEffectiveBonus()) : "-";
        }
    }
}
