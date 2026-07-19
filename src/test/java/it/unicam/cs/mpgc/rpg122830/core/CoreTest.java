package it.unicam.cs.mpgc.rpg122830.core;

import it.unicam.cs.mpgc.rpg122830.core.exception.InsufficientFundsException;
import it.unicam.cs.mpgc.rpg122830.core.model.*;
import it.unicam.cs.mpgc.rpg122830.core.service.*;
import it.unicam.cs.mpgc.rpg122830.persistence.api.SaveManager;
import it.unicam.cs.mpgc.rpg122830.persistence.impl.JsonSaveManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class CoreTest {

    private Player player;
    private Vehicle car;

    @BeforeEach
    public void setUp() {
        player = new Player();
        car = new Vehicle("Toyota AE86 Trueno", 1985, 10000);
    }

    @Test
    public void testPlayerCashAndException() {
        assertEquals(10000, player.getCash());
        player.addCash(500);
        assertEquals(10500, player.getCash());

        assertThrows(InsufficientFundsException.class, () -> player.deductCash(20000));
        assertDoesNotThrow(() -> player.deductCash(500));
        assertEquals(10000, player.getCash());
    }

    @Test
    public void testVehicleComponentsAndValues() {
        // Initially empty
        assertEquals(0.0, car.getAverageCondition());
        assertEquals(0, car.getCurrentValue());
        assertEquals(0, car.getPerformanceScore());

        // Install a component
        Component engine = new Engine("Factory 4AGE", 0.50, 40);
        Component old = car.installComponent(engine);
        assertNull(old);

        // Average condition of 4 slots: Engine (0.5), Turbo (0), Brakes (0), Susp (0) -> 0.5 / 4 = 0.125
        assertEquals(0.125, car.getAverageCondition());
        assertEquals(1250, car.getCurrentValue()); // 10000 * 0.125
        assertEquals(20, car.getPerformanceScore()); // 40 * 0.5

        // Replace component
        Component raceEngine = new Engine("Racing 4AGE", 1.0, 100);
        Component replaced = car.installComponent(raceEngine);
        assertEquals(engine, replaced);

        assertEquals(0.25, car.getAverageCondition()); // 1.0 / 4
        assertEquals(2500, car.getCurrentValue());
        assertEquals(100, car.getPerformanceScore());
    }

    @Test
    public void testComponentFactory() {
        Component rustyEngine = ComponentFactory.createRustyComponent(ComponentType.ENGINE);
        assertNotNull(rustyEngine);
        assertTrue(rustyEngine.getCondition() >= 0.10 && rustyEngine.getCondition() <= 0.30);
        assertEquals(ComponentType.ENGINE, rustyEngine.getType());

        Component racingTurbo = ComponentFactory.createRacingComponent(ComponentType.TURBOCHARGER);
        assertNotNull(racingTurbo);
        assertTrue(racingTurbo.getCondition() >= 0.90 && racingTurbo.getCondition() <= 1.00);
        assertEquals(ComponentType.TURBOCHARGER, racingTurbo.getType());
    }

    @Test
    public void testRepairService() throws InsufficientFundsException {
        RepairService repairService = new RepairService();
        Component wornSusp = new Suspension("Leaky Coilover", 0.20, 50);

        int cost = repairService.calculateRepairCost(wornSusp);
        // missing condition = 0.8, baseCost = 50 * 6 = 300. Expected cost = 0.8 * 300 = 240
        assertEquals(240, cost);

        repairService.repairComponent(player, wornSusp);
        assertEquals(1.0, wornSusp.getCondition());
        assertEquals(10000 - 240, player.getCash());
        // XP gained = (1.0 - 0.2) * 40 = 32 XP. Starting rep 0 -> level 1.
        assertTrue(player.getReputation() > 0);
    }

    @Test
    public void testSaveAndLoad(@TempDir File tempDir) throws IOException {
        SaveManager<Player> saveManager = new JsonSaveManager();
        File saveFile = new File(tempDir, "savegame.json");

        // Set up player state
        player.addReputation(250); // Level 3
        Component inventoryItem = new Turbocharger("Garrett GT35R", 0.95, 55);
        player.addComponent(inventoryItem);

        Vehicle playerCar = new Vehicle("Mazda RX-7 FD", 1994, 15000);
        playerCar.installComponent(new Engine("13B-REW Rotary", 0.80, 80));
        player.addVehicle(playerCar);

        // Save
        saveManager.save(player, saveFile);
        assertTrue(saveFile.exists());

        // Load
        Player loadedPlayer = saveManager.load(saveFile);
        assertNotNull(loadedPlayer);
        assertEquals(player.getCash(), loadedPlayer.getCash());
        assertEquals(3, loadedPlayer.getLevel());
        assertEquals(1, loadedPlayer.getInventory().size());
        assertEquals(1, loadedPlayer.getVehicles().size());

        Component loadedComp = loadedPlayer.getInventory().get(0);
        assertEquals(ComponentType.TURBOCHARGER, loadedComp.getType());
        assertEquals("Garrett GT35R", loadedComp.getName());
        assertEquals(0.95, loadedComp.getCondition());
        assertEquals(55, loadedComp.getPerformanceBonus());

        Vehicle loadedCar = loadedPlayer.getVehicles().get(0);
        assertEquals("Mazda RX-7 FD", loadedCar.getModelName());
        assertEquals(1994, loadedCar.getYear());
        assertEquals(15000, loadedCar.getBaseValue());
        assertNotNull(loadedCar.getComponent(ComponentType.ENGINE));
        assertEquals("13B-REW Rotary", loadedCar.getComponent(ComponentType.ENGINE).getName());
    }
}
