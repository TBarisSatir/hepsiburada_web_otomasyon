package com.hepsiburada.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.hepsiburada.helper.ElementHelper;

public class HomePage extends BasePage {

    private static final String HOME_URL = "https://www.hepsiburada.com/";

    // Locator'lar: resources/element-infos/home.json
    private final By cookieAcceptButton = ElementHelper.getBy("home_cookie_accept_button");
    private final By userInfo = ElementHelper.getBy("home_user_info");
    private final By loginLink = ElementHelper.getBy("home_login_link");
    private final By searchArea = ElementHelper.getBy("home_search_area");
    private final By searchInput = ElementHelper.getBy("home_search_input");
    private final By cartLink = ElementHelper.getBy("home_cart_link");
    // Class adları değişken (sf-OldMyAccount-...), bu yüzden id + data-test-id kullanılır
    private final By accountLink = ElementHelper.getBy("home_account_link");

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
     * Arama alanına tıklar, açılan arama popup'ındaki kutu yerleşince kelimeyi yazar ve Enter'a basar.
     * Kutu açılırken yeniden render edilebildiği için eleman her adımda yeniden bulunur.
     */
    public void searchFor(String keyword) {

        WebDriverWait wait = waitFor(15);

        // 1) Arama alanı tıklanabilir olur olmaz tıkla (sayfanın tamamen yüklenmesi beklenmez)
        waitFor(15)
                .withMessage("Arama alanına tıklanamadı: " + searchArea)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementNotInteractableException.class)
                .until(d -> {
                    WebElement area = ExpectedConditions.elementToBeClickable(searchArea).apply(d);
                    if (area == null) {
                        return false;
                    }
                    area.click();
                    return true;
                });

        // 2) Popup açılıp arama kutusu yerleşene kadar bekle, sonra kutuyu odağa al
        WebElement input = waitForStableInput(searchInput, 10);
        try {
            clickWithJsFallback(input);
        } catch (StaleElementReferenceException | ElementNotInteractableException ignored) {
            // Kutu tam bu anda yenilendiyse yazma adımı onu yeniden bulur
        }

        // 3) Yaz, değeri doğrula; eksik/yanlışsa temizleyip tekrar dene
        boolean typed = false;

        for (int attempt = 1; attempt <= 3 && !typed; attempt++) {

            sendKeysResilient(searchInput, keyword);

            try {
                waitFor(2).until(d ->
                        keyword.trim().equalsIgnoreCase(currentValue(searchInput).trim())
                );
                typed = true;
            } catch (TimeoutException e) {
                clearInput(searchInput);
            }
        }

        if (!typed) {
            throw new AssertionError(
                    "Arama kutusuna '" + keyword + "' yazılamadı. Son değer: '"
                            + currentValue(searchInput) + "'"
            );
        }

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