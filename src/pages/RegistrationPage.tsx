import { useState, FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { PartyPopper, Mail } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { useTranslation } from '../contexts/LanguageContext';
import { apiService } from '../services/apiService';

export const RegistrationPage = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    acceptedTerms: false,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [resendState, setResendState] = useState<'idle' | 'sending' | 'sent'>('idle');
  const { register } = useAuth();
  const { t } = useTranslation();

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Username validation
    if (!formData.username) {
      newErrors.username = t('auth.register.error.usernameRequired');
    } else if (formData.username.length < 3) {
      newErrors.username = t('auth.register.error.usernameShort');
    } else if (!/^[a-zA-Z0-9._-]+$/.test(formData.username)) {
      newErrors.username = t('auth.register.error.usernameInvalid');
    }

    // Email validation
    if (!formData.email) {
      newErrors.email = t('auth.register.error.emailRequired');
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = t('auth.register.error.emailInvalid');
    }

    // Password validation
    if (!formData.password) {
      newErrors.password = t('auth.register.error.passwordRequired');
    } else if (formData.password.length < 6) {
      newErrors.password = t('auth.register.error.passwordShort');
    } else if (!/(?=.*[a-z])(?=.*[A-Z])|(?=.*\d)/.test(formData.password)) {
      newErrors.password = t('auth.register.error.passwordWeak');
    }

    // Confirm password validation
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = t('auth.register.error.confirmRequired');
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = t('auth.register.error.passwordsMismatch');
    }

    // Terms validation
    if (!formData.acceptedTerms) {
      newErrors.terms = t('auth.register.error.termsRequired');
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    try {
      await register({
        username: formData.username,
        email: formData.email,
        password: formData.password,
      });
      setIsSubmitted(true);
    } catch (err) {
      setErrors({ general: err instanceof Error ? err.message : t('auth.register.error.failed') });
    } finally {
      setIsLoading(false);
    }
  };

  const handleResend = async () => {
    if (!formData.email) return;
    setResendState('sending');
    try {
      await apiService.resendVerification(formData.email);
    } catch {
      // resend-verification is always 204 server-side; swallow client errors so we
      // never imply whether the account exists
    } finally {
      setResendState('sent');
    }
  };

  const handleChange = (field: string, value: string | boolean) => {
    setFormData({ ...formData, [field]: value });
    // Clear error for this field when user starts typing
    if (errors[field]) {
      setErrors({ ...errors, [field]: '' });
    }
  };

  if (isSubmitted) {
    return (
      <div className="section-container max-w-md mx-auto animate-fade-in">
        <div className="glass-card p-8 text-center animate-scale-in">
          <div className="inline-block w-16 h-16 bg-gradient-to-br from-accent-500 to-primary-500 rounded-2xl flex items-center justify-center mb-4 mx-auto">
            <Mail size={32} strokeWidth={1.5} className="text-white" />
          </div>
          <h2 className="text-2xl font-bold text-dark-800 dark:text-dark-50 mb-4">
            {t('auth.register.sent.title')}
          </h2>
          <p className="text-dark-600 dark:text-dark-300 mb-4 leading-relaxed">
            {t('auth.register.sent.body.before')}{' '}
            <span className="font-semibold text-primary-600 dark:text-primary-300">{formData.email}</span>
            {t('auth.register.sent.body.after')}
          </p>
          <p className="text-sm text-dark-500 dark:text-dark-400 mb-6">
            {t('auth.register.sent.spam')}
          </p>

          {resendState === 'sent' ? (
            <p className="text-sm text-emerald-700 dark:text-emerald-300 mb-6">
              {t('auth.register.sent.resent')}
            </p>
          ) : (
            <button
              type="button"
              onClick={handleResend}
              disabled={resendState === 'sending'}
              className="text-primary-600 dark:text-primary-300 hover:text-primary-700 dark:hover:text-primary-200 font-medium text-sm mb-6 disabled:opacity-50"
            >
              {resendState === 'sending'
                ? t('auth.register.sent.resending')
                : t('auth.register.sent.resend')}
            </button>
          )}

          <div className="border-t border-gray-200 dark:border-surface-700 pt-4">
            <Link to="/login" className="btn-primary-custom inline-block">
              {t('auth.register.sent.back')}
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="section-container max-w-md mx-auto animate-fade-in">
      <div className="text-center mb-8">
        <div className="inline-block w-16 h-16 bg-gradient-to-br from-accent-500 to-primary-500 rounded-2xl flex items-center justify-center mb-4 mx-auto animate-bounce-slow">
          <PartyPopper size={32} strokeWidth={1.5} className="text-white" />
        </div>
        <h1 className="text-4xl font-bold bg-gradient-to-r from-accent-600 to-primary-600 bg-clip-text text-transparent mb-3 leading-tight pb-[5px]">
          {t('auth.register.title')}
        </h1>
        <p className="text-dark-600 dark:text-dark-300">{t('auth.register.subtitle')}</p>
      </div>

      <div className="glass-card p-8 animate-scale-in">
        <form onSubmit={handleSubmit} className="space-y-5">
          {errors.general && (
            <div className="bg-red-50 border-2 border-red-300 text-red-700 dark:bg-red-950/40 dark:border-red-800 dark:text-red-200 px-4 py-3 rounded-xl animate-slide-down">
              <span>{errors.general}</span>
            </div>
          )}

          <div>
            <label htmlFor="username" className="form-label-custom">
              {t('auth.register.username.label')}
            </label>
            <input
              type="text"
              id="username"
              value={formData.username}
              onChange={(e) => handleChange('username', e.target.value)}
              className={`form-input-custom ${errors.username ? 'border-red-500 ring-2 ring-red-200' : ''}`}
              required
              autoComplete="username"
              placeholder={t('auth.register.username.placeholder')}
            />
            {errors.username && <p className="text-red-600 dark:text-red-300 text-sm mt-1">{errors.username}</p>}
          </div>

          <div>
            <label htmlFor="email" className="form-label-custom">
              {t('auth.register.email.label')}
            </label>
            <input
              type="email"
              id="email"
              value={formData.email}
              onChange={(e) => handleChange('email', e.target.value)}
              className={`form-input-custom ${errors.email ? 'border-red-500 ring-2 ring-red-200' : ''}`}
              required
              autoComplete="email"
              placeholder={t('auth.register.email.placeholder')}
            />
            {errors.email && <p className="text-red-600 dark:text-red-300 text-sm mt-1">{errors.email}</p>}
          </div>

          <div>
            <label htmlFor="password" className="form-label-custom">
              {t('auth.register.password.label')}
            </label>
            <input
              type="password"
              id="password"
              value={formData.password}
              onChange={(e) => handleChange('password', e.target.value)}
              className={`form-input-custom ${errors.password ? 'border-red-500 ring-2 ring-red-200' : ''}`}
              required
              autoComplete="new-password"
              minLength={6}
              placeholder={t('auth.register.password.placeholder')}
            />
            {errors.password && <p className="text-red-600 dark:text-red-300 text-sm mt-1">{errors.password}</p>}
          </div>

          <div>
            <label htmlFor="confirmPassword" className="form-label-custom">
              {t('auth.register.confirm.label')}
            </label>
            <input
              type="password"
              id="confirmPassword"
              value={formData.confirmPassword}
              onChange={(e) => handleChange('confirmPassword', e.target.value)}
              className={`form-input-custom ${errors.confirmPassword ? 'border-red-500 ring-2 ring-red-200' : ''}`}
              required
              autoComplete="new-password"
              placeholder={t('auth.register.confirm.placeholder')}
            />
            {errors.confirmPassword && <p className="text-red-600 dark:text-red-300 text-sm mt-1">{errors.confirmPassword}</p>}
          </div>

          <div className="bg-primary-50 border-2 border-primary-200 dark:bg-primary-900/20 dark:border-primary-800 rounded-xl p-4">
            <div className="flex items-start">
              <input
                type="checkbox"
                id="terms"
                checked={formData.acceptedTerms}
                onChange={(e) => handleChange('acceptedTerms', e.target.checked)}
                className="mt-1 mr-3 w-4 h-4 accent-primary-600"
                required
              />
              <label htmlFor="terms" className="text-sm text-dark-700 dark:text-dark-200">
                {t('auth.register.terms.before')}{' '}
                <Link to="/terms" className="text-primary-600 dark:text-primary-300 hover:text-primary-700 dark:hover:text-primary-200 font-semibold underline">
                  {t('auth.register.terms.link')}
                </Link>
                {' '}*
              </label>
            </div>
            {errors.terms && <p className="text-red-600 dark:text-red-300 text-sm mt-2">{errors.terms}</p>}
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full btn-accent-custom disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? t('auth.register.submitting') : t('auth.register.submit')}
          </button>

          <div className="border-t border-gray-200 dark:border-surface-700 pt-4">
            <p className="text-center text-sm text-dark-600 dark:text-dark-300">
              {t('auth.register.haveAccount')}{' '}
              <Link to="/login" className="text-primary-600 dark:text-primary-300 hover:text-primary-700 dark:hover:text-primary-200 font-bold">
                {t('auth.register.signInLink')}
              </Link>
            </p>
          </div>
        </form>
      </div>
    </div>
  );
};
