import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, LoginCredentials, RegisterData, AuthResponse } from '../types';
import { apiService } from '../services/apiService';
import { useTranslation } from './LanguageContext';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginCredentials) => Promise<AuthResponse>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  updateProfile: (data: { firstName?: string; lastName?: string; city?: string; language?: string }) => Promise<void>;
  deleteAccount: (password: string) => Promise<void>;
}

// Exported so tests can mount a component with a hand-rolled context value
// without spinning up the full AuthProvider + fetch mocks.
export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const { locale } = useTranslation();

  useEffect(() => {
    const loadUser = async () => {
      try {
        const currentUser = await apiService.getCurrentUser();
        if (currentUser) {
          setUser(currentUser);
        }
      } catch {
        // Token invalid or expired
      } finally {
        setIsLoading(false);
      }
    };
    loadUser();
  }, []);

  const login = async (credentials: LoginCredentials): Promise<AuthResponse> => {
    const response = await apiService.login(credentials.username, credentials.password);
    const currentUser = await apiService.getCurrentUser();
    setUser(currentUser);
    return response;
  };

  const register = async (data: RegisterData): Promise<void> => {
    await apiService.register(data.username, data.email, data.password, locale);
  };

  const logout = () => {
    apiService.logout();
    setUser(null);
  };

  const updateProfile = async (data: { firstName?: string; lastName?: string; city?: string; language?: string }) => {
    const updated = await apiService.updateProfile(data);
    // Backend returns string[] roles; we coerce into the User shape used by the UI.
    setUser((prev) => {
      if (!prev) {
        return {
          id: updated.id,
          username: updated.username,
          email: updated.email,
          firstName: updated.firstName,
          lastName: updated.lastName,
          city: updated.city,
          language: updated.language,
          roles: (updated.roles as unknown) as User['roles'],
        };
      }
      return {
        ...prev,
        firstName: updated.firstName,
        lastName: updated.lastName,
        city: updated.city,
        language: updated.language,
      };
    });
  };

  const deleteAccount = async (password: string) => {
    await apiService.deleteAccount(password);
    logout();
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
        updateProfile,
        deleteAccount,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
