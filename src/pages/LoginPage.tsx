import { useState, FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Footprints, Mail } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { useTranslation } from '../contexts/LanguageContext';
import { ApiError, apiService } from '../services/apiService';

export const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [needsVerification, setNeedsVerification] = useState(false);
  const [resendEmail, setResendEmail] = useState('');
  const [resendState, setResendState] = useState<'idle' | 'sending' | 'sent'>('idle');
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setNeedsVerification(false);
    setResendState('idle');
    setIsLoading(true);

    if (!username || !password) {
      setError(t('auth.login.error.allRequired'));
      setIsLoading(false);
      return;
    }

    if (password.length < 6) {
      setError(t('auth.login.error.passwordShort'));
      setIsLoading(false);
      return;
    }

    try {
      await login({ username, password });
      navigate('/');
    } catch (err) {
      if (err instanceof ApiError && err.status === 403) {
        setNeedsVerification(true);
        setError(t('auth.login.error.notVerified'));
      } else {
        setError(err instanceof Error ? err.message : t('auth.login.error.failed'));
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleResendVerification = async () => {
    if (!resendEmail) {
      setError(t('auth.login.resendNeedEmail'));
      return;
    }
    setResendState('sending');
    try {
      await apiService.resendVerification(resendEmail);
    } catch {
      // resend-verification never reveals account state — swallow client errors too
    } finally {
      setResendState('sent');
    }
  };

  return (
    <div className="section-container max-w-md mx-auto animate-fade-in">
      <div className="text-center mb-8">
        <div className="inline-block w-16 h-16 bg-gradient-to-br from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center mb-4 mx-auto animate-bounce-slow">
          <Footprints size={32} strokeWidth={1.5} className="text-white" />
        </div>
        <h1 className="text-4xl font-bold bg-gradient-to-r from-primary-600 to-accent-600 bg-clip-text text-transparent mb-3 leading-tight pb-[5px]">
          {t('auth.login.title')}
        </h1>
        <p className="text-dark-600 dark:text-dark-300">{t('auth.login.subtitle')}</p>
      </div>

      <div className="glass-card p-8 animate-scale-in">
        <form onSubmit={handleSubmit} className="space-y-6">
          {error && (
            <div className="bg-red-50 border-2 border-red-300 text-red-700 dark:bg-red-950/40 dark:border-red-800 dark:text-red-200 px-4 py-3 rounded-xl animate-slide-down">
              <span>{error}</span>
            </div>
          )}

          {needsVerification && (
            <div className="bg-amber-50 border-2 border-amber-300 text-amber-900 dark:bg-amber-950/30 dark:border-amber-800 dark:text-amber-100 px-4 py-4 rounded-xl space-y-3">
              {resendState === 'sent' ? (
                <p className="text-sm flex items-start gap-2">
                  <Mail size={18} className="mt-0.5 flex-shrink-0" />
                  {t('auth.login.resendSent')}
                </p>
              ) : (
                <>
                  <input
                    type="email"
                    value={resendEmail}
                    onChange={(e) => setResendEmail(e.target.value)}
                    className="form-input-custom"
                    placeholder={t('auth.verify.resend.placeholder')}
                    autoComplete="email"
                    disabled={resendState === 'sending'}
                  />
                  <button
                    type="button"
                    onClick={handleResendVerification}
                    disabled={resendState === 'sending' || !resendEmail}
                    className="w-full btn-accent-custom disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {resendState === 'sending'
                      ? t('auth.login.resending')
                      : t('auth.login.resendVerification')}
                  </button>
                </>
              )}
            </div>
          )}

          <div>
            <label htmlFor="username" className="form-label-custom">
              {t('auth.login.username.label')}
            </label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="form-input-custom"
              required
              autoComplete="username"
              placeholder={t('auth.login.username.placeholder')}
            />
          </div>

          <div>
            <label htmlFor="password" className="form-label-custom">
              {t('auth.login.password.label')}
            </label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="form-input-custom"
              required
              autoComplete="current-password"
              minLength={6}
              placeholder={t('auth.login.password.placeholder')}
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full btn-primary-custom disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? t('auth.login.submitting') : t('auth.login.submit')}
          </button>

          <div className="border-t border-gray-200 dark:border-surface-700 pt-4 space-y-3">
            <p className="text-center text-sm">
              <Link to="/forgotten-password" className="text-primary-600 dark:text-primary-300 hover:text-primary-700 dark:hover:text-primary-200 font-medium">
                {t('auth.login.forgotPassword')}
              </Link>
            </p>

            <p className="text-center text-sm text-dark-600 dark:text-dark-300">
              {t('auth.login.noAccount')}{' '}
              <Link to="/registration" className="text-accent-600 dark:text-accent-300 hover:text-accent-700 dark:hover:text-accent-200 font-bold">
                {t('auth.login.registerLink')}
              </Link>
            </p>
          </div>
        </form>
      </div>
    </div>
  );
};
