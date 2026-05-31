import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { HomePage } from '../pages/HomePage';

describe('HomePage Component', () => {
  it('should render main heading', () => {
    renderWithProviders(<HomePage />);
    expect(screen.getByText(/Cesta na závod?/)).toBeInTheDocument();
  });

  it('should render three value propositions', () => {
    renderWithProviders(<HomePage />);
    expect(screen.getByText(/Ekologie/)).toBeInTheDocument();
    expect(screen.getByText(/Komunita/)).toBeInTheDocument();
    expect(screen.getByText(/Úspora/)).toBeInTheDocument();
  });

  it('should render how it works section', () => {
    renderWithProviders(<HomePage />);
    expect(screen.getAllByText(/Jak to funguje?/).length).toBeGreaterThan(0);
    expect(screen.getByText(/Najdi závod/)).toBeInTheDocument();
    expect(screen.getByText(/Domluv spolujízdu/)).toBeInTheDocument();
    expect(screen.getByText(/Běž a užij si to/)).toBeInTheDocument();
  });
});
