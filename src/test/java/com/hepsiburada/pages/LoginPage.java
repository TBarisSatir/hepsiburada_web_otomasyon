package com.hepsiburada.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {

    private static final String LOGIN_URL = "https://giris.hepsiburada.com";

    private final By usernameInput = By.cssSelector(
            "#txtUserName, input[name='username'][autocomplete='username']"
    );
    private final By passwordInput = By.id("txtPassword");
    private final By submitButton = By.id("btnLogin");

    /** Doğrudan giriş sayfasına gider (ana sayfada hover + tıklama adımlarına gerek kalmaz). */
    public void open() {
        driver().get(LOGIN_URL);
        new HomePage().acceptCookiesIfPresent();
    }

    public void enterUsername(String username) {
        WebElement input = waitFor(10).until(
                ExpectedConditions.visibilityOfElementLocated(usernameInput)
        );
        input.click();
        typeHuman(input, username);
        pause(400, 800);
    }

    public void enterPassword(String password) {
        WebElement input = waitFor(10).until(
                ExpectedConditions.visibilityOfElementLocated(passwordInput)
        );
        input.click();
        typeHuman(input, password);
        pause(400, 800);
    }

    public void clickSubmit() {
        WebElement button = waitFor(10).until(
                ExpectedConditions.elementToBeClickable(submitButton)
        );

        pause(500, 1200);

        // Normal (güvenilir) tıklama; sadece başka öğe üstünü kapatıyorsa JS yedeği
        clickWithJsFallback(button);
    }
}