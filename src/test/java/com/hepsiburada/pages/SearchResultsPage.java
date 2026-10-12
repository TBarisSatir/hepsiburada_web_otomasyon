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
     * NOT (arama sonuç sayfası düzeni):
     *  - Pencere boyutu ne olursa olsun her satırda 4 ürün var (sabit 4 sütunlu grid).
     *  - Ürün kartları <li id="i0">, <li id="i1">, ... şeklinde sıfırdan başlayan sırayla numaralı:
     *      1. satır = i0..i3, 2. satır = i4..i7, 3. satır = i8..i11
     *    Bu yüzden ikinci satırın ilk ürünü li#i4'tür: (satır - 1) * 4.
     *  - Listenin arasında id'si olmayan reklam/banner <li> öğeleri olabilir (örn. i11 ile i12 arası).
     *    Sadece id'si "i<sayı>" biçiminde olanlar ürün kartı sayılır.
     *  - Ürün kartındaki <a title="..." target="_blank"> linki ürünü YENİ SEKMEDE açar.
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

    /** Grid düzenini doğrular ve CSS display değerini döndürür. */
    private String checkGridLayout() {

        List<WebElement> cards = findProductCards();
        int required = PRODUCTS_PER_ROW * TARGET_ROW;

        if (cards.size() < required) {
            throw new AssertionError(
                    "Grid kontrolü için en az " + required + " ürün gerekli, bulunan: " + cards.size()
            );
        }

        // 1. satır: ilk 4 kart aynı yükseklikte ve soldan sağa dizili olmalı
        Rectangle first = cards.get(0).getRect();
        Rectangle previous = first;

        for (int i = 1; i < PRODUCTS_PER_ROW; i++) {
            Rectangle current = cards.get(i).getRect();

            if (Math.abs(current.getY() - first.getY()) > POSITION_TOLERANCE_PX) {
                throw new AssertionError(
                        "Satırda " + PRODUCTS_PER_ROW + " ürün bekleniyordu ama i" + i
                                + " ilk ürünle aynı satırda değil (y: " + current.getY()
                                + ", beklenen: " + first.getY() + "). Sayfa düzeni değişmiş olabilir."
                );
            }
            if (current.getX() <= previous.getX()) {
                throw new AssertionError(
                        "Ürünler soldan sağa dizili değil (i" + i + " konumu beklenenden farklı)."
                );
            }
            previous = current;
        }

        // 2. satırın ilk ürünü, 1. satırın altında ve ilk ürünle aynı sütunda olmalı
        Rectangle secondRowFirst = cards.get(targetIndex()).getRect();

        if (secondRowFirst.getY() <= first.getY() + POSITION_TOLERANCE_PX) {
            throw new AssertionError(
                    "i" + targetIndex() + " ikinci satırda değil: 1. satırla aynı yükseklikte. "
                            + "Satırda " + PRODUCTS_PER_ROW + "'ten fazla ürün olabilir."
            );
        }
        if (Math.abs(secondRowFirst.getX() - first.getX()) > POSITION_TOLERANCE_PX) {
            throw new AssertionError(
                    "i" + targetIndex() + " ilk sütunda değil (x: " + secondRowFirst.getX()
                            + ", beklenen: " + first.getX() + ")."
            );
        }

        Object display = js().executeScript(
                "return getComputedStyle(arguments[0].parentElement).display;", cards.get(0)
        );
        return display == null ? "?" : display.toString();
    }

    /** Ürünler satır başına 4 olacak şekilde grid düzeninde listeleniyor. */
    public void verifyGridLayout() {
        String display = checkGridLayout();
        System.out.println(
                "Ürünler " + PRODUCTS_PER_ROW + " sütunlu grid düzeninde (display: " + display + ")"
        );
    }

    /**
     * İkinci satırdaki ilk ürüne tıklar; ürün yeni sekmede açılır ve o sekmeye geçilir.
     *
     * @return tıklanan ürünün arama sonuçlarındaki adı
     */
    public String openSecondRowFirstProduct() {

        WebDriverWait wait = waitFor(15);

        String originalWindow = driver().getWindowHandle();

        WebElement productCard = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("i" + targetIndex()))
        );

        // Düzen değişmişse yanlış ürüne tıklamak yerine net bir hatayla dur
        checkGridLayout();

        scrollIntoViewCenter(productCard);

        WebElement link = productCard.findElement(productLink);
        String productName = link.getAttribute("title");

        pause(400, 900);
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