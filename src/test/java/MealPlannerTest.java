import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import java.nio.file.Paths;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class MealPlannerTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @BeforeEach
    void setup() {
        context = browser.newContext();
        page = context.newPage();
    }

    private String getUrl(String fileName) {
        return Paths.get("templates/" + fileName).toAbsolutePath().toUri().toString();
    }

    @Test
    @DisplayName("TC-01: Save a valid meal plan to Saved Meals")
    void testSaveValidMealPlan() {
        page.navigate(getUrl("generator.html")); // 1. Open Generated meal plan
        page.locator("#save-btn").click();        // 2. Click Save Meals
        page.locator("#confirm-save-btn").click(); // 3. Confirm Save action

        // Expected: Saved successfully message
        assertThat(page.locator("#status-message")).containsText("Saved successfully");
    }

    @Test
    @DisplayName("TC-02: Open Saved Meals and display existing saved plans")
    void testDisplaySavedMeals() {
        // Pre-condition: Save a meal first in this session
        testSaveValidMealPlan();

        page.navigate(getUrl("saved-meals.html")); // 1. Click Saved Meals
        
        // Expected: Displays the specific meal plan name from your table
        assertThat(page.locator("body")).containsText("Weekly High Protein Plan");
    }

    @Test
    @DisplayName("TC-03: Delete an existing saved meal plan")
    void testDeleteSavedMeal() {
        testSaveValidMealPlan(); // Pre-condition
        page.navigate(getUrl("saved-meals.html"));

        page.locator("#delete-btn").click();      // 3. Click Delete
        page.locator("#confirm-delete").click(); // 4. Confirm deletion

        // Expected: Removed from list (look for empty message)
        assertThat(page.locator("#empty-message")).isVisible();
    }

    @Test
    @DisplayName("TC-04: Open Saved Meals when no plans exist")
    void testOpenEmptySavedMeals() {
        // Navigate directly without saving anything
        page.navigate(getUrl("saved-meals.html")); 

        // Expected: "No saved meals found"
        assertThat(page.locator("body")).containsText("No saved meals found");
    }

    @Test
    @DisplayName("TC-05: Attempt to save duplicate meal plan")
    void testSaveDuplicateMealPlan() {
        page.navigate(getUrl("generator.html"));
        
        // 1st Save
        page.locator("#save-btn").click();
        page.locator("#confirm-save-btn").click();
        
        // 2nd Save (Duplicate Attempt)
        page.locator("#save-btn").click();
        page.locator("#confirm-save-btn").click();

        // Expected: Inform user the meal already exists
        assertThat(page.locator("#status-message")).containsText("meal already exists");
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }
}