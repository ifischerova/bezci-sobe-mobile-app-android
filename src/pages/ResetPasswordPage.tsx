import { useState, FormEvent } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { KeyRound, CheckCircle2 } from 'lucide-react';
import { apiService } from '../services/apiService';
import { useTranslation } from '../contexts/LanguageContext';

export const ResetPasswordPage = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const { t } = useTranslation();

  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [isDone, setIsDone] = useState(false);

  const validate = (): boolean => {
    const next: Record<string, string> = {};
    if (!password) next.password = t('auth.reset.error.passwordRequired');
    else if (password.length < 6) next.password = t('auth.reset.error.passwordShort');
    if (password !== confirmPassword) next.confirmPassword = t('auth.reset.error.passwordsMismatch');
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!token) {
      setErrors({ general: t('auth.reset.error.missingToken') });
      return;
    }
    if (!validate()) return;

    setIsLoading(true);
    try {
      await apiService.resetPassword(token, password);
      setIsDone(true);
    } catch (err) {
      setErrors({ general: err instanceof Error ? err.message : t('common.error.generic') });
    } finally {
      setIsLoading(false);
    }
  };

  if (isDone) {
    return (
      <div className="section-container max-w-md mx-auto animate-fade-in">
        <div className="glass-card p-8 text-center animate-scale-in">
          <div className="inline-block w-16 h-16 bg-gradient-to-br from-emerald-500 to-primary-500 rounded-2xl flex items-center justify-center mb-4 mx-auto">
            <CheckCircle2 size={32} strokeWidth={1.5} className="text-white" />
          </div>
          <h2 className="text-2xl font-bold text-dark-800 dark:text-dark-50 mb-3">
            {t('auth.reset.success.title')}
          </h2>
          <p className="text-dark-600 dark:text-dark-300 mb-6">{t('auth.reset.success.body')}</p>
          <Link to="/login" className="btn-primary-custom inline-block">
            {t('auth.reset.success.cta')}
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
          {t('auth.reset.title')}
        </h1>
        <p className="text-dark-600 dark:text-dark-300">{t('auth.reset.subtitle')}</p>
      </div>

      <div className="glass-card p-8 animate-scale-in">
        <form onSubmit={handleSubmit} className="space-y-5">
          {errors.general && (
            <div className="bg-red-50 border-2 border-red-300 text-red-700 dark:bg-red-950/40 dark:border-red-800 dark:text-red-200 px-4 py-3 rounded-xl animate-slide-down">
              <span>{errors.general}</span>
            </div>
          )}

          <div>
            <label htmlFor="password" className="form-label-custom">
              {t('auth.reset.password.label')}
            </label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                if (errors.password) setErrors({ ...errors, password: '' });
              }}
              className={`form-input-custom ${errors.password ? 'border-red-500 ring-2 ring-red-200' : ''}`}
              required
              autoComplete="new-password"
              minLength={6}
              placeholder={t('auth.reset.password.placeholder')}
              disabled={isLoading}
            />
            {errors.password && <p className="text-red-600 dark:text-red-300 text-sm mt-1">{errors.password}</p>}
          </div>

          <div>
            <label htmlFor="confirmPassword" className="form-label-custom">
              {t('auth.reset.confirm.label')}
            </label>
            <input
              type="password"
              id="confirmPassword"
              value={confirmPassword}
              onChange={(e) => {
                setConfirmPassword(e.target.value);
                if (errors.confirmPassword) setErrors({ ...errors, confirmPassword: '' });
              }}
              className={`form-input-custom ${errors.confirmPassword ? 'border-red-500 ring-2 ring-red-200' : ''}`}
              required
              autoComplete="new-password"
              placeholder={t('auth.reset.confirm.placeholder')}
              disabled={isLoading}
            />
            {errors.confirmPassword && <p className="text-red-600 dark:text-red-300 text-sm mt-1">{errors.confirmPassword}</p>}
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full btn-primary-custom disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? t('auth.reset.submitting') : t('auth.reset.submit')}
          </button>
        </form>
      </div>
    </div>
  );
};
