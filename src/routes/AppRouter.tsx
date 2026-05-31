import { Routes, Route } from 'react-router-dom';
import { Layout } from '../components/layout/Layout';
import { HomePage } from '../pages/HomePage';
import { AboutPage } from '../pages/AboutPage';
import { RacesPage } from '../pages/RacesPage';
import { OrganizersPage } from '../pages/OrganizersPage';
import { LoginPage } from '../pages/LoginPage';
import { RegistrationPage } from '../pages/RegistrationPage';
import { ProfilePage } from '../pages/ProfilePage';
import { TermsPage } from '../pages/TermsPage';
import { ForgottenPasswordPage } from '../pages/ForgottenPasswordPage';
import { VerifyEmailPage } from '../pages/VerifyEmailPage';
import { ResetPasswordPage } from '../pages/ResetPasswordPage';

export const AppRouter = () => {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path="/races" element={<RacesPage />} />
        <Route path="/organizers" element={<OrganizersPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/registration" element={<RegistrationPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/terms" element={<TermsPage />} />
        <Route path="/forgotten-password" element={<ForgottenPasswordPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="*" element={<div className="text-center"><h1 className="text-4xl">404 - Stránka nenalezena</h1></div>} />
      </Routes>
    </Layout>
  );
};
