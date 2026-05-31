import { useState, FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { Mail, KeyRound } from 'lucide-react';
import { apiService } from '../services/apiService';
import { useTranslation } from '../contexts/LanguageContext';

export const ForgottenPasswordPage = () => {
  const [email, setEmail] = useState('');
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { t } = useTranslation();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    if (!email) {
      setError(t('auth.forgot.error.emailRequired'));
      setIsLoading(false);
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setError(t('auth.forgot.error.emailInvalid'));
      setIsLoading(false);
      return;
    }

    try {
      await apiService.forgotPassword(email);
      // The backend always responds the same way regardless of whether the email is
      // registered — show the generic confirmation so we don't leak account state.
      setIsSubmitted(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : t('auth.forgot.error.generic'));
    } finally {
      setIsLoading(false);
    }
  };

  if (isSubmitted) {
    return (
      <div className="section-container max-w-md mx-auto animate-fade-in">
        <div className="glass-card p-8 text-center animate-scale-in">
          <div className="inline-block w-16 h-16 bg-gradient-to-br from-accent-500 to-primary-500 rounded-2xl flex items-center justify-center mb-4 mx-auto">
            <Mail size={32} strokeWidth={1.5} className="text-white" />
          </div>
          <h2 className="text-2xl font-bold text-dark-800 dark:text-dark-50 mb-4">{t('auth.forgot.success.title')}</h2>
          <p className="text-dark-600 dark:text-dark-300 mb-6 leading-relaxed">
            {t('auth.forgot.success.body.before')} <span className="font-semibold text-primary-600 dark:text-primary-300">{email}</span> {t('auth.forgot.success.body.after')}
          </p>
          <p className="text-sm text-dark-500 dark:text-dark-400 mb-6">
            {t('auth.forgot.success.spam')}
          </p>
          <Link
            to="/login"
            className="btn-primary-custom inline-block"
          >
            {t('auth.forgot.success.back')}
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="section-container max-w-md mx-auto animate-fade-in">
      <div className="text-center mb-8">
        <div className="inline-block w-16 h-16 bg-gradient-to-br from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center mb-4 mx-auto animate-bounce-slow">
          <KeyRound size={32} strokeWidth={1.5} className="text-white" />
        </div>
        <h1 className="text-4xl font-bold bg-gradient-to-r from-primary-600 to-accent-600 bg-clip-text text-transparent mb-3 leading-tight pb-[5px]">
          {t('auth.forgot.title')}
        </h1>
        <p className="text-dark-600 dark:text-dark-300">{t('auth.forgot.subtitle')}</p>
      </div>

      <div className="glass-card p-8 animate-scale-in">
        <form onSubmit={handleSubmit} className="space-y-6">
          {error && (
            <div className="bg-red-50 border-2 border-red-300 text-red-700 dark:bg-red-950/40 dark:border-red-800 dark:text-red-200 px-4 py-3 rounded-xl animate-slide-down">
              <span>{error}</span>
            </div>
          )}

          <div>
            <label htmlFor="email" className="form-label-custom">
              {t('auth.forgot.email.label')}
            </label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="form-input-custom"
              required
              autoComplete="email"
              placeholder={t('auth.register.email.placeholder')}
              disabled={isLoading}
            />
            <p className="text-xs text-dark-500 dark:text-dark-400 mt-2">
              {t('auth.forgot.email.helper')}
            </p>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full btn-primary-custom disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? t('auth.forgot.submitting') : t('auth.forgot.submit')}
          </button>

          <div className="border-t border-gray-200 dark:border-surface-700 pt-4 space-y-3">
            <p className="text-center text-sm text-dark-600 dark:text-dark-300">
              {t('auth.forgot.remembered')}{' '}
              <Link to="/login" className="text-primary-600 dark:text-primary-300 hover:text-primary-700 dark:hover:text-primary-200 font-bold">
                {t('auth.forgot.signInLink')}
              </Link>
            </p>

            <p className="text-center text-sm text-dark-600 dark:text-dark-300">
              {t('auth.forgot.noAccount')}{' '}
              <Link to="/registration" className="text-accent-600 dark:text-accent-300 hover:text-accent-700 dark:hover:text-accent-200 font-bold">
                {t('auth.forgot.registerLink')}
              </Link>
            </p>
          </div>
        </form>
      </div>
    </div>
  );
};
