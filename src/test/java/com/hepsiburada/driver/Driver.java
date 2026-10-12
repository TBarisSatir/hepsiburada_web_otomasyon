package com.hepsiburada.driver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * WebDriver'ın tek sahibi. Sayfa sınıfları sürücüye buradan ulaşır.
 */
public final class Driver {

    private static WebDriver driver;

    private Driver() {
    }

    /** Chrome'u bot tespitini azaltan ayarlarla başlatır. */
    public static WebDriver start() {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-blink-features=AutomationControlled");

        options.setExperimentalOption(
                "excludeSwitches",
                Collections.singletonList("enable-automation")
        );
        options.setExperimentalOption("useAutomationExtension", false);

        // Chrome'un "şifreyi kaydet" pencerelerini kapatır
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        return driver;
    }

    public static WebDriver get() {
        if (driver == null) {
            throw new IllegalStateException(
                    "Tarayıcı açık değil. Önce 'open chrome' adımı çalışmalı."
            );
        }
        return driver;
    }

    /** Tarayıcıyı kapatır; zaten kapalıysa bir şey yapmaz. */
    public static void quit() {
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                driver = null;
            }
        }
    }
}