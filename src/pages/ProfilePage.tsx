import { useState, FormEvent, useEffect } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useTranslation } from '../contexts/LanguageContext';
import { apiService } from '../services/apiService';
import type { Locale } from '../i18n/translations';

export const ProfilePage = () => {
  const { user, isAuthenticated, updateProfile, deleteAccount } = useAuth();
  const { t, setLocale } = useTranslation();
  const navigate = useNavigate();

  // Basic-info form state. Initialised from the loaded user; kept in sync via
  // useEffect because user may load asynchronously after the first render.
  const [firstName, setFirstName] = useState(user?.firstName ?? '');
  const [lastName, setLastName] = useState(user?.lastName ?? '');
  const [city, setCity] = useState(user?.city ?? '');
  const [language, setLanguage] = useState<Locale>(
    user?.language === 'en' ? 'en' : 'cs'
  );
  const [basicSaving, setBasicSaving] = useState(false);
  const [basicMessage, setBasicMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Change-password form state.
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [pwdSaving, setPwdSaving] = useState(false);
  const [pwdMessage, setPwdMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Danger zone (delete account) state.
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletePwd, setDeletePwd] = useState('');
  const [deleteError, setDeleteError] = useState('');
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (user) {
      setFirstName(user.firstName ?? '');
      setLastName(user.lastName ?? '');
      setCity(user.city ?? '');
      setLanguage(user.language === 'en' ? 'en' : 'cs');
    }
  }, [user]);

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" />;
  }

  const handleSaveBasic = async (e: FormEvent) => {
    e.preventDefault();
    setBasicSaving(true);
    setBasicMessage(null);
    try {
      await updateProfile({ firstName, lastName, city, language });
      // Flip the active UI locale to match the saved preference, so the page
      // re-renders in the language the user just picked.
      setLocale(language);
      setBasicMessage({ type: 'success', text: t('profile.basicInfo.saved') });
    } catch (err) {
      const msg = err instanceof Error && err.message ? err.message : t('profile.basicInfo.error');
      setBasicMessage({ type: 'error', text: msg });
    } finally {
      setBasicSaving(false);
    }
  };

  const handleDeleteAccount = async () => {
    setDeleting(true);
    setDeleteError('');
    try {
      await deleteAccount(deletePwd);
      navigate('/');
    } catch (err) {
      const msg = err instanceof Error && err.message ? err.message : t('profile.dangerZone.error');
      setDeleteError(msg);
    } finally {
      setDeleting(false);
    }
  };

  const handleChangePassword = async (e: FormEvent) => {
    e.preventDefault();
    setPwdMessage(null);
    if (newPassword !== confirmPassword) {
      setPwdMessage({ type: 'error', text: t('profile.changePassword.mismatch') });
      return;
    }
    setPwdSaving(true);
    try {
      await apiService.changePassword(currentPassword, newPassword);
      setPwdMessage({ type: 'success', text: t('profile.changePassword.success') });
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      const msg = err instanceof Error && err.message ? err.message : t('profile.changePassword.error');
      setPwdMessage({ type: 'error', text: msg });
    } finally {
      setPwdSaving(false);
    }
  };

  return (
    <div className="section-container max-w-3xl mx-auto animate-fade-in">
      <div className="text-center mb-8">
        <div className="inline-block w-20 h-20 bg-gradient-to-br from-primary-500 to-accent-500 rounded-2xl flex items-center justify-center mb-4 mx-auto">
          <span className="text-4xl"></span>
        </div>
        <h1 className="text-4xl font-bold bg-gradient-to-r from-primary-600 to-accent-600 bg-clip-text text-transparent mb-2 leading-tight pb-[5px]">
          {t('profile.title')}
        </h1>
        <p className="text-dark-600 dark:text-dark-300">{t('profile.welcome', { name: user.username })}</p>
      </div>

      {/* Basic information — editable */}
      <div className="glass-card p-8 mb-6">
        <h2 className="text-2xl font-bold text-dark-800 dark:text-dark-50 mb-6">
          {t('profile.basicInfo.title')}
        </h2>

        <form onSubmit={handleSaveBasic} className="space-y-5">
          {basicMessage && (
            <div
              className={
                basicMessage.type === 'success'
                  ? 'bg-emerald-50 border-2 border-emerald-300 text-emerald-700 dark:bg-emerald-950/40 dark:border-emerald-800 dark:text-emerald-200 px-4 py-3 rounded-xl'
                  : 'bg-red-50 border-2 border-red-300 text-red-700 dark:bg-red-950/40 dark:border-red-800 dark:text-red-200 px-4 py-3 rounded-xl'
              }
              role={basicMessage.type === 'error' ? 'alert' : 'status'}
            >
              <span>{basicMessage.text}</span>
            </div>
          )}

          {/* Read-only: username + email */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="form-label-custom">{t('profile.username')}</label>
              <input
                type="text"
                value={user.username}
                readOnly
                disabled
                className="form-input-custom opacity-70 cursor-not-allowed"
              />
            </div>
            <div>
              <label className="form-label-custom">{t('profile.email')}</label>
              <input
                type="email"
                value={user.email}
                readOnly
                disabled
                className="form-input-custom opacity-70 cursor-not-allowed"
              />
            </div>
          </div>

          {/* Editable: first name + last name */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="firstName" className="form-label-custom">
                {t('profile.firstName.label')}
              </label>
              <input
                id="firstName"
                type="text"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                className="form-input-custom"
                maxLength={50}
                autoComplete="given-name"
              />
            </div>
            <div>
              <label htmlFor="lastName" className="form-label-custom">
                {t('profile.lastName.label')}
              </label>
              <input
                id="lastName"
                type="text"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                className="form-input-custom"
                maxLength={50}
                autoComplete="family-name"
              />
            </div>
          </div>

          {/* Editable: city */}
          <div>
            <label htmlFor="city" className="form-label-custom">
              {t('profile.city.label')}
            </label>
            <input
              id="city"
              type="text"
              value={city}
              onChange={(e) => setCity(e.target.value)}
              className="form-input-custom"
              maxLength={50}
              autoComplete="address-level2"
            />
          </div>

          {/* Editable: language */}
          <div>
            <label htmlFor="language" className="form-label-custom">
              {t('profile.language.label')}
            </label>
            <select
              id="language"
              value={language}
              onChange={(e) => setLanguage(e.target.value === 'en' ? 'en' : 'cs')}
              className="form-input-custom"
            >
              <option value="cs">{t('profile.language.cs')}</option>
              <option value="en">{t('profile.language.en')}</option>
            </select>
          </div>

          <button
            type="submit"
            disabled={basicSaving}
            className="btn-primary-custom disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {t('profile.basicInfo.save')}
          </button>
        </form>
      </div>

      {/* Change password */}
      <div className="glass-card p-8 mb-6">
        <h2 className="text-2xl font-bold text-dark-800 dark:text-dark-50 mb-6">
          {t('profile.changePassword.title')}
        </h2>

        <form onSubmit={handleChangePassword} className="space-y-5">
          {pwdMessage && (
            <div
              className={
                pwdMessage.type === 'success'
                  ? 'bg-emerald-50 border-2 border-emerald-300 text-emerald-700 dark:bg-emerald-950/40 dark:border-emerald-800 dark:text-emerald-200 px-4 py-3 rounded-xl'
                  : 'bg-red-50 border-2 border-red-300 text-red-700 dark:bg-red-950/40 dark:border-red-800 dark:text-red-200 px-4 py-3 rounded-xl'
              }
              role={pwdMessage.type === 'error' ? 'alert' : 'status'}
            >
              <span>{pwdMessage.text}</span>
            </div>
          )}

          <div>
            <label htmlFor="currentPassword" className="form-label-custom">
              {t('profile.changePassword.current')}
            </label>
            <input
              id="currentPassword"
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              className="form-input-custom"
              autoComplete="current-password"
              required
            />
          </div>

          <div>
            <label htmlFor="newPassword" className="form-label-custom">
              {t('profile.changePassword.new')}
            </label>
            <input
              id="newPassword"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="form-input-custom"
              autoComplete="new-password"
              minLength={6}
              required
            />
          </div>

          <div>
            <label htmlFor="confirmPassword" className="form-label-custom">
              {t('profile.changePassword.confirm')}
            </label>
            <input
              id="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="form-input-custom"
              autoComplete="new-password"
              minLength={6}
              required
            />
          </div>

          <button
            type="submit"
            disabled={pwdSaving}
            className="btn-primary-custom disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {t('profile.changePassword.submit')}
          </button>
        </form>
      </div>

      {/* My rides — unchanged placeholder */}
      <div className="glass-card p-8">
        <h2 className="text-2xl font-bold text-dark-800 dark:text-dark-50 mb-6">
          {t('profile.rides.title')}
        </h2>
        <div className="text-center py-8">
          <p className="text-dark-600 dark:text-dark-300 text-lg mb-2">{t('profile.rides.placeholder1')}</p>
          <p className="text-sm text-dark-500 dark:text-dark-400">{t('profile.rides.placeholder2')}</p>
        </div>
      </div>

      {/* Danger zone */}
      <section className="mt-6 rounded border border-red-500/60 dark:border-red-500/40 p-4 bg-red-50/40 dark:bg-red-900/10">
        <h2 className="text-xl font-semibold text-red-700 dark:text-red-400">
          {t('profile.dangerZone.title')}
        </h2>
        <p className="mt-2 text-sm text-gray-700 dark:text-gray-300">
          {t('profile.dangerZone.warning')}
        </p>
        <button
          type="button"
          onClick={() => setShowDeleteModal(true)}
          className="mt-3 rounded bg-red-600 hover:bg-red-700 text-white px-4 py-2"
        >
          {t('profile.dangerZone.deleteAccount')}
        </button>
      </section>

      {showDeleteModal && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="delete-account-title"
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          onClick={() => setShowDeleteModal(false)}
        >
          <div
            className="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 id="delete-account-title" className="text-lg font-semibold">
              {t('profile.dangerZone.confirm.title')}
            </h3>
            <p className="mt-2 text-sm">{t('profile.dangerZone.confirm.password')}</p>
            <input
              type="password"
              value={deletePwd}
              onChange={(e) => setDeletePwd(e.target.value)}
              autoComplete="current-password"
              className="mt-3 w-full rounded border px-3 py-2 bg-white dark:bg-gray-700"
              aria-label={t('profile.dangerZone.confirm.password')}
            />
            {deleteError && (
              <p role="alert" className="mt-2 text-sm text-red-600">{deleteError}</p>
            )}
            <div className="mt-4 flex gap-2 justify-end">
              <button
                type="button"
                onClick={() => {
                  setShowDeleteModal(false);
                  setDeletePwd('');
                  setDeleteError('');
                }}
                className="rounded border px-4 py-2"
              >
                {t('profile.dangerZone.confirm.cancel')}
              </button>
              <button
                type="button"
                onClick={handleDeleteAccount}
                disabled={deleting || !deletePwd}
                className="rounded bg-red-600 hover:bg-red-700 text-white px-4 py-2 disabled:opacity-50"
              >
                {t('profile.dangerZone.confirm.submit')}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
