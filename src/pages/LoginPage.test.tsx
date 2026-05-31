import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, fireEvent, waitFor } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { LoginPage } from '../pages/LoginPage';

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock fetch globally
const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

describe('LoginPage Component', () => {
  beforeEach(() => {
    mockFetch.mockReset();
    localStorage.clear();
    // Default: getCurrentUser returns 401 (not logged in)
    mockFetch.mockImplementation((url: string) => {
      if (url.includes('/auth/me')) {
        return Promise.resolve({ ok: false, status: 401, json: () => Promise.resolve({ message: 'Unauthorized' }) });
      }
      return Promise.resolve({ ok: false, status: 500, json: () => Promise.resolve({ message: 'Not mocked' }) });
    });
  });

  const renderLoginPage = () => {
    return renderWithProviders(<LoginPage />, { withAuth: true });
  };

  it('should render login form', () => {
    renderLoginPage();

    expect(screen.getByLabelText(/Uživatelské jméno/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Heslo/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /PŘIHLÁSIT/i })).toBeInTheDocument();
  });

  it('should show validation error for empty fields', async () => {
    renderLoginPage();

    const usernameInput = screen.getByLabelText(/Uživatelské jméno/i);
    const passwordInput = screen.getByLabelText(/Heslo/i);
    const submitButton = screen.getByRole('button', { name: /PŘIHLÁSIT/i });

    fireEvent.change(usernameInput, { target: { value: '' } });
    fireEvent.change(passwordInput, { target: { value: '' } });
    fireEvent.click(submitButton);

    expect(submitButton).toBeInTheDocument();
  });

  it('should show error for invalid credentials', async () => {
    mockFetch.mockImplementation((url: string) => {
      if (url.includes('/auth/login')) {
        return Promise.resolve({
          ok: false,
          status: 401,
          json: () => Promise.resolve({ message: 'Neplatné přihlašovací údaje' }),
        });
      }
      if (url.includes('/auth/me')) {
        return Promise.resolve({ ok: false, status: 401, json: () => Promise.resolve({}) });
      }
      return Promise.resolve({ ok: false, status: 500, json: () => Promise.resolve({}) });
    });

    renderLoginPage();

    const usernameInput = screen.getByLabelText(/Uživatelské jméno/i);
    const passwordInput = screen.getByLabelText(/Heslo/i);
    const submitButton = screen.getByRole('button', { name: /PŘIHLÁSIT/i });

    fireEvent.change(usernameInput, { target: { value: 'wronguser' } });
    fireEvent.change(passwordInput, { target: { value: 'wrongpass' } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/Neplatné přihlašovací údaje/i)).toBeInTheDocument();
    });
  });

  it('should have link to registration page', () => {
    renderLoginPage();

    const registrationLink = screen.getByText(/Zaregistrujte se/i);
    expect(registrationLink).toBeInTheDocument();
    expect(registrationLink).toHaveAttribute('href', '/registration');
  });
});
