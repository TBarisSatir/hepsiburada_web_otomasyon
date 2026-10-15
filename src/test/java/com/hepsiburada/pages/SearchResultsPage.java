package com.hepsiburada.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SearchResultsPage extends BasePage {

    /*
     * Ürün kartları li#i0, li#i1, ... şeklinde numaralandırılır.
     * Yalnızca id'si "i<sayı>" biçiminde olan kartlar değerlendirilir.
     * Kartlar liste sırasıyla taranır ve ilk kartın Y koordinatı alınır.
     * Y koordinatı ilk değişen kart, ikinci satırın ilk ürünü olarak seçilir.
     * Ürün bağlantısı yeni sekmede açılır.
     */
    private static final int PRODUCTS_PER_ROW = 4;
    private static final int TARGET_ROW = 2;
    private static final int POSITION_TOLERANCE_PX = 3;
    private static final Pattern PRODUCT_ID = Pattern.compile("i\\d+");

    private final By productCardCandidates = By.cssSelector("li[id^='i']");
    private final By productLink = By.cssSelector("a[title]");

    /** Ürün kartlarını (id'si i<sayı> olan li öğeleri) DOM sırasıyla döndürür. */
    private List<WebElement> findProductCards() {
        List<WebElement> cards = new ArrayList<>();
        for (WebElement li : driver().findElements(productCardCandidates)) {
            try {
                String id = li.getAttribute("id");
                if (id != null && PRODUCT_ID.matcher(id).matches()) {
                    cards.add(li);
                }
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return cards;
    }

    /** İkinci satırın ilk ürününün indeksi: (2 - 1) * 4 = 4 */
    private int targetIndex() {
        return (TARGET_ROW - 1) * PRODUCTS_PER_ROW;
    }

    /** Ürün listesi görünür olmalı ve ikinci satıra yetecek kadar ürün içermeli. */
    public void verifyProductListVisible() {

        int required = PRODUCTS_PER_ROW * TARGET_ROW;

        try {
            waitFor(15).until(d -> {
                List<WebElement> cards = findProductCards();
                if (cards.size() < required) {
                    return false;
                }
                try {
                    return cards.get(0).isDisplayed();
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            });
        } catch (TimeoutException e) {
            throw new AssertionError(
                    "Ürün listesi görünür değil veya ikinci satıra yetecek kadar ürün yok. "
                            + "Bulunan ürün sayısı: " + findProductCards().size()
                            + " (en az " + required + " gerekli) | URL: " + driver().getCurrentUrl()
            );
        }

        System.out.println("Ürün listesi görünür (" + findProductCards().size() + " ürün)");
    }

    /**
     * İkinci satırdaki ilk ürüne tıklar; ürün yeni sekmede açılır ve o sekmeye geçilir.
     *
     * @return tıklanan ürünün arama sonuçlarındaki adı
     */
    public String openSecondRowFirstProduct() {

        WebDriverWait wait = waitFor(15);
        String originalWindow = driver().getWindowHandle();

        WebElement productCard = wait
                .withMessage("İkinci satırın ilk ürünü bulunamadı.")
                .until(d -> {
                    Integer firstRowY = null;

                    for (WebElement card :
                            d.findElements(By.cssSelector("li[id^='i']"))) {

                        try {
                            String id = card.getDomAttribute("id");

                            // Reklam/banner öğelerini ürün olarak sayma.
                            if (id == null || !id.matches("i\\d+")
                                    || !card.isDisplayed()) {
                                continue;
                            }

                            int currentY = card.getLocation().getY();

                            if (firstRowY == null) {
                                firstRowY = currentY;
                            } else if (currentY != firstRowY) {
                                // Y koordinatı değişen ilk ürün:
                                // ikinci satırın ilk kartı.
                                return card;
                            }

                        } catch (StaleElementReferenceException e) {
                            // Liste yenilendiyse sonraki kontrolde yeniden bul.
                            return null;
                        }
                    }

                    return null;
                });

        scrollIntoViewCenter(productCard);

        WebElement link = productCard.findElement(productLink);
        String productName = link.getAttribute("title");

        link.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String handle : driver().getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver().switchTo().window(handle);
                break;
            }
        }

        return productName;
    }
}