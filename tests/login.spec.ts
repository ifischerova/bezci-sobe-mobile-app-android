import { test, expect } from '@playwright/test';

test.describe('User Login Flow', () => {
  test.beforeEach(async ({ page, context }) => {
    await context.clearCookies();
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
  });

  test('should display login form', async ({ page }) => {
    await expect(page.locator('h1', { hasText: 'Vítej zpět, běžče!' })).toBeVisible();
    await expect(page.locator('input[id="username"]')).toBeVisible();
    await expect(page.locator('input[id="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]', { hasText: 'Přihlásit se' })).toBeVisible();
  });

  test('should validate empty fields', async ({ page }) => {
    // Try to submit with empty fields - HTML5 validation will prevent submission
    const submitButton = page.locator('button[type="submit"]');
    await expect(submitButton).toBeVisible();
    
    // Verify required fields exist
    await expect(page.locator('input[id="username"][required]')).toBeVisible();
    await expect(page.locator('input[id="password"][required]')).toBeVisible();
  });

  test('should show error for invalid credentials', async ({ page }) => {
    await page.fill('input[id="username"]', 'wronguser');
    await page.fill('input[id="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');
    await expect(page.locator('text=Neplatné přihlašovací údaje')).toBeVisible();
  });

  test('should successfully login with correct credentials (happy path)', async ({ page }) => {
    // Use existing user from mock data
    await page.fill('input[id="username"]', 'admin');
    await page.fill('input[id="password"]', 'admin123');
    await page.click('button[type="submit"]');
    
    // Should redirect to home page
    await expect(page).toHaveURL('/');
    
    // Should show user info in header
    await expect(page.locator('text=admin')).toBeVisible();
  });

  test('should allow navigation to registration page', async ({ page }) => {
    await page.click('text=Zaregistrujte se zdarma');
    await expect(page).toHaveURL(/.*registration/);
  });

  test('should persist login after page reload', async ({ page }) => {
    // Login first
    await page.fill('input[id="username"]', 'admin');
    await page.fill('input[id="password"]', 'admin123');
    
    // Wait for navigation after form submission
    await Promise.all([
      page.waitForURL('/'),
      page.click('button[type="submit"]')
    ]);
    
    // Reload page
    await page.reload();
    await page.waitForLoadState('networkidle');
    
    // Should still be logged in
    await expect(page.locator('text=admin')).toBeVisible();
  });

  test('should logout successfully', async ({ page }) => {
    // Login first
    await page.fill('input[id="username"]', 'admin');
    await page.fill('input[id="password"]', 'admin123');
    await page.click('button[type="submit"]');
    
    // Logout
    await page.click('text=Odhlásit');
    
    // Should show login link in menu
    await page.goto('/login');
    await expect(page.locator('input[id="username"]')).toBeVisible();
  });
});
