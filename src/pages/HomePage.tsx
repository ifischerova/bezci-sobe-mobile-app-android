import { Link } from 'react-router-dom';
import { Sprout, Users, PiggyBank } from 'lucide-react';
import { useTranslation } from '../contexts/LanguageContext';

export const HomePage = () => {
  const { t } = useTranslation();
  return (
    <div className="section-container">
      {/* Hero Section */}
      <section className="text-center max-w-5xl mx-auto mb-16 animate-fade-in">
        <div className="inline-block mb-6 px-4 py-2 bg-gradient-to-r from-accent-100 to-primary-100 dark:from-accent-900/40 dark:to-primary-900/40 rounded-full">
          <span className="text-sm font-semibold text-primary-700 dark:text-primary-200">{t('home.badge')}</span>
        </div>

        <h1 className="text-5xl/tight md:text-6xl/tight lg:text-7xl/tight font-bold mb-6 bg-gradient-to-r from-primary-600 via-primary-500 to-accent-600 bg-clip-text text-transparent pb-[5px]">
          {t('home.hero.title1')}<br />{t('home.hero.title2')}
        </h1>

        <p className="text-xl md:text-2xl text-dark-600 dark:text-dark-300 mb-10 max-w-3xl mx-auto leading-relaxed">
          {t('home.hero.subtitle')}
        </p>

        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
          <Link
            to="/races"
            className="btn-primary-custom text-lg px-8 group"
          >
            <span className="inline-flex items-center">
              {t('home.cta.explore')}
              <span className="ml-2 transform group-hover:translate-x-1 transition-transform">→</span>
            </span>
          </Link>
          <Link
            to="/about"
            className="btn-outline-custom text-lg px-8"
          >
            {t('home.cta.howItWorks')}
          </Link>
        </div>
      </section>

      {/* Benefits Section */}
      <section className="mb-16 animate-slide-up">
        <h2 className="text-3xl md:text-4xl font-bold text-center mb-12 text-dark-800 dark:text-dark-50">
          {t('home.benefits.title')}
        </h2>

        <div className="grid md:grid-cols-3 gap-8 max-w-6xl mx-auto">
          {/* Ecology Card */}
          <div className="feature-card group">
            <div className="w-16 h-16 bg-gradient-to-br from-accent-400 to-accent-500 rounded-2xl flex items-center justify-center mb-4 transform group-hover:rotate-6 transition-transform shadow-lg">
              <Sprout size={32} strokeWidth={1.5} className="text-white" />
            </div>
            <h3 className="text-xl font-bold mb-3 text-dark-800 dark:text-dark-50">{t('home.benefits.eco.title')}</h3>
            <p className="text-dark-600 dark:text-dark-300 leading-relaxed">
              {t('home.benefits.eco.desc')}
            </p>
          </div>

          {/* Community Card */}
          <div className="feature-card group">
            <div className="w-16 h-16 bg-gradient-to-br from-primary-400 to-primary-500 rounded-2xl flex items-center justify-center mb-4 transform group-hover:rotate-6 transition-transform shadow-lg">
              <Users size={32} strokeWidth={1.5} className="text-white" />
            </div>
            <h3 className="text-xl font-bold mb-3 text-dark-800 dark:text-dark-50">{t('home.benefits.community.title')}</h3>
            <p className="text-dark-600 dark:text-dark-300 leading-relaxed">
              {t('home.benefits.community.desc')}
            </p>
          </div>

          {/* Savings Card */}
          <div className="feature-card group">
            <div className="w-16 h-16 bg-gradient-to-br from-yellow-400 to-primary-400 rounded-2xl flex items-center justify-center mb-4 transform group-hover:rotate-6 transition-transform shadow-lg">
              <PiggyBank size={32} strokeWidth={1.5} className="text-white" />
            </div>
            <h3 className="text-xl font-bold mb-3 text-dark-800 dark:text-dark-50">{t('home.benefits.savings.title')}</h3>
            <p className="text-dark-600 dark:text-dark-300 leading-relaxed">
              {t('home.benefits.savings.desc')}
            </p>
          </div>
        </div>

        <p className="text-center mt-10 text-lg text-dark-600 dark:text-dark-300 italic max-w-3xl mx-auto">
          {t('home.benefits.tagline')}
        </p>
      </section>

      {/* How It Works Section */}
      <section className="mb-16 animate-slide-up">
        <div className="glass-card max-w-4xl mx-auto p-8 md:p-12">
          <h2 className="text-3xl md:text-4xl font-bold text-center mb-10 text-dark-800 dark:text-dark-50">
            {t('home.howItWorks.title')}
          </h2>

          <div className="space-y-6">
            {/* Step 1 */}
            <div className="flex items-start space-x-4 group">
              <div className="flex-shrink-0 w-12 h-12 bg-gradient-to-br from-primary-500 to-primary-600 rounded-xl flex items-center justify-center text-white font-bold text-lg shadow-md group-hover:scale-110 transition-transform">
                1
              </div>
              <div className="flex-1">
                <h3 className="text-xl font-bold mb-2 text-dark-800 dark:text-dark-50">{t('home.steps.find.title')}</h3>
                <p className="text-dark-600 dark:text-dark-300">
                  <Link to="/races" className="text-primary-600 dark:text-primary-300 hover:text-primary-700 dark:hover:text-primary-200 font-semibold underline decoration-2 underline-offset-2">
                    {t('home.steps.find.link')}
                  </Link>{' '}
                  {t('home.steps.find.suffix')}
                </p>
              </div>
            </div>

            {/* Step 2 */}
            <div className="flex items-start space-x-4 group">
              <div className="flex-shrink-0 w-12 h-12 bg-gradient-to-br from-accent-500 to-accent-600 rounded-xl flex items-center justify-center text-white font-bold text-lg shadow-md group-hover:scale-110 transition-transform">
                2
              </div>
              <div className="flex-1">
                <h3 className="text-xl font-bold mb-2 text-dark-800 dark:text-dark-50">{t('home.steps.arrange.title')}</h3>
                <p className="text-dark-600 dark:text-dark-300">
                  <Link to="/races" className="text-accent-600 dark:text-accent-300 hover:text-accent-700 dark:hover:text-accent-200 font-semibold underline decoration-2 underline-offset-2">
                    {t('home.steps.arrange.link')}
                  </Link>{' '}
                  {t('home.steps.arrange.suffix')}
                </p>
              </div>
            </div>

            {/* Step 3 */}
            <div className="flex items-start space-x-4 group">
              <div className="flex-shrink-0 w-12 h-12 bg-gradient-to-br from-primary-500 to-accent-500 rounded-xl flex items-center justify-center text-white font-bold text-lg shadow-md group-hover:scale-110 transition-transform">
                3
              </div>
              <div className="flex-1">
                <h3 className="text-xl font-bold mb-2 text-dark-800 dark:text-dark-50">{t('home.steps.run.title')}</h3>
                <p className="text-dark-600 dark:text-dark-300">
                  {t('home.steps.run.desc')}
                </p>
              </div>
            </div>
          </div>

          <div className="mt-10 text-center">
            <p className="text-dark-600 dark:text-dark-300 italic mb-6">
              {t('home.howItWorks.tagline')}
            </p>
            <Link to="/registration" className="btn-accent-custom inline-block">
              {t('home.howItWorks.cta')}
            </Link>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="mb-16">
        <div className="glass-card max-w-5xl mx-auto p-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6 text-center">
            <div className="animate-scale-in">
              <div className="text-4xl font-bold bg-gradient-to-r from-primary-600 to-primary-700 bg-clip-text text-transparent mb-2">
                1000+
              </div>
              <div className="text-sm text-dark-600 dark:text-dark-300">{t('home.stats.runners')}</div>
            </div>
            <div className="animate-scale-in" style={{ animationDelay: '0.1s' }}>
              <div className="text-4xl font-bold bg-gradient-to-r from-accent-600 to-accent-700 bg-clip-text text-transparent mb-2">
                250+
              </div>
              <div className="text-sm text-dark-600 dark:text-dark-300">{t('home.stats.races')}</div>
            </div>
            <div className="animate-scale-in" style={{ animationDelay: '0.2s' }}>
              <div className="text-4xl font-bold bg-gradient-to-r from-primary-600 to-accent-600 bg-clip-text text-transparent mb-2">
                500+
              </div>
              <div className="text-sm text-dark-600 dark:text-dark-300">{t('home.stats.rides')}</div>
            </div>
            <div className="animate-scale-in" style={{ animationDelay: '0.3s' }}>
              <div className="text-4xl font-bold bg-gradient-to-r from-accent-600 to-primary-700 bg-clip-text text-transparent mb-2">
                15t
              </div>
              <div className="text-sm text-dark-600 dark:text-dark-300">{t('home.stats.co2')}</div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="text-center mb-16">
        <div className="glass-card max-w-3xl mx-auto p-10">
          <h2 className="text-3xl font-bold mb-4 text-dark-800 dark:text-dark-50">
            {t('home.finalCta.title')}
          </h2>
          <p className="text-lg text-dark-600 dark:text-dark-300 mb-8">
            {t('home.finalCta.desc')}
          </p>
          <Link
            to="/registration"
            className="btn-primary-custom text-lg inline-block"
          >
            {t('home.finalCta.button')}
          </Link>
        </div>
      </section>
    </div>
  );
};

