package com.hepsiburada.step;

import com.hepsiburada.driver.Driver;
import com.hepsiburada.pages.CartPage;
import com.hepsiburada.pages.HomePage;
import com.hepsiburada.pages.LoginPage;
import com.hepsiburada.pages.ProductDetailPage;
import com.hepsiburada.pages.SearchResultsPage;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.Step;

/**
 * Gauge adımları. Sadece sayfa sınıflarına yönlendirir; sayfa mantığı pages/ altında.
 */
public class StepImplementation {

    private final HomePage homePage = new HomePage();
    private final LoginPage loginPage = new LoginPage();
    private final SearchResultsPage searchResultsPage = new SearchResultsPage();
    private final ProductDetailPage productDetailPage = new ProductDetailPage();
    private final CartPage cartPage = new CartPage();

    // Adımlar arasında taşınan test verisi (sepet doğrulaması için)
    private static String searchedKeyword;
    private static String selectedProductName;
    private static String detailPageTitle;

    // ------------------------------------------------------------------
    // Tarayıcı
    // ------------------------------------------------------------------

    @Step("open chrome")
    public void openChrome() {
        searchedKeyword = null;
        selectedProductName = null;
        detailPageTitle = null;
        Driver.start();
    }

    @AfterScenario
    public void closeBrowser() {
        Driver.quit();
    }

    @Step("go to hepsiburada")
    public void goToHepsiburada() {
        homePage.open();
    }

    @Step("go to login page")
    public void goToLoginPage() {
        loginPage.open();
    }

    // ------------------------------------------------------------------
    // Login
    // ------------------------------------------------------------------

    @Step("hover user info")
    public void hoverUserInfo() {
        homePage.hoverUserInfo();
    }

    @Step("click login")
    public void clickLogin() {
        homePage.clickLogin();
    }

    @Step("enter username <username>")
    public void enterUsername(String username) {
        loginPage.enterUsername(username);
    }

    @Step("enter password <password>")
    public void enterPassword(String password) {
        loginPage.enterPassword(password);
    }

    @Step("enter username from config")
    public void enterUsernameFromConfig() {
        loginPage.enterUsername(getCredential("HB_USERNAME"));
    }

    @Step("enter password from config")
    public void enterPasswordFromConfig() {
        loginPage.enterPassword(getCredential("HB_PASSWORD"));
    }

    @Step("click login submit")
    public void clickLoginSubmit() {
        loginPage.clickSubmit();
    }

    @Step("verify user is logged in")
    public void verifyUserLoggedIn() {
        homePage.verifyUserLoggedIn();
    }

    /**
     * Kimlik bilgisini önce ortam değişkeninden (Gauge, env/*.properties değerlerini
     * ortam değişkeni olarak da sunar), bulamazsa JVM property'sinden okur.
     */
    private static String getCredential(String key) {
        String value = System.getenv(key);

        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "'" + key + "' tanımlı değil. env/default/ altındaki .properties "
                            + "dosyasına veya ortam değişkenlerine ekle."
            );
        }

        return value;
    }

    // ------------------------------------------------------------------
    // Arama ve ürün
    // ------------------------------------------------------------------

    @Step("search for <keyword>")
    public void searchForProduct(String keyword) {
        homePage.searchFor(keyword);
        searchedKeyword = keyword;

        System.out.println("Arama sonuçları kullanıcı girdisine uygun gelir");
    }

    @Step("open second row first product")
    public void openSecondRowFirstProduct() {
        selectedProductName = searchResultsPage.openSecondRowFirstProduct();
        detailPageTitle = null;

        productDetailPage.waitUntilLoaded();

        // Detay sayfasındaki ürün başlığını da sakla (sepetteki ada karşı ikinci referans)
        detailPageTitle = productDetailPage.getTitle();

        System.out.println("İkinci satırdaki ilk ürünün sayfasına yönlenir");
        System.out.println("  Ürün: " + selectedProductName);
    }

    @Step("add product to cart from detail page")
    public void addProductToCartFromDetailPage() {
        productDetailPage.addToCart();
    }

    @Step("open cart from header")
    public void openCartFromHeader() {
        homePage.openCartFromHeader();
    }

    // ------------------------------------------------------------------
    // Sepet doğrulamaları
    // ------------------------------------------------------------------

    @Step("verify product name is visible in cart")
    public void verifyProductNameVisibleInCart() {
        cartPage.verifyProductNameVisible(selectedProductName, detailPageTitle);
    }

    @Step("verify product basic details in cart")
    public void verifyProductBasicDetailsInCart() {
        cartPage.verifyProductBasicDetails(selectedProductName, searchedKeyword);
    }
}