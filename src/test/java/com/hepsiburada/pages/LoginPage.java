package com.hepsiburada.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.hepsiburada.helper.ElementHelper;

public class LoginPage extends BasePage {

    private static final String LOGIN_URL = "https://giris.hepsiburada.com";

    // Locator'lar: resources/element-infos/login.json
    private final By usernameInput = ElementHelper.getBy("login_username_input");
    private final By passwordInput = ElementHelper.getBy("login_password_input");
    private final By submitButton = ElementHelper.getBy("login_submit_button");

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