import clsx from 'clsx';
import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { AuthProvider } from '@/contexts/AuthContext';
import { AccessibilityProvider, useAccessibility } from '@/contexts/AccessibilityContext';

const AppLayoutContent = () => {
  const { settings } = useAccessibility();
  const bgClass = settings.bgColor === 'white' ? 'bg-solid-gray-50' : `bg-theme-${settings.bgColor}`;

  return (
    <div className={clsx('flex flex-col min-h-screen', bgClass)}>
      <Header />
      <main id="main-content" className="flex-1 p-6 max-w-5xl mx-auto w-full">
        <Outlet />
      </main>
    </div>
  );
};

export const AppLayout = () => (
  <AccessibilityProvider>
    <AuthProvider>
      <AppLayoutContent />
    </AuthProvider>
  </AccessibilityProvider>
);
