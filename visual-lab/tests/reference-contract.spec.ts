import { test, expect } from '@playwright/test';

test.beforeEach(async ({ page }) => {
  await page.route('**/*', route => new URL(route.request().url()).hostname === '127.0.0.1' ? route.continue() : route.abort());
  await page.goto('/');
});
test('touch selection opens preview and expansion returns without losing context', async ({ page }, info) => {
  await page.getByRole('option', { name: /Guía de aviación/ }).click();
  await expect(page.getByRole('complementary', { name: 'Panel lateral' })).toBeVisible();
  await page.getByRole('button', { name: 'Ampliar vista previa' }).click();
  await expect(page.getByRole('listbox')).not.toBeVisible();
  await page.getByRole('button', { name: 'Ampliar vista previa' }).click();
  await expect(page.getByRole('option', { name: /Guía de aviación/ })).toHaveAttribute('aria-selected', 'true');
  await page.screenshot({ path: info.outputPath('preview.png'), fullPage: true });
});
test('Tab and Shift+Tab change contextual focus; Enter runs only the focused action', async ({ page }, info) => {
  const search = page.getByRole('textbox', { name: 'Buscar referencias' });
  await search.press('Tab');
  await expect(page.getByRole('button', { name: 'Abrir', exact: false }).filter({ hasText: 'Enter' })).toBeFocused();
  await page.keyboard.press('Tab');
  await expect(page.getByRole('button', { name: /Copiar nombre/ })).toBeFocused();
  await page.keyboard.press('Shift+Tab');
  await expect(page.getByRole('button', { name: /^Abrir/ })).toBeFocused();
  await page.keyboard.press('Tab');
  await page.keyboard.press('Enter');
  await expect(page.getByRole('status')).toHaveText('Copiar nombre · Guía de aviación.pdf');
  await page.screenshot({ path: info.outputPath('context-actions.png'), fullPage: true });
  await page.keyboard.press('Escape');
  await expect(search).toBeFocused();
  await expect(page.getByRole('complementary')).toBeVisible();
  await search.press('Escape');
  await expect(page.getByRole('complementary')).toHaveCount(0);
});
test('filtering never performs external requests and handles an empty result', async ({ page }) => {
  const search = page.getByRole('textbox', { name: 'Buscar referencias' });
  await search.fill('Checklist');
  await expect(page.getByRole('option')).toHaveCount(1);
  await search.press('Enter');
  await expect(page.getByRole('status')).toHaveText('Abrir · Checklist.txt');
  await search.fill('Sin coincidencias');
  await expect(page.getByText('Sin resultados de prueba')).toBeVisible();
  await expect(page.getByRole('option')).toHaveCount(0);
});
