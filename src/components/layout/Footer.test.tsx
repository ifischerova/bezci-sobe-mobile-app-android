import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../../test/renderWithProviders';
import { Footer } from './Footer';

describe('Footer Component', () => {
  it('should render footer with copyright text', () => {
    renderWithProviders(<Footer />);
    expect(screen.getByText(/2026/)).toBeInTheDocument();
    expect(screen.getByText(/Iva Fischerová/)).toBeInTheDocument();
  });
});
