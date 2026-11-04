package com.gym;

import com.gym.service.GymService;
import com.gym.storage.JsonStorage;
import com.gym.ui.Demo;
import com.gym.ui.MenuController;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Entry point.
 *   (no arguments) -> interactive console menu
 *   --demo         -> runs a non-interactive walkthrough of every feature
 */
public class Main {

    public static void main(String[] args) throws IOException {
        GymService service = new GymService();
        JsonStorage storage = new JsonStorage();

        if (args.length > 0 && args[0].equalsIgnoreCase("--demo")) {
            new Demo(service, storage).run();
            return;
        }

        Path dataFile = Paths.get("data", "gym-data.json");
        new MenuController(service, storage, dataFile, new Scanner(System.in)).run();
    }
}
