package com.hepsiburada.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HomePage extends BasePage {

    private static final String HOME_URL = "https://www.hepsiburada.com/";

    private final By cookieAcceptButton = By.id("onetrust-accept-btn-handler");
    private final By userInfo = By.cssSelector(".user-info");
    private final By loginLink = By.id("login");
    private final By searchArea =
            By.cssSelector("div[role='search'][aria-label='Site İçi Arama']");
    private final By searchInput =
            By.cssSelector("input[data-test-id='search-bar-input']");
    private final By cartLink =
            By.cssSelector("a[href*='checkout.hepsiburada.com/sepetim']");
    // Class adları değişken (sf-OldMyAccount-...), bu yüzden id + data-test-id kullanılır
    private final By accountLink =
            By.cssSelector("#myAccount a[data-test-id='account']");

    public void open() {
        driver().get(HOME_URL);
        acceptCookiesIfPresent();
    }

    public void acceptCookiesIfPresent() {
        try {
            WebElement cookieButton = waitFor(5).until(
                    ExpectedConditions.elementToBeClickable(cookieAcceptButton)
            );
            cookieButton.click();
            pause(500, 1000);
        } catch (TimeoutException ignored) {
            // Çerez bannerı çıkmadıysa devam et
        }
    }

    public void hoverUserInfo() {
        WebElement element = waitFor(10).until(
                ExpectedConditions.visibilityOfElementLocated(userInfo)
        );
        new Actions(driver()).moveToElement(element).perform();
    }

    public void clickLogin() {
        waitFor(10).until(ExpectedConditions.elementToBeClickable(loginLink)).click();
    }

    /**
     * Arama kutusuna kelimeyi yazar ve Enter'a basar.
     * Kutu yazarken yeniden render edilebildiği için eleman her adımda yeniden bulunur.
     */
    public void searchFor(String keyword) {

        WebDriverWait wait = waitFor(15);

        // 1) Sayfa tamamen yüklensin, header/arama alanı otursun
        wait.until(d -> "complete".equals(js().executeScript("return document.readyState")));
        wait.until(ExpectedConditions.elementToBeClickable(searchArea));
        pause(800, 1200);

        // 2) Arama alanına tıkla ve arama kutusunun görünmesini bekle
        for (int i = 0; i < 3; i++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(searchArea)).click();
                break;
            } catch (StaleElementReferenceException | ElementClickInterceptedException ignored) {
                pause(400, 700);
            }
        }

        WebDriverWait inputWait = waitFor(15);
        inputWait.withMessage(
                "Arama kutusu görünür olmadı: input[data-test-id='search-bar-input']"
        );
        inputWait.until(d -> findDisplayedInput(searchInput) != null);

        // Kutunun tamamen yerleşmesi için bekle, sonra odağa girmesini dene (zorunlu değil)
        pause(600, 1000);

        for (int i = 0; i < 5 && !isFocused(searchInput); i++) {
            try {
                WebElement input = findDisplayedInput(searchInput);
                if (input != null) {
                    input.click();
                }
            } catch (StaleElementReferenceException | ElementClickInterceptedException ignored) {
            }
            pause(300, 500);
        }

        pause(300, 600);

        // 3) Yaz, değeri doğrula; eksik/yanlışsa temizleyip tekrar dene
        boolean typed = false;

        for (int attempt = 1; attempt <= 3 && !typed; attempt++) {

            typeHumanResilient(searchInput, keyword);

            if (keyword.trim().equalsIgnoreCase(currentValue(searchInput).trim())) {
                typed = true;
            } else {
                clearInput(searchInput);
                pause(500, 900);
            }
        }

        if (!typed) {
            throw new AssertionError(
                    "Arama kutusuna '" + keyword + "' yazılamadı. Son değer: '"
                            + currentValue(searchInput) + "'"
            );
        }

        pause(350, 700);
        sendKeysResilient(searchInput, Keys.ENTER);

        // Arama sonuçlarının gelmesini bekle
        wait.until(d ->
                d.getCurrentUrl().toLowerCase().contains(keyword.toLowerCase())
                        || d.getPageSource().toLowerCase().contains(keyword.toLowerCase())
        );
    }

    /**
     * Login'in başarılı olduğunu doğrular: header'daki hesap alanında "Hesabım" ve
     * kullanıcının adı görünmeli. Giriş yapılmamışsa aynı alanda "Giriş yap" yazar.
     */
    public void verifyUserLoggedIn() {

        WebElement account;

        try {
            // Login sonrası yönlendirme için süre tanı
            account = waitFor(20).until(d -> {
                try {
                    WebElement element = d.findElement(accountLink);
                    return element.isDisplayed() ? element : null;
                } catch (NoSuchElementException | StaleElementReferenceException e) {
                    return null;
                }
            });
        } catch (TimeoutException e) {
            throw new AssertionError(
                    "Login doğrulanamadı: header'da hesap alanı bulunamadı. URL: "
                            + driver().getCurrentUrl()
            );
        }

        String title = normalize(account.getAttribute("title"));
        String text = account.getText() == null ? "" : account.getText().trim();

        if (normalize(text).contains("giriş yap") || !title.equals("hesabım")) {
            throw new AssertionError(
                    "Kullanıcı giriş yapmış görünmüyor. Hesap alanı: title='"
                            + account.getAttribute("title") + "', metin='" + text + "'"
            );
        }

        // "Hesabım" etiketinin yanında kullanıcı adı da görünmeli
        String displayedName = text.replaceFirst("(?i)^hesabım", "").trim();
        if (displayedName.isEmpty()) {
            throw new AssertionError(
                    "Hesap alanında kullanıcı adı görünmüyor. Metin: '" + text + "'"
            );
        }

        System.out.println("Kullanıcı başarılı şekilde login olur");
    }

    public void openCartFromHeader() {
        waitFor(15).until(ExpectedConditions.elementToBeClickable(cartLink)).click();
    }
}