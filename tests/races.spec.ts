import { test, expect, Page } from '@playwright/test';

// Helper function to login
async function login(page: Page, username: string, password: string) {
  await page.goto('/login');
  await page.fill('input[id="username"]', username);
  await page.fill('input[id="password"]', password);
  await page.click('button[type="submit"]');
  await page.waitForURL('/');
}

test.describe('Races and Rides Management', () => {
  test.beforeEach(async ({ context }) => {
    await context.clearCookies();
  });

  test('should display races list', async ({ page }) => {
    await page.goto('/races');
    await expect(page.locator('text=Zvol termín závodu')).toBeVisible();
    await expect(page.locator('select')).toBeVisible();
  });

  test('should select a race and view details', async ({ page }) => {
    await page.goto('/races');
    await page.selectOption('select', { index: 1 }); // Select first race
    await expect(page.locator('h3:has-text("Pražský maraton")')).toBeVisible();
    await expect(page.locator('text=Dostupné jízdy')).toBeVisible();
  });

  test('should require login to create a ride', async ({ page }) => {
    await page.goto('/races');
    await page.selectOption('select', { index: 1 });
    await expect(page.locator('text=+ Přidat jízdu')).not.toBeVisible();
    await expect(page.locator('text=Přihlas se a jdi do toho!')).toBeVisible();
  });

  test('should display existing rides in table', async ({ page }) => {
    await page.goto('/races');
    await page.selectOption('select', { index: 1 });
    
    // Wait for race details
    await expect(page.locator('h3:has-text("Pražský maraton")')).toBeVisible();
    
    // Should show "Dostupné jízdy" heading
    await expect(page.locator('text=Dostupné jízdy')).toBeVisible();
  });

  test('should cancel ride creation form', async ({ page }) => {
    await login(page, 'admin', 'admin123');
    await page.goto('/races');
    await page.selectOption('select', { index: 1 });
    
    // Wait for race details
    await expect(page.locator('h3:has-text("Pražský maraton")')).toBeVisible();
    
    // Click add ride button
    await page.click('text=+ Přidat jízdu');
    
    // Should show form
    await expect(page.locator('text=Vytvořit novou jízdu')).toBeVisible();
    
    // Click cancel button
    await page.click('text=Zrušit');
    
    // Form should be hidden
    await expect(page.locator('text=Vytvořit novou jízdu')).not.toBeVisible();
  });
});
