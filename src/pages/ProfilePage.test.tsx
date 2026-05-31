import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { ProfilePage } from './ProfilePage';
import { AuthContext } from '../contexts/AuthContext';
import { LanguageProvider } from '../contexts/LanguageContext';
import { apiService } from '../services/apiService';
import { Role, type User } from '../types';

vi.mock('../services/apiService', () => ({
  apiService: {
    changePassword: vi.fn(),
    updateProfile: vi.fn(),
    deleteAccount: vi.fn(),
  },
}));

const mockUser: User = {
  id: 'u1',
  username: 'ivka',
  email: 'ivka@x.cz',
  firstName: 'Iva',
  lastName: 'F.',
  city: 'Praha',
  language: 'cs',
  roles: [Role.USER],
};

function renderWith(user: User | null = mockUser, ctxOverrides: Record<string, unknown> = {}) {
  // Ensure tests run with the Czech locale so the translated labels match.
  window.localStorage.setItem('bezci_locale', 'cs');
  const ctx = {
    user,
    isAuthenticated: !!user,
    isLoading: false,
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
    updateProfile: vi.fn(),
    deleteAccount: vi.fn(),
    ...ctxOverrides,
  };
  return render(
    <BrowserRouter>
      <LanguageProvider>
        <AuthContext.Provider value={ctx as never}>
          <ProfilePage />
        </AuthContext.Provider>
      </LanguageProvider>
    </BrowserRouter>
  );
}

describe('ProfilePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders user basic info', () => {
    renderWith();
    expect(screen.getByDisplayValue('Iva')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Praha')).toBeInTheDocument();
    expect(screen.getByDisplayValue('ivka')).toBeInTheDocument();
    expect(screen.getByDisplayValue('ivka@x.cz')).toBeInTheDocument();
  });

  it('rejects mismatched new passwords', async () => {
    renderWith();
    const current = screen.getByLabelText(/aktuální heslo/i);
    const newPwd = screen.getByLabelText(/^nové heslo$/i);
    const confirm = screen.getByLabelText(/potvrzení nového hesla/i);

    fireEvent.change(current, { target: { value: 'oldpwd' } });
    fireEvent.change(newPwd, { target: { value: 'abcdef' } });
    fireEvent.change(confirm, { target: { value: 'xxxxxx' } });

    const submit = screen.getByRole('button', { name: /změnit heslo/i });
    fireEvent.click(submit);

    await waitFor(() => {
      expect(screen.getByText(/se neshodují/i)).toBeInTheDocument();
    });
    expect(apiService.changePassword).not.toHaveBeenCalled();
  });

  it('submits change-password when fields match', async () => {
    (apiService.changePassword as unknown as ReturnType<typeof vi.fn>).mockResolvedValue(undefined);
    renderWith();

    fireEvent.change(screen.getByLabelText(/aktuální heslo/i), { target: { value: 'oldpwd' } });
    fireEvent.change(screen.getByLabelText(/^nové heslo$/i), { target: { value: 'abcdef' } });
    fireEvent.change(screen.getByLabelText(/potvrzení nového hesla/i), { target: { value: 'abcdef' } });

    fireEvent.click(screen.getByRole('button', { name: /změnit heslo/i }));

    await waitFor(() => {
      expect(apiService.changePassword).toHaveBeenCalledWith('oldpwd', 'abcdef');
    });
  });

  it('calls updateProfile when saving basic info', async () => {
    const updateProfileMock = vi.fn().mockResolvedValue(undefined);
    renderWith(mockUser, { updateProfile: updateProfileMock });

    const firstNameInput = screen.getByLabelText(/^jméno$/i);
    fireEvent.change(firstNameInput, { target: { value: 'Ivana' } });

    fireEvent.click(screen.getByRole('button', { name: /^uložit$/i }));

    await waitFor(() => {
      expect(updateProfileMock).toHaveBeenCalledWith({
        firstName: 'Ivana',
        lastName: 'F.',
        city: 'Praha',
        language: 'cs',
      });
    });
  });

  it('opens delete modal and submits with password', async () => {
    const deleteAccount = vi.fn().mockResolvedValue(undefined);
    renderWith(mockUser, { deleteAccount });

    // Click the Danger Zone "Delete account" button (outside the modal).
    fireEvent.click(screen.getByRole('button', { name: /^smazat účet$/i }));
    expect(screen.getByRole('dialog')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText(/pro potvrzení zadej své heslo/i), {
      target: { value: 'mypwd' },
    });
    fireEvent.click(screen.getByRole('button', { name: /ano, smazat účet/i }));

    await waitFor(() => expect(deleteAccount).toHaveBeenCalledWith('mypwd'));
  });

  it('cancel button closes modal without calling deleteAccount', async () => {
    const deleteAccount = vi.fn();
    renderWith(mockUser, { deleteAccount });

    fireEvent.click(screen.getByRole('button', { name: /^smazat účet$/i }));
    expect(screen.getByRole('dialog')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /^zrušit$/i }));

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(deleteAccount).not.toHaveBeenCalled();
  });
});
