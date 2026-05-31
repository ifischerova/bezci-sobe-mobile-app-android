import { describe, it, expect, beforeEach } from 'vitest';
import { fireEvent, screen } from '@testing-library/react';
import { renderWithProviders } from '../../test/renderWithProviders';
import { ThemeSwitcher } from './ThemeSwitcher';

describe('ThemeSwitcher', () => {
  beforeEach(() => {
    window.localStorage.clear();
    document.documentElement.classList.remove('dark');
  });

  it('renders with light-state aria-label by default', () => {
    renderWithProviders(<ThemeSwitcher />);
    const button = screen.getByRole('button', { name: /tmavý motiv/i });
    expect(button).toBeInTheDocument();
    expect(button).toHaveAttribute('aria-pressed', 'false');
  });

  it('toggles to dark state on click', () => {
    renderWithProviders(<ThemeSwitcher />);
    const button = screen.getByRole('button', { name: /tmavý motiv/i });
    fireEvent.click(button);
    expect(document.documentElement.classList.contains('dark')).toBe(true);
    const flipped = screen.getByRole('button', { name: /světlý motiv/i });
    expect(flipped).toHaveAttribute('aria-pressed', 'true');
  });

  it('uses English aria-labels under the en locale', () => {
    renderWithProviders(<ThemeSwitcher />, { locale: 'en' });
    expect(
      screen.getByRole('button', { name: /switch to dark theme/i }),
    ).toBeInTheDocument();
  });
});
