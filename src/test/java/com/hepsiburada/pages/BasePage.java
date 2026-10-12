package com.hepsiburada.pages;

import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.hepsiburada.driver.Driver;

/**
 * Tüm sayfaların ortak yardımcıları: bekleme, güvenilir yazma/tıklama, metin işleme.
 */
public abstract class BasePage {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");

    protected WebDriver driver() {
        return Driver.get();
    }

    protected WebDriverWait waitFor(int seconds) {
        return new WebDriverWait(driver(), Duration.ofSeconds(seconds));
    }

    protected JavascriptExecutor js() {
        return (JavascriptExecutor) driver();
    }

    // ------------------------------------------------------------------
    // Bekleme / insan davranışı
    // ------------------------------------------------------------------

    /** Kısa, rastgele bir bekleme (insan davranışını taklit etmek için). */
    protected static void pause(long minMs, long maxMs) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(minMs, maxMs + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Karakter karakter, her karakter arasında kısa rastgele beklemeyle yazar. */
    protected void typeHuman(WebElement element, String text) {
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            pause(60, 180);
        }
    }

    // ------------------------------------------------------------------
    // Yeniden render edilen input'lara dayanıklı yardımcılar
    // ------------------------------------------------------------------

    /** Görünür ve etkin ilk elemanı döndürür, yoksa null. */
    protected WebElement findDisplayedInput(By locator) {
        for (WebElement element : driver().findElements(locator)) {
            try {
                if (element.isDisplayed() && element.isEnabled()) {
                    return element;
                }
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return null;
    }

    /** Elemanı her denemede yeniden bulur; sayfa yeniden render etse bile sendKeys çalışır. */
    protected void sendKeysResilient(By locator, CharSequence keys) {
        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                WebElement element = findDisplayedInput(locator);
                if (element != null) {
                    element.sendKeys(keys);
                    return;
                }
            } catch (StaleElementReferenceException ignored) {
            }
            pause(150, 300);
        }
        throw new AssertionError("Eleman bulunamadı veya sürekli yenilendi: " + locator);
    }

    /** Karakter karakter yazar; her karakterde elemanı yeniden bulur. */
    protected void typeHumanResilient(By locator, String text) {
        for (char c : text.toCharArray()) {
            sendKeysResilient(locator, String.valueOf(c));
            pause(60, 180);
        }
    }

    /** Verilen input şu an odakta (aktif eleman) mı? */
    protected boolean isFocused(By locator) {
        try {
            WebElement element = findDisplayedInput(locator);
            if (element == null) {
                return false;
            }
            Object result = js().executeScript(
                    "return document.activeElement === arguments[0];", element
            );
            return Boolean.TRUE.equals(result);
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    /** Input'un şu anki değerini döndürür (bulunamazsa boş string). */
    protected String currentValue(By locator) {
        try {
            WebElement element = findDisplayedInput(locator);
            String value = element == null ? null : element.getAttribute("value");
            return value == null ? "" : value;
        } catch (StaleElementReferenceException e) {
            return "";
        }
    }

    /** Input içeriğini temizler. */
    protected void clearInput(By locator) {
        sendKeysResilient(locator, Keys.chord(Keys.CONTROL, "a"));
        sendKeysResilient(locator, Keys.BACK_SPACE);
    }

    /** Normal tıklar; başka bir öğe üstünü kapatıyorsa JS ile tıklar. */
    protected void clickWithJsFallback(WebElement element) {
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            js().executeScript("arguments[0].click();", element);
        }
    }

    protected void scrollIntoViewCenter(WebElement element) {
        js().executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    // ------------------------------------------------------------------
    // Metin işleme
    // ------------------------------------------------------------------

    /** Türkçe küçük harfe çevirir, boşlukları tek boşluğa indirir. */
    protected static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(TR)
                .replace(' ', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    /** Normalize edilmiş adın ilk n kelimesi. */
    protected static String firstWords(String normalizedName, int count) {
        String[] words = normalizedName.split(" ");
        return String.join(" ", Arrays.copyOfRange(words, 0, Math.min(count, words.length)));
    }

    /** Sayfada görünen metnin tamamını (normalize edilmiş) döndürür. */
    protected String getPageText() {
        Object text = js().executeScript(
                "return document.body ? document.body.innerText : '';"
        );
        return normalize(text == null ? "" : text.toString());
    }
}