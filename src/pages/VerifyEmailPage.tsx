import { useEffect, useRef, useState, FormEvent } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { CheckCircle2, XCircle, Loader2, Mail } from 'lucide-react';
import { apiService } from '../services/apiService';
import { useTranslation } from '../contexts/LanguageContext';

type Status = 'verifying' | 'success' | 'failure';

export const VerifyEmailPage = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const { t } = useTranslation();

  const [status, setStatus] = useState<Status>('verifying');
  const [errorMessage, setErrorMessage] = useState('');
  const verifyAttempted = useRef(false);

  // Resend form state (used only when verification failed)
  const [resendEmail, setResendEmail] = useState('');
  const [resendLoading, setResendLoading] = useState(false);
  const [resendSent, setResendSent] = useState(false);

  useEffect(() => {
    if (!token) {
      setStatus('failure');
      setErrorMessage(t('auth.verify.body.missingToken'));
      return;
    }
    if (verifyAttempted.current) return;
    verifyAttempted.current = true;
    (async () => {
      try {
        await apiService.verifyEmail(token);
        setStatus('success');
      } catch (err) {
        setStatus('failure');
        setErrorMessage(err instanceof Error ? err.message : t('common.error.generic'));
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  const handleResend = async (e: FormEvent) => {
    e.preventDefault();
    if (!resendEmail) return;
    setResendLoading(true);
    setResendSent(false);
    try {
      await apiService.resendVerification(resendEmail);
      setResendSent(true);
    } catch {
      // Endpoint always succeeds server-side; treat any client-side error as "ok"
      setResendSent(true);
    } finally {
      setResendLoading(false);
    }
  };

  return (
    <div className="section-container max-w-md mx-auto animate-fade-in">
      <div className="glass-card p-8 text-center animate-scale-in">
        {status === 'verifying' && (
          <>
            <div className="inline-block w-16 h-16 bg-gradient-to-br from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center mb-4 mx-auto">
              <Loader2 size={32} strokeWidth={1.5} className="text-white animate-spin" />
            </div>
            <h2 className="text-2xl font-bold text-dark-800 dark:text-dark-50 mb-3">
              {t('auth.verify.title.verifying')}
            </h2>
            <p className="text-dark-600 dark:text-dark-300">{t('auth.verify.body.verifying')}</p>
          </>
        )}

        {status === 'success' && (
          <>
            <div className="inline-block w-16 h-16 bg-gradient-to-br from-emerald-500 to-primary-500 rounded-2xl flex items-center justify-center mb-4 mx-auto">
              <CheckCircle2 size={32} strokeWidth={1.5} className="text-white" />
            </div>
            <h2 className="text-2xl font-bold text-dark-800 dark:text-dark-50 mb-3">
              {t('auth.verify.title.success')}
            </h2>
            <p className="text-dark-600 dark:text-dark-300 mb-6">{t('auth.verify.body.success')}</p>
            <Link to="/login" className="btn-primary-custom inline-block">
              {t('auth.verify.cta.login')}
            </Link>
          </>
        )}

        {status === 'failure' && (
          <>
            <div className="inline-block w-16 h-16 bg-gradient-to-br from-red-500 to-accent-500 rounded-2xl flex items-center justify-center mb-4 mx-auto">
              <XCircle size={32} strokeWidth={1.5} className="text-white" />
            </div>
            <h2 className="text-2xl font-bold text-dark-800 dark:text-dark-50 mb-3">
              {t('auth.verify.title.failure')}
            </h2>
            {errorMessage && (
              <p className="text-dark-600 dark:text-dark-300 mb-6">{errorMessage}</p>
            )}

            {resendSent ? (
              <div className="bg-emerald-50 border-2 border-emerald-300 text-emerald-800 dark:bg-emerald-950/40 dark:border-emerald-800 dark:text-emerald-200 px-4 py-3 rounded-xl mb-4 flex items-start gap-2">
                <Mail size={18} className="mt-0.5 flex-shrink-0" />
                <span className="text-sm">{t('auth.verify.resend.sent')}</span>
              </div>
            ) : (
              <form onSubmit={handleResend} className="space-y-3 text-left">
                <input
                  type="email"
                  value={resendEmail}
                  onChange={(e) => setResendEmail(e.target.value)}
                  className="form-input-custom"
                  placeholder={t('auth.verify.resend.placeholder')}
                  required
                  autoComplete="email"
                  disabled={resendLoading}
                />
                <button
                  type="submit"
                  disabled={resendLoading || !resendEmail}
                  className="w-full btn-accent-custom disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {resendLoading ? t('auth.register.sent.resending') : t('auth.verify.cta.resend')}
                </button>
              </form>
            )}

            <div className="border-t border-gray-200 dark:border-surface-700 pt-4 mt-6">
              <Link to="/login" className="text-primary-600 dark:text-primary-300 hover:text-primary-700 dark:hover:text-primary-200 font-bold text-sm">
                {t('auth.register.sent.back')}
              </Link>
            </div>
          </>
        )}
      </div>
    </div>
  );
};
