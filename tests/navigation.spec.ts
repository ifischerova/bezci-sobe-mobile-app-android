import { test, expect } from '@playwright/test';

test.describe('Navigation Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should navigate to all main pages', async ({ page }) => {
    // Test About page
    await page.click('text=O nás');
    await expect(page).toHaveURL(/.*about/);
    await expect(page.locator('text=O projektu Běžci sobě')).toBeVisible();

    // Test Races page
    await page.click('text=Závody');
    await expect(page).toHaveURL(/.*races/);
    await expect(page.locator('text=Zvol termín závodu')).toBeVisible();

    // Test Organizers page
    await page.click('text=Organizátoři');
    await expect(page).toHaveURL(/.*organizers/);
    await expect(page.locator('text=Pro pořadatele závodů')).toBeVisible();

    // Test Registration page
    await page.click('text=Registrace');
    await expect(page).toHaveURL(/.*registration/);
    await expect(page.locator('text=Vytvořit účet')).toBeVisible();
  });

  test('should have working mobile menu', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 812 });
    
    // Open mobile menu
    await page.click('button[aria-label="Toggle menu"]');
    
    // Check if mobile menu items are visible (use more specific selector)
    await expect(page.locator('a[href="/about"].block')).toBeVisible();
    await expect(page.locator('a[href="/races"].block')).toBeVisible();
  });

  test('should display home page content', async ({ page }) => {
    await expect(page.locator('text=Cesta na závod?')).toBeVisible();
    await expect(page.locator('text=Ekologie')).toBeVisible();
    await expect(page.locator('text=Komunita')).toBeVisible();
    await expect(page.locator('text=Úspora')).toBeVisible();
  });
});
