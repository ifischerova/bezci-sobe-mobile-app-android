import { test, expect } from '@playwright/test';

test.describe('User Registration Flow', () => {
  test.beforeEach(async ({ page, context }) => {
    await context.clearCookies();
    await page.goto('/registration');
  });

  test('should display registration form', async ({ page }) => {
    await expect(page.locator('h1', { hasText: 'Staň se jedním z nás!' })).toBeVisible();
    await expect(page.locator('input[id="username"]')).toBeVisible();
    await expect(page.locator('input[id="email"]')).toBeVisible();
    await expect(page.locator('input[id="password"]')).toBeVisible();
    await expect(page.locator('input[id="confirmPassword"]')).toBeVisible();
    await expect(page.locator('input[id="terms"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]', { hasText: 'Vytvořit účet' })).toBeVisible();
  });

  test('should validate empty form submission', async ({ page }) => {
    await page.click('button[type="submit"]');
    const usernameInput = page.locator('input[id="username"]');
    await expect(usernameInput).toHaveAttribute('required', '');
  });

  test('should validate password mismatch', async ({ page }) => {
    await page.fill('input[id="username"]', 'testuser');
    await page.fill('input[id="email"]', 'test@example.com');
    await page.fill('input[id="password"]', 'Password123');
    await page.fill('input[id="confirmPassword"]', 'DifferentPassword');
    await page.check('input[id="terms"]');
    await page.click('button[type="submit"]');
    await expect(page.locator('text=Hesla se neshodují')).toBeVisible();
  });

  test('should validate email format', async ({ page }) => {
    await page.fill('input[id="username"]', 'testuser');
    await page.fill('input[id="email"]', 'invalidemail');
    await page.fill('input[id="password"]', 'Password123');
    await page.fill('input[id="confirmPassword"]', 'Password123');
    await page.check('input[id="terms"]');
    
    // HTML5 validation should show invalid email
    const emailInput = page.locator('input[id="email"]');
    await expect(emailInput).toHaveAttribute('type', 'email');
  });

  test('should require terms acceptance', async ({ page }) => {
    await page.fill('input[id="username"]', 'testuser');
    await page.fill('input[id="email"]', 'test@example.com');
    await page.fill('input[id="password"]', 'Password123');
    await page.fill('input[id="confirmPassword"]', 'Password123');
    
    // Verify checkbox is required
    await expect(page.locator('input[id="terms"][required]')).toBeVisible();
  });

  test('should successfully register a new user (happy path)', async ({ page }) => {
    const timestamp = Date.now();
    const username = `testuser${timestamp}`;
    const email = `test${timestamp}@example.com`;
    
    await page.fill('input[id="username"]', username);
    await page.fill('input[id="email"]', email);
    await page.fill('input[id="password"]', 'Password123');
    await page.fill('input[id="confirmPassword"]', 'Password123');
    await page.check('input[id="terms"]');
    await page.click('button[type="submit"]');
    
    // Should redirect to login after successful registration
    await expect(page).toHaveURL(/.*login/);
  });

  test('should show error for duplicate username', async ({ page }) => {
    // Use a username that already exists in mock data
    const username = 'admin';
    
    await page.fill('input[id="username"]', username);
    await page.fill('input[id="email"]', 'newadmin@example.com');
    await page.fill('input[id="password"]', 'Password123');
    await page.fill('input[id="confirmPassword"]', 'Password123');
    await page.check('input[id="terms"]');
    await page.click('button[type="submit"]');
    
    await expect(page.locator('text=Uživatelské jméno již existuje')).toBeVisible();
  });
});
